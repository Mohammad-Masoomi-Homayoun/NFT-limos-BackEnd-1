package com.niftylimos.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.web3j.protocol.core.methods.response.EthLog;

import java.io.IOException;
import java.util.List;

public interface MintTrackerService {
    List<EthLog.LogObject> getMintLogs(long from, long to) throws IOException;

    List<EthLog.LogObject> getRevealLogs(long from, long to) throws IOException;

    @Scheduled(fixedDelay = 60 * 1000)
    void scan() throws IOException;
}
