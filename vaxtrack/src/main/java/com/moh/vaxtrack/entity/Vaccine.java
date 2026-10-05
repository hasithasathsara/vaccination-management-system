package com.moh.vaxtrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Represents a row in the vaccine table — the global master list of vaccine

@Entity
@Table(name = "vaccine")
public class Vaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccine_id")
    private Long vaccineId;

    // vaccine's brand name
    @NotBlank(message = "Brand name is required")
    @Column(name = "brand_name", nullable = false)
    private String brandName;

    // How many doses a patient needs
    @NotNull(message = "Doses required is required")
    @Min(value = 1, message = "Doses required must be at least 1")
    @Column(name = "doses_required", nullable = false)
    private Integer dosesRequired;

    // Soft-delete  When a vaccine brand is discontinued change the status to INACTIVE
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VaccineStatus status;



    public Vaccine() {
    }

    public Vaccine(String brandName, Integer dosesRequired) {
        this.brandName = brandName;
        this.dosesRequired = dosesRequired;
        this.status = VaccineStatus.ACTIVE; // New vaccines are always ACTIVE by default.
    }



    public Long getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Integer getDosesRequired() {
        return dosesRequired;
    }

    public void setDosesRequired(Integer dosesRequired) {
        this.dosesRequired = dosesRequired;
    }

    public VaccineStatus getStatus() {
        return status;
    }

    public void setStatus(VaccineStatus status) {
        this.status = status;
    }
}
