package com.moh.vaxtrack.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "event_nurse_assignment")
public class EventNurseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private VaccinationEvent event;

    @ManyToOne
    @JoinColumn(name = "nurse_id", nullable = false)
    private User nurse;

    public EventNurseAssignment() {
    }

    public EventNurseAssignment(VaccinationEvent event, User nurse) {
        this.event = event;
        this.nurse = nurse;
    }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public VaccinationEvent getEvent() { return event; }
    public void setEvent(VaccinationEvent event) { this.event = event; }
    public User getNurse() { return nurse; }
    public void setNurse(User nurse) { this.nurse = nurse; }
}
