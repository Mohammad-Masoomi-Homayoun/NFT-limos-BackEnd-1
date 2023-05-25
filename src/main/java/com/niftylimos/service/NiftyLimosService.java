package com.niftylimos.service;

import com.niftylimos.domain.Account;
import com.niftylimos.domain.Limo;
import com.niftylimos.domain.LimoTicket;
import com.niftylimos.domain.dto.*;
import org.web3j.protocol.core.methods.response.EthLog;

import java.util.List;

public interface NiftyLimosService {
    void reveal(EthLog.LogObject revealLog);

    void newMint(List<EthLog.LogObject> logs);

    String getContractAddress();

    void change04TicketsTokenIds();

    String getContractAbi();

    Long setExpire(Long newExpire);

    Account getOrCreateAccount(String address);

    AccountDTO getAccount(String address);

    List<AccountDTO> getAllAccounts();

    ReservationDTO getReservation(Long id);

    List<ReservationDTO> getAllReservations();

    Long getReservationCount();

    Long getMintedCount();

    LimoDTO getLimo(Long tokenId);

    List<LimoDTO> getAllLimos();

    LimoTicketDTO getTicket(Long tokenId);

    List<LimoTicketDTO> getAllTickets();

    LimoTicket issue(Account account, Limo limo, Long expire);

    LimoTicketDTO issueTicket(IssueTicketRequestDTO req);

    LimoTicketDTO issueTicket(String address, Long tokenId, Long expire);

    List<LimoTicketDTO> issueTicketForAllReservations();

    void initLimos();

    void reserve(String acc, String tx);

    List<String> getLimos();

    LimoMetadataDTO getLimoMetadata(Long id);
}
