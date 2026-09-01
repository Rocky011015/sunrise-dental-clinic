package com.sunrisedental.clinic.repository;

import com.sunrisedental.clinic.domain.Dentist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DentistRepository extends JpaRepository<Dentist, Long> {

    Optional<Dentist> findByDentistCodeIgnoreCase(String dentistCode);

    boolean existsByDentistCodeIgnoreCase(String dentistCode);

    boolean existsByDentistCodeIgnoreCaseAndIdNot(
            String dentistCode,
            Long id
    );

    Page<Dentist>
    findByDentistCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrSpecializationContainingIgnoreCase(
            String dentistCode,
            String fullName,
            String specialization,
            Pageable pageable
    );

    List<Dentist> findByActiveTrueOrderByFullNameAsc();
}