package com.moh.vaxtrack.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hospitalstock")
public class HospitalStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_stock_id")
    private Long hospitalStockId;

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public HospitalStock() {
    }

    public Long getHospitalStockId() { return hospitalStockId; }
    public void setHospitalStockId(Long hospitalStockId) { this.hospitalStockId = hospitalStockId; }
    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
    public Vaccine getVaccine() { return vaccine; }
    public void setVaccine(Vaccine vaccine) { this.vaccine = vaccine; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
