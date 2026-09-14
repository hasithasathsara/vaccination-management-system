package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.*;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.repository.HospitalRepository;
import com.moh.vaxtrack.repository.VaccinationEventRepository;
import com.moh.vaxtrack.security.PatientPrincipal;
import com.moh.vaxtrack.util.QrCodeGenerator;
import com.google.zxing.WriterException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// Patient-facing appointment booking — district -> hospital -> event cascade
@Controller
@RequestMapping("/patient/book-appointment")
public class PatientAppointmentController {

    private final HospitalRepository hospitalRepository;
    private final VaccinationEventRepository eventRepository;
    private final AppointmentRepository appointmentRepository;
    private final QrCodeGenerator qrCodeGenerator;

    public PatientAppointmentController(HospitalRepository hospitalRepository,
                                         VaccinationEventRepository eventRepository,
                                         AppointmentRepository appointmentRepository,
                                         QrCodeGenerator qrCodeGenerator) {
        this.hospitalRepository = hospitalRepository;
        this.eventRepository = eventRepository;
        this.appointmentRepository = appointmentRepository;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    // Shows the booking form with all districts/hospitals/events preloaded for the cascade
    @GetMapping
    public String showForm(@AuthenticationPrincipal PatientPrincipal principal, Model model) {

        List<Hospital> activeHospitals = hospitalRepository.findByStatusOrderByDistrictAscNameAsc(HospitalStatus.ACTIVE);
        List<VaccinationEvent> upcomingEvents = eventRepository
                .findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(VaccinationEventStatus.SCHEDULED, LocalDate.now());

        // Distinct districts that actually have an active hospital, in a stable order
        List<String> districts = activeHospitals.stream()
                .map(Hospital::getDistrict)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("districts", districts);
        model.addAttribute("hospitals", activeHospitals);
        model.addAttribute("events", upcomingEvents);

        // Remaining capacity per event, computed fresh each time — never stored, always live
        Map<Long, Long> remainingByEvent = new HashMap<>();
        for (VaccinationEvent event : upcomingEvents) {
            long booked = appointmentRepository.countByEvent_EventIdAndStatus(event.getEventId(), AppointmentStatus.BOOKED);
            remainingByEvent.put(event.getEventId(), Math.max(0, event.getCapacity() - booked));
        }
        model.addAttribute("remainingByEvent", remainingByEvent);

        model.addAttribute("activePage", "book");
        model.addAttribute("pageTitle", "Book Appointment");
        return "patient/book-appointment";
    }

    // Confirms a booking
    @PostMapping
    public String bookAppointment(@AuthenticationPrincipal PatientPrincipal principal,
                                   @RequestParam Long hospitalId,
                                   @RequestParam Long eventId,
                                   RedirectAttributes redirectAttributes) {

        Patient patient = principal.getPatient();

        VaccinationEvent event = eventRepository.findById(eventId).orElse(null);
        if (event == null || event.getStatus() != VaccinationEventStatus.SCHEDULED
                || !event.getHospital().getHospitalId().equals(hospitalId)
                || event.getEventDate().isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "This event is no longer available.");
            return "redirect:/patient/book-appointment";
        }

        long booked = appointmentRepository.countByEvent_EventIdAndStatus(eventId, AppointmentStatus.BOOKED);
        if (booked >= event.getCapacity()) {
            redirectAttributes.addFlashAttribute("errorMessage", "This event is fully booked.");
            return "redirect:/patient/book-appointment";
        }

        // Block a second active booking for the same vaccine
        boolean alreadyBooked = appointmentRepository.findByPatient_PatientIdAndEvent_Vaccine_VaccineIdAndStatus(
                patient.getPatientId(), event.getVaccine().getVaccineId(), AppointmentStatus.BOOKED).isPresent();
        if (alreadyBooked) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You already have an active booking for " + event.getVaccine().getBrandName() + ".");
            return "redirect:/patient/book-appointment";
        }

        // Dose number: always 1 for now — there's no vaccine_logs history yet to check
        // completed doses against. TODO: once Clinical Operations (VaccineLog) exists,
        // look up the patient's completed doses for this vaccine and book the next one.
        int doseNumber = 1;

        // Generate a unique booking code, retrying on the rare chance of a collision
        String qrCode = qrCodeGenerator.generateBookingCode();
        while (appointmentRepository.findByQrCode(qrCode).isPresent()) {
            qrCode = qrCodeGenerator.generateBookingCode();
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setEvent(event);
        appointment.setDoseNumber(doseNumber);
        appointment.setQrCode(qrCode);
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setBookedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute("bookingConfirmed", appointment);
        return "redirect:/patient/book-appointment";
    }

    // Serves the QR code as a PNG image, rebuilt fresh from the stored text every time
    @GetMapping("/{id}/qr-image")
    @ResponseBody
    public ResponseEntity<byte[]> qrImage(@AuthenticationPrincipal PatientPrincipal principal,
                                           @PathVariable Long id) throws WriterException, IOException {

        Appointment appointment = appointmentRepository.findById(id).orElse(null);

        // Only the owning patient can ever see their own QR image
        if (appointment == null || !appointment.getPatient().getPatientId().equals(principal.getPatient().getPatientId())) {
            return ResponseEntity.notFound().build();
        }

        byte[] png = qrCodeGenerator.generateQrPng(appointment.getQrCode(), 260);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(png);
    }
}
