package com.moh.vaxtrack.scheduler;

import com.moh.vaxtrack.entity.Appointment;
import com.moh.vaxtrack.entity.AppointmentStatus;
import com.moh.vaxtrack.entity.Notification;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class MissedAppointmentScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;

    public MissedAppointmentScheduler(AppointmentRepository appointmentRepository,
                                       NotificationRepository notificationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void runScheduledSweep() {
        sweepMissedAppointments();
    }

    public int sweepMissedAppointments() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<Appointment> candidates = appointmentRepository
                .findByStatusAndEvent_EventDateLessThanEqual(AppointmentStatus.BOOKED, today);

        int sweptCount = 0;
        for (Appointment appointment : candidates) {
            LocalDateTime slotEnd = parseSlotEnd(appointment);
            if (slotEnd == null || !now.isAfter(slotEnd)) {
                continue;
            }

            appointment.setStatus(AppointmentStatus.MISSED);
            appointmentRepository.save(appointment);
            sweptCount++;

            String message = "Your booking for " + appointment.getEvent().getVaccine().getBrandName()
                    + " at " + appointment.getEvent().getHospital().getName()
                    + " on " + appointment.getEvent().getEventDate()
                    + " was marked as missed since the appointment time passed. You can book again anytime.";
            notificationRepository.save(new Notification(appointment.getPatient(), message));
        }

        return sweptCount;
    }

    private LocalDateTime parseSlotEnd(Appointment appointment) {
        try {
            String timeSlot = appointment.getEvent().getTimeSlot();
            String endPart = timeSlot.split(" - ")[1].trim();
            LocalTime endTime = LocalTime.parse(endPart);
            return LocalDateTime.of(appointment.getEvent().getEventDate(), endTime);
        } catch (Exception e) {
            return null;
        }
    }
}
