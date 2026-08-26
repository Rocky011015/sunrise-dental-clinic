package com.sunrisedental.clinic.repository;

import com.sunrisedental.clinic.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientCodeIgnoreCase(String patientCode);

    boolean existsByPatientCodeIgnoreCase(String patientCode);

    boolean existsByPatientCodeIgnoreCaseAndIdNot(
            String patientCode,
            Long id
    );

    Page<Patient>
    findByPatientCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrContactNumberContaining(
            String patientCode,
            String fullName,
            String contactNumber,
            Pageable pageable
    );
}