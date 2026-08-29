package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.Vaccine;
import com.moh.vaxtrack.entity.VaccineStatus;
import com.moh.vaxtrack.repository.VaccineRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/vaccines")
public class VaccineController {

    private final VaccineRepository vaccineRepository;

    public VaccineController(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    //shows every vaccine, active AND inactive
    @GetMapping
    public String list(Model model) {
        model.addAttribute("vaccines", vaccineRepository.findAllByOrderByVaccineIdDesc());
        model.addAttribute("newVaccine", new Vaccine()); // empty object for the "Add Vaccine" form
        model.addAttribute("activePage", "vaccines");
        model.addAttribute("pageTitle", "Vaccine Management");
        return "admin/vaccines";
    }

    // registers a new vaccine brand into the master list
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("newVaccine") Vaccine newVaccine,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/admin/vaccines";
        }

        newVaccine.setStatus(VaccineStatus.ACTIVE);
        vaccineRepository.save(newVaccine);

        redirectAttributes.addFlashAttribute("successMessage",
                "Vaccine \"" + newVaccine.getBrandName() + "\" was added successfully.");
        return "redirect:/admin/vaccines";
    }

    // updates brand name / doses required.
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                        @Valid @ModelAttribute("editVaccine") Vaccine form,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/admin/vaccines";
        }

        Vaccine existing = vaccineRepository.findById(id).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "That vaccine no longer exists.");
            return "redirect:/admin/vaccines";
        }

        existing.setBrandName(form.getBrandName());
        existing.setDosesRequired(form.getDosesRequired());
        vaccineRepository.save(existing);

        redirectAttributes.addFlashAttribute("successMessage",
                "Vaccine \"" + existing.getBrandName() + "\" was updated successfully.");
        return "redirect:/admin/vaccines";
    }

    // soft delete / reactivate.

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        Vaccine vaccine = vaccineRepository.findById(id).orElse(null);
        if (vaccine == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "That vaccine no longer exists.");
            return "redirect:/admin/vaccines";
        }

        if (vaccine.getStatus() == VaccineStatus.ACTIVE) {
            vaccine.setStatus(VaccineStatus.INACTIVE);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vaccine \"" + vaccine.getBrandName() + "\" was deactivated.");
        } else {
            vaccine.setStatus(VaccineStatus.ACTIVE);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vaccine \"" + vaccine.getBrandName() + "\" was reactivated.");
        }
        vaccineRepository.save(vaccine);

        return "redirect:/admin/vaccines";
    }

    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
