package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.AppointmentStatus;
import com.sunrisedental.clinic.domain.PaymentStatus;
import com.sunrisedental.clinic.repository.AppointmentRepository;
import com.sunrisedental.clinic.repository.BillingRepository;
import com.sunrisedental.clinic.repository.DentistWorkloadReport;
import com.sunrisedental.clinic.repository.TreatmentUsageReport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private BillingRepository billingRepository;

    @Mock
    private DentistWorkloadReport dentistWorkloadReport;

    @Mock
    private TreatmentUsageReport treatmentUsageReport;

    private ReportService reportService;


    @BeforeEach
    void setUp() {

        reportService =
                new ReportService(
                        appointmentRepository,
                        billingRepository
                );
    }


    @Test
    void shouldReturnTotalAppointments() {

        when(
                appointmentRepository.count()
        ).thenReturn(12L);


        long result =
                reportService
                        .getTotalAppointments();


        assertEquals(
                12L,
                result
        );

        verify(
                appointmentRepository
        ).count();
    }


    @Test
    void shouldReturnAppointmentStatusCounts() {

        when(
                appointmentRepository
                        .countByStatus(
                                AppointmentStatus.SCHEDULED
                        )
        ).thenReturn(5L);

        when(
                appointmentRepository
                        .countByStatus(
                                AppointmentStatus.COMPLETED
                        )
        ).thenReturn(4L);

        when(
                appointmentRepository
                        .countByStatus(
                                AppointmentStatus.CANCELLED
                        )
        ).thenReturn(3L);


        assertEquals(
                5L,
                reportService
                        .getScheduledAppointments()
        );

        assertEquals(
                4L,
                reportService
                        .getCompletedAppointments()
        );

        assertEquals(
                3L,
                reportService
                        .getCancelledAppointments()
        );
    }


    @Test
    void shouldReturnBillingStatusCounts() {

        when(
                billingRepository.count()
        ).thenReturn(8L);

        when(
                billingRepository
                        .countByPaymentStatus(
                                PaymentStatus.PAID
                        )
        ).thenReturn(4L);

        when(
                billingRepository
                        .countByPaymentStatus(
                                PaymentStatus.PARTIALLY_PAID
                        )
        ).thenReturn(2L);

        when(
                billingRepository
                        .countByPaymentStatus(
                                PaymentStatus.UNPAID
                        )
        ).thenReturn(2L);


        assertEquals(
                8L,
                reportService
                        .getTotalBillings()
        );

        assertEquals(
                4L,
                reportService
                        .getPaidBillings()
        );

        assertEquals(
                2L,
                reportService
                        .getPartiallyPaidBillings()
        );

        assertEquals(
                2L,
                reportService
                        .getUnpaidBillings()
        );
    }


    @Test
    void shouldReturnFinancialSummary() {

        when(
                billingRepository
                        .getTotalBilledAmount()
        ).thenReturn(
                new BigDecimal("25000.00")
        );

        when(
                billingRepository
                        .getTotalCollectedAmount()
        ).thenReturn(
                new BigDecimal("17500.00")
        );


        assertEquals(
                0,
                reportService
                        .getTotalBilledAmount()
                        .compareTo(
                                new BigDecimal("25000.00")
                        )
        );

        assertEquals(
                0,
                reportService
                        .getTotalCollectedAmount()
                        .compareTo(
                                new BigDecimal("17500.00")
                        )
        );

        assertEquals(
                0,
                reportService
                        .getOutstandingAmount()
                        .compareTo(
                                new BigDecimal("7500.00")
                        )
        );
    }


    @Test
    void shouldNeverReturnNegativeOutstandingAmount() {

        when(
                billingRepository
                        .getTotalBilledAmount()
        ).thenReturn(
                new BigDecimal("5000.00")
        );

        when(
                billingRepository
                        .getTotalCollectedAmount()
        ).thenReturn(
                new BigDecimal("6000.00")
        );


        BigDecimal result =
                reportService
                        .getOutstandingAmount();


        assertEquals(
                0,
                result.compareTo(
                        BigDecimal.ZERO
                )
        );
    }


    @Test
    void shouldHandleNullFinancialValues() {

        when(
                billingRepository
                        .getTotalBilledAmount()
        ).thenReturn(null);

        when(
                billingRepository
                        .getTotalCollectedAmount()
        ).thenReturn(null);


        assertEquals(
                0,
                reportService
                        .getTotalBilledAmount()
                        .compareTo(
                                BigDecimal.ZERO
                        )
        );

        assertEquals(
                0,
                reportService
                        .getTotalCollectedAmount()
                        .compareTo(
                                BigDecimal.ZERO
                        )
        );
    }


    @Test
    void shouldReturnDentistWorkloadReport() {

        List<DentistWorkloadReport> expected =
                List.of(
                        dentistWorkloadReport
                );

        when(
                appointmentRepository
                        .getDentistWorkloadReport()
        ).thenReturn(expected);


        List<DentistWorkloadReport> result =
                reportService
                        .getDentistWorkload();


        assertSame(
                expected,
                result
        );
    }


    @Test
    void shouldReturnTreatmentUsageReport() {

        List<TreatmentUsageReport> expected =
                List.of(
                        treatmentUsageReport
                );

        when(
                appointmentRepository
                        .getTreatmentUsageReport()
        ).thenReturn(expected);


        List<TreatmentUsageReport> result =
                reportService
                        .getTreatmentUsage();


        assertSame(
                expected,
                result
        );
    }
}