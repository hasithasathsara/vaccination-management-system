package com.moh.vaxtrack.repository;

import com.moh.vaxtrack.entity.EventNurseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface EventNurseAssignmentRepository extends JpaRepository<EventNurseAssignment, Long> {

    List<EventNurseAssignment> findByEvent_EventId(Long eventId);


    @Modifying
    @Transactional
    @Query("DELETE FROM EventNurseAssignment a WHERE a.event.eventId = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

}
