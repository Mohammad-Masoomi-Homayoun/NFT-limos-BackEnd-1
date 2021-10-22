package com.niftylimos.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Convert;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationTrackerService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationTrackerService.class);

    private final StateService stateService;

    private final NiftyLimosService service;

    @Value("${NL.etherscan-base}")
    private String etherscanBase;

    @Value("${NL.reserve-price}")
    private String priceString;

    private BigDecimal price;

    private AtomicLong newReservations = new AtomicLong(0L);

    private LocalDateTime lastCheckedTimestamp = LocalDateTime.now();

    @Value("${NL.etherscan-apikey}")
    private String etherscanAPIkey;

    @Value("${NL.reserve-account}")
    private String reservationAccount;

    private RestTemplate restTemplate;

    public ReservationTrackerService(StateService stateService, NiftyLimosService service) {
        this.stateService = stateService;
        this.service = service;
    }

    @PostConstruct
    private void init() {
        logger.info("account = {}", reservationAccount);
        this.price = Convert.toWei(priceString, Convert.Unit.ETHER);
        logger.info("price  = {} ETH", this.priceString);
        this.restTemplate = new RestTemplate();
        logger.info("last scanned block : {}", getPreviousBlock());
        logger.info("using ethereum network : {}", etherscanBase);
        lastCheckedTimestamp = LocalDateTime.now();
        update();
    }

    private String makeEtherscanURL(Long startBlock, Long endBlock) {
        String url =
                "%s/api?" +
                        "module=account&" +
                        "action=txlist&" +
                        "address=%s&" +
                        "startblock=%d&" +
                        "endblock=%d&" +
                        "page=1&" +
                        "offset=10000&" +
                        "sort=asc&" +
                        "apikey=%s";
        return String.format(url, etherscanBase, this.reservationAccount, startBlock, endBlock, this.etherscanAPIkey);
    }

    private Long getPreviousBlock(){
        return Long.parseLong(stateService.get("reservation-tracker-block").orElse("0"));
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    protected void report() {
        logger.info("new reservations found from {} = {}", lastCheckedTimestamp, newReservations);
        lastCheckedTimestamp = LocalDateTime.now();
        newReservations = new AtomicLong(0);
    }

    @Scheduled(fixedRate = 60 * 1000)
    protected void update() {
        boolean needReport = false;
        Long latestBlock = getEthLatestBlockNumber();
        String url = makeEtherscanURL(getPreviousBlock() + 1, latestBlock);
        EtherScanResult result =
                this.restTemplate.postForObject(url, "", EtherScanResult.class);
        if (result == null) {
            throw new RuntimeException("result is null");
        }
        var txs = result.result;
        if (txs == null) {
            txs = new ArrayList<>();
        }
        List<Map<String, String>> valid_txs = txs.stream()

                //incoming tx
                .filter(tx -> tx.get("to").equalsIgnoreCase(this.reservationAccount))

                //value
                .filter(tx -> {
                            BigDecimal[] n = new BigDecimal(tx.get("value")).divideAndRemainder(price);
                            //expect minimum n[0] == 1, and n[1] == 0
                            return n[0].compareTo(BigDecimal.ZERO) != 0 && n[1].compareTo(BigDecimal.ZERO) == 0;
                        }
                )

                //exclude refunded tx
                .filter(tx -> !tx.get("hash").equalsIgnoreCase("0x02c503cb094bb2d345f6c3ec4504892879434002066b1347af856b555ef87205"))

                .collect(Collectors.toList());

        for (Map<String, String> tx : valid_txs) {
            String acc = tx.get("from").toLowerCase();
            int count = new BigDecimal(tx.get("value")).divide(price).intValue();
            for (int i = 0; i < count; i++) {
                needReport = true;
                service.reserve(acc, tx.get("hash") + "_" + i);
                newReservations.incrementAndGet();
            }
        }
        updated(latestBlock);
        if (needReport) {
            report();
        }
    }

    private void updated(Long block) {
        stateService.set("reservation-tracker-block", block.toString());
    }

    private Long getEthLatestBlockNumber() {
        String url = String.format("%s/api?module=proxy&action=eth_blockNumber&apikey=%s", etherscanBase, etherscanAPIkey);
        Map response = restTemplate.postForObject(url, "", Map.class);
        String result = (String) response.get("result");
        return Long.decode(result);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class EtherScanResult {
        private String status;
        private String message;
        private List<Map<String, String>> result;
    }
}
