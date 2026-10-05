package com.moh.vaxtrack.controller;

import com.moh.vaxtrack.dto.VaccinationEventForm;
import com.moh.vaxtrack.entity.*;
import com.moh.vaxtrack.repository.EventNurseAssignmentRepository;
import com.moh.vaxtrack.repository.HospitalRepository;
import com.moh.vaxtrack.repository.UserRepository;
import com.moh.vaxtrack.repository.VaccinationEventRepository;
import com.moh.vaxtrack.repository.VaccineRepository;
import com.moh.vaxtrack.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/subadmin/events")
public class VaccinationEventController {

    private final VaccinationEventRepository eventRepository;
    private final HospitalRepository hospitalRepository;
    private final VaccineRepository vaccineRepository;
    private final UserRepository userRepository;
    private final EventNurseAssignmentRepository assignmentRepository;

    public VaccinationEventController(VaccinationEventRepository eventRepository,
                                       HospitalRepository hospitalRepository,
                                       VaccineRepository vaccineRepository,
                                       UserRepository userRepository,
                                       EventNurseAssignmentRepository assignmentRepository) {
        this.eventRepository = eventRepository;
        this.hospitalRepository = hospitalRepository;
        this.vaccineRepository = vaccineRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        String district = principal.getUser().getDistrict();

        List<VaccinationEvent> events = eventRepository.findByHospital_DistrictOrderByEventDateDesc(district);
        model.addAttribute("events", events);
        model.addAttribute("hospitals",
                hospitalRepository.findByDistrictAndStatusOrderByName(district, HospitalStatus.ACTIVE));
        model.addAttribute("vaccines", vaccineRepository.findByStatusOrderByBrandName(VaccineStatus.ACTIVE));
        model.addAttribute("newEvent", new VaccinationEventForm());
        model.addAttribute("today", LocalDate.now());

        List<User> districtNurses = userRepository.findByRoleAndHospital_DistrictOrderByUserIdDesc(
                Role.MEDICAL_STAFF, district);
        Map<Long, List<User>> nursesByHospital = districtNurses.stream()
                .collect(Collectors.groupingBy(n -> n.getHospital().getHospitalId()));
        model.addAttribute("nursesByHospital", nursesByHospital);

        Map<Long, List<Long>> assignedNurseIdsByEvent = new HashMap<>();
        for (VaccinationEvent event : events) {
            List<Long> nurseIds = assignmentRepository.findByEvent_EventId(event.getEventId()).stream()
                    .map(a -> a.getNurse().getUserId())
                    .collect(Collectors.toList());
            assignedNurseIdsByEvent.put(event.getEventId(), nurseIds);
        }
        model.addAttribute("assignedNurseIdsByEvent", assignedNurseIdsByEvent);

        model.addAttribute("activePage", "events");
        model.addAttribute("pageTitle", "Vaccination Event Management");
        return "subadmin/events";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal CustomUserDetails principal,
                       @Valid @ModelAttribute("newEvent") VaccinationEventForm form,
                       BindingResult result,
                       @RequestParam(value = "nurseIds", required = false) List<Long> nurseIds,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/subadmin/events";
        }

        String district = principal.getUser().getDistrict();

        Hospital hospital = hospitalRepository.findById(form.getHospitalId()).orElse(null);
        if (hospital == null || hospital.getStatus() != HospitalStatus.ACTIVE
                || !hospital.getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid hospital selection.");
            return "redirect:/subadmin/events";
        }

        Vaccine vaccine = vaccineRepository.findById(form.getVaccineId()).orElse(null);
        if (vaccine == null || vaccine.getStatus() != VaccineStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid vaccine selection.");
            return "redirect:/subadmin/events";
        }

        LocalDate eventDate;
        LocalTime startTime;
        LocalTime endTime;
        try {
            eventDate = LocalDate.parse(form.getEventDate());
            startTime = LocalTime.parse(form.getStartTime());
            endTime = LocalTime.parse(form.getEndTime());
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date or time format.");
            return "redirect:/subadmin/events";
        }

        if (eventDate.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Event date cannot be in the past.");
            return "redirect:/subadmin/events";
        }
        if (!startTime.isBefore(endTime)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Start time must be before end time.");
            return "redirect:/subadmin/events";
        }

        String timeSlot = form.getStartTime() + " - " + form.getEndTime();

        boolean duplicateExists = eventRepository.findByHospital_DistrictOrderByEventDateDesc(district).stream()
                .anyMatch(e -> e.getStatus() == VaccinationEventStatus.SCHEDULED
                        && e.getHospital().getHospitalId().equals(hospital.getHospitalId())
                        && e.getVaccine().getVaccineId().equals(vaccine.getVaccineId())
                        && e.getEventDate().equals(eventDate)
                        && e.getTimeSlot().equals(timeSlot));
        if (duplicateExists) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An identical event already exists — same hospital, vaccine, date, and time. "
                            + "Adjust the time or vaccine, or edit the existing event instead.");
            return "redirect:/subadmin/events";
        }

        int alreadyBooked = eventRepository.sumScheduledCapacity(hospital.getHospitalId(), eventDate, -1L);
        if (alreadyBooked + form.getCapacity() > hospital.getDailyCapacity()) {
            int remaining = hospital.getDailyCapacity() - alreadyBooked;
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This exceeds " + hospital.getName() + "'s daily capacity. Only " + remaining
                            + " slots remain on that date.");
            return "redirect:/subadmin/events";
        }

        VaccinationEvent event = new VaccinationEvent();
        event.setHospital(hospital);
        event.setVaccine(vaccine);
        event.setEventDate(eventDate);
        event.setTimeSlot(timeSlot);
        event.setCapacity(form.getCapacity());
        event.setCreatedBy(principal.getUser());
        event.setStatus(VaccinationEventStatus.SCHEDULED);
        eventRepository.save(event);

        saveNurseAssignments(event, hospital, nurseIds);

        redirectAttributes.addFlashAttribute("successMessage",
                "Vaccination event at " + hospital.getName() + " on " + eventDate + " was scheduled.");
        return "redirect:/subadmin/events";
    }

    @PostMapping("/{id}/edit")
    public String edit(@AuthenticationPrincipal CustomUserDetails principal,
                        @PathVariable Long id,
                        @Valid @ModelAttribute("editEvent") VaccinationEventForm form,
                        BindingResult result,
                        @RequestParam(value = "nurseIds", required = false) List<Long> nurseIds,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(result));
            return "redirect:/subadmin/events";
        }

        VaccinationEvent event = eventRepository.findById(id).orElse(null);
        String district = principal.getUser().getDistrict();

        if (event == null || !event.getHospital().getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "That event no longer exists.");
            return "redirect:/subadmin/events";
        }
        if (event.getStatus() != VaccinationEventStatus.SCHEDULED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cancelled events cannot be edited.");
            return "redirect:/subadmin/events";
        }
        if (event.getEventDate().isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Past events cannot be edited.");
            return "redirect:/subadmin/events";
        }

        Vaccine vaccine = vaccineRepository.findById(form.getVaccineId()).orElse(null);
        if (vaccine == null || vaccine.getStatus() != VaccineStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid vaccine selection.");
            return "redirect:/subadmin/events";
        }

        LocalDate eventDate;
        LocalTime startTime;
        LocalTime endTime;
        try {
            eventDate = LocalDate.parse(form.getEventDate());
            startTime = LocalTime.parse(form.getStartTime());
            endTime = LocalTime.parse(form.getEndTime());
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date or time format.");
            return "redirect:/subadmin/events";
        }

        if (eventDate.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Event date cannot be in the past.");
            return "redirect:/subadmin/events";
        }
        if (!startTime.isBefore(endTime)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Start time must be before end time.");
            return "redirect:/subadmin/events";
        }

        String timeSlot = form.getStartTime() + " - " + form.getEndTime();

        boolean duplicateExists = eventRepository.findByHospital_DistrictOrderByEventDateDesc(district).stream()
                .anyMatch(e -> !e.getEventId().equals(id)
                        && e.getStatus() == VaccinationEventStatus.SCHEDULED
                        && e.getHospital().getHospitalId().equals(event.getHospital().getHospitalId())
                        && e.getVaccine().getVaccineId().equals(vaccine.getVaccineId())
                        && e.getEventDate().equals(eventDate)
                        && e.getTimeSlot().equals(timeSlot));
        if (duplicateExists) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An identical event already exists — same hospital, vaccine, date, and time.");
            return "redirect:/subadmin/events";
        }

        int alreadyBooked = eventRepository.sumScheduledCapacity(
                event.getHospital().getHospitalId(), eventDate, event.getEventId());
        if (alreadyBooked + form.getCapacity() > event.getHospital().getDailyCapacity()) {
            int remaining = event.getHospital().getDailyCapacity() - alreadyBooked;
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This exceeds the hospital's daily capacity. Only " + remaining + " slots remain on that date.");
            return "redirect:/subadmin/events";
        }

        event.setVaccine(vaccine);
        event.setEventDate(eventDate);
        event.setTimeSlot(timeSlot);
        event.setCapacity(form.getCapacity());
        eventRepository.save(event);

        assignmentRepository.deleteByEventId(event.getEventId());
        saveNurseAssignments(event, event.getHospital(), nurseIds);

        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully.");
        return "redirect:/subadmin/events";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {

        VaccinationEvent event = eventRepository.findById(id).orElse(null);
        String district = principal.getUser().getDistrict();

        if (event == null || !event.getHospital().getDistrict().equals(district)) {
            redirectAttributes.addFlashAttribute("errorMessage", "That event no longer exists.");
            return "redirect:/subadmin/events";
        }
        if (event.getStatus() != VaccinationEventStatus.SCHEDULED) {
            redirectAttributes.addFlashAttribute("errorMessage", "This event is already cancelled.");
            return "redirect:/subadmin/events";
        }
        if (event.getEventDate().isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Past events cannot be cancelled.");
            return "redirect:/subadmin/events";
        }

        event.setStatus(VaccinationEventStatus.CANCELLED);
        eventRepository.save(event);

        redirectAttributes.addFlashAttribute("successMessage", "Event was cancelled.");
        return "redirect:/subadmin/events";
    }

    private void saveNurseAssignments(VaccinationEvent event, Hospital hospital, List<Long> nurseIds) {
        if (nurseIds == null || nurseIds.isEmpty()) {
            return;
        }
        for (Long nurseId : nurseIds) {
            User nurse = userRepository.findById(nurseId).orElse(null);
            if (nurse != null && nurse.getRole() == Role.MEDICAL_STAFF
                    && nurse.getHospital() != null
                    && nurse.getHospital().getHospitalId().equals(hospital.getHospitalId())) {
                assignmentRepository.save(new EventNurseAssignment(event, nurse));
            }
        }
    }

    private String firstErrorMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
