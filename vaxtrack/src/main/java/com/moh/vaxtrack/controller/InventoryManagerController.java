package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.dto.InventoryManagerForm;
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

// Inventory Manager account management
@Controller
@RequestMapping("/admin/inventory-staff")
public class InventoryManagerController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;

    public InventoryManagerController(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       PasswordGeneratorService passwordGeneratorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordGeneratorService = passwordGeneratorService;
    }

    // List all Inventory Managers
    @GetMapping
    public String list(Model model) {
        model.addAttribute("inventoryStaff", userRepository.findByRoleOrderByUserIdDesc(Role.INVENTORY_MANAGER));
        model.addAttribute("newInventoryManager", new InventoryManagerForm());
        model.addAttribute("activePage", "inventoryStaff");
        model.addAttribute("pageTitle", "Inventory Staff Management");
        return "admin/inventory-staff";
    }

    // Create new Inventory Manager
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("newInventoryManager") InventoryManagerForm form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/admin/inventory-staff";
        }

        // Block duplicate usernames
        if (userRepository.existsByUsername(form.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The username \"" + form.getUsername() + "\" is already taken.");
            return "redirect:/admin/inventory-staff";
        }

        // Generate and hash password
        String plainPassword = passwordGeneratorService.generate();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        User newManager = new User(form.getUsername(), hashedPassword, Role.INVENTORY_MANAGER,
                form.getEmail(), form.getPhoneNumber());
        userRepository.save(newManager);

        // One-time password handoff
        redirectAttributes.addFlashAttribute("generatedUsername", newManager.getUsername());
        redirectAttributes.addFlashAttribute("generatedPassword", plainPassword);

        return "redirect:/admin/inventory-staff";
    }

    // Suspend or reactivate
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        User manager = userRepository.findById(id).orElse(null);

        // Role safety check
        if (manager == null || manager.getRole() != Role.INVENTORY_MANAGER) {
            redirectAttributes.addFlashAttribute("errorMessage", "That Inventory Manager account no longer exists.");
            return "redirect:/admin/inventory-staff";
        }

        if (manager.getStatus() == UserStatus.ACTIVE) {
            manager.setStatus(UserStatus.SUSPENDED);
            redirectAttributes.addFlashAttribute("successMessage",
                    "\"" + manager.getUsername() + "\" was suspended.");
        } else {
            manager.setStatus(UserStatus.ACTIVE);
            redirectAttributes.addFlashAttribute("successMessage",
                    "\"" + manager.getUsername() + "\" was reactivated.");
        }
        userRepository.save(manager);

        return "redirect:/admin/inventory-staff";
    }

    // Collect validation errors
    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
