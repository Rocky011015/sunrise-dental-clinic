package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.Patient;
import com.sunrisedental.clinic.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private Patient patient;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository);
    }

    @Test
    void shouldCreatePatientWhenCodeIsUnique() {
        when(patient.getPatientCode()).thenReturn("PAT-1001");
        when(patientRepository.existsByPatientCodeIgnoreCase("PAT-1001"))
                .thenReturn(false);
        when(patientRepository.save(patient)).thenReturn(patient);

        Patient savedPatient = patientService.createPatient(patient);

        assertSame(patient, savedPatient);
        verify(patientRepository).save(patient);
    }

    @Test
    void shouldRejectDuplicatePatientCode() {
        when(patient.getPatientCode()).thenReturn("PAT-1001");
        when(patientRepository.existsByPatientCodeIgnoreCase("PAT-1001"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(patient)
        );

        assertTrue(exception.getMessage().contains("PAT-1001"));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void shouldReturnPatientWhenIdExists() {
        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientById(1L);

        assertSame(patient, result);
    }

    @Test
    void shouldRejectMissingPatientId() {
        when(patientRepository.findById(99L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> patientService.getPatientById(99L)
        );

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void shouldSearchPatientCodeNameAndContactNumber() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> expectedPage =
                new PageImpl<>(List.of(patient));

        when(patientRepository
                .findByPatientCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrContactNumberContaining(
                        "ann",
                        "ann",
                        "ann",
                        pageable
                ))
                .thenReturn(expectedPage);

        Page<Patient> result =
                patientService.searchPatients(" ann ", pageable);

        assertSame(expectedPage, result);
    }

    @Test
    void shouldReturnAllPatientsWhenSearchIsBlank() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> expectedPage =
                new PageImpl<>(List.of(patient));

        when(patientRepository.findAll(pageable))
                .thenReturn(expectedPage);

        Page<Patient> result =
                patientService.searchPatients("   ", pageable);

        assertSame(expectedPage, result);
        verify(patientRepository).findAll(pageable);
    }

    @Test
    void shouldDeleteExistingPatient() {
        when(patientRepository.existsById(1L)).thenReturn(true);

        patientService.deletePatient(1L);

        verify(patientRepository).deleteById(1L);
    }

    @Test
    void shouldRejectDeletingMissingPatient() {
        when(patientRepository.existsById(99L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> patientService.deletePatient(99L)
        );

        assertTrue(exception.getMessage().contains("99"));
        verify(patientRepository, never()).deleteById(anyLong());
    }
}