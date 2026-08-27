package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.AppUser;
import com.sunrisedental.clinic.domain.Appointment;
import com.sunrisedental.clinic.domain.AppointmentStatus;
import com.sunrisedental.clinic.domain.Dentist;
import com.sunrisedental.clinic.domain.Patient;
import com.sunrisedental.clinic.domain.Treatment;
import com.sunrisedental.clinic.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final LocalDate FUTURE_DATE =
            LocalDate.of(2026, 8, 28);

    private static final LocalTime FUTURE_TIME =
            LocalTime.of(10, 30);

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private Appointment appointment;

    @Mock
    private Appointment existingAppointment;

    @Mock
    private Appointment updatedAppointment;

    @Mock
    private Patient patient;

    @Mock
    private Dentist dentist;

    @Mock
    private Treatment treatment;

    @Mock
    private AppUser createdBy;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-08-27T06:30:00Z"),
                ZoneId.of("Asia/Colombo")
        );

        appointmentService =
                new AppointmentService(
                        appointmentRepository,
                        fixedClock
                );
    }

    @Test
    void shouldCreateAppointmentWhenNumberAndSlotAreAvailable() {

        stubValidCreateAppointment(appointment);

        when(appointment.getAppointmentNumber())
                .thenReturn(" apt-1001 ");

        when(
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                "APT-1001"
                        )
        ).thenReturn(false);

        when(
                appointmentRepository
                        .existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusIn(
                                eq(dentist),
                                eq(FUTURE_DATE),
                                eq(FUTURE_TIME),
                                any()
                        )
        ).thenReturn(false);

        when(appointmentRepository.save(appointment))
                .thenReturn(appointment);

        Appointment result =
                appointmentService.createAppointment(
                        appointment
                );

        assertSame(appointment, result);

        verify(appointment)
                .setAppointmentNumber("APT-1001");

        verify(appointmentRepository)
                .save(appointment);
    }

    @Test
    void shouldGenerateAppointmentNumberWhenBlank() {

        stubValidCreateAppointment(appointment);

        when(appointment.getAppointmentNumber())
                .thenReturn(" ");

        when(
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                anyString()
                        )
        ).thenReturn(false);

        when(
                appointmentRepository
                        .existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusIn(
                                eq(dentist),
                                eq(FUTURE_DATE),
                                eq(FUTURE_TIME),
                                any()
                        )
        ).thenReturn(false);

        when(appointmentRepository.save(appointment))
                .thenReturn(appointment);

        appointmentService.createAppointment(
                appointment
        );

        verify(appointment)
                .setAppointmentNumber(
                        argThat(number ->
                                number.matches(
                                        "APT-20260827-[A-F0-9]{8}"
                                )
                        )
                );
    }

    @Test
    void shouldRejectDuplicateAppointmentNumber() {

        when(appointment.getAppointmentNumber())
                .thenReturn("APT-1001");

        when(
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                "APT-1001"
                        )
        ).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                appointmentService
                                        .createAppointment(
                                                appointment
                                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("APT-1001")
        );

        verify(
                appointmentRepository,
                never()
        ).save(any(Appointment.class));
    }

    @Test
    void shouldRejectInactiveDentist() {

        when(appointment.getAppointmentNumber())
                .thenReturn("APT-1001");

        when(
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                "APT-1001"
                        )
        ).thenReturn(false);

        when(appointment.getPatient())
                .thenReturn(patient);

        when(appointment.getDentist())
                .thenReturn(dentist);

        when(dentist.isActive())
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                appointmentService
                                        .createAppointment(
                                                appointment
                                        )
                );

        assertTrue(
                exception.getMessage()
                        .toLowerCase()
                        .contains("dentist")
        );

        verify(
                appointmentRepository,
                never()
        ).save(any(Appointment.class));
    }

    @Test
    void shouldRejectInactiveTreatment() {

        when(appointment.getAppointmentNumber())
                .thenReturn("APT-1001");

        when(
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                "APT-1001"
                        )
        ).thenReturn(false);

        when(appointment.getPatient())
                .thenReturn(patient);

        when(appointment.getDentist())
                .thenReturn(dentist);

        when(dentist.isActive())
                .thenReturn(true);

        when(appointment.getTreatment())
                .thenReturn(treatment);

        when(treatment.isActive())
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                appointmentService
                                        .createAppointment(
                                                appointment
                                        )
                );

        assertTrue(
                exception.getMessage()
                        .toLowerCase()
                        .contains("treatment")
        );

        verify(
                appointmentRepository,
                never()
        ).save(any(Appointment.class));
    }

    @Test
    void shouldRejectAppointmentDateAndTimeInPast() {

        when(appointment.getAppointmentNumber())
                .thenReturn("APT-1001");

        when(
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                "APT-1001"
                        )
        ).thenReturn(false);

        when(appointment.getPatient())
                .thenReturn(patient);

        when(appointment.getDentist())
                .thenReturn(dentist);

        when(dentist.isActive())
                .thenReturn(true);

        when(appointment.getTreatment())
                .thenReturn(treatment);

        when(treatment.isActive())
                .thenReturn(true);

        when(appointment.getAppointmentDate())
                .thenReturn(
                        LocalDate.of(2026, 8, 27)
                );

        when(appointment.getAppointmentTime())
                .thenReturn(
                        LocalTime.of(11, 30)
                );

        when(appointment.getStatus())
                .thenReturn(
                        AppointmentStatus.SCHEDULED
                );

        when(appointment.getCreatedBy())
                .thenReturn(createdBy);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                appointmentService
                                        .createAppointment(
                                                appointment
                                        )
                );

        assertTrue(
                exception.getMessage()
                        .toLowerCase()
                        .contains("past")
        );
    }

    @Test
    void shouldRejectDentistDoubleBooking() {

        stubValidCreateAppointment(appointment);

        when(appointment.getAppointmentNumber())
                .thenReturn("APT-1001");

        when(
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                "APT-1001"
                        )
        ).thenReturn(false);

        when(
                appointmentRepository
                        .existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusIn(
                                eq(dentist),
                                eq(FUTURE_DATE),
                                eq(FUTURE_TIME),
                                any()
                        )
        ).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                appointmentService
                                        .createAppointment(
                                                appointment
                                        )
                );

        assertTrue(
                exception.getMessage()
                        .toLowerCase()
                        .contains("book")
        );

        verify(
                appointmentRepository,
                never()
        ).save(any(Appointment.class));
    }

    @Test
    void shouldOnlyTreatScheduledAndConfirmedAsBlockingStatuses() {

        stubValidCreateAppointment(appointment);

        when(appointment.getAppointmentNumber())
                .thenReturn("APT-1001");

        when(
                appointmentRepository
                        .existsByAppointmentNumberIgnoreCase(
                                "APT-1001"
                        )
        ).thenReturn(false);

        when(
                appointmentRepository
                        .existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusIn(
                                eq(dentist),
                                eq(FUTURE_DATE),
                                eq(FUTURE_TIME),
                                any()
                        )
        ).thenReturn(false);

        when(appointmentRepository.save(appointment))
                .thenReturn(appointment);

        appointmentService.createAppointment(
                appointment
        );

        verify(appointmentRepository)
                .existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusIn(
                        eq(dentist),
                        eq(FUTURE_DATE),
                        eq(FUTURE_TIME),
                        argThat(statuses ->
                                statuses.size() == 2
                                        &&
                                        statuses.contains(
                                                AppointmentStatus.SCHEDULED
                                        )
                                        &&
                                        statuses.contains(
                                                AppointmentStatus.CONFIRMED
                                        )
                                        &&
                                        !statuses.contains(
                                                AppointmentStatus.CANCELLED
                                        )
                                        &&
                                        !statuses.contains(
                                                AppointmentStatus.COMPLETED
                                        )
                        )
                );
    }

    @Test
    void shouldFindAppointmentByNormalisedNumber() {

        when(
                appointmentRepository
                        .findByAppointmentNumberIgnoreCase(
                                "APT-1001"
                        )
        ).thenReturn(
                Optional.of(appointment)
        );

        Appointment result =
                appointmentService
                        .getAppointmentByNumber(
                                " apt-1001 "
                        );

        assertSame(appointment, result);
    }

    @Test
    void shouldRejectMissingAppointmentNumber() {

        when(
                appointmentRepository
                        .findByAppointmentNumberIgnoreCase(
                                "APT-9999"
                        )
        ).thenReturn(
                Optional.empty()
        );

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () ->
                                appointmentService
                                        .getAppointmentByNumber(
                                                "APT-9999"
                                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("APT-9999")
        );
    }

    @Test
    void shouldUpdateAppointmentWithoutConflictingWithItself() {

        when(
                appointmentRepository.findById(7L)
        ).thenReturn(
                Optional.of(existingAppointment)
        );

        when(updatedAppointment.getPatient())
                .thenReturn(patient);

        when(updatedAppointment.getDentist())
                .thenReturn(dentist);

        when(dentist.isActive())
                .thenReturn(true);

        when(updatedAppointment.getTreatment())
                .thenReturn(treatment);

        when(treatment.isActive())
                .thenReturn(true);

        when(updatedAppointment.getAppointmentDate())
                .thenReturn(FUTURE_DATE);

        when(updatedAppointment.getAppointmentTime())
                .thenReturn(FUTURE_TIME);

        when(updatedAppointment.getStatus())
                .thenReturn(
                        AppointmentStatus.CONFIRMED
                );

        when(updatedAppointment.getNotes())
                .thenReturn(
                        "Patient confirmed appointment."
                );

        when(
                appointmentRepository
                        .existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(
                                eq(dentist),
                                eq(FUTURE_DATE),
                                eq(FUTURE_TIME),
                                any(),
                                eq(7L)
                        )
        ).thenReturn(false);

        when(
                appointmentRepository
                        .save(existingAppointment)
        ).thenReturn(existingAppointment);

        Appointment result =
                appointmentService.updateAppointment(
                        7L,
                        updatedAppointment
                );

        assertSame(existingAppointment, result);

        verify(existingAppointment)
                .setPatient(patient);

        verify(existingAppointment)
                .setDentist(dentist);

        verify(existingAppointment)
                .setTreatment(treatment);

        verify(existingAppointment)
                .setAppointmentDate(FUTURE_DATE);

        verify(existingAppointment)
                .setAppointmentTime(FUTURE_TIME);

        verify(existingAppointment)
                .setStatus(
                        AppointmentStatus.CONFIRMED
                );

        verify(existingAppointment)
                .setNotes(
                        "Patient confirmed appointment."
                );

        verify(existingAppointment, never())
                .setAppointmentNumber(anyString());

        verify(existingAppointment, never())
                .setCreatedBy(any(AppUser.class));

        verify(appointmentRepository)
                .save(existingAppointment);
    }

    @Test
    void shouldCancelExistingAppointment() {

        when(
                appointmentRepository.findById(7L)
        ).thenReturn(
                Optional.of(existingAppointment)
        );

        when(
                appointmentRepository
                        .save(existingAppointment)
        ).thenReturn(existingAppointment);

        Appointment result =
                appointmentService
                        .cancelAppointment(7L);

        assertSame(existingAppointment, result);

        verify(existingAppointment)
                .setStatus(
                        AppointmentStatus.CANCELLED
                );

        verify(appointmentRepository)
                .save(existingAppointment);
    }

    private void stubValidCreateAppointment(
            Appointment target
    ) {

        when(target.getPatient())
                .thenReturn(patient);

        when(target.getDentist())
                .thenReturn(dentist);

        when(target.getTreatment())
                .thenReturn(treatment);

        when(target.getCreatedBy())
                .thenReturn(createdBy);

        when(target.getAppointmentDate())
                .thenReturn(FUTURE_DATE);

        when(target.getAppointmentTime())
                .thenReturn(FUTURE_TIME);

        when(target.getStatus())
                .thenReturn(
                        AppointmentStatus.SCHEDULED
                );

        when(dentist.isActive())
                .thenReturn(true);

        when(treatment.isActive())
                .thenReturn(true);
    }
}