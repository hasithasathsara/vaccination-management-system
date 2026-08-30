package com.moh.vaxtrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Add Inventory Manager form fields
public class InventoryManagerForm {

    // Required username
    @NotBlank(message = "Username is required")
    private String username;

    // Optional email
    @Email(message = "Enter a valid email address")
    private String email;

    // Optional phone
    private String phoneNumber;

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
}
