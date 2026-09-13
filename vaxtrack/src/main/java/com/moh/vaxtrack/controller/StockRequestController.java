package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.dto.StockRequestForm;
import com.moh.vaxtrack.entity.*;
import com.moh.vaxtrack.repository.HospitalRepository;
import com.moh.vaxtrack.repository.StockRequestRepository;
import com.moh.vaxtrack.repository.VaccineRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/subadmin/stock-requests")
public class StockRequestController {

    private final StockRequestRepository stockRequestRepository;
    private final HospitalRepository hospitalRepository;
    private final VaccineRepository vaccineRepository;

    public StockRequestController(StockRequestRepository stockRequestRepository,
                                   HospitalRepository hospitalRepository,
                                   VaccineRepository vaccineRepository) {
        this.stockRequestRepository = stockRequestRepository;
        this.hospitalRepository = hospitalRepository;
        this.vaccineRepository = vaccineRepository;
    }


    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        String district = principal.getUser().getDistrict();

        model.addAttribute("requests", stockRequestRepository.findByHospital_DistrictOrderByRequestedAtDesc(district));
        model.addAttribute("hospitals",
                hospitalRepository.findByDistrictAndStatusOrderByName(district, HospitalStatus.ACTIVE));
        model.addAttribute("vaccines", vaccineRepository.findByStatusOrderByBrandName(VaccineStatus.ACTIVE));
        model.addAttribute("newRequest", new StockRequestForm());
        model.addAttribute("activePage", "stockRequests");
        model.addAttribute("pageTitle", "Inventory Request Management");
        return "subadmin/stock-requests";
    }


    @PostMapping("/add")
    public String add(@AuthenticationPrincipal CustomUserDetails principal,
                       @Valid @ModelAttribute("newRequest") StockRequestForm form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/subadmin/stock-requests";
        }

        String district = principal.getUser().getDistrict();


        Hospital hospital = hospitalRepository.findById(form.getHospitalId()).orElse(null);
        if (hospital == null || hospital.getStatus() != HospitalStatus.ACTIVE
                || !hospital.getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid hospital selection.");
            return "redirect:/subadmin/stock-requests";
        }


        Vaccine vaccine = vaccineRepository.findById(form.getVaccineId()).orElse(null);
        if (vaccine == null || vaccine.getStatus() != VaccineStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid vaccine selection.");
            return "redirect:/subadmin/stock-requests";
        }

        StockRequest request = new StockRequest();
        request.setHospital(hospital);
        request.setVaccine(vaccine);
        request.setRequestedQuantity(form.getRequestedQuantity());
        request.setStatus(StockRequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        request.setRequestedBy(principal.getUser());
        stockRequestRepository.save(request);

        redirectAttributes.addFlashAttribute("successMessage",
                "Stock request for " + vaccine.getBrandName() + " at " + hospital.getName() + " was submitted.");
        return "redirect:/subadmin/stock-requests";
    }


    @PostMapping("/{id}/withdraw")
    public String withdraw(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable Long id,
                            RedirectAttributes redirectAttributes) {

        StockRequest request = stockRequestRepository.findById(id).orElse(null);
        String district = principal.getUser().getDistrict();

        if (request == null || !request.getHospital().getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "That request no longer exists.");
            return "redirect:/subadmin/stock-requests";
        }
        if (request.getStatus() != StockRequestStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Only pending requests can be withdrawn — the Inventory Manager has already acted on this one.");
            return "redirect:/subadmin/stock-requests";
        }

        stockRequestRepository.delete(request);

        redirectAttributes.addFlashAttribute("successMessage", "Stock request withdrawn.");
        return "redirect:/subadmin/stock-requests";
    }

    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
