package com.sunrisedental.clinic.repository;

import com.sunrisedental.clinic.domain.Treatment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreatmentRepository
        extends JpaRepository<Treatment, Long> {

    Optional<Treatment> findByTreatmentCodeIgnoreCase(
            String treatmentCode
    );

    boolean existsByTreatmentCodeIgnoreCase(
            String treatmentCode
    );

    boolean existsByTreatmentCodeIgnoreCaseAndIdNot(
            String treatmentCode,
            Long id
    );

    List<Treatment> findByActiveTrueOrderByTreatmentNameAsc();

    Page<Treatment>
    findByTreatmentCodeContainingIgnoreCaseOrTreatmentNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String treatmentCode,
            String treatmentName,
            String description,
            Pageable pageable
    );
}