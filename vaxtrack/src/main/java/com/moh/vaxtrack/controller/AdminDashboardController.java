package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.HospitalStatus;
import com.moh.vaxtrack.entity.VaccineStatus;
import com.moh.vaxtrack.repository.HospitalRepository;
import com.moh.vaxtrack.repository.NationalStockRepository;
import com.moh.vaxtrack.repository.PatientRepository;
import com.moh.vaxtrack.repository.VaccineRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AdminDashboardController {

    private final HospitalRepository hospitalRepository;
    private final VaccineRepository vaccineRepository;
    private final PatientRepository patientRepository;
    private final NationalStockRepository nationalStockRepository;

    public AdminDashboardController(HospitalRepository hospitalRepository,
                                     VaccineRepository vaccineRepository,
                                     PatientRepository patientRepository,
                                     NationalStockRepository nationalStockRepository) {
        this.hospitalRepository = hospitalRepository;
        this.vaccineRepository = vaccineRepository;
        this.patientRepository = patientRepository;
        this.nationalStockRepository = nationalStockRepository;
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model, Principal principal) {

        model.addAttribute("username", principal.getName());
        model.addAttribute("roleLabel", "Super Administrator");

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Global Dashboard");

        long activeHospitals = hospitalRepository.countByStatus(HospitalStatus.ACTIVE);
        model.addAttribute("activeHospitals", activeHospitals);
        model.addAttribute("recentHospitals",
                hospitalRepository.findTop5ByStatusOrderByHospitalIdDesc(HospitalStatus.ACTIVE));
        model.addAttribute("totalHospitals", hospitalRepository.count());

        model.addAttribute("activeVaccines", vaccineRepository.countByStatus(VaccineStatus.ACTIVE));
        model.addAttribute("deactivatedVaccines", vaccineRepository.countByStatus(VaccineStatus.INACTIVE));

        model.addAttribute("recentPatients", patientRepository.findTop5ByOrderByPatientIdDesc());
        model.addAttribute("totalPatients", patientRepository.count());

        model.addAttribute("totalStock", nationalStockRepository.sumAllQuantity());

        // TODO: wire once VaccineLog entity exists
        model.addAttribute("totalVaccinated", 0);
        model.addAttribute("totalFailed", 0);

        return "admin/dashboard";
    }
}
