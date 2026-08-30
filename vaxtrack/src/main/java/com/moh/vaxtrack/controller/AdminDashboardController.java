package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.HospitalStatus;
import com.moh.vaxtrack.entity.VaccineStatus;
import com.moh.vaxtrack.repository.HospitalRepository;
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

    public AdminDashboardController(HospitalRepository hospitalRepository,
                                     VaccineRepository vaccineRepository,
                                     PatientRepository patientRepository) {
        this.hospitalRepository = hospitalRepository;
        this.vaccineRepository = vaccineRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model, Principal principal) {

        // Logged-in user info
        model.addAttribute("username", principal.getName());
        model.addAttribute("roleLabel", "Super Administrator");

        // Layout targeting
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Global Dashboard");

        // Real hospital data
        long activeHospitals = hospitalRepository.countByStatus(HospitalStatus.ACTIVE);
        model.addAttribute("activeHospitals", activeHospitals);
        model.addAttribute("recentHospitals",
                hospitalRepository.findTop5ByStatusOrderByHospitalIdDesc(HospitalStatus.ACTIVE));
        model.addAttribute("totalHospitals", hospitalRepository.count());

        // Real vaccine data
        model.addAttribute("activeVaccines", vaccineRepository.countByStatus(VaccineStatus.ACTIVE));
        model.addAttribute("deactivatedVaccines", vaccineRepository.countByStatus(VaccineStatus.INACTIVE));

        // Real patient data
        model.addAttribute("recentPatients", patientRepository.findTop5ByOrderByPatientIdDesc());
        model.addAttribute("totalPatients", patientRepository.count());

        // Not built yet — clinical/stock modules
        // TODO: wire once VaccineLog and stock entities exist
        model.addAttribute("totalVaccinated", 0);
        model.addAttribute("totalFailed", 0);
        model.addAttribute("totalStock", 0);

        return "admin/dashboard";
    }
}
