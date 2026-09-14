package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.WasteReport;
import com.moh.vaxtrack.entity.WasteType;
import com.moh.vaxtrack.repository.WasteReportRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/staff/waste-reports")
public class WasteReportController {

    private final WasteReportRepository wasteReportRepository;

    public WasteReportController(WasteReportRepository wasteReportRepository) {
        this.wasteReportRepository = wasteReportRepository;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("reports",
                wasteReportRepository.findByReportedBy_UserIdOrderByReportedAtDesc(principal.getUser().getUserId()));
        model.addAttribute("wasteTypes", WasteType.values());
        model.addAttribute("activePage", "waste");
        model.addAttribute("pageTitle", "Waste Reporting Management");
        return "staff/waste-reports";
    }

    // Submit a new report
    @PostMapping("/add")
    public String add(@AuthenticationPrincipal CustomUserDetails principal,
                       @RequestParam WasteType wasteType,
                       @RequestParam Integer quantity,
                       RedirectAttributes redirectAttributes) {

        if (quantity == null || quantity < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quantity must be at least 1.");
            return "redirect:/staff/waste-reports";
        }

        WasteReport report = new WasteReport();
        report.setWasteType(wasteType);
        report.setQuantity(quantity);
        report.setHospital(principal.getUser().getHospital());
        report.setReportedBy(principal.getUser());
        report.setReportedAt(LocalDateTime.now());
        wasteReportRepository.save(report);

        redirectAttributes.addFlashAttribute("successMessage", "Waste report submitted.");
        return "redirect:/staff/waste-reports";
    }
}
