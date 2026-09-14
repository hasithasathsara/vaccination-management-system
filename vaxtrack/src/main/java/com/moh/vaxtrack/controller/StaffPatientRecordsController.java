package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.*;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.repository.HospitalStockRepository;
import com.moh.vaxtrack.repository.VaccineLogRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/staff/patient-records")
public class StaffPatientRecordsController {

    private final VaccineLogRepository vaccineLogRepository;
    private final AppointmentRepository appointmentRepository;
    private final HospitalStockRepository hospitalStockRepository;

    public StaffPatientRecordsController(VaccineLogRepository vaccineLogRepository,
                                          AppointmentRepository appointmentRepository,
                                          HospitalStockRepository hospitalStockRepository) {
        this.vaccineLogRepository = vaccineLogRepository;
        this.appointmentRepository = appointmentRepository;
        this.hospitalStockRepository = hospitalStockRepository;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        Long hospitalId = principal.getUser().getHospital().getHospitalId();
        model.addAttribute("logs",
                vaccineLogRepository.findByAppointment_Event_Hospital_HospitalIdAndIsDeletedFalseOrderByLoggedAtDesc(hospitalId));

        model.addAttribute("activePage", "patientRecords");
        model.addAttribute("pageTitle", "Patients Records Management");
        return "staff/patient-records";
    }


    @PostMapping("/{logId}/undo")
    public String undo(@AuthenticationPrincipal CustomUserDetails principal,
                        @PathVariable Long logId, RedirectAttributes redirectAttributes) {

        VaccineLog log = vaccineLogRepository.findById(logId).orElse(null);
        if (log == null || log.getIsDeleted()
                || !log.getAppointment().getEvent().getHospital().getHospitalId()
                .equals(principal.getUser().getHospital().getHospitalId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "That record no longer exists.");
            return "redirect:/staff/patient-records";
        }
        if (log.getStatus() != VaccineLogStatus.VACCINATED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only a Vaccinated entry can be undone.");
            return "redirect:/staff/patient-records";
        }

        HospitalStock stock = hospitalStockRepository
                .findByHospitalAndVaccine(log.getAppointment().getEvent().getHospital(), log.getVaccine())
                .orElse(null);
        if (stock != null) {
            stock.setQuantity(stock.getQuantity() + 1);
            hospitalStockRepository.save(stock);
        }

        log.setStatus(VaccineLogStatus.PENDING);
        vaccineLogRepository.save(log);

        Appointment appointment = log.getAppointment();
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute("successMessage", "Entry undone — dose returned to stock.");
        return "redirect:/staff/patient-records";
    }


    @PostMapping("/{logId}/void")
    public String voidEntry(@AuthenticationPrincipal CustomUserDetails principal,
                             @PathVariable Long logId, RedirectAttributes redirectAttributes) {

        VaccineLog log = vaccineLogRepository.findById(logId).orElse(null);
        if (log == null || log.getIsDeleted()
                || !log.getAppointment().getEvent().getHospital().getHospitalId()
                .equals(principal.getUser().getHospital().getHospitalId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "That record no longer exists.");
            return "redirect:/staff/patient-records";
        }

        if (log.getStatus() == VaccineLogStatus.VACCINATED) {
            HospitalStock stock = hospitalStockRepository
                    .findByHospitalAndVaccine(log.getAppointment().getEvent().getHospital(), log.getVaccine())
                    .orElse(null);
            if (stock != null) {
                stock.setQuantity(stock.getQuantity() + 1);
                hospitalStockRepository.save(stock);
            }
        }

        log.setIsDeleted(true);
        vaccineLogRepository.save(log);

        Appointment appointment = log.getAppointment();
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute("successMessage", "Entry voided.");
        return "redirect:/staff/patient-records";
    }
}
