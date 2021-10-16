package com.niftylimos.repo;

import com.niftylimos.domain.Account;
import com.niftylimos.domain.LimoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimoTicketRepository extends JpaRepository<LimoTicket, Long> {
    List<LimoTicket> findAllByAccount(Account account);
}
