package com.moh.vaxtrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PatientLoginController {

    @GetMapping("/login/patient")
    public String showPatientLogin() {
        return "patient/login";
    }
}
