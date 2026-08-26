package com.sunrisedental.clinic.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientTest {

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
    void shouldAcceptValidPatient() {
        Patient patient = validPatient();

        Set<ConstraintViolation<Patient>> violations =
                validator.validate(patient);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectBlankPatientCode() {
        Patient patient = validPatient();
        patient.setPatientCode(" ");

        assertTrue(hasViolation(patient, "patientCode"));
    }

    @Test
    void shouldRejectBlankFullName() {
        Patient patient = validPatient();
        patient.setFullName(" ");

        assertTrue(hasViolation(patient, "fullName"));
    }

    @Test
    void shouldRejectBlankAddress() {
        Patient patient = validPatient();
        patient.setAddress(" ");

        assertTrue(hasViolation(patient, "address"));
    }

    @Test
    void shouldRejectInvalidContactNumber() {
        Patient patient = validPatient();
        patient.setContactNumber("123");

        assertTrue(hasViolation(patient, "contactNumber"));
    }

    @Test
    void shouldRejectInvalidEmail() {
        Patient patient = validPatient();
        patient.setEmail("not-an-email");

        assertTrue(hasViolation(patient, "email"));
    }

    @Test
    void shouldRejectFutureDateOfBirth() {
        Patient patient = validPatient();
        patient.setDateOfBirth(LocalDate.now().plusDays(1));

        assertTrue(hasViolation(patient, "dateOfBirth"));
    }

    private static Patient validPatient() {
        return new Patient(
                "PAT-0001",
                "Nimal Perera",
                "12 Galle Road, Colombo",
                "0771234567",
                "nimal@example.com",
                LocalDate.of(1990, 5, 10)
        );
    }

    private static boolean hasViolation(
            Patient patient,
            String propertyName
    ) {
        Set<ConstraintViolation<Patient>> violations =
                validator.validate(patient);

        assertFalse(violations.isEmpty());

        return violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath()
                                .toString()
                                .equals(propertyName)
                );
    }
}