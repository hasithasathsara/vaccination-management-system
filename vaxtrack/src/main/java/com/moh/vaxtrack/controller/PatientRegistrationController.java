package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.dto.PatientRegistrationForm;
import com.moh.vaxtrack.entity.IdType;
import com.moh.vaxtrack.entity.Patient;
import com.moh.vaxtrack.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/register/patient")
public class PatientRegistrationController {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientRegistrationController(PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show the registration form
    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("registrationForm", new PatientRegistrationForm());
        return "patient/register";
    }

    // Process the registration form
    @PostMapping
    public String register(@Valid @ModelAttribute("registrationForm") PatientRegistrationForm form,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("errorMessage", firstErrorMessage(result));
            return "patient/register";
        }

        // Passwords must match
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "patient/register";
        }

        // Block duplicate ID number registration
        if (patientRepository.existsByIdNumber(form.getIdNumber())) {
            model.addAttribute("errorMessage", "An account with this ID number already exists.");
            return "patient/register";
        }

        // Build and save the new patient
        Patient patient = new Patient();
        patient.setIdType(IdType.valueOf(form.getIdType()));
        patient.setIdNumber(form.getIdNumber());
        patient.setFullName(form.getFullName());
        patient.setAge(form.getAge());
        patient.setPhoneNumber(form.getPhoneNumber());
        patient.setEmail(form.getEmail());
        patient.setAddress(form.getAddress());
        patient.setPassword(passwordEncoder.encode(form.getPassword())); // hash before saving

        patientRepository.save(patient);

        redirectAttributes.addFlashAttribute("successMessage",
                "Account created! Log in with your ID number or phone number.");
        return "redirect:/login/patient";
    }

    // Collect validation errors
    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
