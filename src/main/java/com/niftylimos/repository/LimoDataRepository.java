package com.niftylimos.repository;

import com.niftylimos.domain.LimoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimoDataRepository extends JpaRepository<LimoData, Long> {
    LimoData getBySignature(String sig);
    List<LimoData> findAllByLimoIsNullOrderById();
}
