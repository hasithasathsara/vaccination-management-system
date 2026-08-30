package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.security.PatientPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PatientDashboardController {

    // Full patient dashboard (booking, QR, history) comes in a later delivery.
    // This just shows the digital ID card, to prove login + the shell work end-to-end.
    @GetMapping("/patient/dashboard")
    public String showDashboard(@AuthenticationPrincipal PatientPrincipal principal, Model model) {
        model.addAttribute("patient", principal.getPatient());
        return "patient/dashboard";
    }
}
