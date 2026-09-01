package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.Dentist;
import com.sunrisedental.clinic.repository.DentistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class DentistService {

    private final DentistRepository dentistRepository;

    public DentistService(DentistRepository dentistRepository) {
        this.dentistRepository = dentistRepository;
    }

    @Transactional
    public Dentist createDentist(Dentist dentist) {

        String dentistCode = normaliseDentistCode(dentist);

        if (dentistRepository.existsByDentistCodeIgnoreCase(dentistCode)) {
            throw new IllegalArgumentException(
                    "Dentist code already exists: " + dentistCode
            );
        }

        dentist.setDentistCode(dentistCode);

        return dentistRepository.save(dentist);
    }

    public Dentist getDentistById(Long id) {
        return dentistRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No dentist found with ID: " + id
                        )
                );
    }

    public Page<Dentist> searchDentists(
            String searchTerm,
            Pageable pageable
    ) {

        String query =
                searchTerm == null
                        ? ""
                        : searchTerm.trim();

        if (query.isBlank()) {
            return dentistRepository.findAll(pageable);
        }

        return dentistRepository
                .findByDentistCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrSpecializationContainingIgnoreCase(
                        query,
                        query,
                        query,
                        pageable
                );
    }

    @Transactional
    public Dentist updateDentist(
            Long id,
            Dentist updatedDentist
    ) {

        Dentist existingDentist = getDentistById(id);

        String dentistCode =
                normaliseDentistCode(updatedDentist);

        if (dentistRepository
                .existsByDentistCodeIgnoreCaseAndIdNot(
                        dentistCode,
                        id
                )) {

            throw new IllegalArgumentException(
                    "Dentist code already exists: " +
                            dentistCode
            );
        }

        existingDentist.setDentistCode(dentistCode);
        existingDentist.setFullName(
                updatedDentist.getFullName()
        );
        existingDentist.setSpecialization(
                updatedDentist.getSpecialization()
        );
        existingDentist.setConsultationFee(
                updatedDentist.getConsultationFee()
        );
        existingDentist.setActive(
                updatedDentist.isActive()
        );

        return dentistRepository.save(existingDentist);
    }

    @Transactional
    public Dentist setDentistActive(
            Long id,
            boolean active
    ) {

        Dentist dentist = getDentistById(id);

        dentist.setActive(active);

        return dentistRepository.save(dentist);
    }

    private String normaliseDentistCode(
            Dentist dentist
    ) {

        if (dentist == null) {
            throw new IllegalArgumentException(
                    "Dentist is required"
            );
        }

        String dentistCode =
                dentist.getDentistCode();

        if (dentistCode == null ||
                dentistCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Dentist code is required"
            );
        }

        return dentistCode.trim().toUpperCase();
    }
}