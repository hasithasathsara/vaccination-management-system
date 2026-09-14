package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.StockRequest;
import com.moh.vaxtrack.entity.StockRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {

    // Full request list for one district, newest first — used by Sub-Admin's own page
    List<StockRequest> findByHospital_DistrictOrderByRequestedAtDesc(String district);

    // Latest 5 for one district — used by the District Dashboard
    List<StockRequest> findTop5ByHospital_DistrictOrderByRequestedAtDesc(String district);

    // Nationwide pending — used by the Inventory Manager's dashboard preview
    List<StockRequest> findByStatusOrderByRequestedAtAsc(StockRequestStatus status);

    long countByStatus(StockRequestStatus status);

    // EVERY request nationwide, any status — the full Hospital Request Management page
    List<StockRequest> findAllByOrderByRequestedAtDesc();

}
