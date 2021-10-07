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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReserveTrackerService {

    public static final Logger logger = LoggerFactory.getLogger(ReserveTrackerService.class);

    private static final BigDecimal PRICE = Convert.toWei("0.08", Convert.Unit.ETHER);


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

    @PostConstruct
    private void init() {
        String url =
                "https://api.etherscan.io/api?" +
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
    }

    @Scheduled(fixedRate = 60 * 1000)
    private void update() {
        this.accounts.clear();
        this.numReserved = 0;
        EtherScanResult result =
                this.restTemplate.postForObject(this.etherscanEndpoint, "", EtherScanResult.class);
        if (result == null) {
            throw new RuntimeException("result is null");
        }
        var txs = result.result;
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
            this.accounts.put(acc, this.accounts.get(acc) + new BigDecimal(tx.get("value")).divide(PRICE).intValue());
        }
        this.numReserved = this.accounts.values().stream().mapToInt(integer -> integer).sum();
        logger.info("reserve status updated");
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
