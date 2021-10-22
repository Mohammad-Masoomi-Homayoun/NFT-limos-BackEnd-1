package com.niftylimos.service;

import com.niftylimos.domain.Account;
import com.niftylimos.domain.Limo;
import com.niftylimos.domain.LimoTicket;
import com.niftylimos.domain.Reservation;
import com.niftylimos.repo.AccountRepository;
import com.niftylimos.repo.LimoRepository;
import com.niftylimos.repo.LimoTicketRepository;
import com.niftylimos.repo.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@Transactional
public class NiftyLimosService {

    private static final Logger logger = LoggerFactory.getLogger(NiftyLimosService.class);

    @Value("${NL.ticket-expire}")
    private Long ticketExpire;

    @Value("${NL.ticket-privateKey}")
    private String privateKey;

    private ECKeyPair keyPair;

    @Value("${NL.limo-image-repository}")
    private String limoImageRepository;

    @Value("${NL.limo-animation-repository}")
    private String limoAnimationRepository;

    private final NiftyLimosStateService stateService;

    private final AccountRepository accountRepo;

    private final LimoRepository limoRepo;

    private final ReservationRepository reservationRepo;

    private final LimoTicketRepository ticketRepo;

    @Value("${NL.issue-ticket-on-reservation}")
    private boolean issueTicketOnReservationDefault;

    private boolean issueTicketOnReservation;

    public NiftyLimosService(NiftyLimosStateService stateService, AccountRepository accountRepo,
                             LimoRepository limoRepo,
                             ReservationRepository reservationRepo,
                             LimoTicketRepository ticketRepo) {
        this.stateService = stateService;
        this.accountRepo = accountRepo;
        this.limoRepo = limoRepo;
        this.reservationRepo = reservationRepo;
        this.ticketRepo = ticketRepo;
    }


    @PostConstruct
    private void init() {
        this.keyPair = ECKeyPair.create(Numeric.hexStringToByteArray(this.privateKey));
        if (stateService.get("limosInitialized").isEmpty()) {
            logger.info("initializing Limos...");
            initLimos();
            logger.info("Limos initialized.");
            stateService.set("limosInitialized", "true");
        }
        if(stateService.get("issue-ticket-on-reservation").isEmpty()){
            this.issueTicketOnReservation = issueTicketOnReservationDefault;
        }else {
            this.issueTicketOnReservation = Boolean.parseBoolean(stateService.get("issue-ticket-on-reservation").get());
        }
        if(stateService.get("ticket-expire").isPresent()){
            this.ticketExpire = Long.parseLong(stateService.get("ticket-expire").get());
        }
        logger.info("issue ticket on reservation = {}", this.issueTicketOnReservation);
        logger.info("ticket expire = {}", this.ticketExpire);
        logger.info("initialized");
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
        return ticketRepo.save(ticket);
    }

    public LimoTicketDTO issueTicket(String address, Long tokenId, Long expire) {
        Account account = accountRepo.findById(address.toLowerCase()).orElseThrow();
        Limo limo = limoRepo.findById(tokenId).orElseThrow();
        return ticketToDTO(issue(account, limo, expire));
    }

    public List<LimoTicketDTO> issueTicketForAllReservations() {
        var res = reservationRepo.findAll();
        var tickets = new ArrayList<LimoTicketDTO>();
        for (var r : res) {
            Limo limo = getNextLimoForTicket();
            Account account = r.getAccount();
            var ticket = issue(account, limo, ticketExpire);
            ticket.setReservation(r);
            ticketRepo.save(ticket);
            r.setLimo(limo);
            reservationRepo.save(r);
            tickets.add(ticketToDTO(ticket));
        }
        this.issueTicketOnReservation = true;
        return tickets;
    }

    private Limo getNextLimoForTicket() {
        Long next = stateService.get("nextLimoForTicket").isEmpty()? 1000L : Long.parseLong(stateService.get("nextLimoForTicket").get());
        if (next >= 10000) {
            logger.error("next limo = {}", next);
            throw new RuntimeException("out of limo");
        }
        Limo limo = limoRepo.findById(next).orElseThrow();
        ++next;
        stateService.set("nextLimoForTicket", next + "");
        return limo;
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

    public LimoMetadataDTO getLimoMetadata(Long id) {
        LimoMetadataDTO dto = new LimoMetadataDTO();
        dto.setName("Limo #" + id);
        dto.setDescription("Nifty Limos is an Ethereum-based NFT collection of 10K 3D rendered limos that you can race to win eth prizes! By buying a Nifty Limo, you gain access to an exclusive members-only experience, with a stellar community, curated NFT airdrops, and branded merchandise.");
        dto.setImage("https://niftylimos.com:7070/api/limo/image/before-reveal-limos.jpeg");
        dto.setAnimation_url("https://niftylimos.com:7070/api/limo/animation/before-reveal-limos.mp4");
        return dto;
    }

    public byte[] getLimoImage(String img) {
        File file = new File(limoImageRepository + File.separator + img);
        try {
            return new FileInputStream(file).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public byte[] getLimoAnimation(String img) {
        File file = new File(limoAnimationRepository + File.separator + img);
        try {
            return new FileInputStream(file).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
