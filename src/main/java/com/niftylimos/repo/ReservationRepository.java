package com.niftylimos.repo;

import com.niftylimos.domain.Account;
import com.niftylimos.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByAccount(Account account);
}
