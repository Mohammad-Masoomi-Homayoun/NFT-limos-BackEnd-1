package com.niftylimos.repo;

import com.niftylimos.domain.MintEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MintEventRepository extends JpaRepository<MintEvent, Long> {
}
