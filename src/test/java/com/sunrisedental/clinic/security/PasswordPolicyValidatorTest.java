package com.sunrisedental.clinic.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator validator =
            new PasswordPolicyValidator();

    @Test
    void shouldAcceptStrongPassword() {
        assertTrue(validator.isValid("Clinic@2026"));
    }

    @Test
    void shouldRejectShortPassword() {
        assertFalse(validator.isValid("Cli@1"));
    }

    @Test
    void shouldRejectPasswordWithoutUppercaseLetter() {
        assertFalse(validator.isValid("clinic@2026"));
    }

    @Test
    void shouldRejectPasswordWithoutLowercaseLetter() {
        assertFalse(validator.isValid("CLINIC@2026"));
    }

    @Test
    void shouldRejectPasswordWithoutNumber() {
        assertFalse(validator.isValid("Clinic@Safe"));
    }

    @Test
    void shouldRejectPasswordWithoutSpecialCharacter() {
        assertFalse(validator.isValid("Clinic2026"));
    }

    @Test
    void shouldRejectNullOrBlankPassword() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("        "));
    }
}