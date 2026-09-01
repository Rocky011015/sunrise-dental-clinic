package com.sunrisedental.clinic.repository;

import com.sunrisedental.clinic.domain.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DentistRepository
        extends JpaRepository<Dentist, Long> {

    Optional<Dentist> findByDentistCodeIgnoreCase(
            String dentistCode
    );

    boolean existsByDentistCodeIgnoreCase(
            String dentistCode
    );

    List<Dentist> findByActiveTrueOrderByFullNameAsc();
}