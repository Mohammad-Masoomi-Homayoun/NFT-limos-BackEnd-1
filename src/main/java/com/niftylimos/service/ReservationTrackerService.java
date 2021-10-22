package com.niftylimos.service;

import com.niftylimos.domain.Account;
import com.niftylimos.domain.NiftyLimosState;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationTrackerService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationTrackerService.class);

    private final NiftyLimosStateService stateService;

    private final NiftyLimosService service;


    private static final BigDecimal PRICE = Convert.toWei("0.08", Convert.Unit.ETHER);

    private Long block = 0L;


//    @Value("${niftylimos.web3Endpoint}")
//    private String web3Endpoint;

    @Value("${niftylimos.etherscan.apikey}")
    private String apikey;

    @Value("${niftylimos.account}")
    private String account;

    private RestTemplate restTemplate;

    private String etherscanEndpoint;

    private Map<String, Integer> accounts = new HashMap<>();

    private int numReserved = 0;

    public ReservationTrackerService(NiftyLimosStateService stateService, NiftyLimosService service) {
        this.stateService = stateService;
        this.service = service;
    }


    private void updateURL(Long startBlock, Long endBlock) {
        String url =
                "https://api-ropsten.etherscan.io/api?" +
                        "module=account&" +
                        "action=txlist&" +
                        "address=%s&" +
                        "startblock=%d&" +
                        "endblock=%d&" +
                        "page=1&" +
                        "offset=10000&" +
                        "sort=asc&" +
                        "apikey=%s";
        url = String.format(url, this.account, startBlock, endBlock, this.apikey);
        this.etherscanEndpoint = url;
    }

    @PostConstruct
    private void init() {
        String url =
                "https://api-ropsten.etherscan.io/api?" +
                        "module=account&" +
                        "action=txlist&" +
                        "address=%s&" +
                        "startblock=0&" +
                        "endblock=99999999&" +
                        "page=1&" +
                        "offset=10000&" +
                        "sort=asc&" +
                        "apikey=%s";
        url = String.format(url, this.account, this.apikey);
        this.etherscanEndpoint = url;
        this.restTemplate = new RestTemplate();

        if (stateService.get("reservation.tracker.block") == null) {
            this.block = 0L;
        }else {
            this.block = Long.parseLong(stateService.get("reservation.tracker.block"));
        }

    }

    @Scheduled(fixedRate = 60 * 1000)
    protected void update() {
        Long b = getEthLatestBlockNumber();
        updateURL(this.block, b);
        this.accounts.clear();
        this.numReserved = 0;
        EtherScanResult result =
                this.restTemplate.postForObject(this.etherscanEndpoint, "", EtherScanResult.class);
        if (result == null) {
            throw new RuntimeException("result is null");
        }
        var txs = result.result;
        if (txs == null) {
            txs = new ArrayList<>();
        }
        List<Map<String, String>> valid_txs = txs.stream()
                .filter(tx -> tx.get("to").equalsIgnoreCase(this.account))
                .filter(tx -> {
                            BigDecimal[] n = new BigDecimal(tx.get("value")).divideAndRemainder(PRICE);
                            //expect minimum n[0] == 1, and n[1] == 0
                            return n[0].compareTo(BigDecimal.ZERO) != 0 && n[1].compareTo(BigDecimal.ZERO) == 0;
                        }
                )
                //exclude refunded tx
                .filter(tx -> !tx.get("hash").equalsIgnoreCase("0x02c503cb094bb2d345f6c3ec4504892879434002066b1347af856b555ef87205"))
                .collect(Collectors.toList());

        for (Map<String, String> tx : valid_txs) {
            String acc = tx.get("from").toLowerCase();
            if (!this.accounts.containsKey(acc)) {
                this.accounts.put(acc, 0);
            }
            Account account = service.getOrCreateAccount(acc);

            int count = new BigDecimal(tx.get("value")).divide(PRICE).intValue();
            for (int i = 0; i < count; i++) {
                service.reserve(account, tx.get("hash") + "_" + i);
            }
            this.accounts.put(acc, this.accounts.get(acc) + new BigDecimal(tx.get("value")).divide(PRICE).intValue());
        }
        this.numReserved = this.accounts.values().stream().mapToInt(integer -> integer).sum();

        updated(b);
        logger.info("reserve status updated");
    }

    private void updated(Long b) {
        stateService.set("reservation.tracker.block", b.toString());
        this.block = b;
    }

    private Long getEthLatestBlockNumber() {
        String url = "https://api-ropsten.etherscan.io/api?module=proxy&action=eth_blockNumber&apikey=1M5FNB617T3228CQ3R4SIT8SF5QNX6JS8V";
        Map result = restTemplate.postForObject(url, "", Map.class);
        return Long.decode((String) result.get("result"));
    }

    public int getNumTotalReserved() {
        return this.numReserved;
    }

    public int getNumAccountReserved(String account) {
        account = account.toLowerCase();
        return accounts.getOrDefault(account, 0);
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
