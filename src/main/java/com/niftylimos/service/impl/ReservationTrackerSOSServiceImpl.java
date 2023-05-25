package com.niftylimos.service.impl;

import com.niftylimos.service.NiftyLimosService;
import com.niftylimos.service.ReservationTrackerSOSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationTrackerSOSServiceImpl implements ReservationTrackerSOSService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationTrackerSOSServiceImpl.class);

    private static final String TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
    private static final String TO_TOPIC = "0x0000000000000000000000001E91bB97e4DEdb4923c3e499fEcDC6aeAf14ca74";
    private static final BigDecimal PRICE = Convert.toWei("10000000", Convert.Unit.ETHER);

    private final NiftyLimosService niftyLimosService;
    private final StateServiceImpl stateServiceImpl;

    @Value("${NL.eth-node-url}")
    private String ethNodeURL;

    @Value("${NL.sos-address}")
    private String contractAddress;

    private Web3j w3;

    public ReservationTrackerSOSServiceImpl(NiftyLimosService niftyLimosService, StateServiceImpl service) {
        this.niftyLimosService = niftyLimosService;
        this.stateServiceImpl = service;
    }

    @PostConstruct
    private void init() {
        this.w3 = Web3j.build(new HttpService(this.ethNodeURL));
        logger.info("SOS contract address :  {}", contractAddress);
        logger.info("to topic: {}", TO_TOPIC);
        logger.info("price : {} SOS", PRICE);
        logger.info("using endpoint {}", ethNodeURL);
    }


    private EthFilter createTransferFilter(long from, long to) {
        BigInteger fromBlock = BigInteger.valueOf(from);
        BigInteger toBlock = BigInteger.valueOf(to);
        var filter = new EthFilter(DefaultBlockParameter.valueOf(fromBlock),
                DefaultBlockParameter.valueOf(toBlock),
                this.contractAddress);
        filter.addSingleTopic(TRANSFER_TOPIC);
        filter.addNullTopic();
        filter.addSingleTopic(TO_TOPIC);
        return filter;
    }

    private long getLastCheckedBlock() {
        var stringValue = stateServiceImpl.get("reservation-sos-tracker-last-checked-block");
        if (stringValue.isEmpty()) {
            return -1;
        }
        return Long.parseLong(stringValue.get());
    }

    private void setLastCheckedBlock(Long newValue) {
        stateServiceImpl.set("reservation-sos-tracker-last-checked-block", newValue.toString());
    }

    private long getLatestEthBlock() throws IOException {
        return w3.ethBlockNumber().send().getBlockNumber().longValue();
    }

    @Override
    public List<EthLog.LogObject> getTransferLogs(long from, long to) throws IOException {
        return w3.ethGetLogs(createTransferFilter(from, to)).send().getLogs().stream()
                .map(r -> (EthLog.LogObject) r.get())
                .collect(Collectors.toList());
    }

    private void verifyAndReserve(EthLog.LogObject logObject) {
        String address = new Address(Numeric.toBigInt(logObject.getTopics().get(1))).getValue();
        BigDecimal value = new BigDecimal(Numeric.toBigInt(logObject.getData()));
        var n = value.divideAndRemainder(PRICE);
        logger.info("verify transfer log, tx: {}, log index: {},  address: {}, value: {}, n[0]: {}, n[1]: {}",
                logObject.getTransactionHash(),
                logObject.getLogIndex(),
                new Address(Numeric.toBigInt(logObject.getTopics().get(1))).getValue(),
                value,
                n[0],
                n[1]);

        if (Numeric.toBigInt(address).compareTo(BigInteger.ZERO) == 0) {
            logger.warn("to address is 0, transfer rejected");
            return;
        }

        if (n[0].compareTo(BigDecimal.ZERO) == 0 || n[1].compareTo(BigDecimal.ZERO) != 0) {
            logger.warn("invalid transfer value, transfer ignored");
            return;
        }

        for (int i = 0; i < n[0].intValue(); i++) {
            String tx = "SOS:" + logObject.getTransactionHash() + ":" + logObject.getLogIndex().toString() + ":" + i;
            niftyLimosService.reserve(address, tx);
        }
    }

    @Override
    @Scheduled(fixedDelay = 60 * 1000)
    synchronized public void scan() throws IOException {
        long latestEthBlock = getLatestEthBlock();
        long lastChecked = getLastCheckedBlock();

        if (lastChecked == latestEthBlock) {
            logger.info("no new Block to scan");
            return;
        }

        long from = lastChecked + 1;
        long to = latestEthBlock - 10L;
        if (!(from <= to)) {
            return;
        }

        var transferLogs = getTransferLogs(from, to);
        if (!transferLogs.isEmpty()) {
            transferLogs.forEach(l -> logger.info("transfer log detected, block number = {}, tx: {}",
                    l.getBlockNumber(),
                    l.getTransactionHash()));
        }

        for (var log : transferLogs) {
            verifyAndReserve(log);
        }
        setLastCheckedBlock(to);
    }
}
