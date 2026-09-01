package com.sunrisedental.clinic.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentTest {

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
    void shouldAcceptValidAppointment() {
        Appointment appointment = validAppointment();

        Set<ConstraintViolation<Appointment>> violations =
                validator.validate(appointment);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldBeScheduledByDefault() {
        Appointment appointment = validAppointment();

        assertEquals(
                AppointmentStatus.SCHEDULED,
                appointment.getStatus()
        );
    }

    @Test
    void shouldRejectBlankAppointmentNumber() {
        Appointment appointment = validAppointment();
        appointment.setAppointmentNumber(" ");

        assertTrue(
                hasViolation(
                        appointment,
                        "appointmentNumber"
                )
        );
    }

    @Test
    void shouldRejectMissingPatient() {
        Appointment appointment = validAppointment();
        appointment.setPatient(null);

        assertTrue(hasViolation(appointment, "patient"));
    }

    @Test
    void shouldRejectMissingDentist() {
        Appointment appointment = validAppointment();
        appointment.setDentist(null);

        assertTrue(hasViolation(appointment, "dentist"));
    }

    @Test
    void shouldRejectMissingTreatment() {
        Appointment appointment = validAppointment();
        appointment.setTreatment(null);

        assertTrue(hasViolation(appointment, "treatment"));
    }

    @Test
    void shouldRejectPastAppointmentDate() {
        Appointment appointment = validAppointment();

        appointment.setAppointmentDate(
                LocalDate.now().minusDays(1)
        );

        assertTrue(
                hasViolation(
                        appointment,
                        "appointmentDate"
                )
        );
    }

    @Test
    void shouldRejectMissingAppointmentTime() {
        Appointment appointment = validAppointment();
        appointment.setAppointmentTime(null);

        assertTrue(
                hasViolation(
                        appointment,
                        "appointmentTime"
                )
        );
    }

    @Test
    void shouldRejectMissingStatus() {
        Appointment appointment = validAppointment();
        appointment.setStatus(null);

        assertTrue(hasViolation(appointment, "status"));
    }

    @Test
    void shouldRejectMissingCreatedByUser() {
        Appointment appointment = validAppointment();
        appointment.setCreatedBy(null);

        assertTrue(hasViolation(appointment, "createdBy"));
    }

    @Test
    void shouldRejectNotesOverFiveHundredCharacters() {
        Appointment appointment = validAppointment();
        appointment.setNotes("A".repeat(501));

        assertTrue(hasViolation(appointment, "notes"));
    }

    private static Appointment validAppointment() {

        Patient patient = new Patient(
                "PAT-0001",
                "Nimal Perera",
                "12 Galle Road, Colombo",
                "0771234567",
                "nimal@example.com",
                LocalDate.of(1990, 5, 10)
        );

        Dentist dentist = new Dentist(
                "DEN-001",
                "Dr. Kamal Silva",
                "General Dentistry",
                new BigDecimal("2500.00")
        );

        Treatment treatment = new Treatment(
                "TRT-001",
                "Dental Cleaning",
                "Professional dental cleaning and polishing.",
                new BigDecimal("5000.00"),
                45
        );

        AppUser administrator = new AppUser(
                "admin",
                "encoded-password",
                "Clinic Administrator",
                UserRole.ADMIN,
                true
        );

        return new Appointment(
                "APT-000001",
                patient,
                dentist,
                treatment,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 30),
                administrator
        );
    }

    private static boolean hasViolation(
            Appointment appointment,
            String propertyName
    ) {
        Set<ConstraintViolation<Appointment>> violations =
                validator.validate(appointment);

        assertFalse(violations.isEmpty());

        return violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath()
                                .toString()
                                .equals(propertyName)
                );
    }
}