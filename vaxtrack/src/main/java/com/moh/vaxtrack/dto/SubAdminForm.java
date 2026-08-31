package com.moh.vaxtrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// This class exists ONLY to receive what the "Add Sub-Admin" form submits.
//
// Why not bind straight to the User entity? Because User also has fields like
// role, status, password, and mustResetPassword — fields we NEVER want a submitted
// form to control directly (the system decides those, not whatever was typed into
// the browser). Using a small, separate "form object" like this one means the
// controller stays in charge of anything security-sensitive.
public class SubAdminForm {

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Enter a valid email address")
    private String email;

    private String phoneNumber;

    // Required — every Sub-Admin must be scoped to exactly one district
    @NotBlank(message = "Please select an assigned district")
    private String district;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
