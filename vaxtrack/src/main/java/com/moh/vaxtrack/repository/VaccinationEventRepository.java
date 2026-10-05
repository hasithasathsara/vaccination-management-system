package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.VaccinationEvent;
import com.moh.vaxtrack.entity.VaccinationEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccinationEventRepository extends JpaRepository<VaccinationEvent, Long> {

    List<VaccinationEvent> findByHospital_DistrictOrderByEventDateDesc(String district);

    long countByHospital_DistrictAndStatus(String district, VaccinationEventStatus status);

    List<VaccinationEvent> findTop5ByHospital_DistrictAndStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(
            String district, VaccinationEventStatus status, LocalDate today);

    @Query("SELECT COALESCE(SUM(e.capacity), 0) FROM VaccinationEvent e " +
            "WHERE e.hospital.hospitalId = :hospitalId AND e.eventDate = :eventDate " +
            "AND e.status = 'SCHEDULED' AND e.eventId <> :excludeEventId")
    int sumScheduledCapacity(@Param("hospitalId") Long hospitalId,
                              @Param("eventDate") LocalDate eventDate,
                              @Param("excludeEventId") Long excludeEventId);

    List<VaccinationEvent> findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(
            VaccinationEventStatus status, LocalDate today);

}
