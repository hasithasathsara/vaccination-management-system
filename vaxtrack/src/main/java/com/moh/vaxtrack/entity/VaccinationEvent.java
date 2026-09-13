package com.moh.vaxtrack.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

// A scheduled vaccination session at one hospital
@Entity
@Table(name = "vaccinationevent")
public class VaccinationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    // Stored as "08:00 - 12:00" — the schema only has one column for this,
    // so the two time pickers in the UI get combined into one string here.
    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    // Which Sub-Admin scheduled this event
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VaccinationEventStatus status;

    public VaccinationEvent() {
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
    public Vaccine getVaccine() { return vaccine; }
    public void setVaccine(Vaccine vaccine) { this.vaccine = vaccine; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public VaccinationEventStatus getStatus() { return status; }
    public void setStatus(VaccinationEventStatus status) { this.status = status; }
}
