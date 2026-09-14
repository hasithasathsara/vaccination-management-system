package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.VaccinationEventStatus;
import com.moh.vaxtrack.repository.HospitalStockRepository;
import com.moh.vaxtrack.repository.StockRequestRepository;
import com.moh.vaxtrack.repository.VaccinationEventRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Collections;

@Controller
public class SubAdminDashboardController {

    private final VaccinationEventRepository eventRepository;
    private final StockRequestRepository stockRequestRepository;
    private final HospitalStockRepository hospitalStockRepository;

    public SubAdminDashboardController(VaccinationEventRepository eventRepository,
                                        StockRequestRepository stockRequestRepository,
                                        HospitalStockRepository hospitalStockRepository) {
        this.eventRepository = eventRepository;
        this.stockRequestRepository = stockRequestRepository;
        this.hospitalStockRepository = hospitalStockRepository;
    }

    @GetMapping("/subadmin/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        String district = principal.getUser().getDistrict();

        model.addAttribute("username", principal.getUsername());
        model.addAttribute("district", district);
        model.addAttribute("roleLabel", "Sub Administrator");

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "District Dashboard");

        model.addAttribute("totalEvents",
                eventRepository.countByHospital_DistrictAndStatus(district, VaccinationEventStatus.SCHEDULED));
        model.addAttribute("upcomingEvents",
                eventRepository.findTop5ByHospital_DistrictAndStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(
                        district, VaccinationEventStatus.SCHEDULED, LocalDate.now()));

        model.addAttribute("inventoryRequests",
                stockRequestRepository.findTop5ByHospital_DistrictOrderByRequestedAtDesc(district));

        model.addAttribute("availableStock", hospitalStockRepository.sumQuantityByDistrict(district));

        // TODO: wire these once the clinical module is built
        model.addAttribute("totalVaccinated", 0);
        model.addAttribute("totalFailed", 0);
        model.addAttribute("recentEvents", Collections.emptyList());

        return "subadmin/dashboard";
    }
}
