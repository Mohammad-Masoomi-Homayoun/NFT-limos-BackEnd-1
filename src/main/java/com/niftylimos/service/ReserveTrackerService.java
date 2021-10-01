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
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ReserveTrackerService {
    public static final Logger logger = LoggerFactory.getLogger(ReserveTrackerService.class);

    @Value("${niftylimos.etherscan.apikey}")
    private String apikey;

    @Value("${niftylimos.account}")
    private String account;

    private RestTemplate restTemplate;

    private String url;

    private ReserveStatus status;

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
        this.url = url;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(fixedRate = 15 * 60 * 1000)
    private void update() {
        logger.info("updating reserve status...");
        ReserveStatus status = new ReserveStatus();
        EtherScanResult result = this.restTemplate.postForObject(this.url, "", EtherScanResult.class);
        List<Map<String, String>> txs = (List<Map<String, String>>) result.result;
        long num;
        BigDecimal price = Convert.toWei("0.08", Convert.Unit.ETHER);
        num = txs.stream()
                .filter(tx -> tx.get("to").equalsIgnoreCase(this.account) && (new BigDecimal(tx.get("value")).compareTo(price)) == 0)
                .count();
        status.setNumReserved(num);
        status.setTimestamp(new Date().getTime());
        status.setAccounts(null);
        this.status = status;
        logger.info("reserve status updated: {}", this.status.getNumReserved());
    }

    public ReserveStatus getStatus() {
        return this.status;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class EtherScanResult {
        private String status;
        private String message;
        private Object result;
    }
}
