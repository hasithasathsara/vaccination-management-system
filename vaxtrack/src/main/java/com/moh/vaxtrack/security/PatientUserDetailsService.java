package com.moh.vaxtrack.security;

import com.moh.vaxtrack.entity.Patient;
import com.moh.vaxtrack.repository.PatientRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Loads a Patient during login, by ID number OR phone number
@Service
public class PatientUserDetailsService implements UserDetailsService {

    private final PatientRepository patientRepository;

    public PatientUserDetailsService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {

        // Try ID number first, then phone number
        Patient patient = patientRepository.findByIdNumber(loginInput)
                .or(() -> patientRepository.findByPhoneNumber(loginInput))
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found: " + loginInput));

        return new PatientPrincipal(patient);
    }
}
