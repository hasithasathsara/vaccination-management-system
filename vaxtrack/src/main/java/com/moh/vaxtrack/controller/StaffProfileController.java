package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.Role;
import com.moh.vaxtrack.entity.User;
import com.moh.vaxtrack.repository.UserRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff-profile")
public class StaffProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String show(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User user = userRepository.findById(principal.getUser().getUserId()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("activePage", "profile");
        model.addAttribute("pageTitle", "My Profile");
        return templateForRole(user.getRole());
    }

    @PostMapping("/update")
    public String update(@AuthenticationPrincipal CustomUserDetails principal,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String phoneNumber,
                          RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(principal.getUser().getUserId()).orElseThrow();
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/staff-profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(principal.getUser().getUserId()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
            return "redirect:/staff-profile";
        }
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters.");
            return "redirect:/staff-profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirmation do not match.");
            return "redirect:/staff-profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        return "redirect:/staff-profile";
    }

    @GetMapping("/force-reset-password")
    public String showForceReset(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("username", principal.getUsername());
        return "staff-force-reset";
    }

    @PostMapping("/force-reset-password")
    public String submitForceReset(@AuthenticationPrincipal CustomUserDetails principal,
                                    @RequestParam String currentPassword,
                                    @RequestParam String newPassword,
                                    @RequestParam String confirmPassword,
                                    HttpServletRequest request,
                                    Model model) {

        User user = userRepository.findById(principal.getUser().getUserId()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("errorMessage", "Current password is incorrect.");
            model.addAttribute("username", principal.getUsername());
            return "staff-force-reset";
        }
        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("errorMessage", "New password must be at least 6 characters.");
            model.addAttribute("username", principal.getUsername());
            return "staff-force-reset";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "New password and confirmation do not match.");
            model.addAttribute("username", principal.getUsername());
            return "staff-force-reset";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustResetPassword(false);
        userRepository.save(user);

        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/login/staff?passwordChanged=true";
    }

    private String templateForRole(Role role) {
        return switch (role) {
            case SUPER_ADMIN -> "admin/profile";
            case SUB_ADMIN -> "subadmin/profile";
            case INVENTORY_MANAGER -> "inventory/profile";
            case MEDICAL_STAFF -> "staff/profile";
        };
    }

}
