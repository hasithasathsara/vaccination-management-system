package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.entity.Appointment;
import com.moh.vaxtrack.entity.AppointmentStatus;
import com.moh.vaxtrack.repository.AppointmentRepository;
import com.moh.vaxtrack.security.PatientPrincipal;
import com.moh.vaxtrack.util.PdfGenerator;
import com.lowagie.text.DocumentException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/patient/my-records")
public class PatientRecordsController {

    private final AppointmentRepository appointmentRepository;
    private final PdfGenerator pdfGenerator;

    public PatientRecordsController(AppointmentRepository appointmentRepository, PdfGenerator pdfGenerator) {
        this.appointmentRepository = appointmentRepository;
        this.pdfGenerator = pdfGenerator;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal PatientPrincipal principal, Model model) {

        List<Appointment> vaccinations = appointmentRepository.findByPatient_PatientIdAndStatusOrderByBookedAtDesc(
                principal.getPatient().getPatientId(), AppointmentStatus.VACCINATED);

        model.addAttribute("vaccinations", vaccinations);
        model.addAttribute("activePage", "records");
        model.addAttribute("pageTitle", "My Records");
        return "patient/my-records";
    }

    @GetMapping("/certificate-pdf")
    @ResponseBody
    public ResponseEntity<byte[]> certificatePdf(@AuthenticationPrincipal PatientPrincipal principal) throws DocumentException {

        List<Appointment> vaccinations = appointmentRepository.findByPatient_PatientIdAndStatusOrderByBookedAtDesc(
                principal.getPatient().getPatientId(), AppointmentStatus.VACCINATED);

        List<String[]> rows = new ArrayList<>();
        for (Appointment a : vaccinations) {
            rows.add(new String[]{
                    a.getEvent().getVaccine().getBrandName(),
                    "Dose " + a.getDoseNumber(),
                    a.getEvent().getHospital().getName(),
                    a.getEvent().getEventDate().toString()
            });
        }

        byte[] pdf = pdfGenerator.generateCertificatePdf(
                principal.getPatient().getFullName(),
                principal.getPatient().getIdType().toString(),
                principal.getPatient().getIdNumber(),
                rows);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("vaxtrack-certificate.pdf").build().toString())
                .body(pdf);
    }
}
