package com.niftylimos.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MintTrackerServiceImpl implements MintTrackerService {
    private static final Logger logger = LoggerFactory.getLogger(MintTrackerService.class);

    private static final String TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
    private static final String REVEAL_TOPIC = "0x66b9f0d2f5af4125e8098bf5f1efc517ed46a70d8638734d186af310e2f8bc75";
    private static final String ZERO_ADDRESS_TOPIC = "0x0000000000000000000000000000000000000000000000000000000000000000";

    private final NiftyLimosService niftyLimosService;
    private final StateService stateService;

    @Value("${NL.eth-node-url}")
    private String ethNodeURL;

    @Value("${NL.contract-address}")
    private String contractAddress;

    private Web3j w3;

    @PostConstruct
    private void init() {
        this.w3 = Web3j.build(new HttpService(this.ethNodeURL));
        logger.info("contract address :  {}", contractAddress);
        logger.info("using endpoint {}", ethNodeURL);
    }

    private boolean revealed() {
        return stateService.get("revealed").isPresent();
    }

    private EthFilter createMintFilter(long from, long to) {
        BigInteger fromBlock = BigInteger.valueOf(from);
        BigInteger toBlock = BigInteger.valueOf(to);
        var filter = new EthFilter(DefaultBlockParameter.valueOf(fromBlock),
                DefaultBlockParameter.valueOf(toBlock),
                this.contractAddress);
        filter.addSingleTopic(TRANSFER_TOPIC);
        filter.addSingleTopic(ZERO_ADDRESS_TOPIC);
        return filter;
    }

    private EthFilter createRevealFilter(long from, long to) {
        BigInteger fromBlock = BigInteger.valueOf(from);
        BigInteger toBlock = BigInteger.valueOf(to);
        var filter = new EthFilter(DefaultBlockParameter.valueOf(fromBlock),
                DefaultBlockParameter.valueOf(toBlock),
                this.contractAddress);
        filter.addSingleTopic(REVEAL_TOPIC);
        return filter;
    }

    private long getLastCheckedBlock() {
        var stringValue = stateService.get("mint-tracker-last-checked-block");
        if (stringValue.isEmpty()) {
            return -1;
        }
        return Long.parseLong(stringValue.get());
    }

    private void setLastCheckedBlock(Long newValue) {
        stateService.set("mint-tracker-last-checked-block", newValue.toString());
    }

    private long getLatestEthBlock() throws IOException {
        return w3.ethBlockNumber().send().getBlockNumber().longValue();
    }

    public List<EthLog.LogObject> getMintLogs(long from, long to) throws IOException {
        return w3.ethGetLogs(createMintFilter(from, to)).send().getLogs().stream()
                .map(r -> (EthLog.LogObject) r.get())
                .collect(Collectors.toList());
    }

    public List<EthLog.LogObject> getRevealLogs(long from, long to) throws IOException {
        return w3.ethGetLogs(createRevealFilter(from, to)).send().getLogs().stream()
                .map(r -> (EthLog.LogObject) r.get())
                .collect(Collectors.toList());
    }

    @Scheduled(fixedDelay = 60 * 1000)
    synchronized public void scan() throws IOException {
        long latestEthBlock = getLatestEthBlock();
        long lastChecked = getLastCheckedBlock();

        if (lastChecked == latestEthBlock) {
            logger.info("no new Block to scan");
            return;
        }

        long from = lastChecked + 1;
        long to = latestEthBlock;

        if (!revealed()) {
            var revealLogs = getRevealLogs(from, to);
            if (!revealLogs.isEmpty()) {
                logger.info("reveal log detected, block number = {}", revealLogs.get(0).getBlockNumber());
                niftyLimosService.reveal(revealLogs.get(0));
            }
        }

        var mintLogs = getMintLogs(from, to);
        if (!mintLogs.isEmpty()) {
            mintLogs.forEach(l -> logger.info("mint log detected, block number = {}, tokenId = {}",
                    l.getBlockNumber(),
                    Numeric.toBigInt(l.getTopics().get(3)).longValue()));
            niftyLimosService.newMint(mintLogs);
        }
        setLastCheckedBlock(to);
    }
}
