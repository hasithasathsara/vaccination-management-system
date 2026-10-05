package com.moh.vaxtrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffLoginController {


    @GetMapping("/login/staff")
    public String showStaffLogin() {
        return "auth/staff-login";
    }
}