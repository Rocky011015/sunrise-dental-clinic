package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.Patient;
import com.sunrisedental.clinic.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Patient createPatient(Patient patient) {
        String patientCode = normalisePatientCode(patient);

        if (patientRepository.existsByPatientCodeIgnoreCase(patientCode)) {
            throw new IllegalArgumentException(
                    "Patient code already exists: " + patientCode
            );
        }

        patient.setPatientCode(patientCode);
        return patientRepository.save(patient);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "No patient found with ID: " + id
                ));
    }

    public Page<Patient> searchPatients(
            String searchTerm,
            Pageable pageable
    ) {
        String query =
                searchTerm == null ? "" : searchTerm.trim();

        if (query.isBlank()) {
            return patientRepository.findAll(pageable);
        }

        return patientRepository
                .findByPatientCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrContactNumberContaining(
                        query,
                        query,
                        query,
                        pageable
                );
    }

    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "No patient found with ID: " + id
            );
        }

        patientRepository.deleteById(id);
    }

    private String normalisePatientCode(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient is required");
        }

        String patientCode = patient.getPatientCode();

        if (patientCode == null || patientCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Patient code is required"
            );
        }

        return patientCode.trim();
    }
    @Transactional
    public Patient updatePatient(Long id, Patient updatedPatient) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "No patient found with ID: " + id
                ));

        existingPatient.setFullName(updatedPatient.getFullName());
        existingPatient.setAddress(updatedPatient.getAddress());
        existingPatient.setContactNumber(updatedPatient.getContactNumber());

        return patientRepository.save(existingPatient);
    }
}