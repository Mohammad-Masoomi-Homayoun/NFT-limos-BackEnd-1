package com.niftylimos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niftylimos.domain.*;
import com.niftylimos.repo.*;
import com.niftylimos.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@Transactional
public class NiftyLimosService {

    private static final Logger logger = LoggerFactory.getLogger(NiftyLimosService.class);

    @Value("${NL.contract-address}")
    private String contractAddress;

    @Value("${NL.contract-abi}")
    private String contractAbi;

    @Value("${NL.ticket-expire}")
    private Long ticketExpire;

    @Value("${NL.ticket-privateKey}")
    private String privateKey;

    private ECKeyPair keyPair;

    @Value("${NL.limo-data-file}")
    private String limoDataFile;

    private final MintEventRepository mintEventRepository;

    private final LimoDataRepository limoDataRepository;

    private final StateService stateService;

    private final AccountRepository accountRepo;

    private final LimoRepository limoRepo;

    private final ReservationRepository reservationRepo;

    private final LimoTicketRepository ticketRepo;

    @Value("${NL.issue-ticket-on-reservation}")
    private boolean issueTicketOnReservationDefault;

    private boolean issueTicketOnReservation;

    public NiftyLimosService(MintEventRepository mintEventRepository,
                             LimoDataRepository limoDataRepository,
                             StateService stateService,
                             AccountRepository accountRepo,
                             LimoRepository limoRepo,
                             ReservationRepository reservationRepo,
                             LimoTicketRepository ticketRepo) {
        this.mintEventRepository = mintEventRepository;
        this.limoDataRepository = limoDataRepository;
        this.stateService = stateService;
        this.accountRepo = accountRepo;
        this.limoRepo = limoRepo;
        this.reservationRepo = reservationRepo;
        this.ticketRepo = ticketRepo;
    }


    @PostConstruct
    private void init() {
        logger.info("contract address : {}", contractAddress);
        this.keyPair = ECKeyPair.create(Numeric.hexStringToByteArray(this.privateKey));
        if (stateService.get("limosInitialized").isEmpty()) {
            logger.info("initializing Limos...");
            initLimos();
            logger.info("Limos initialized.");
            stateService.set("limosInitialized", "true");
        }
        if (stateService.get("issue-ticket-on-reservation").isEmpty()) {
            this.issueTicketOnReservation = issueTicketOnReservationDefault;
        } else {
            this.issueTicketOnReservation = Boolean.parseBoolean(stateService.get("issue-ticket-on-reservation").get());
        }
        if (stateService.get("ticket-expire").isPresent()) {
            this.ticketExpire = Long.parseLong(stateService.get("ticket-expire").get());
        }
        logger.info("issue ticket on reservation = {}", this.issueTicketOnReservation);
        logger.info("ticket expire = {}", this.ticketExpire);

        if (stateService.get("migration_priceDown04_done").isEmpty()) {
            changePrice04AndDoubleTickets();
            stateService.set("migration_priceDown04_done", "true");
        }
        importLimoData();
        logger.info("initialized");
    }

    private List<LimoData> getUnAssignedLimoDataList() {
        return limoDataRepository.findAllByLimoIsNullOrderById();
    }

    public void reveal(EthLog.LogObject revealLog) {
        if (stateService.get("revealed").isPresent()) {
            logger.warn("already revealed");
            return;
        }
        logger.info("revealing, reveal_block_hash = {}", revealLog.getBlockHash());
        stateService.set("revealed", "true");
        stateService.set("reveal_block_hash", revealLog.getBlockHash());
        logger.info("assigning LimoData to existing MintEvents");
        mintEventRepository.findAll().forEach(this::assignLimoData);
    }

    private String getRevealHash() {
        if (stateService.get("revealed").isEmpty()) {
            logger.error("not revealed");
            throw new RuntimeException("not revealed");
        }
        return stateService.get("reveal_block_hash").get();
    }

    private BigInteger getMintRandomNumber(MintEvent mintEvent) {
        Uint256 R = new Uint256(Numeric.toBigInt(getRevealHash()));
        Uint256 B = new Uint256(Numeric.toBigInt(mintEvent.getBlockHash()));
        Uint256 T = new Uint256(Numeric.toBigInt(mintEvent.getTxHash()));
        Uint256 I = new Uint256(mintEvent.getTransferIndex());
        String encoded =
                        TypeEncoder.encode(R) +
                        TypeEncoder.encode(B) +
                        TypeEncoder.encode(T) +
                        TypeEncoder.encode(I);
        String hash = Hash.sha3(encoded);
        return Numeric.toBigInt(hash);
    }

    private void assignLimoData(MintEvent mintEvent){
        var limoDataList = getUnAssignedLimoDataList();
        var listSize = BigInteger.valueOf(limoDataList.size());
        logger.info("assigning LimoData, unassigned LimoData list size = {}", listSize);
        var r = getMintRandomNumber(mintEvent);
        var index = r.mod(listSize).longValue();
        var assignedData = limoDataList.get((int) index);
        assignedData.setLimo(limoRepo.getById(mintEvent.getTokenId()));
        limoDataRepository.save(assignedData);
        logger.info("LimoData {} assigned to tokenId {}", assignedData.getId(), assignedData.getLimo().getId());
    }

    public void newMint(List<EthLog.LogObject> logs) {
        for (var log: logs){
            MintEvent mintEvent = new MintEvent();
            mintEvent.setBlock(log.getBlockNumber().longValue());
            mintEvent.setBlockHash(log.getBlockHash());
            mintEvent.setTxHash(log.getTransactionHash());
            mintEvent.setTxIndex(log.getTransactionIndex().longValue());
            mintEvent.setTransferIndex(log.getLogIndex().longValue());
            mintEvent.setTokenId(Numeric.toBigInt(log.getTopics().get(3)).longValue());
            mintEventRepository.save(mintEvent);
            mintEventRepository.flush();
            logger.info("new mint: block = {}, tokenId = {}",log.getBlockNumber(), Numeric.toBigInt(log.getTopics().get(3)).longValue());
            if(stateService.get("revealed").isPresent()){
                assignLimoData(mintEvent);
            }
        }
    }

    private void importLimoData() {
        if (stateService.get("limo_data_imported").isPresent()) {
            logger.info("limo data already imported");
            return;
        }

        logger.info("importing limo data");
        LimoData[] limosArray;
        var mapper = new ObjectMapper();
        try {
            limosArray = mapper.readValue(new File(limoDataFile), LimoData[].class);
        } catch (IOException e) {
            logger.error("can not read limo data file {}", limoDataFile);
            e.printStackTrace();
            throw new RuntimeException("can not read limo data file");
        }

        List<LimoData> limos = Arrays.asList(limosArray);
        if(limos.size() != 10000){
            logger.error("set size is not equal to 10,000");
            throw new RuntimeException("set size is not equal to 10,000");
        }
        Collections.shuffle(limos);
        long id = 0L;
        for (var limo : limos) {
            ++id;
            limo.setId(id);
        }
        limoDataRepository.saveAll(limos);
        limoDataRepository.flush();
        stateService.set("limo_data_imported", "true");
        logger.info("limo data imported");
    }

    public String getContractAddress() {
        return this.contractAddress;
    }

    public void change04TicketsTokenIds() {
        if (stateService.get("migration_priceDown04_2_done").isPresent()) {
            return;
        }

        logger.info("* changing 04 Tickets...");
        var reservations04 = reservationRepo.findAll().stream()
                .filter(r -> r.getTx().endsWith("_priceDown04"))
                .collect(Collectors.toList());
        logger.info("{} reservation found", reservations04.size());
        for (var r : reservations04) {
            var ticket = r.getTickets().iterator().next();
            logger.info("deleting ticket {} ...", ticket.getLimo().getId());
            ticketRepo.delete(ticket);
            ticketRepo.flush();
            r.getTickets().remove(ticket);
            var limo = getNextLimoForTicket(11L);
            var newTicket = issue(r.getAccount(), limo, ticketExpire);
            newTicket.setReservation(r);
            ticketRepo.save(newTicket);
            ticketRepo.flush();
            r.setLimo(limo);
            reservationRepo.save(r);
            reservationRepo.flush();
        }
        stateService.set("migration_priceDown04_2_done", "true");
    }

    private void changePrice04AndDoubleTickets() {
        logger.info("* Doubling All Tickets...");
        var count = 0;
        var reservations = reservationRepo.findAll();
        for (var r : reservations) {
            var newR = new Reservation();
            newR.setTx(r.getTx() + "_priceDown04");
            newR.setAccount(r.getAccount());
            reservationRepo.save(newR);
            reservationRepo.flush();
            var limo = getNextLimoForTicket();
            var newT = issue(newR.getAccount(), limo, ticketExpire);
            newT.setReservation(newR);
            newR.setLimo(limo);
            ticketRepo.save(newT);
            ticketRepo.flush();
            reservationRepo.save(newR);
            reservationRepo.flush();
            ++count;
        }
        logger.info("{} ticket issued", count);
    }

    public String getContractAbi() {
        try {
            // Jar
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contractAbi);
            // IDE
            if (inputStream == null) {
                inputStream = this.getClass().getResourceAsStream(contractAbi);
            }
            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long setExpire(Long newExpire) {
        this.ticketExpire = newExpire;
        stateService.set("ticket-expire", String.valueOf(newExpire));
        return this.ticketExpire;
    }

    public Account getOrCreateAccount(String address) {
        address = address.toLowerCase();
        return accountRepo.findById(address).orElse(accountRepo.save(new Account(address)));
    }

    public AccountDTO getAccount(String address) {
        return accountRepo.findById(address.toLowerCase()).map(this::accountToDTO).orElse(null);
    }

    public List<AccountDTO> getAllAccounts() {
        return accountRepo.findAll()
                .stream()
                .map(this::accountToDTO)
                .collect(Collectors.toList());
    }

    public ReservationDTO getReservation(Long id) {
        return reservationRepo.findById(id).map(this::reservationToDTO).orElse(null);
    }

    public List<ReservationDTO> getAllReservations() {
        return reservationRepo.findAll()
                .stream()
                .map(this::reservationToDTO)
                .collect(Collectors.toList());
    }

    public Long getReservationCount() {
        return reservationRepo.count();
    }

    public Long getMintedCount() {
        return mintEventRepository.count();
    }

    public LimoDTO getLimo(Long tokenId) {
        return limoRepo.findById(tokenId).map(this::limoToDTO).orElse(null);
    }

    public List<LimoDTO> getAllLimos() {
        return limoRepo.findAll()
                .stream()
                .map(this::limoToDTO)
                .collect(Collectors.toList());
    }

    public LimoTicketDTO getTicket(Long tokenId) {
        return ticketRepo.findById(tokenId).map(this::ticketToDTO).orElse(null);
    }

    public List<LimoTicketDTO> getAllTickets() {
        return ticketRepo.findAll()
                .stream()
                .map(this::ticketToDTO)
                .collect(Collectors.toList());
    }

    private Sign.SignatureData generateTicket(String account, Long tokenId, Long expire) {
        Address _address = new Address(account);
        Uint256 _tokenId = new Uint256(BigInteger.valueOf(tokenId));
        Uint256 _expire = new Uint256(BigInteger.valueOf(expire));
        String msgHexString =
                TypeEncoder.encode(_address) + TypeEncoder.encode(_tokenId) + TypeEncoder.encode(_expire);
        byte[] msg = Numeric.hexStringToByteArray(msgHexString);
        byte[] msgHash = Hash.sha3(msg);
        msgHash = Sign.getEthereumMessageHash(msgHash);
        return Sign.signMessage(msgHash, keyPair, false);
    }

    public LimoTicket issue(Account account, Limo limo, Long expire) {
        var sig = generateTicket(account.getAddress(), limo.getId(), expire);
        LimoTicket ticket = new LimoTicket();
        ticket.setAccount(account);
        ticket.setExpire(expire);
        ticket.setLimo(limo);
        byte[] signature = new byte[65];
        for (int i = 0; i < 32; i++) {
            signature[i] = sig.getR()[i];
            signature[i + 32] = sig.getS()[i];
        }
        signature[64] = sig.getV()[0];
        ticket.setSignature(Numeric.toHexString(signature));
        logger.info("issued : [{}, {}, {}]", account.getAddress(), limo.getId(), expire);
        return ticketRepo.save(ticket);
    }

    public LimoTicketDTO issueTicket(IssueTicketRequestDTO req) {
        Account a = getOrCreateAccount(req.getAddress());
        Limo l = req.getLimo() == null ? getNextLimoForTicket() : limoRepo.getById(req.getLimo());
        Long e = req.getExpire() == null ? ticketExpire : req.getLimo();
        return ticketToDTO(issue(a, l, e));
    }

    public LimoTicketDTO issueTicket(String address, Long tokenId, Long expire) {
        Account account = accountRepo.findById(address.toLowerCase()).orElseThrow();
        Limo limo = limoRepo.findById(tokenId).orElseThrow();
        return ticketToDTO(issue(account, limo, expire));
    }

    public List<LimoTicketDTO> issueTicketForAllReservations() {
        var res = reservationRepo.findAllByTicketsEmptyOrderById();
        var tickets = new ArrayList<LimoTicketDTO>();
        for (var r : res) {
            Limo limo = getNextLimoForTicket();
            Account account = r.getAccount();
            var ticket = issue(account, limo, ticketExpire);
            ticket.setReservation(r);
            ticketRepo.save(ticket);
            r.setLimo(limo);
            reservationRepo.save(r);
            ticketRepo.flush();
            reservationRepo.flush();
            tickets.add(ticketToDTO(ticket));
        }
        this.issueTicketOnReservation = true;
        return tickets;
    }

    private Limo getNextLimoForTicket(Long offset) {
        List<Limo> limos = limoRepo.findAllByTicketsEmptyAndIdGreaterThanEqualOrderByIdAsc(offset);
        logger.info("found {} limos with no tickets issued", limos.size());
        if (limos.isEmpty()) {
            logger.error("out of limo");
            throw new RuntimeException("out of limo");
        }
        return limos.get(0);
    }

    private Limo getNextLimoForTicket() {
        List<Limo> limos = limoRepo.findAllByTicketsEmptyAndIdGreaterThanEqualOrderByIdAsc(1000L);
        logger.info("found {} limos with no tickets issued", limos.size());
        if (limos.isEmpty()) {
            logger.error("out of limo");
            throw new RuntimeException("out of limo");
        }
        return limos.get(0);
    }

    public void initLimos() {
        var i =
                LongStream.rangeClosed(0, 9999)
                        .mapToObj(Limo::new).collect(Collectors.toList());
        limoRepo.saveAll(i);
    }

    public void reserve(String acc, String tx) {
        Account account = getOrCreateAccount(acc);
        Reservation reservation = new Reservation(account);
        reservation.setTx(tx);
        reservation = reservationRepo.save(reservation);
        if (this.issueTicketOnReservation) {
            Limo limo = getNextLimoForTicket();
            var ticket = issue(account, limo, ticketExpire);
            ticket.setReservation(reservation);
            reservation.setLimo(limo);
            ticketRepo.save(ticket);
        }
        reservationRepo.save(reservation);
    }

    public String getLimoDataSignature(Long id){
        return limoDataRepository.findById(id).get().getSignature();
    }

    public LimoMetadataDTO getLimoMetadata(Long id) {
        LimoMetadataDTO dto = new LimoMetadataDTO();
        dto.setName("Limo #" + id);
        dto.setDescription("Nifty Limos is an Ethereum-based NFT collection of 10K 3D rendered limos that you can race to win eth prizes! By buying a Nifty Limo, you gain access to an exclusive members-only experience, with a stellar community, curated NFT airdrops, and branded merchandise.");
        dto.setImage("https://niftylimos.com/before-reveal-limos.gif");
        dto.setAnimation_url("https://niftylimos.com/before-reveal-limos.webm");

        Limo limo = limoRepo.findById(id).get();
        if(limo.getData() != null){
            dto.setImage("https://niftylimos.com/limo-images/3000/" + limo.getData().getSignature() + ".png");
            dto.setAnimation_url(null);

            if(limo.getData().getBody() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Body", limo.getData().getBody()));
            }
            if(limo.getData().getRing() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Ring", limo.getData().getRing()));
            }
            if(limo.getData().getTrunk() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Trunk", limo.getData().getTrunk()));
            }
            if(limo.getData().getRoof() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Roof", limo.getData().getRoof()));
            }
            if(limo.getData().getFootstep() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Footstep", limo.getData().getFootstep()));
            }
            if(limo.getData().getDoor() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Door", limo.getData().getDoor()));
            }
            if(limo.getData().getMirror() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Side Mirror", limo.getData().getMirror()));
            }
            if(limo.getData().getHood() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Hood", limo.getData().getHood()));
            }
            if(limo.getData().getBumper() != null){
                dto.getAttributes().add(new LimoMetadataDTO.AttrDTO("Bumper", limo.getData().getBumper()));
            }
        }
        return dto;
    }

    private AccountDTO accountToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setAddress(account.getAddress());
        dto.setReservations(account.getReservations().stream().map(Reservation::getId).collect(Collectors.toList()));
        dto.setTickets(account.getTickets().stream().map(this::ticketToDTO).collect(Collectors.toList()));
        return dto;
    }

    private ReservationDTO reservationToDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setAccount(reservation.getAccount().getAddress());
        dto.setTickets(reservation.getTickets().stream().map(LimoTicket::getId).collect(Collectors.toList()));
        if (reservation.getLimo() != null)
            dto.setLimo(reservation.getLimo().getId());
        return dto;
    }

    private LimoTicketDTO ticketToDTO(LimoTicket ticket) {
        LimoTicketDTO dto = new LimoTicketDTO();
        dto.setId(ticket.getId());
        dto.setAccount(ticket.getAccount().getAddress());
        dto.setId(ticket.getId());
        dto.setLimo(ticket.getLimo().getId());
        dto.setExpire(ticket.getExpire());
        dto.setSignature(ticket.getSignature());
        if (ticket.getReservation() != null)
            dto.setReservation(ticket.getReservation().getId());
        return dto;
    }

    private LimoDTO limoToDTO(Limo limo) {
        LimoDTO dto = new LimoDTO();
        dto.setId(limo.getId());
        dto.setReservations(limo.getReservations().stream().map(Reservation::getId).collect(Collectors.toList()));
        dto.setTickets(limo.getTickets().stream().map(LimoTicket::getId).collect(Collectors.toList()));
        return dto;
    }
}
