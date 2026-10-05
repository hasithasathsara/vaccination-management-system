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
        model.addAttribute("activePage", "wasteReports");
        model.addAttribute("pageTitle", "Waste Reporting Management");
        return "staff/waste-reports";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal CustomUserDetails principal,
                       @RequestParam WasteType wasteType,
                       @RequestParam Integer quantity,
                       @RequestParam(required = false) String notes,
                       RedirectAttributes redirectAttributes) {

        if (quantity == null || quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quantity must be a positive number.");
            return "redirect:/staff/waste-reports";
        }

        WasteReport report = new WasteReport();
        report.setWasteType(wasteType);
        report.setQuantity(quantity);
        report.setNotes(notes);
        report.setHospital(principal.getUser().getHospital());
        report.setReportedBy(principal.getUser());
        report.setReportedAt(LocalDateTime.now());
        wasteReportRepository.save(report);

        redirectAttributes.addFlashAttribute("successMessage", "Waste report submitted.");
        return "redirect:/staff/waste-reports";
    }

    @PostMapping("/{id}/edit")
    public String edit(@AuthenticationPrincipal CustomUserDetails principal,
                        @PathVariable Long id,
                        @RequestParam WasteType wasteType,
                        @RequestParam Integer quantity,
                        @RequestParam(required = false) String notes,
                        RedirectAttributes redirectAttributes) {

        WasteReport report = wasteReportRepository.findById(id).orElse(null);
        if (report == null || !report.getReportedBy().getUserId().equals(principal.getUser().getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "That report no longer exists.");
            return "redirect:/staff/waste-reports";
        }
        if (quantity == null || quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quantity must be a positive number.");
            return "redirect:/staff/waste-reports";
        }

        report.setWasteType(wasteType);
        report.setQuantity(quantity);
        report.setNotes(notes);
        wasteReportRepository.save(report);

        redirectAttributes.addFlashAttribute("successMessage", "Report updated.");
        return "redirect:/staff/waste-reports";
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {

        WasteReport report = wasteReportRepository.findById(id).orElse(null);
        if (report == null || !report.getReportedBy().getUserId().equals(principal.getUser().getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "That report no longer exists.");
            return "redirect:/staff/waste-reports";
        }

        wasteReportRepository.delete(report);

        redirectAttributes.addFlashAttribute("successMessage", "Report deleted.");
        return "redirect:/staff/waste-reports";
    }
}
