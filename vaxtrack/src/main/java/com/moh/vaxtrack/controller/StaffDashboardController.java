package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.AppointmentStatus;
import com.moh.vaxtrack.entity.VaccineLogStatus;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.repository.VaccineLogRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class StaffDashboardController {

    private final AppointmentRepository appointmentRepository;
    private final VaccineLogRepository vaccineLogRepository;

    public StaffDashboardController(AppointmentRepository appointmentRepository,
                                     VaccineLogRepository vaccineLogRepository) {
        this.appointmentRepository = appointmentRepository;
        this.vaccineLogRepository = vaccineLogRepository;
    }

    @GetMapping("/staff/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        Long hospitalId = principal.getUser().getHospital().getHospitalId();

        model.addAttribute("username", principal.getUsername());
        model.addAttribute("roleLabel", "Medical Staff");
        model.addAttribute("hospitalName", principal.getUser().getHospital().getName());
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Medical Staff Dashboard");

        List<com.moh.vaxtrack.entity.Appointment> todaysQueue = appointmentRepository
                .findByEvent_Hospital_HospitalIdAndEvent_EventDateOrderByEvent_TimeSlotAsc(hospitalId, LocalDate.now());

        long vaccinated = todaysQueue.stream().filter(a -> a.getStatus() == AppointmentStatus.VACCINATED).count();
        long failed = todaysQueue.stream().filter(a -> a.getStatus() == AppointmentStatus.FAILED).count();
        long pending = todaysQueue.stream().filter(a -> a.getStatus() == AppointmentStatus.BOOKED).count();

        model.addAttribute("todaysAppointments", todaysQueue.size());
        model.addAttribute("vaccinatedToday", vaccinated);
        model.addAttribute("failedToday", failed);
        model.addAttribute("remainingQueue", pending);

        return "staff/dashboard";
    }
}
