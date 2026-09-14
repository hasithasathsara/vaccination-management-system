package com.moh.vaxtrack.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;


@Service
public class PasswordGeneratorService {

    private static final String CHARACTERS =
            "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";

    private static final int PASSWORD_LENGTH = 10;


    private final SecureRandom random = new SecureRandom();


    public String generate() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }
}
