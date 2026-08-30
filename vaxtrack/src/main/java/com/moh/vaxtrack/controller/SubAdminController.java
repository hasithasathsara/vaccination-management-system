package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.dto.SubAdminForm;
import com.moh.vaxtrack.entity.Role;
import com.moh.vaxtrack.entity.User;
import com.moh.vaxtrack.entity.UserStatus;
import com.moh.vaxtrack.repository.UserRepository;
import com.moh.vaxtrack.service.PasswordGeneratorService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

// Everything under /admin/sub-admins — the Super Admin's Sub-Admin Management screen.
// Unlike Hospital/Vaccine, there's no "Edit" here: per the spec, Sub-Admins manage their
// OWN email/phone/password themselves once logged in. The Super Admin's job is limited to
// creating the account and suspending/reactivating it — nothing else.
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

    // GET /admin/sub-admins — lists every Sub-Admin account, active and suspended.
    @GetMapping
    public String list(Model model) {
        model.addAttribute("subAdmins", userRepository.findByRoleOrderByUserIdDesc(Role.SUB_ADMIN));
        model.addAttribute("newSubAdmin", new SubAdminForm());
        model.addAttribute("activePage", "subAdmins");
        model.addAttribute("pageTitle", "Sub Admin Management");
        return "admin/sub-admins";
    }

    // POST /admin/sub-admins/add — creates a new Sub-Admin with a system-generated password.
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("newSubAdmin") SubAdminForm form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/admin/sub-admins";
        }

        // Usernames must be unique across the WHOLE user table (Super Admins, Sub-Admins,
        // Inventory Managers, and Nurses all share the same "user" table).
        if (userRepository.existsByUsername(form.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The username \"" + form.getUsername() + "\" is already taken.");
            return "redirect:/admin/sub-admins";
        }

        // Generate a plain-text temporary password, then hash it before it ever touches the database.
        String plainPassword = passwordGeneratorService.generate();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // This constructor already sets mustResetPassword = true and status = ACTIVE.
        User newSubAdmin = new User(form.getUsername(), hashedPassword, Role.SUB_ADMIN,
                form.getEmail(), form.getPhoneNumber());
        userRepository.save(newSubAdmin);

        // The PLAIN password only ever exists here, for this one redirect — it is never stored
        // anywhere, and once this page is left, there is no way to retrieve it again. The Super
        // Admin must copy it and share it with the Sub-Admin right now.
        redirectAttributes.addFlashAttribute("generatedUsername", newSubAdmin.getUsername());
        redirectAttributes.addFlashAttribute("generatedPassword", plainPassword);

        return "redirect:/admin/sub-admins";
    }

    // POST /admin/sub-admins/{id}/toggle-status — suspend or reactivate an account.
    // This IS the "soft delete" for staff accounts (never a hard delete).
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        User subAdmin = userRepository.findById(id).orElse(null);

        // Safety check: only allow this endpoint to touch SUB_ADMIN accounts, even if
        // someone guesses a different user's id in the URL.
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
