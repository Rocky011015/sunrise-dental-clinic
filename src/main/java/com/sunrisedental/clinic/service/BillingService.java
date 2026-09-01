package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.AppUser;
import com.sunrisedental.clinic.domain.Appointment;
import com.sunrisedental.clinic.domain.Billing;
import com.sunrisedental.clinic.domain.PaymentMethod;
import com.sunrisedental.clinic.domain.PaymentStatus;
import com.sunrisedental.clinic.domain.Treatment;
import com.sunrisedental.clinic.repository.AppointmentRepository;
import com.sunrisedental.clinic.repository.BillingRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BillingService {

    private final BillingRepository billingRepository;
    private final AppointmentRepository appointmentRepository;

    public BillingService(
            BillingRepository billingRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.billingRepository = billingRepository;
        this.appointmentRepository = appointmentRepository;
    }


    /*
     * =========================================================
     * LIST / SEARCH
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Page<Billing> findBillings(
            String search,
            Pageable pageable
    ) {

        if (pageable == null) {
            throw new IllegalArgumentException(
                    "Page information is required"
            );
        }

        if (search == null || search.isBlank()) {
            return billingRepository.findAll(pageable);
        }

        return billingRepository.search(
                search.trim(),
                pageable
        );
    }


    /*
     * =========================================================
     * FIND BILLING BY ID
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Billing getBilling(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Billing ID is required"
            );
        }

        return billingRepository
                .findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Billing record not found"
                        )
                );
    }


    /*
     * =========================================================
     * FIND BY BILLING NUMBER
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Billing getBillingByNumber(
            String billingNumber
    ) {

        if (billingNumber == null ||
                billingNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Billing number is required"
            );
        }

        return billingRepository
                .findByBillingNumberIgnoreCase(
                        billingNumber.trim()
                )
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Billing record not found"
                        )
                );
    }


    /*
     * =========================================================
     * FIND BILL FOR APPOINTMENT
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Optional<Billing> findByAppointmentId(
            Long appointmentId
    ) {

        if (appointmentId == null) {
            return Optional.empty();
        }

        return billingRepository
                .findByAppointment_Id(
                        appointmentId
                );
    }


    /*
     * =========================================================
     * CHECK IF APPOINTMENT ALREADY HAS BILL
     * =========================================================
     */

    @Transactional(readOnly = true)
    public boolean billingExistsForAppointment(
            Long appointmentId
    ) {

        if (appointmentId == null) {
            return false;
        }

        return billingRepository
                .existsByAppointment_Id(
                        appointmentId
                );
    }


    /*
     * =========================================================
     * CREATE BILL
     * =========================================================
     */

    public Billing createBilling(
            Long appointmentId,
            AppUser createdBy
    ) {

        if (appointmentId == null) {
            throw new IllegalArgumentException(
                    "Appointment is required"
            );
        }

        if (createdBy == null) {
            throw new IllegalArgumentException(
                    "Created by user is required"
            );
        }


        Appointment appointment =
                appointmentRepository
                        .findById(appointmentId)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Appointment not found"
                                )
                        );


        /*
         * One appointment must only have one billing record.
         */
        if (billingRepository
                .existsByAppointment_Id(
                        appointmentId
                )) {

            throw new IllegalArgumentException(
                    "A billing record already exists for this appointment"
            );
        }


        Treatment treatment =
                appointment.getTreatment();

        if (treatment == null) {
            throw new IllegalArgumentException(
                    "Appointment does not contain a treatment"
            );
        }


        BigDecimal totalAmount =
                treatment.getBasePrice();

        if (totalAmount == null) {
            throw new IllegalArgumentException(
                    "Treatment price is not configured"
            );
        }


        if (totalAmount.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException(
                    "Treatment price cannot be negative"
            );
        }


        Billing billing =
                new Billing(
                        generateBillingNumber(),
                        appointment,
                        totalAmount,
                        createdBy
                );


        billing.setAmountPaid(
                BigDecimal.ZERO
        );

        billing.setPaymentStatus(
                PaymentStatus.UNPAID
        );

        billing.setPaymentMethod(null);


        return billingRepository.save(
                billing
        );
    }


    /*
     * =========================================================
     * RECORD PAYMENT
     * =========================================================
     */

    public Billing recordPayment(
            Long billingId,
            BigDecimal paymentAmount,
            PaymentMethod paymentMethod
    ) {

        Billing billing =
                getBilling(billingId);


        if (paymentAmount == null) {
            throw new IllegalArgumentException(
                    "Payment amount is required"
            );
        }


        if (paymentAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }


        if (paymentMethod == null) {
            throw new IllegalArgumentException(
                    "Payment method is required"
            );
        }


        BigDecimal totalAmount =
                billing.getTotalAmount();

        if (totalAmount == null) {
            throw new IllegalArgumentException(
                    "Billing total amount is not configured"
            );
        }


        BigDecimal currentPaid =
                billing.getAmountPaid() == null
                        ? BigDecimal.ZERO
                        : billing.getAmountPaid();


        BigDecimal outstandingBalance =
                totalAmount.subtract(
                        currentPaid
                );


        if (outstandingBalance.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalArgumentException(
                    "This billing record has already been fully paid"
            );
        }


        if (paymentAmount.compareTo(
                outstandingBalance
        ) > 0) {

            throw new IllegalArgumentException(
                    "Payment amount cannot exceed the outstanding balance"
            );
        }


        BigDecimal newAmountPaid =
                currentPaid.add(
                        paymentAmount
                );


        billing.setAmountPaid(
                newAmountPaid
        );

        billing.setPaymentMethod(
                paymentMethod
        );


        updatePaymentStatus(
                billing
        );


        return billingRepository.save(
                billing
        );
    }


    /*
     * =========================================================
     * OUTSTANDING BALANCE
     * =========================================================
     */

    @Transactional(readOnly = true)
    public BigDecimal getOutstandingBalance(
            Long billingId
    ) {

        Billing billing =
                getBilling(billingId);

        return billing.getBalance();
    }


    /*
     * =========================================================
     * PAYMENT STATUS
     * =========================================================
     */

    private void updatePaymentStatus(
            Billing billing
    ) {

        BigDecimal totalAmount =
                billing.getTotalAmount();

        BigDecimal amountPaid =
                billing.getAmountPaid();


        if (amountPaid == null ||
                amountPaid.compareTo(
                        BigDecimal.ZERO
                ) == 0) {

            billing.setPaymentStatus(
                    PaymentStatus.UNPAID
            );

            billing.setPaymentMethod(null);

            return;
        }


        if (amountPaid.compareTo(
                totalAmount
        ) >= 0) {

            billing.setPaymentStatus(
                    PaymentStatus.PAID
            );

            return;
        }


        billing.setPaymentStatus(
                PaymentStatus.PARTIALLY_PAID
        );
    }


    /*
     * =========================================================
     * BILLING NUMBER GENERATOR
     * =========================================================
     */

    private String generateBillingNumber() {

        String billingNumber;

        do {

            String datePart =
                    LocalDate.now()
                            .format(
                                    DateTimeFormatter.BASIC_ISO_DATE
                            );


            String randomPart =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase();


            billingNumber =
                    "BIL-" +
                            datePart +
                            "-" +
                            randomPart;

        } while (
                billingRepository
                        .existsByBillingNumberIgnoreCase(
                                billingNumber
                        )
        );


        return billingNumber;
    }
}