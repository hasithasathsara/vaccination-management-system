package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.StockRequest;
import com.moh.vaxtrack.entity.StockRequestStatus;
import com.moh.vaxtrack.repository.NationalStockRepository;
import com.moh.vaxtrack.repository.StockRequestRepository;
import com.moh.vaxtrack.service.DispatchService;
import com.moh.vaxtrack.service.InsufficientStockException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/inventory/requests")
public class InventoryRequestController {

    private final StockRequestRepository stockRequestRepository;
    private final NationalStockRepository nationalStockRepository;
    private final DispatchService dispatchService;

    public InventoryRequestController(StockRequestRepository stockRequestRepository,
                                       NationalStockRepository nationalStockRepository,
                                       DispatchService dispatchService) {
        this.stockRequestRepository = stockRequestRepository;
        this.nationalStockRepository = nationalStockRepository;
        this.dispatchService = dispatchService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("requests", stockRequestRepository.findAllByOrderByRequestedAtDesc());
        model.addAttribute("activePage", "requests");
        model.addAttribute("pageTitle", "Hospital Request Management");
        return "inventory/requests";
    }

    @GetMapping("/{id}/dispatch-form")
    public String dispatchForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        StockRequest request = stockRequestRepository.findById(id).orElse(null);

        if (request == null || request.getStatus() != StockRequestStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "This request is no longer pending.");
            return "redirect:/inventory/requests";
        }

        model.addAttribute("stockRequest", request);
        model.addAttribute("availableStock",
                nationalStockRepository.sumQuantityByVaccine(request.getVaccine().getVaccineId()));
        model.addAttribute("activePage", "requests");
        model.addAttribute("pageTitle", "Dispatch Stock");
        return "inventory/dispatch-form";
    }

    @PostMapping("/{id}/dispatch")
    public String dispatch(@PathVariable Long id,
                            @RequestParam Integer dispatchQuantity,
                            RedirectAttributes redirectAttributes) {

        StockRequest request = stockRequestRepository.findById(id).orElse(null);
        if (request == null || request.getStatus() != StockRequestStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "This request is no longer pending.");
            return "redirect:/inventory/requests";
        }
        if (dispatchQuantity == null || dispatchQuantity < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dispatch quantity must be at least 1.");
            return "redirect:/inventory/requests/" + id + "/dispatch-form";
        }

        try {
            dispatchService.dispatch(request, dispatchQuantity);
            redirectAttributes.addFlashAttribute("successMessage",
                    dispatchQuantity + " doses of " + request.getVaccine().getBrandName()
                            + " dispatched to " + request.getHospital().getName() + ".");
        } catch (InsufficientStockException e) {
            request.setStatus(StockRequestStatus.REJECTED);
            request.setReason("Insufficient national stock: requested " + dispatchQuantity
                    + ", available " + e.getAvailable());
            request.setResolvedAt(LocalDateTime.now());
            stockRequestRepository.save(request);

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Only " + e.getAvailable() + " doses available — request was automatically rejected.");
        }

        return "redirect:/inventory/requests";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                          @RequestParam String reason,
                          RedirectAttributes redirectAttributes) {

        StockRequest request = stockRequestRepository.findById(id).orElse(null);
        if (request == null || request.getStatus() != StockRequestStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "This request is no longer pending.");
            return "redirect:/inventory/requests";
        }
        if (reason == null || reason.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "A reason is required to reject a request.");
            return "redirect:/inventory/requests";
        }

        request.setStatus(StockRequestStatus.REJECTED);
        request.setReason(reason);
        request.setResolvedAt(LocalDateTime.now());
        stockRequestRepository.save(request);

        redirectAttributes.addFlashAttribute("successMessage", "Request rejected.");
        return "redirect:/inventory/requests";
    }
}
