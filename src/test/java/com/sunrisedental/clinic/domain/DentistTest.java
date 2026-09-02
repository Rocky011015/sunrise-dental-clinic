package com.sunrisedental.clinic.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DentistTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidDentist() {
        Dentist dentist = validDentist();

        Set<ConstraintViolation<Dentist>> violations =
                validator.validate(dentist);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldBeActiveByDefault() {
        Dentist dentist = validDentist();

        assertTrue(dentist.isActive());
    }

    @Test
    void shouldRejectBlankDentistCode() {
        Dentist dentist = validDentist();
        dentist.setDentistCode(" ");

        assertTrue(hasViolation(dentist, "dentistCode"));
    }

    @Test
    void shouldRejectBlankFullName() {
        Dentist dentist = validDentist();
        dentist.setFullName(" ");

        assertTrue(hasViolation(dentist, "fullName"));
    }

    @Test
    void shouldRejectBlankSpecialization() {
        Dentist dentist = validDentist();
        dentist.setSpecialization(" ");

        assertTrue(hasViolation(dentist, "specialization"));
    }

    @Test
    void shouldRejectNegativeConsultationFee() {
        Dentist dentist = validDentist();
        dentist.setConsultationFee(new BigDecimal("-1.00"));

        assertTrue(hasViolation(dentist, "consultationFee"));
    }

    private static Dentist validDentist() {
        return new Dentist(
                "DEN-001",
                "Dr. Nimal Perera",
                "General Dentistry",
                new BigDecimal("2500.00")
        );
    }

    private static boolean hasViolation(
            Dentist dentist,
            String propertyName
    ) {
        Set<ConstraintViolation<Dentist>> violations =
                validator.validate(dentist);

        assertFalse(violations.isEmpty());

        return violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath()
                                .toString()
                                .equals(propertyName)
                );
    }
}