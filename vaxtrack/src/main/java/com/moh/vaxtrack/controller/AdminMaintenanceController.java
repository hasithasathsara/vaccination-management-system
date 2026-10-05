package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.scheduler.MissedAppointmentScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/maintenance")
public class AdminMaintenanceController {

    private final MissedAppointmentScheduler missedAppointmentScheduler;

    public AdminMaintenanceController(MissedAppointmentScheduler missedAppointmentScheduler) {
        this.missedAppointmentScheduler = missedAppointmentScheduler;
    }

    @GetMapping
    public String show(Model model) {
        model.addAttribute("activePage", "maintenance");
        model.addAttribute("pageTitle", "System Maintenance");
        return "admin/maintenance";
    }

    @PostMapping("/sweep-missed")
    public String runSweep(Model model) {
        int count = missedAppointmentScheduler.sweepMissedAppointments();
        model.addAttribute("sweepResult", count);
        model.addAttribute("activePage", "maintenance");
        model.addAttribute("pageTitle", "System Maintenance");
        return "admin/maintenance";
    }
}
