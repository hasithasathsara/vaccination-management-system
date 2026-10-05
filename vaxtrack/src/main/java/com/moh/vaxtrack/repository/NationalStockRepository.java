package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.NationalStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NationalStockRepository extends JpaRepository<NationalStock, Long> {


    List<NationalStock> findAllByOrderByStockIdDesc();

    @Query("SELECT COALESCE(SUM(n.quantity), 0) FROM NationalStock n")
    long sumAllQuantity();

    @Query("SELECT COALESCE(SUM(n.quantity), 0) FROM NationalStock n WHERE n.vaccine.vaccineId = :vaccineId")
    long sumQuantityByVaccine(Long vaccineId);

    List<NationalStock> findByVaccine_VaccineIdAndQuantityGreaterThanOrderByExpiryDateAsc(Long vaccineId, int minQuantity);

}
