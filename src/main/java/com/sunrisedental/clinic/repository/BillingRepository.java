package com.sunrisedental.clinic.repository;

import com.sunrisedental.clinic.domain.Billing;
import com.sunrisedental.clinic.domain.PaymentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface BillingRepository
        extends JpaRepository<Billing, Long> {


    /*
     * =========================================================
     * FIND ALL
     * =========================================================
     */

    @Override
    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.patient",
            "appointment.dentist",
            "appointment.treatment",
            "createdBy"
    })
    Page<Billing> findAll(Pageable pageable);


    /*
     * =========================================================
     * FIND BY ID
     * =========================================================
     */

    @Override
    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.patient",
            "appointment.dentist",
            "appointment.treatment",
            "createdBy"
    })
    Optional<Billing> findById(Long id);


    /*
     * =========================================================
     * FIND BY BILLING NUMBER
     * =========================================================
     */

    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.patient",
            "appointment.dentist",
            "appointment.treatment",
            "createdBy"
    })
    Optional<Billing> findByBillingNumberIgnoreCase(
            String billingNumber
    );


    /*
     * =========================================================
     * FIND BY APPOINTMENT
     * =========================================================
     */

    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.patient",
            "appointment.dentist",
            "appointment.treatment",
            "createdBy"
    })
    Optional<Billing> findByAppointment_Id(
            Long appointmentId
    );


    /*
     * =========================================================
     * DUPLICATE CHECKS
     * =========================================================
     */

    boolean existsByBillingNumberIgnoreCase(
            String billingNumber
    );


    boolean existsByAppointment_Id(
            Long appointmentId
    );


    /*
     * =========================================================
     * SEARCH
     * =========================================================
     */

    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.patient",
            "appointment.dentist",
            "appointment.treatment",
            "createdBy"
    })
    @Query("""
            SELECT b
            FROM Billing b
            JOIN b.appointment a
            JOIN a.patient p
            JOIN a.treatment t
            WHERE LOWER(b.billingNumber)
                    LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(a.appointmentNumber)
                    LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.fullName)
                    LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(t.treatmentName)
                    LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Billing> search(
            @Param("search") String search,
            Pageable pageable
    );


    /*
     * =========================================================
     * REPORTS - PAYMENT STATUS COUNTS
     * =========================================================
     */

    long countByPaymentStatus(
            PaymentStatus paymentStatus
    );


    /*
     * =========================================================
     * REPORTS - TOTAL BILLED
     * =========================================================
     */

    @Query("""
            SELECT COALESCE(
                SUM(b.totalAmount),
                0
            )
            FROM Billing b
            """)
    BigDecimal getTotalBilledAmount();


    /*
     * =========================================================
     * REPORTS - TOTAL COLLECTED
     * =========================================================
     */

    @Query("""
            SELECT COALESCE(
                SUM(b.amountPaid),
                0
            )
            FROM Billing b
            """)
    BigDecimal getTotalCollectedAmount();
}