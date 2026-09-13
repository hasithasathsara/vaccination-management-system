package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.StockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {

    List<StockRequest> findByHospital_DistrictOrderByRequestedAtDesc(String district);

    List<StockRequest> findTop5ByHospital_DistrictOrderByRequestedAtDesc(String district);

}
