package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.*;
import com.moh.vaxtrack.repository.*;
import com.moh.vaxtrack.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/staff")
public class ClinicalController {

    private final AppointmentRepository appointmentRepository;
    private final VaccineLogRepository vaccineLogRepository;
    private final HospitalStockRepository hospitalStockRepository;

    public ClinicalController(AppointmentRepository appointmentRepository,
                               VaccineLogRepository vaccineLogRepository,
                               HospitalStockRepository hospitalStockRepository) {
        this.appointmentRepository = appointmentRepository;
        this.vaccineLogRepository = vaccineLogRepository;
        this.hospitalStockRepository = hospitalStockRepository;
    }

    @GetMapping("/scan-qr")
    public String showScanPage(Model model) {
        model.addAttribute("activePage", "scanQr");
        model.addAttribute("pageTitle", "Scan QR");
        return "staff/scan-qr";
    }

    @GetMapping("/scan-qr/lookup-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> lookupAjax(@AuthenticationPrincipal CustomUserDetails principal,
                                                            @RequestParam String code) {

        Map<String, Object> response = new LinkedHashMap<>();
        Appointment appointment = appointmentRepository.findByQrCode(code.trim()).orElse(null);
        Hospital myHospital = principal.getUser().getHospital();

        if (appointment == null) {
            response.put("error", "No booking found for that code.");
            return ResponseEntity.ok(response);
        }
        if (!appointment.getEvent().getHospital().getHospitalId().equals(myHospital.getHospitalId())) {
            response.put("error", "This booking belongs to a different hospital.");
            return ResponseEntity.ok(response);
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            response.put("error", "This booking is no longer active (status: " + appointment.getStatus() + ").");
            return ResponseEntity.ok(response);
        }

        Patient patient = appointment.getPatient();
        Vaccine vaccine = appointment.getEvent().getVaccine();

        long completedDoses = vaccineLogRepository
                .countByAppointment_Patient_PatientIdAndVaccine_VaccineIdAndStatusAndIsDeletedFalse(
                        patient.getPatientId(), vaccine.getVaccineId(), VaccineLogStatus.VACCINATED);

        List<VaccineLog> history = vaccineLogRepository
                .findByAppointment_Patient_PatientIdAndIsDeletedFalseOrderByLoggedAtDesc(patient.getPatientId());

        List<Map<String, Object>> historyJson = new ArrayList<>();
        for (VaccineLog log : history) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("vaccine", log.getVaccine().getBrandName());
            row.put("dose", log.getDoseNumber());
            row.put("status", log.getStatus().toString());
            row.put("date", log.getLoggedAt().toLocalDate().toString());
            historyJson.add(row);
        }

        response.put("appointmentId", appointment.getAppointmentId());
        response.put("patientName", patient.getFullName());
        response.put("idType", patient.getIdType().toString());
        response.put("idNumber", patient.getIdNumber());
        response.put("age", patient.getAge());
        response.put("phone", patient.getPhoneNumber());
        response.put("disabilities", patient.getDisabilities() == null || patient.getDisabilities().isBlank()
                ? "None reported" : patient.getDisabilities());
        response.put("vaccineName", vaccine.getBrandName());
        response.put("doseNumber", completedDoses + 1);
        response.put("dosesRequired", vaccine.getDosesRequired());
        response.put("hospitalName", appointment.getEvent().getHospital().getName());
        response.put("eventDate", appointment.getEvent().getEventDate().toString());
        response.put("timeSlot", appointment.getEvent().getTimeSlot());
        response.put("history", historyJson);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/verify/{appointmentId}")
    public String verify(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable Long appointmentId, Model model,
                          RedirectAttributes redirectAttributes) {

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        Hospital myHospital = principal.getUser().getHospital();

        if (appointment == null || !appointment.getEvent().getHospital().getHospitalId().equals(myHospital.getHospitalId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "That booking no longer exists.");
            return "redirect:/staff/daily-queue";
        }

        Patient patient = appointment.getPatient();
        Vaccine vaccine = appointment.getEvent().getVaccine();

        long completedDoses = vaccineLogRepository
                .countByAppointment_Patient_PatientIdAndVaccine_VaccineIdAndStatusAndIsDeletedFalse(
                        patient.getPatientId(), vaccine.getVaccineId(), VaccineLogStatus.VACCINATED);

        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", patient);
        model.addAttribute("completedDoses", completedDoses);
        model.addAttribute("history",
                vaccineLogRepository.findByAppointment_Patient_PatientIdAndIsDeletedFalseOrderByLoggedAtDesc(patient.getPatientId()));
        model.addAttribute("activePage", "scanQr");
        model.addAttribute("pageTitle", "Verify Patient");
        return "staff/verify";
    }

    @PostMapping("/verify/{appointmentId}/vaccinate")
    public String vaccinate(@AuthenticationPrincipal CustomUserDetails principal,
                             @PathVariable Long appointmentId,
                             RedirectAttributes redirectAttributes) {

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null || appointment.getStatus() != AppointmentStatus.BOOKED) {
            redirectAttributes.addFlashAttribute("errorMessage", "This booking can no longer be processed.");
            return "redirect:/staff/daily-queue";
        }

        Vaccine vaccine = appointment.getEvent().getVaccine();
        Hospital hospital = appointment.getEvent().getHospital();
        Patient patient = appointment.getPatient();

        long completedDoses = vaccineLogRepository
                .countByAppointment_Patient_PatientIdAndVaccine_VaccineIdAndStatusAndIsDeletedFalse(
                        patient.getPatientId(), vaccine.getVaccineId(), VaccineLogStatus.VACCINATED);

        if (completedDoses >= vaccine.getDosesRequired()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This patient has already completed all required doses of " + vaccine.getBrandName() + ".");
            return "redirect:/staff/daily-queue";
        }

        HospitalStock stock = hospitalStockRepository.findByHospitalAndVaccine(hospital, vaccine).orElse(null);
        if (stock == null || stock.getQuantity() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No " + vaccine.getBrandName() + " stock available at this hospital.");
            return "redirect:/staff/daily-queue";
        }

        stock.setQuantity(stock.getQuantity() - 1);
        hospitalStockRepository.save(stock);

        VaccineLog log = new VaccineLog();
        log.setAppointment(appointment);
        log.setVaccine(vaccine);
        log.setMedicalStaff(principal.getUser());
        log.setDoseNumber((int) completedDoses + 1);
        log.setStatus(VaccineLogStatus.VACCINATED);
        log.setIsDeleted(false);
        log.setLoggedAt(LocalDateTime.now());
        vaccineLogRepository.save(log);

        appointment.setStatus(AppointmentStatus.VACCINATED);
        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute("successMessage",
                patient.getFullName() + " marked as vaccinated.");
        return "redirect:/staff/daily-queue";
    }

    @PostMapping("/verify/{appointmentId}/fail")
    public String fail(@AuthenticationPrincipal CustomUserDetails principal,
                        @PathVariable Long appointmentId,
                        @RequestParam String reason,
                        RedirectAttributes redirectAttributes) {

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null || appointment.getStatus() != AppointmentStatus.BOOKED) {
            redirectAttributes.addFlashAttribute("errorMessage", "This booking can no longer be processed.");
            return "redirect:/staff/daily-queue";
        }

        Vaccine vaccine = appointment.getEvent().getVaccine();
        Patient patient = appointment.getPatient();

        long completedDoses = vaccineLogRepository
                .countByAppointment_Patient_PatientIdAndVaccine_VaccineIdAndStatusAndIsDeletedFalse(
                        patient.getPatientId(), vaccine.getVaccineId(), VaccineLogStatus.VACCINATED);

        VaccineLog log = new VaccineLog();
        log.setAppointment(appointment);
        log.setVaccine(vaccine);
        log.setMedicalStaff(principal.getUser());
        log.setDoseNumber((int) completedDoses + 1);
        log.setStatus(VaccineLogStatus.FAILED);
        log.setIsDeleted(false);
        log.setLoggedAt(LocalDateTime.now());
        vaccineLogRepository.save(log);

        appointment.setStatus(AppointmentStatus.FAILED);
        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute("successMessage",
                patient.getFullName() + " marked as Failed/Issue: " + reason);
        return "redirect:/staff/daily-queue";
    }
}
