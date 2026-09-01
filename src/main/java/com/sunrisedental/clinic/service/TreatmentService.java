package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.Treatment;
import com.sunrisedental.clinic.repository.TreatmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;

    public TreatmentService(
            TreatmentRepository treatmentRepository
    ) {
        this.treatmentRepository = treatmentRepository;
    }


    @Transactional
    public Treatment createTreatment(Treatment treatment) {

        String treatmentCode =
                normaliseTreatmentCode(treatment);

        if (treatmentRepository
                .existsByTreatmentCodeIgnoreCase(treatmentCode)) {

            throw new IllegalArgumentException(
                    "Treatment code already exists: "
                            + treatmentCode
            );
        }

        treatment.setTreatmentCode(treatmentCode);

        return treatmentRepository.save(treatment);
    }


    public Treatment getTreatmentById(Long id) {

        return treatmentRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No treatment found with ID: " + id
                        )
                );
    }


    public Treatment getTreatmentByCode(
            String treatmentCode
    ) {

        if (treatmentCode == null ||
                treatmentCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Treatment code is required"
            );
        }

        return treatmentRepository
                .findByTreatmentCodeIgnoreCase(
                        treatmentCode.trim()
                )
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No treatment found with code: "
                                        + treatmentCode
                        )
                );
    }


    public Page<Treatment> searchTreatments(
            String searchTerm,
            Pageable pageable
    ) {

        String query =
                searchTerm == null
                        ? ""
                        : searchTerm.trim();

        if (query.isBlank()) {
            return treatmentRepository.findAll(pageable);
        }

        return treatmentRepository
                .findByTreatmentCodeContainingIgnoreCaseOrTreatmentNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        query,
                        query,
                        query,
                        pageable
                );
    }


    public List<Treatment> getActiveTreatments() {

        return treatmentRepository
                .findByActiveTrueOrderByTreatmentNameAsc();
    }


    @Transactional
    public Treatment updateTreatment(
            Long id,
            Treatment updatedTreatment
    ) {

        Treatment existingTreatment =
                treatmentRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "No treatment found with ID: "
                                                + id
                                )
                        );

        String treatmentCode =
                normaliseTreatmentCode(updatedTreatment);

        if (treatmentRepository
                .existsByTreatmentCodeIgnoreCaseAndIdNot(
                        treatmentCode,
                        id
                )) {

            throw new IllegalArgumentException(
                    "Treatment code already exists: "
                            + treatmentCode
            );
        }

        existingTreatment.setTreatmentCode(
                treatmentCode
        );

        existingTreatment.setTreatmentName(
                updatedTreatment.getTreatmentName()
        );

        existingTreatment.setDescription(
                updatedTreatment.getDescription()
        );

        existingTreatment.setBasePrice(
                updatedTreatment.getBasePrice()
        );

        existingTreatment.setEstimatedDurationMinutes(
                updatedTreatment
                        .getEstimatedDurationMinutes()
        );

        existingTreatment.setActive(
                updatedTreatment.isActive()
        );

        return treatmentRepository.save(
                existingTreatment
        );
    }


    @Transactional
    public Treatment setTreatmentActive(
            Long id,
            boolean active
    ) {

        Treatment treatment =
                treatmentRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "No treatment found with ID: "
                                                + id
                                )
                        );

        treatment.setActive(active);

        return treatmentRepository.save(treatment);
    }


    private String normaliseTreatmentCode(
            Treatment treatment
    ) {

        if (treatment == null) {
            throw new IllegalArgumentException(
                    "Treatment is required"
            );
        }

        String treatmentCode =
                treatment.getTreatmentCode();

        if (treatmentCode == null ||
                treatmentCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Treatment code is required"
            );
        }

        return treatmentCode
                .trim()
                .toUpperCase();
    }
}