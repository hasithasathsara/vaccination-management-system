package com.moh.vaxtrack.event;

import com.moh.vaxtrack.entity.Appointment;

// Observer Pattern: Represents an event containing the appointment that was marked as MISSED.

public class AppointmentMissedEvent {

    private final Appointment appointment;

    public AppointmentMissedEvent(Appointment appointment) {
        this.appointment = appointment;
    }

    public Appointment getAppointment() {
        return appointment;
    }
}
