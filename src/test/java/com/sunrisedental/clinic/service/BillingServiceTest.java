package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.AppUser;
import com.sunrisedental.clinic.domain.Appointment;
import com.sunrisedental.clinic.domain.Billing;
import com.sunrisedental.clinic.domain.PaymentMethod;
import com.sunrisedental.clinic.domain.PaymentStatus;
import com.sunrisedental.clinic.domain.Treatment;
import com.sunrisedental.clinic.repository.AppointmentRepository;
import com.sunrisedental.clinic.repository.BillingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillingRepository billingRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private Appointment appointment;

    @Mock
    private Treatment treatment;

    @Mock
    private AppUser createdBy;

    private BillingService billingService;


    @BeforeEach
    void setUp() {

        billingService =
                new BillingService(
                        billingRepository,
                        appointmentRepository
                );
    }


    /*
     * =========================================================
     * CREATE BILLING
     * =========================================================
     */

    @Test
    void shouldCreateBillingFromAppointment() {

        Long appointmentId = 1L;

        when(
                appointmentRepository.findById(
                        appointmentId
                )
        ).thenReturn(
                Optional.of(appointment)
        );

        when(
                billingRepository.existsByAppointment_Id(
                        appointmentId
                )
        ).thenReturn(false);

        when(
                appointment.getTreatment()
        ).thenReturn(treatment);

        when(
                treatment.getBasePrice()
        ).thenReturn(
                new BigDecimal("5000.00")
        );

        when(
                billingRepository
                        .existsByBillingNumberIgnoreCase(
                                anyString()
                        )
        ).thenReturn(false);

        when(
                billingRepository.save(
                        any(Billing.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        Billing result =
                billingService.createBilling(
                        appointmentId,
                        createdBy
                );


        assertNotNull(result);

        assertSame(
                appointment,
                result.getAppointment()
        );

        assertSame(
                createdBy,
                result.getCreatedBy()
        );

        assertEquals(
                0,
                result.getTotalAmount()
                        .compareTo(
                                new BigDecimal("5000.00")
                        )
        );

        assertEquals(
                0,
                result.getAmountPaid()
                        .compareTo(
                                BigDecimal.ZERO
                        )
        );

        assertEquals(
                PaymentStatus.UNPAID,
                result.getPaymentStatus()
        );

        assertNull(
                result.getPaymentMethod()
        );


        assertNotNull(
                result.getBillingNumber()
        );

        assertTrue(
                result.getBillingNumber()
                        .matches(
                                "BIL-\\d{8}-[A-Z0-9]{8}"
                        )
        );


        verify(
                billingRepository
        ).save(
                any(Billing.class)
        );
    }


    /*
     * =========================================================
     * DUPLICATE APPOINTMENT BILLING
     * =========================================================
     */

    @Test
    void shouldRejectDuplicateBillingForAppointment() {

        Long appointmentId = 1L;

        when(
                appointmentRepository.findById(
                        appointmentId
                )
        ).thenReturn(
                Optional.of(appointment)
        );

        when(
                billingRepository.existsByAppointment_Id(
                        appointmentId
                )
        ).thenReturn(true);


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                billingService.createBilling(
                                        appointmentId,
                                        createdBy
                                )
                );


        assertEquals(
                "A billing record already exists for this appointment",
                exception.getMessage()
        );


        verify(
                billingRepository,
                never()
        ).save(
                any(Billing.class)
        );
    }


    /*
     * =========================================================
     * APPOINTMENT NOT FOUND
     * =========================================================
     */

    @Test
    void shouldRejectBillingWhenAppointmentDoesNotExist() {

        Long appointmentId = 999L;

        when(
                appointmentRepository.findById(
                        appointmentId
                )
        ).thenReturn(
                Optional.empty()
        );


        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () ->
                                billingService.createBilling(
                                        appointmentId,
                                        createdBy
                                )
                );


        assertEquals(
                "Appointment not found",
                exception.getMessage()
        );


        verify(
                billingRepository,
                never()
        ).save(
                any(Billing.class)
        );
    }


    /*
     * =========================================================
     * PARTIAL PAYMENT
     * =========================================================
     */

    @Test
    void shouldRecordPartialPayment() {

        Billing billing =
                new Billing(
                        "BIL-20260901-12345678",
                        appointment,
                        new BigDecimal("10000.00"),
                        createdBy
                );

        billing.setAmountPaid(
                BigDecimal.ZERO
        );

        billing.setPaymentStatus(
                PaymentStatus.UNPAID
        );


        when(
                billingRepository.findById(1L)
        ).thenReturn(
                Optional.of(billing)
        );

        when(
                billingRepository.save(
                        any(Billing.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        Billing result =
                billingService.recordPayment(
                        1L,
                        new BigDecimal("4000.00"),
                        PaymentMethod.CASH
                );


        assertEquals(
                0,
                result.getAmountPaid()
                        .compareTo(
                                new BigDecimal("4000.00")
                        )
        );

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                result.getPaymentStatus()
        );

        assertEquals(
                PaymentMethod.CASH,
                result.getPaymentMethod()
        );

        assertEquals(
                0,
                result.getBalance()
                        .compareTo(
                                new BigDecimal("6000.00")
                        )
        );


        verify(
                billingRepository
        ).save(billing);
    }


    /*
     * =========================================================
     * FULL PAYMENT
     * =========================================================
     */

    @Test
    void shouldRecordFullPayment() {

        Billing billing =
                new Billing(
                        "BIL-20260901-12345678",
                        appointment,
                        new BigDecimal("10000.00"),
                        createdBy
                );

        billing.setAmountPaid(
                new BigDecimal("4000.00")
        );

        billing.setPaymentStatus(
                PaymentStatus.PARTIALLY_PAID
        );


        when(
                billingRepository.findById(1L)
        ).thenReturn(
                Optional.of(billing)
        );

        when(
                billingRepository.save(
                        any(Billing.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        Billing result =
                billingService.recordPayment(
                        1L,
                        new BigDecimal("6000.00"),
                        PaymentMethod.CARD
                );


        assertEquals(
                0,
                result.getAmountPaid()
                        .compareTo(
                                new BigDecimal("10000.00")
                        )
        );

        assertEquals(
                PaymentStatus.PAID,
                result.getPaymentStatus()
        );

        assertEquals(
                PaymentMethod.CARD,
                result.getPaymentMethod()
        );

        assertEquals(
                0,
                result.getBalance()
                        .compareTo(
                                BigDecimal.ZERO
                        )
        );
    }


    /*
     * =========================================================
     * OVERPAYMENT
     * =========================================================
     */

    @Test
    void shouldRejectOverpayment() {

        Billing billing =
                new Billing(
                        "BIL-20260901-12345678",
                        appointment,
                        new BigDecimal("10000.00"),
                        createdBy
                );

        billing.setAmountPaid(
                new BigDecimal("4000.00")
        );

        billing.setPaymentStatus(
                PaymentStatus.PARTIALLY_PAID
        );


        when(
                billingRepository.findById(1L)
        ).thenReturn(
                Optional.of(billing)
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                billingService.recordPayment(
                                        1L,
                                        new BigDecimal("7000.00"),
                                        PaymentMethod.CASH
                                )
                );


        assertEquals(
                "Payment amount cannot exceed the outstanding balance",
                exception.getMessage()
        );


        verify(
                billingRepository,
                never()
        ).save(
                any(Billing.class)
        );
    }


    /*
     * =========================================================
     * PAYMENT METHOD REQUIRED
     * =========================================================
     */

    @Test
    void shouldRequirePaymentMethod() {

        Billing billing =
                new Billing(
                        "BIL-20260901-12345678",
                        appointment,
                        new BigDecimal("10000.00"),
                        createdBy
                );


        when(
                billingRepository.findById(1L)
        ).thenReturn(
                Optional.of(billing)
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                billingService.recordPayment(
                                        1L,
                                        new BigDecimal("1000.00"),
                                        null
                                )
                );


        assertEquals(
                "Payment method is required",
                exception.getMessage()
        );


        verify(
                billingRepository,
                never()
        ).save(
                any(Billing.class)
        );
    }


    /*
     * =========================================================
     * INVALID PAYMENT AMOUNT
     * =========================================================
     */

    @Test
    void shouldRejectZeroPayment() {

        Billing billing =
                new Billing(
                        "BIL-20260901-12345678",
                        appointment,
                        new BigDecimal("10000.00"),
                        createdBy
                );


        when(
                billingRepository.findById(1L)
        ).thenReturn(
                Optional.of(billing)
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                billingService.recordPayment(
                                        1L,
                                        BigDecimal.ZERO,
                                        PaymentMethod.CASH
                                )
                );


        assertEquals(
                "Payment amount must be greater than zero",
                exception.getMessage()
        );


        verify(
                billingRepository,
                never()
        ).save(
                any(Billing.class)
        );
    }


    /*
     * =========================================================
     * ALREADY PAID
     * =========================================================
     */

    @Test
    void shouldRejectPaymentWhenAlreadyFullyPaid() {

        Billing billing =
                new Billing(
                        "BIL-20260901-12345678",
                        appointment,
                        new BigDecimal("10000.00"),
                        createdBy
                );

        billing.setAmountPaid(
                new BigDecimal("10000.00")
        );

        billing.setPaymentStatus(
                PaymentStatus.PAID
        );


        when(
                billingRepository.findById(1L)
        ).thenReturn(
                Optional.of(billing)
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                billingService.recordPayment(
                                        1L,
                                        new BigDecimal("100.00"),
                                        PaymentMethod.CASH
                                )
                );


        assertEquals(
                "This billing record has already been fully paid",
                exception.getMessage()
        );


        verify(
                billingRepository,
                never()
        ).save(
                any(Billing.class)
        );
    }
}