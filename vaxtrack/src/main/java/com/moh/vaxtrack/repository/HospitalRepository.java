package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    // We don't need to write any methods ourselves yet.
    // JpaRepository already gives us everything we need for basic CRUD.
    // Later, if we need a custom search (e.g., "find hospitals by district"),
    // we'll add a method signature here, and Spring will auto-generate the query.

}