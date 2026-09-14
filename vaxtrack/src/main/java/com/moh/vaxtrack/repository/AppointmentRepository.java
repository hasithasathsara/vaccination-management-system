package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Appointment;
import com.moh.vaxtrack.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatient_PatientIdOrderByBookedAtDesc(Long patientId);

    Optional<Appointment> findByPatient_PatientIdAndStatus(Long patientId, AppointmentStatus status);

    Optional<Appointment> findByPatient_PatientIdAndEvent_Vaccine_VaccineIdAndStatus(
            Long patientId, Long vaccineId, AppointmentStatus status);

    long countByEvent_EventIdAndStatus(Long eventId, AppointmentStatus status);

    Optional<Appointment> findByQrCode(String qrCode);

    List<Appointment> findByPatient_PatientIdAndStatusNotOrderByBookedAtDesc(Long patientId, AppointmentStatus status);

    List<Appointment> findByPatient_PatientIdAndStatusOrderByBookedAtDesc(Long patientId, AppointmentStatus status);

}
