package com.sunrisedental.clinic.repository;

import com.sunrisedental.clinic.domain.Appointment;
import com.sunrisedental.clinic.domain.AppointmentStatus;
import com.sunrisedental.clinic.domain.Dentist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "patient",
            "dentist",
            "treatment",
            "createdBy"
    })
    Optional<Appointment> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {
            "patient",
            "dentist",
            "treatment",
            "createdBy"
    })
    Page<Appointment> findAll(Pageable pageable);

    Optional<Appointment> findByAppointmentNumberIgnoreCase(
            String appointmentNumber
    );

    boolean existsByAppointmentNumberIgnoreCase(
            String appointmentNumber
    );

    boolean existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusIn(
            Dentist dentist,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            Collection<AppointmentStatus> statuses
    );

    boolean existsByDentistAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(
            Dentist dentist,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            Collection<AppointmentStatus> statuses,
            Long id
    );

    @EntityGraph(attributePaths = {
            "patient",
            "dentist",
            "treatment",
            "createdBy"
    })
    Page<Appointment>
    findByAppointmentNumberContainingIgnoreCaseOrPatient_FullNameContainingIgnoreCaseOrDentist_FullNameContainingIgnoreCaseOrTreatment_TreatmentNameContainingIgnoreCase(
            String appointmentNumber,
            String patientName,
            String dentistName,
            String treatmentName,
            Pageable pageable
    );

}