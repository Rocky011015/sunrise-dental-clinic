package com.sunrisedental.clinic.security;

import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {

    private static final int MINIMUM_LENGTH = 8;

    public boolean isValid(String password) {
        if (password == null
                || password.isBlank()
                || password.length() < MINIMUM_LENGTH) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumber = false;
        boolean hasSpecialCharacter = false;

        for (char character : password.toCharArray()) {
            if (Character.isWhitespace(character)) {
                return false;
            }

            if (Character.isUpperCase(character)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(character)) {
                hasLowercase = true;
            } else if (Character.isDigit(character)) {
                hasNumber = true;
            } else {
                hasSpecialCharacter = true;
            }
        }

        return hasUppercase
                && hasLowercase
                && hasNumber
                && hasSpecialCharacter;
    }
}