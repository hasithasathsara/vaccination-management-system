package com.moh.vaxtrack.entity;

import jakarta.persistence.*;

// This annotation tells Hibernate: "This class represents a database table."
@Entity
// This tells Hibernate exactly which table name to use in MySQL.
@Table(name = "hospital")
public class Hospital {

    // This is the Primary Key of the table (unique ID for each hospital).
    @Id
    // This tells MySQL to auto-increment this value (1, 2, 3, 4...) by itself.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long hospitalId;

    // The hospital's name. "nullable = false" means this field is REQUIRED — cannot be empty in the database.
    @Column(name = "name", nullable = false)
    private String name;

    // The district the hospital belongs to (e.g., "Colombo").
    @Column(name = "district", nullable = false)
    private String district;

    // The hospital's physical address.
    @Column(name = "address", nullable = false)
    private String address;

    // Maximum number of patients this hospital can handle per day.
    @Column(name = "daily_capacity", nullable = false)
    private Integer dailyCapacity;

    // This uses an ENUM (a fixed list of allowed values) instead of a plain String,
    // so the database can only ever store "ACTIVE" or "INACTIVE" — nothing else.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HospitalStatus status;

    // --- Constructors ---

    // Empty constructor - Hibernate REQUIRES this to exist, even if we never call it ourselves.
    // It uses this internally when loading data from the database.
    public Hospital() {
    }

    // A constructor we can actually use in our own code, to create a new Hospital object easily.
    public Hospital(String name, String district, String address, Integer dailyCapacity) {
        this.name = name;
        this.district = district;
        this.address = address;
        this.dailyCapacity = dailyCapacity;
        this.status = HospitalStatus.ACTIVE; // New hospitals are always ACTIVE by default.
    }

    // --- Getters and Setters ---
    // Hibernate and our own code need these to read/write each field from outside the class.

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getDailyCapacity() {
        return dailyCapacity;
    }

    public void setDailyCapacity(Integer dailyCapacity) {
        this.dailyCapacity = dailyCapacity;
    }

    public HospitalStatus getStatus() {
        return status;
    }

    public void setStatus(HospitalStatus status) {
        this.status = status;
    }
}