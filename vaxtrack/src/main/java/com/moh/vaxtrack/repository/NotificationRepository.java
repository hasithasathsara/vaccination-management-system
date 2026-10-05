package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByPatient_PatientIdOrderByCreatedAtDesc(Long patientId);

    List<Notification> findTop5ByPatient_PatientIdOrderByCreatedAtDesc(Long patientId);

}
