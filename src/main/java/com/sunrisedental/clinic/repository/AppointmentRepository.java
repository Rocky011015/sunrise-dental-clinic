package com.sunrisedental.clinic.repository;

import com.sunrisedental.clinic.domain.Appointment;
import com.sunrisedental.clinic.domain.AppointmentStatus;
import com.sunrisedental.clinic.domain.Dentist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {


    /*
     * =========================================================
     * BASIC RETRIEVAL
     * =========================================================
     */

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


    /*
     * =========================================================
     * APPOINTMENT CONFLICT CHECKS
     * =========================================================
     */

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


    /*
     * =========================================================
     * SEARCH
     * =========================================================
     */

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


    /*
     * =========================================================
     * REPORTS - STATUS COUNTS
     * =========================================================
     */

    long countByStatus(
            AppointmentStatus status
    );


    /*
     * =========================================================
     * REPORTS - DENTIST WORKLOAD
     * =========================================================
     */

    @Query("""
            SELECT
                d.id AS dentistId,
                d.fullName AS dentistName,
                COUNT(a) AS appointmentCount
            FROM Appointment a
            JOIN a.dentist d
            GROUP BY
                d.id,
                d.fullName
            ORDER BY
                COUNT(a) DESC,
                d.fullName ASC
            """)
    List<DentistWorkloadReport>
    getDentistWorkloadReport();


    /*
     * =========================================================
     * REPORTS - TREATMENT USAGE
     * =========================================================
     */

    @Query("""
            SELECT
                t.id AS treatmentId,
                t.treatmentName AS treatmentName,
                COUNT(a) AS appointmentCount
            FROM Appointment a
            JOIN a.treatment t
            GROUP BY
                t.id,
                t.treatmentName
            ORDER BY
                COUNT(a) DESC,
                t.treatmentName ASC
            """)
    List<TreatmentUsageReport>
    getTreatmentUsageReport();
}