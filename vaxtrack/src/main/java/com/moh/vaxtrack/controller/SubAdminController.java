package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.dto.SubAdminForm;
import com.moh.vaxtrack.entity.Role;
import com.moh.vaxtrack.entity.User;
import com.moh.vaxtrack.entity.UserStatus;
import com.moh.vaxtrack.repository.UserRepository;
import com.moh.vaxtrack.service.PasswordGeneratorService;
import com.moh.vaxtrack.util.SriLankaDistricts;
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
@RequestMapping("/admin/sub-admins")
public class SubAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;

    public SubAdminController(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               PasswordGeneratorService passwordGeneratorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordGeneratorService = passwordGeneratorService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("subAdmins", userRepository.findByRoleOrderByUserIdDesc(Role.SUB_ADMIN));
        model.addAttribute("newSubAdmin", new SubAdminForm());
        model.addAttribute("districtsByProvince", SriLankaDistricts.BY_PROVINCE);
        model.addAttribute("activePage", "subAdmins");
        model.addAttribute("pageTitle", "Sub Admin Management");
        return "admin/sub-admins";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("newSubAdmin") SubAdminForm form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/admin/sub-admins";
        }


        if (userRepository.existsByUsername(form.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The username \"" + form.getUsername() + "\" is already taken.");
            return "redirect:/admin/sub-admins";
        }

        String plainPassword = passwordGeneratorService.generate();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        User newSubAdmin = new User(form.getUsername(), hashedPassword, Role.SUB_ADMIN,
                form.getEmail(), form.getPhoneNumber());
        newSubAdmin.setDistrict(form.getDistrict()); // set once at creation, never edited afterward
        userRepository.save(newSubAdmin);


        redirectAttributes.addFlashAttribute("generatedUsername", newSubAdmin.getUsername());
        redirectAttributes.addFlashAttribute("generatedPassword", plainPassword);

        return "redirect:/admin/sub-admins";
    }


    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        User subAdmin = userRepository.findById(id).orElse(null);


        if (subAdmin == null || subAdmin.getRole() != Role.SUB_ADMIN) {
            redirectAttributes.addFlashAttribute("errorMessage", "That Sub-Admin account no longer exists.");
            return "redirect:/admin/sub-admins";
        }

        if (subAdmin.getStatus() == UserStatus.ACTIVE) {
            subAdmin.setStatus(UserStatus.SUSPENDED);
            redirectAttributes.addFlashAttribute("successMessage",
                    "\"" + subAdmin.getUsername() + "\" was suspended.");
        } else {
            subAdmin.setStatus(UserStatus.ACTIVE);
            redirectAttributes.addFlashAttribute("successMessage",
                    "\"" + subAdmin.getUsername() + "\" was reactivated.");
        }
        userRepository.save(subAdmin);

        return "redirect:/admin/sub-admins";
    }

    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
