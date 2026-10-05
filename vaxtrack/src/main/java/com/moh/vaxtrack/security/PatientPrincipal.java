package com.moh.vaxtrack.security;

import com.moh.vaxtrack.entity.Patient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Wraps a Patient for Spring Security
public class PatientPrincipal implements UserDetails {

    private final Patient patient;

    public PatientPrincipal(Patient patient) {
        this.patient = patient;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PATIENT"));
    }

    @Override
    public String getPassword() {
        return patient.getPassword();
    }

    // Identifies the logged-in patient by their ID number
    @Override
    public String getUsername() {
        return patient.getIdNumber();
    }

    @Override
    public boolean isEnabled() {
        // No suspend concept for patients — only the hard-delete exception exists
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Access to the full patient record from controllers
    public Patient getPatient() {
        return patient;
    }
}
