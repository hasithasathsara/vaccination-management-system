package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findAllByOrderByPatientIdDesc();

    List<Patient> findTop5ByOrderByPatientIdDesc();

    boolean existsByIdNumber(String idNumber);

    Optional<Patient> findByIdNumber(String idNumber);

    Optional<Patient> findByPhoneNumber(String phoneNumber);

}
