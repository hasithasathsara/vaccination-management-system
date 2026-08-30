package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Full patient list, newest first
    List<Patient> findAllByOrderByPatientIdDesc();

    // Latest 5 for the dashboard
    List<Patient> findTop5ByOrderByPatientIdDesc();

    // Blocks duplicate registrations
    boolean existsByIdNumber(String idNumber);

    // Login lookup by ID number
    Optional<Patient> findByIdNumber(String idNumber);

    // Login lookup by phone number
    Optional<Patient> findByPhoneNumber(String phoneNumber);

}
