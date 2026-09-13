package com.moh.vaxtrack.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class VaccinationEventForm {

    @NotNull(message = "Please select a hospital")
    private Long hospitalId;

    @NotNull(message = "Please select a vaccine")
    private Long vaccineId;

    @NotNull(message = "Event date is required")
    private String eventDate; // "yyyy-MM-dd" from the date input

    @NotNull(message = "Start time is required")
    private String startTime; // "HH:mm" from the time input

    @NotNull(message = "End time is required")
    private String endTime; // "HH:mm" from the time input

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public Long getVaccineId() { return vaccineId; }
    public void setVaccineId(Long vaccineId) { this.vaccineId = vaccineId; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
