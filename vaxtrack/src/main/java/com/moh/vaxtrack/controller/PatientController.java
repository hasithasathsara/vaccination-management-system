package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/patients")
public class PatientController {


    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // List all patients, read-only
    @GetMapping
    public String list(Model model) {
        model.addAttribute("patients", patientRepository.findAllByOrderByPatientIdDesc());
        model.addAttribute("activePage", "patients");
        model.addAttribute("pageTitle", "Patients Management");
        return "admin/patients";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                          @RequestParam String reason,
                          @RequestParam(required = false) String notes,
                          RedirectAttributes redirectAttributes) {

        if (reason == null || reason.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "A reason is required before permanently deleting a patient record.");
            return "redirect:/admin/patients";
        }

        return patientRepository.findById(id).map(patient -> {

            logger.warn("PATIENT HARD DELETE — id={}, name={}, idNumber={}, reason={}, notes={}",
                    patient.getPatientId(), patient.getFullName(), patient.getIdNumber(), reason, notes);

            String name = patient.getFullName();
            patientRepository.delete(patient); // real, permanent delete — no soft-delete flag exists here

            redirectAttributes.addFlashAttribute("successMessage",
                    "Patient \"" + name + "\" was permanently deleted (" + reason + ").");
            return "redirect:/admin/patients";

        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "That patient record no longer exists.");
            return "redirect:/admin/patients";
        });
    }
}
