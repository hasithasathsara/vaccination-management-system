package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.HospitalStatus;
import com.moh.vaxtrack.entity.NationalStock;
import com.moh.vaxtrack.entity.StockRequestStatus;
import com.moh.vaxtrack.repository.HospitalRepository;
import com.moh.vaxtrack.repository.NationalStockRepository;
import com.moh.vaxtrack.repository.StockRequestRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class InventoryDashboardController {

    private final NationalStockRepository stockRepository;
    private final HospitalRepository hospitalRepository;
    private final StockRequestRepository stockRequestRepository;

    public InventoryDashboardController(NationalStockRepository stockRepository,
                                         HospitalRepository hospitalRepository,
                                         StockRequestRepository stockRequestRepository) {
        this.stockRepository = stockRepository;
        this.hospitalRepository = hospitalRepository;
        this.stockRequestRepository = stockRequestRepository;
    }

    @GetMapping("/inventory/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        model.addAttribute("username", principal.getUsername());
        model.addAttribute("roleLabel", "Inventory Manager");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Inventory Manager Dashboard");

        model.addAttribute("totalStock", stockRepository.sumAllQuantity());
        model.addAttribute("totalHospitals", hospitalRepository.countByStatus(HospitalStatus.ACTIVE));
        model.addAttribute("pendingRequests",
                stockRequestRepository.countByStatus(StockRequestStatus.PENDING));
        model.addAttribute("recentPendingRequests",
                stockRequestRepository.findByStatusOrderByRequestedAtAsc(StockRequestStatus.PENDING));

        model.addAttribute("approvedRequests", stockRequestRepository.countByStatus(StockRequestStatus.DISPATCHED));

        long totalDistributed = stockRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                .filter(r -> r.getStatus() == StockRequestStatus.DISPATCHED && r.getDispatchedQuantity() != null)
                .mapToLong(r -> r.getDispatchedQuantity())
                .sum();
        model.addAttribute("totalDistributed", totalDistributed);

        Map<String, Long> stockByVaccine = new LinkedHashMap<>();
        for (NationalStock batch : stockRepository.findAllByOrderByStockIdDesc()) {
            String brand = batch.getVaccine().getBrandName();
            stockByVaccine.merge(brand, (long) batch.getQuantity(), Long::sum);
        }
        model.addAttribute("stockByVaccine", stockByVaccine);

        return "inventory/dashboard";
    }
}
