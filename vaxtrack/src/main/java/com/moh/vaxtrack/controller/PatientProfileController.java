package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.IdType;
import com.moh.vaxtrack.entity.Patient;
import com.moh.vaxtrack.repository.PatientRepository;
import com.moh.vaxtrack.security.PatientPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patient/profile")
public class PatientProfileController {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientProfileController(PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String show(@AuthenticationPrincipal PatientPrincipal principal, Model model) {
        Patient patient = patientRepository.findById(principal.getPatient().getPatientId()).orElseThrow();
        model.addAttribute("patient", patient);
        model.addAttribute("activePage", "profile");
        model.addAttribute("pageTitle", "My Profile");
        return "patient/profile";
    }


    @PostMapping("/update")
    public String update(@AuthenticationPrincipal PatientPrincipal principal,
                          @RequestParam String fullName,
                          @RequestParam Integer age,
                          @RequestParam String phoneNumber,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String address,
                          @RequestParam(required = false) String disabilities,
                          RedirectAttributes redirectAttributes) {

        if (fullName == null || fullName.isBlank() || age == null || age <= 0 || phoneNumber == null || phoneNumber.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Full name, age, and phone number are required.");
            return "redirect:/patient/profile";
        }

        Patient patient = patientRepository.findById(principal.getPatient().getPatientId()).orElseThrow();
        patient.setFullName(fullName);
        patient.setAge(age);
        patient.setPhoneNumber(phoneNumber);
        patient.setEmail(email);
        patient.setAddress(address);
        patient.setDisabilities(disabilities);
        patientRepository.save(patient);

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/patient/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal PatientPrincipal principal,
                                  @RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  RedirectAttributes redirectAttributes) {

        Patient patient = patientRepository.findById(principal.getPatient().getPatientId()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, patient.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
            return "redirect:/patient/profile";
        }
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters.");
            return "redirect:/patient/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirmation do not match.");
            return "redirect:/patient/profile";
        }

        patient.setPassword(passwordEncoder.encode(newPassword));
        patientRepository.save(patient);

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        return "redirect:/patient/profile";
    }
}
