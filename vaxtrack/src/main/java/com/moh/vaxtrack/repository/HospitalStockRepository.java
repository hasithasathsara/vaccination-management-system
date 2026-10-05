package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Hospital;
import com.moh.vaxtrack.entity.HospitalStock;
import com.moh.vaxtrack.entity.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalStockRepository extends JpaRepository<HospitalStock, Long> {

    Optional<HospitalStock> findByHospitalAndVaccine(Hospital hospital, Vaccine vaccine);

    List<HospitalStock> findByHospital_HospitalId(Long hospitalId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(hs.quantity), 0) FROM HospitalStock hs WHERE hs.hospital.district = :district")
    long sumQuantityByDistrict(@org.springframework.data.repository.query.Param("district") String district);

}
