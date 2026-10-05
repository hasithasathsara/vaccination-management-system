package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.Appointment;
import com.moh.vaxtrack.entity.AppointmentStatus;
import com.moh.vaxtrack.entity.Patient;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.repository.NotificationRepository;
import com.moh.vaxtrack.repository.PatientRepository;
import com.moh.vaxtrack.security.PatientPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class PatientDashboardController {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final PatientRepository patientRepository;

    public PatientDashboardController(AppointmentRepository appointmentRepository,
                                       NotificationRepository notificationRepository,
                                       PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/patient/dashboard")
    public String showDashboard(@AuthenticationPrincipal PatientPrincipal principal, Model model) {

        Long patientId = principal.getPatient().getPatientId();

        // Re-fetch fresh, not principal.getPatient() — that's a login-time snapshot, so
        // editing the profile (name/age/phone) wouldn't show up here until re-login.
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        model.addAttribute("patient", patient);

        // A patient can have several active bookings at once (one per vaccine) — show
        // whichever one's date is soonest as "the" upcoming appointment on this card.
        List<Appointment> activeBookings =
                appointmentRepository.findByPatient_PatientIdAndStatusOrderByBookedAtDesc(patientId, AppointmentStatus.BOOKED);
        Appointment nextAppointment = activeBookings.stream()
                .min(Comparator.comparing(a -> a.getEvent().getEventDate()))
                .orElse(null);
        model.addAttribute("upcomingAppointment", nextAppointment);

        model.addAttribute("notifications",
                notificationRepository.findTop5ByPatient_PatientIdOrderByCreatedAtDesc(patientId));

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "My Dashboard");
        return "patient/dashboard";
    }
}
