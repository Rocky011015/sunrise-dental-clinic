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

class TreatmentTest {

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
    void shouldAcceptValidTreatment() {
        Treatment treatment = validTreatment();

        Set<ConstraintViolation<Treatment>> violations =
                validator.validate(treatment);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldBeActiveByDefault() {
        Treatment treatment = validTreatment();

        assertTrue(treatment.isActive());
    }

    @Test
    void shouldRejectBlankTreatmentCode() {
        Treatment treatment = validTreatment();
        treatment.setTreatmentCode(" ");

        assertTrue(hasViolation(treatment, "treatmentCode"));
    }

    @Test
    void shouldRejectBlankTreatmentName() {
        Treatment treatment = validTreatment();
        treatment.setTreatmentName(" ");

        assertTrue(hasViolation(treatment, "treatmentName"));
    }

    @Test
    void shouldRejectNegativeBasePrice() {
        Treatment treatment = validTreatment();
        treatment.setBasePrice(new BigDecimal("-1.00"));

        assertTrue(hasViolation(treatment, "basePrice"));
    }

    @Test
    void shouldRejectZeroDuration() {
        Treatment treatment = validTreatment();
        treatment.setEstimatedDurationMinutes(0);

        assertTrue(
                hasViolation(
                        treatment,
                        "estimatedDurationMinutes"
                )
        );
    }

    @Test
    void shouldRejectDescriptionOverFiveHundredCharacters() {
        Treatment treatment = validTreatment();
        treatment.setDescription("A".repeat(501));

        assertTrue(hasViolation(treatment, "description"));
    }

    private static Treatment validTreatment() {
        return new Treatment(
                "TRT-001",
                "Dental Cleaning",
                "Professional dental cleaning and polishing.",
                new BigDecimal("5000.00"),
                45
        );
    }

    private static boolean hasViolation(
            Treatment treatment,
            String propertyName
    ) {
        Set<ConstraintViolation<Treatment>> violations =
                validator.validate(treatment);

        assertFalse(violations.isEmpty());

        return violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath()
                                .toString()
                                .equals(propertyName)
                );
    }
}