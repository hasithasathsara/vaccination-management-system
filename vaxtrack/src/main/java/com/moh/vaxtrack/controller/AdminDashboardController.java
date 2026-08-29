package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.HospitalStatus;
import com.moh.vaxtrack.entity.VaccineStatus;
import com.moh.vaxtrack.repository.HospitalRepository;
import com.moh.vaxtrack.repository.VaccineRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Collections;

@Controller
public class AdminDashboardController {

    private final HospitalRepository hospitalRepository;
    private final VaccineRepository vaccineRepository;

    public AdminDashboardController(HospitalRepository hospitalRepository,
                                     VaccineRepository vaccineRepository) {
        this.hospitalRepository = hospitalRepository;
        this.vaccineRepository = vaccineRepository;
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model, Principal principal) {

        model.addAttribute("username", principal.getName());
        model.addAttribute("roleLabel", "Super Administrator");

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Global Dashboard");

        //  Hospital Management
        long activeHospitals = hospitalRepository.countByStatus(HospitalStatus.ACTIVE);
        model.addAttribute("activeHospitals", activeHospitals);
        model.addAttribute("recentHospitals",
                hospitalRepository.findTop5ByStatusOrderByHospitalIdDesc(HospitalStatus.ACTIVE));
        model.addAttribute("totalHospitals", hospitalRepository.count());

        // Vaccine Management
        model.addAttribute("activeVaccines", vaccineRepository.countByStatus(VaccineStatus.ACTIVE));
        model.addAttribute("deactivatedVaccines", vaccineRepository.countByStatus(VaccineStatus.INACTIVE));

        //  Placeholder data (Patient, VaccineLog, Stock entities not built yet)
        // TODO: Wire these to real repositories once each module is implemented.
        model.addAttribute("totalVaccinated", 0);
        model.addAttribute("totalFailed", 0);
        model.addAttribute("totalStock", 0);
        model.addAttribute("recentPatients", Collections.emptyList());
        model.addAttribute("totalPatients", 0);

        return "admin/dashboard";
    }
}
