package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.VaccineLog;
import com.moh.vaxtrack.entity.VaccineLogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VaccineLogRepository extends JpaRepository<VaccineLog, Long> {

    Optional<VaccineLog> findByAppointment_AppointmentIdAndIsDeletedFalse(Long appointmentId);

    long countByAppointment_Patient_PatientIdAndVaccine_VaccineIdAndStatusAndIsDeletedFalse(
            Long patientId, Long vaccineId, VaccineLogStatus status);

    List<VaccineLog> findByAppointment_Event_Hospital_HospitalIdAndIsDeletedFalseOrderByLoggedAtDesc(Long hospitalId);

    List<VaccineLog> findByAppointment_Patient_PatientIdAndIsDeletedFalseOrderByLoggedAtDesc(Long patientId);

    long countByAppointment_Event_Hospital_HospitalIdAndStatusAndIsDeletedFalseAndLoggedAtBetween(
            Long hospitalId, VaccineLogStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);

    long countByAppointment_Event_Hospital_DistrictAndStatusAndIsDeletedFalse(String district, VaccineLogStatus status);

    long countByStatusAndIsDeletedFalse(VaccineLogStatus status);

}
