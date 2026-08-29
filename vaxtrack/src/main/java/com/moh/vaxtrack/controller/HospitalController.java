package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.Hospital;
import com.moh.vaxtrack.entity.HospitalStatus;
import com.moh.vaxtrack.repository.HospitalRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/hospitals")
public class HospitalController {

    private final HospitalRepository hospitalRepository;

    public HospitalController(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

   //shows the full hospital list
    @GetMapping
    public String list(Model model) {
        model.addAttribute("hospitals", hospitalRepository.findAllByOrderByHospitalIdDesc());
        model.addAttribute("newHospital", new Hospital()); // empty object for the "Add Hospital" form
        model.addAttribute("activePage", "hospitals");
        model.addAttribute("pageTitle", "Hospital Management");
        return "admin/hospitals";
    }

    // creates a new hospital

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("newHospital") Hospital newHospital,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/admin/hospitals";
        }


        newHospital.setStatus(HospitalStatus.ACTIVE);
        hospitalRepository.save(newHospital);

        redirectAttributes.addFlashAttribute("successMessage",
                "Hospital \"" + newHospital.getName() + "\" was added successfully.");
        return "redirect:/admin/hospitals";
    }

    //updates an existing hospital's details
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                        @Valid @ModelAttribute("editHospital") Hospital form,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/admin/hospitals";
        }

        Hospital existing = hospitalRepository.findById(id).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "That hospital no longer exists.");
            return "redirect:/admin/hospitals";
        }

        existing.setName(form.getName());
        existing.setDistrict(form.getDistrict());
        existing.setDailyCapacity(form.getDailyCapacity());
        hospitalRepository.save(existing);

        redirectAttributes.addFlashAttribute("successMessage",
                "Hospital \"" + existing.getName() + "\" was updated successfully.");
        return "redirect:/admin/hospitals";
    }

    //the "Soft Delete" from the spec

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        Hospital hospital = hospitalRepository.findById(id).orElse(null);
        if (hospital == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "That hospital no longer exists.");
            return "redirect:/admin/hospitals";
        }

        if (hospital.getStatus() == HospitalStatus.ACTIVE) {
            hospital.setStatus(HospitalStatus.INACTIVE);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Hospital \"" + hospital.getName() + "\" was deactivated.");
        } else {
            hospital.setStatus(HospitalStatus.ACTIVE);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Hospital \"" + hospital.getName() + "\" was reactivated.");
        }
        hospitalRepository.save(hospital);

        return "redirect:/admin/hospitals";
    }



    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
