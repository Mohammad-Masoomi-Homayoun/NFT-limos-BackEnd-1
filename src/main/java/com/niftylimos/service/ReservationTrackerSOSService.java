package com.niftylimos.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.web3j.protocol.core.methods.response.EthLog;

import java.io.IOException;
import java.util.List;

public interface ReservationTrackerSOSService {
    List<EthLog.LogObject> getTransferLogs(long from, long to) throws IOException;

    @Scheduled(fixedDelay = 60 * 1000)
    void scan() throws IOException;
}
