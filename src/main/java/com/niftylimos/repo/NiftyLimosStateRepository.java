package com.niftylimos.repo;


import com.niftylimos.domain.NiftyLimosState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NiftyLimosStateRepository extends JpaRepository<NiftyLimosState, String> {
}
