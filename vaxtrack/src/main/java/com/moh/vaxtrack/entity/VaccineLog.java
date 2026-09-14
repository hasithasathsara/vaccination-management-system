package com.moh.vaxtrack.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "vaccinelog")
public class VaccineLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VaccineLogStatus status;


    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "medical_staff_id", nullable = false)
    private User medicalStaff;

    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    public VaccineLog() {
    }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Integer getDoseNumber() { return doseNumber; }
    public void setDoseNumber(Integer doseNumber) { this.doseNumber = doseNumber; }
    public VaccineLogStatus getStatus() { return status; }
    public void setStatus(VaccineLogStatus status) { this.status = status; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public User getMedicalStaff() { return medicalStaff; }
    public void setMedicalStaff(User medicalStaff) { this.medicalStaff = medicalStaff; }
    public Vaccine getVaccine() { return vaccine; }
    public void setVaccine(Vaccine vaccine) { this.vaccine = vaccine; }
}
