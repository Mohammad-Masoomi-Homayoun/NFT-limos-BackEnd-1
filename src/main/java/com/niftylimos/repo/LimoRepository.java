package com.niftylimos.repo;

import com.niftylimos.domain.Limo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimoRepository extends JpaRepository<Limo, Long> {
    List<Limo> findAllByTicketsEmptyAndIdGreaterThanEqualOrderByIdAsc(Long offset);
}
