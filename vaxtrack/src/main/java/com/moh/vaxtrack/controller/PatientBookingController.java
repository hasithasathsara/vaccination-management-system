package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.Appointment;
import com.moh.vaxtrack.entity.AppointmentStatus;
import com.moh.vaxtrack.entity.Notification;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.repository.NotificationRepository;
import com.moh.vaxtrack.security.PatientPrincipal;
import com.moh.vaxtrack.util.PdfGenerator;
import com.moh.vaxtrack.util.QrCodeGenerator;
import com.lowagie.text.DocumentException;
import com.google.zxing.WriterException;
import org.springframework.http.ContentDisposition;
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

@Controller
@RequestMapping("/patient/my-bookings")
public class PatientBookingController {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final PdfGenerator pdfGenerator;

    public PatientBookingController(AppointmentRepository appointmentRepository,
                                     NotificationRepository notificationRepository,
                                     QrCodeGenerator qrCodeGenerator,
                                     PdfGenerator pdfGenerator) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.qrCodeGenerator = qrCodeGenerator;
        this.pdfGenerator = pdfGenerator;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal PatientPrincipal principal, Model model) {
        Long patientId = principal.getPatient().getPatientId();

        model.addAttribute("activeBooking",
                appointmentRepository.findByPatient_PatientIdAndStatus(patientId, AppointmentStatus.BOOKED).orElse(null));
        model.addAttribute("history",
                appointmentRepository.findByPatient_PatientIdAndStatusNotOrderByBookedAtDesc(patientId, AppointmentStatus.BOOKED));

        model.addAttribute("activePage", "bookings");
        model.addAttribute("pageTitle", "My Bookings");
        return "patient/my-bookings";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@AuthenticationPrincipal PatientPrincipal principal,
                          @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {

        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        Long patientId = principal.getPatient().getPatientId();

        if (appointment == null || !appointment.getPatient().getPatientId().equals(patientId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "That booking no longer exists.");
            return "redirect:/patient/my-bookings";
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            redirectAttributes.addFlashAttribute("errorMessage", "This booking is no longer active.");
            return "redirect:/patient/my-bookings";
        }
        if (appointment.getEvent().getEventDate().isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "This appointment date has already passed.");
            return "redirect:/patient/my-bookings";
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        String message = "Your booking for " + appointment.getEvent().getVaccine().getBrandName()
                + " at " + appointment.getEvent().getHospital().getName()
                + " on " + appointment.getEvent().getEventDate() + " was cancelled.";
        notificationRepository.save(new Notification(appointment.getPatient(), message));

        redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled.");
        return "redirect:/patient/my-bookings";
    }

    @GetMapping("/{id}/qr-download")
    @ResponseBody
    public ResponseEntity<byte[]> qrDownload(@AuthenticationPrincipal PatientPrincipal principal,
                                              @PathVariable Long id) throws WriterException, IOException {

        Appointment appointment = validateOwnAppointment(principal, id);
        if (appointment == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] png = qrCodeGenerator.generateQrPng(appointment.getQrCode(), 500);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("vaxtrack-qr-" + id + ".png").build().toString())
                .body(png);
    }

    @GetMapping("/{id}/qr-pdf")
    @ResponseBody
    public ResponseEntity<byte[]> qrPdf(@AuthenticationPrincipal PatientPrincipal principal,
                                         @PathVariable Long id) throws WriterException, IOException, DocumentException {

        Appointment appointment = validateOwnAppointment(principal, id);
        if (appointment == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] qrPng = qrCodeGenerator.generateQrPng(appointment.getQrCode(), 260);
        byte[] pdf = pdfGenerator.generateQrPdf(
                qrPng,
                appointment.getPatient().getFullName(),
                appointment.getEvent().getHospital().getName(),
                appointment.getEvent().getVaccine().getBrandName(),
                appointment.getDoseNumber(),
                appointment.getEvent().getEventDate().toString(),
                appointment.getEvent().getTimeSlot(),
                appointment.getQrCode());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("vaxtrack-booking-" + id + ".pdf").build().toString())
                .body(pdf);
    }

    private Appointment validateOwnAppointment(PatientPrincipal principal, Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment == null || !appointment.getPatient().getPatientId().equals(principal.getPatient().getPatientId())) {
            return null;
        }
        return appointment;
    }
}
