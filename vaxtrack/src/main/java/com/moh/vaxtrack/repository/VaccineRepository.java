package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Vaccine;
import com.moh.vaxtrack.entity.VaccineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    long countByStatus(VaccineStatus status);

    List<Vaccine> findAllByOrderByVaccineIdDesc();

}
