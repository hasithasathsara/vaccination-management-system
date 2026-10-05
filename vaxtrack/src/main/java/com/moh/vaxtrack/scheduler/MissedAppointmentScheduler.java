package com.moh.vaxtrack.scheduler;

import com.moh.vaxtrack.entity.Appointment;
import com.moh.vaxtrack.entity.AppointmentStatus;
import com.moh.vaxtrack.event.AppointmentMissedEvent;
import com.moh.vaxtrack.repository.AppointmentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class MissedAppointmentScheduler {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MissedAppointmentScheduler(AppointmentRepository appointmentRepository,
                                       ApplicationEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.eventPublisher = eventPublisher;
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


            // Observer Pattern: Publishes an event when a booked appointment becomes MISSED.
            eventPublisher.publishEvent(new AppointmentMissedEvent(appointment));
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
