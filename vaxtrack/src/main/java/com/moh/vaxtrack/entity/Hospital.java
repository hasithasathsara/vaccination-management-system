package com.moh.vaxtrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "hospital")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long hospitalId;


    @NotBlank(message = "Hospital name is required")
    @Column(name = "name", nullable = false)
    private String name;

    // The district the hospital belongs to
    @NotBlank(message = "District is required")
    @Column(name = "district", nullable = false)
    private String district;

    // Maximum number of patients this hospital can handle per day
    @NotNull(message = "Daily capacity is required")
    @Min(value = 1, message = "Daily capacity must be at least 1")
    @Column(name = "daily_capacity", nullable = false)
    private Integer dailyCapacity;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HospitalStatus status;



    public Hospital() {
    }

    public Hospital(String name, String district, Integer dailyCapacity) {
        this.name = name;
        this.district = district;
        this.dailyCapacity = dailyCapacity;
        this.status = HospitalStatus.ACTIVE; // New hospitals are always ACTIVE in default.
    }


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
