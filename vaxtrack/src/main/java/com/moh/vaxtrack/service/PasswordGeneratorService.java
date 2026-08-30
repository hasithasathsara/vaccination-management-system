package com.moh.vaxtrack.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

// Generates a random, temporary password for any account the SYSTEM creates
// on someone's behalf (Sub-Admin, Inventory Manager, Nurse — never Patients,
// who set their own password at registration).
//
// This is deliberately its own small class instead of being copy-pasted into
// every controller that creates a staff account, because we need the exact
// same logic in at least three places: Super Admin creating Sub-Admins,
// Super Admin creating Inventory Managers, and Sub-Admin creating Nurses.
@Service
public class PasswordGeneratorService {

    // Characters allowed in a generated password. Deliberately excludes visually
    // confusing characters like 0/O and l/1/I, since a human has to type this
    // password back in during their first login.
    private static final String CHARACTERS =
            "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";

    private static final int PASSWORD_LENGTH = 10;

    // SecureRandom (not the regular Random class) because this is used for something
    // security-sensitive — a temporary login password.
    private final SecureRandom random = new SecureRandom();

    // Builds and returns a brand-new random password, e.g. "aB7!kP2mQz".
    public String generate() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }
}
