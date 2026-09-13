package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Hospital;
import com.moh.vaxtrack.entity.HospitalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository tells Spring: "This interface talks to the database."
// It's optional here (JpaRepository already implies it), but we add it for clarity.
@Repository
// We "extend" JpaRepository, which already comes packed with built-in methods
// like save(), findAll(), findById(), deleteById(), etc.
//
// The two things in < > brackets tell Spring:
// 1. Hospital  -> which Entity class this repository manages
// 2. Long      -> the data type of that entity's Primary Key (hospitalId is a Long)
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // Spring Data JPA reads this method name and automatically writes the SQL for us:
    // "SELECT COUNT(*) FROM hospital WHERE status = ?"
    // Used on the Global Dashboard to show "Active Hospitals".
    long countByStatus(HospitalStatus status);

    // "SELECT * FROM hospital WHERE status = ? ORDER BY hospital_id DESC LIMIT 5"
    // Used on the Global Dashboard's "Hospital Overview" table (most recently added first).
    List<Hospital> findTop5ByStatusOrderByHospitalIdDesc(HospitalStatus status);

    // "SELECT * FROM hospital ORDER BY hospital_id DESC"
    // Used on the Hospital Management page — shows every hospital (active AND inactive),
    // newest first, so a Super Admin can see everything including soft-deleted entries.
    List<Hospital> findAllByOrderByHospitalIdDesc();

    // Hospitals in ONE district only — used by Vaccination Event Management so a
    // Sub-Admin can only ever pick a hospital from their own assigned district.
    List<Hospital> findByDistrictAndStatusOrderByName(String district, HospitalStatus status);

}
