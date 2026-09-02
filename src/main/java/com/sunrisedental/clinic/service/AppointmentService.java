package com.sunrisedental.clinic.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.sunrisedental.clinic.domain.Appointment;
import com.sunrisedental.clinic.domain.AppointmentStatus;
import com.sunrisedental.clinic.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private static final List<AppointmentStatus> BLOCKING_STATUSES =
            List.of(
                    AppointmentStatus.SCHEDULED,
                    AppointmentStatus.CONFIRMED
            );

    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    @Autowired
    public AppointmentService(
            AppointmentRepository appointmentRepository
    ) {
        this(
                appointmentRepository,
                Clock.systemDefaultZone()
        );
    }

    AppointmentService(
            AppointmentRepository appointmentRepository,
            Clock clock
    ) {
        this.appointmentRepository =
                appointmentRepository;

        this.clock = clock;
    }

    @Transactional
    public Appointment createAppointment(
            Appointment appointment
    ) {

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment is required"
            );
        }

        String appointmentNumber =
                prepareAppointmentNumber(
                        appointment.getAppointmentNumber()
                );

        validateAppointmentDetails(
                appointment,
                true
        );

        ensureDentistSlotAvailable(
                appointment,
                null
        );

        appointment.setAppointmentNumber(
                appointmentNumber
        );

        return appointmentRepository.save(
                appointment
        );
    }

    public Appointment getAppointmentByNumber(
            String appointmentNumber
    ) {

        String normalisedNumber =
                normaliseAppointmentNumber(
                        appointmentNumber
                );

        return appointmentRepository
                .findByAppointmentNumberIgnoreCase(
                        normalisedNumber
                )
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "No appointment found with number: "
                                                + normalisedNumber
                                )
                );
    }

    public Appointment getAppointmentById(
            Long id
    ) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Appointment ID is required"
            );
        }

        return appointmentRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "No appointment found with ID: "
                                                + id
                                )
                );
    }

    @Transactional
    public Appointment updateAppointment(
            Long id,
            Appointment updatedAppointment
    ) {

        if (updatedAppointment == null) {
            throw new IllegalArgumentException(
                    "Updated appointment is required"
            );
        }

        Appointment existingAppointment =
                appointmentRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "No appointment found with ID: "
                                                        + id
                                        )
                        );

        validateAppointmentDetails(
                updatedAppointment,
                false
        );

        ensureDentistSlotAvailable(
                updatedAppointment,
                id
        );

        existingAppointment.setPatient(
                updatedAppointment.getPatient()
        );

        existingAppointment.setDentist(
                updatedAppointment.getDentist()
        );

        existingAppointment.setTreatment(
                updatedAppointment.getTreatment()
        );

        existingAppointment.setAppointmentDate(
                updatedAppointment.getAppointmentDate()
        );

        existingAppointment.setAppointmentTime(
                updatedAppointment.getAppointmentTime()
        );

        existingAppointment.setStatus(
                updatedAppointment.getStatus()
        );

        existingAppointment.setNotes(
                updatedAppointment.getNotes()
        );

        /*
         * Appointment number and created-by user are intentionally
         * preserved during an update.
         */
        return appointmentRepository.save(
                existingAppointment
        );
    }

    public Page<Appointment> searchAppointments(
            String searchTerm,
            Pageable pageable
    ) {

        String query =
                searchTerm == null
                        ? ""
                        : searchTerm.trim();

        if (query.isBlank()) {
            return appointmentRepository.findAll(pageable);
        }

        return appointmentRepository
                .findByAppointmentNumberContainingIgnoreCaseOrPatient_FullNameContainingIgnoreCaseOrDentist_FullNameContainingIgnoreCaseOrTreatment_TreatmentNameContainingIgnoreCase(
                        query,
                        query,
                        query,
                        query,
                        pageable
                );
    }

    @Transactional
    public Appointment cancelAppointment(
            Long id
    ) {

        Appointment appointment =
                appointmentRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "No appointment found with ID: "
                                                        + id
                                        )
                        );

        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        return appointmentRepository.save(
                appointment
        );
    }

    private String prepareAppointmentNumber(
            String appointmentNumber
    ) {

        if (
                appointmentNumber == null
                        ||
                        appointmentNumber.isBlank()
        ) {
            return generateUniqueAppointmentNumber();
        }

        String normalisedNumber =
                appointmentNumber
                        .trim()
                        .toUpperCase();

        if (
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                normalisedNumber
                        )
        ) {
            throw new IllegalArgumentException(
                    "Appointment number already exists: "
                            + normalisedNumber
            );
        }

        return normalisedNumber;
    }

    private String generateUniqueAppointmentNumber() {

        String datePart =
                LocalDate.now(clock)
                        .format(
                                DateTimeFormatter.BASIC_ISO_DATE
                        );

        String appointmentNumber;

        do {
            String randomPart =
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

            appointmentNumber =
                    "APT-"
                            + datePart
                            + "-"
                            + randomPart;

        } while (
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                appointmentNumber
                        )
        );

        return appointmentNumber;
    }

    private String normaliseAppointmentNumber(
            String appointmentNumber
    ) {

        if (
                appointmentNumber == null
                        ||
                        appointmentNumber.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Appointment number is required"
            );
        }

        return appointmentNumber
                .trim()
                .toUpperCase();
    }

    private void validateAppointmentDetails(
            Appointment appointment,
            boolean requireCreatedBy
    ) {

        if (appointment.getPatient() == null) {
            throw new IllegalArgumentException(
                    "Patient is required"
            );
        }

        if (appointment.getDentist() == null) {
            throw new IllegalArgumentException(
                    "Dentist is required"
            );
        }

        if (!appointment.getDentist().isActive()) {
            throw new IllegalArgumentException(
                    "Selected dentist is not active"
            );
        }

        if (appointment.getTreatment() == null) {
            throw new IllegalArgumentException(
                    "Treatment is required"
            );
        }

        if (!appointment.getTreatment().isActive()) {
            throw new IllegalArgumentException(
                    "Selected treatment is not active"
            );
        }

        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException(
                    "Appointment date is required"
            );
        }

        if (appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException(
                    "Appointment time is required"
            );
        }

        if (appointment.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Appointment status is required"
            );
        }

        if (
                requireCreatedBy
                        &&
                        appointment.getCreatedBy() == null
        ) {
            throw new IllegalArgumentException(
                    "Created by user is required"
            );
        }

        LocalDateTime appointmentDateTime =
                LocalDateTime.of(
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime()
                );

        LocalDateTime currentDateTime =
                LocalDateTime.now(clock);

        if (
                appointmentDateTime
                        .isBefore(currentDateTime)
        ) {
            throw new IllegalArgumentException(
                    "Appointment date and time cannot be in the past"
            );
        }
    }

    private void ensureDentistSlotAvailable(
            Appointment appointment,
            Long appointmentId
    ) {

        /*
         * Completed and cancelled appointments do not reserve a
         * future dentist time slot.
         */
        if (
                !BLOCKING_STATUSES.contains(
                        appointment.getStatus()
                )
        ) {
            return;
        }

        boolean slotAlreadyBooked;

        if (appointmentId == null) {

            slotAlreadyBooked =
                    appointmentRepository
                            .existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusIn(
                                    appointment.getDentist(),
                                    appointment.getAppointmentDate(),
                                    appointment.getAppointmentTime(),
                                    BLOCKING_STATUSES
                            );

        } else {

            slotAlreadyBooked =
                    appointmentRepository
                            .existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(
                                    appointment.getDentist(),
                                    appointment.getAppointmentDate(),
                                    appointment.getAppointmentTime(),
                                    BLOCKING_STATUSES,
                                    appointmentId
                            );
        }

        if (slotAlreadyBooked) {
            throw new IllegalArgumentException(
                    "Dentist is already booked for the selected date and time"
            );
        }
    }
}