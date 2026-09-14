package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.AppointmentStatus;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.security.PatientPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PatientDashboardController {

    private final AppointmentRepository appointmentRepository;

    public PatientDashboardController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/patient/dashboard")
    public String showDashboard(@AuthenticationPrincipal PatientPrincipal principal, Model model) {

        model.addAttribute("patient", principal.getPatient());

        model.addAttribute("upcomingAppointment",
                appointmentRepository.findByPatient_PatientIdAndStatus(
                        principal.getPatient().getPatientId(), AppointmentStatus.BOOKED).orElse(null));

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "My Dashboard");
        return "patient/dashboard";
    }
}
