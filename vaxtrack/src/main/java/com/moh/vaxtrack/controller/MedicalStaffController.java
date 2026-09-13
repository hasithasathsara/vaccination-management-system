package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.dto.MedicalStaffForm;
import com.moh.vaxtrack.entity.Hospital;
import com.moh.vaxtrack.entity.HospitalStatus;
import com.moh.vaxtrack.entity.Role;
import com.moh.vaxtrack.entity.User;
import com.moh.vaxtrack.entity.UserStatus;
import com.moh.vaxtrack.repository.HospitalRepository;
import com.moh.vaxtrack.repository.UserRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import com.moh.vaxtrack.service.PasswordGeneratorService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/subadmin/hospital-staff")
public class MedicalStaffController {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;

    public MedicalStaffController(UserRepository userRepository,
                                   HospitalRepository hospitalRepository,
                                   PasswordEncoder passwordEncoder,
                                   PasswordGeneratorService passwordGeneratorService) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordGeneratorService = passwordGeneratorService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        String district = principal.getUser().getDistrict();

        model.addAttribute("staff",
                userRepository.findByRoleAndHospital_DistrictOrderByUserIdDesc(Role.MEDICAL_STAFF, district));
        model.addAttribute("hospitals",
                hospitalRepository.findByDistrictAndStatusOrderByName(district, HospitalStatus.ACTIVE));
        model.addAttribute("newStaff", new MedicalStaffForm());
        model.addAttribute("activePage", "hospitalStaff");
        model.addAttribute("pageTitle", "Hospital Staff Management");
        return "subadmin/hospital-staff";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal CustomUserDetails principal,
                       @Valid @ModelAttribute("newStaff") MedicalStaffForm form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/subadmin/hospital-staff";
        }

        String district = principal.getUser().getDistrict();

        Hospital hospital = hospitalRepository.findById(form.getHospitalId()).orElse(null);
        if (hospital == null || hospital.getStatus() != HospitalStatus.ACTIVE
                || !hospital.getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid hospital selection.");
            return "redirect:/subadmin/hospital-staff";
        }

        if (userRepository.existsByUsername(form.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The username \"" + form.getUsername() + "\" is already taken.");
            return "redirect:/subadmin/hospital-staff";
        }

        String plainPassword = passwordGeneratorService.generate();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        User newStaff = new User(form.getUsername(), hashedPassword, Role.MEDICAL_STAFF,
                form.getEmail(), form.getPhoneNumber());
        newStaff.setHospital(hospital);
        userRepository.save(newStaff);

        redirectAttributes.addFlashAttribute("generatedUsername", newStaff.getUsername());
        redirectAttributes.addFlashAttribute("generatedPassword", plainPassword);

        return "redirect:/subadmin/hospital-staff";
    }

    @PostMapping("/{id}/reassign")
    public String reassign(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable Long id,
                            @RequestParam Long hospitalId,
                            RedirectAttributes redirectAttributes) {

        String district = principal.getUser().getDistrict();

        User staff = userRepository.findById(id).orElse(null);
        if (staff == null || staff.getRole() != Role.MEDICAL_STAFF
                || !staff.getHospital().getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "That staff account no longer exists.");
            return "redirect:/subadmin/hospital-staff";
        }

        Hospital newHospital = hospitalRepository.findById(hospitalId).orElse(null);
        if (newHospital == null || newHospital.getStatus() != HospitalStatus.ACTIVE
                || !newHospital.getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid hospital selection.");
            return "redirect:/subadmin/hospital-staff";
        }

        staff.setHospital(newHospital);
        userRepository.save(staff);

        redirectAttributes.addFlashAttribute("successMessage",
                "\"" + staff.getUsername() + "\" was reassigned to " + newHospital.getName() + ".");
        return "redirect:/subadmin/hospital-staff";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@AuthenticationPrincipal CustomUserDetails principal,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        String district = principal.getUser().getDistrict();

        User staff = userRepository.findById(id).orElse(null);
        if (staff == null || staff.getRole() != Role.MEDICAL_STAFF
                || !staff.getHospital().getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "That staff account no longer exists.");
            return "redirect:/subadmin/hospital-staff";
        }

        if (staff.getStatus() == UserStatus.ACTIVE) {
            staff.setStatus(UserStatus.SUSPENDED);
            redirectAttributes.addFlashAttribute("successMessage", "\"" + staff.getUsername() + "\" was suspended.");
        } else {
            staff.setStatus(UserStatus.ACTIVE);
            redirectAttributes.addFlashAttribute("successMessage", "\"" + staff.getUsername() + "\" was reactivated.");
        }
        userRepository.save(staff);

        return "redirect:/subadmin/hospital-staff";
    }

    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
