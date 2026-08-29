package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Hospital;
import com.moh.vaxtrack.entity.HospitalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository

public interface HospitalRepository extends JpaRepository<Hospital, Long> {


    long countByStatus(HospitalStatus status);


    List<Hospital> findTop5ByStatusOrderByHospitalIdDesc(HospitalStatus status);


    List<Hospital> findAllByOrderByHospitalIdDesc();

}
