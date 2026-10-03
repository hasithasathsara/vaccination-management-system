package com.moh.vaxtrack.event;

import com.moh.vaxtrack.entity.Appointment;
import com.moh.vaxtrack.entity.Notification;
import com.moh.vaxtrack.repository.NotificationRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Observer Pattern: Listens for missed appointment events and creates a patient notification.

@Component
public class AppointmentMissedNotifier {

    private final NotificationRepository notificationRepository;

    public AppointmentMissedNotifier(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @EventListener
    public void onAppointmentMissed(AppointmentMissedEvent event) {
        Appointment appointment = event.getAppointment();
        String message = "Your booking for " + appointment.getEvent().getVaccine().getBrandName()
                + " at " + appointment.getEvent().getHospital().getName()
                + " on " + appointment.getEvent().getEventDate()
                + " was marked as missed since the appointment time passed. You can book again anytime.";
        notificationRepository.save(new Notification(appointment.getPatient(), message));
    }
}
