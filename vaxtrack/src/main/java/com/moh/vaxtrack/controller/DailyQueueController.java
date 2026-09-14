package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.Appointment;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DailyQueueController {

    private final AppointmentRepository appointmentRepository;

    public DailyQueueController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/staff/daily-queue")
    public String showQueue(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        Long hospitalId = principal.getUser().getHospital().getHospitalId();
        List<Appointment> queue = appointmentRepository
                .findByEvent_Hospital_HospitalIdAndEvent_EventDateOrderByEvent_TimeSlotAsc(hospitalId, LocalDate.now());

        model.addAttribute("queue", queue);
        model.addAttribute("activePage", "dailyQueue");
        model.addAttribute("pageTitle", "Daily Queue");
        return "staff/daily-queue";
    }
}
