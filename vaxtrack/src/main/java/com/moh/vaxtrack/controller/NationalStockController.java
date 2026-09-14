package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.dto.NationalStockForm;
import com.moh.vaxtrack.entity.NationalStock;
import com.moh.vaxtrack.entity.Vaccine;
import com.moh.vaxtrack.entity.VaccineStatus;
import com.moh.vaxtrack.repository.NationalStockRepository;
import com.moh.vaxtrack.repository.VaccineRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/stock")
public class NationalStockController {

    private final NationalStockRepository stockRepository;
    private final VaccineRepository vaccineRepository;

    public NationalStockController(NationalStockRepository stockRepository, VaccineRepository vaccineRepository) {
        this.stockRepository = stockRepository;
        this.vaccineRepository = vaccineRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("stockBatches", stockRepository.findAllByOrderByStockIdDesc());
        model.addAttribute("vaccines", vaccineRepository.findByStatusOrderByBrandName(VaccineStatus.ACTIVE));
        model.addAttribute("newStock", new NationalStockForm());
        model.addAttribute("activePage", "stock");
        model.addAttribute("pageTitle", "Inventory Management");
        return "inventory/stock";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("newStock") NationalStockForm form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/inventory/stock";
        }

        Vaccine vaccine = vaccineRepository.findById(form.getVaccineId()).orElse(null);
        if (vaccine == null || vaccine.getStatus() != VaccineStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid vaccine selection.");
            return "redirect:/inventory/stock";
        }

        NationalStock stock = new NationalStock();
        stock.setVaccine(vaccine);
        stock.setBatchNumber(form.getBatchNumber());
        stock.setExpiryDate(form.getExpiryDate());
        stock.setQuantity(form.getQuantity());
        stock.setOriginalQuantity(form.getQuantity());
        stock.setReceivedDate(LocalDateTime.now());
        stockRepository.save(stock);

        redirectAttributes.addFlashAttribute("successMessage",
                form.getQuantity() + " doses of " + vaccine.getBrandName() + " added to national stock.");
        return "redirect:/inventory/stock";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                        @RequestParam String batchNumber,
                        @RequestParam String expiryDate,
                        @RequestParam Integer quantity,
                        RedirectAttributes redirectAttributes) {

        NationalStock stock = stockRepository.findById(id).orElse(null);
        if (stock == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "That stock batch no longer exists.");
            return "redirect:/inventory/stock";
        }
        if (!stock.isUntouched()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This batch has already been dispatched from — it can no longer be edited.");
            return "redirect:/inventory/stock";
        }
        if (quantity == null || quantity < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quantity must be at least 1.");
            return "redirect:/inventory/stock";
        }

        stock.setBatchNumber(batchNumber);
        stock.setExpiryDate(java.time.LocalDate.parse(expiryDate));
        stock.setQuantity(quantity);
        stock.setOriginalQuantity(quantity);
        stockRepository.save(stock);

        redirectAttributes.addFlashAttribute("successMessage", "Stock batch updated successfully.");
        return "redirect:/inventory/stock";
    }


    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        NationalStock stock = stockRepository.findById(id).orElse(null);
        if (stock == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "That stock batch no longer exists.");
            return "redirect:/inventory/stock";
        }
        if (!stock.isUntouched()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This batch has already been dispatched from — it can no longer be deleted.");
            return "redirect:/inventory/stock";
        }

        stockRepository.delete(stock);
        redirectAttributes.addFlashAttribute("successMessage", "Stock batch removed.");
        return "redirect:/inventory/stock";
    }

    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
