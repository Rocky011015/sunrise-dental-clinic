package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.AppointmentStatus;
import com.sunrisedental.clinic.domain.PaymentStatus;
import com.sunrisedental.clinic.repository.AppointmentRepository;
import com.sunrisedental.clinic.repository.BillingRepository;
import com.sunrisedental.clinic.repository.DentistWorkloadReport;
import com.sunrisedental.clinic.repository.TreatmentUsageReport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final AppointmentRepository appointmentRepository;
    private final BillingRepository billingRepository;


    public ReportService(
            AppointmentRepository appointmentRepository,
            BillingRepository billingRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.billingRepository = billingRepository;
    }


    /*
     * =========================================================
     * APPOINTMENT SUMMARY
     * =========================================================
     */

    public long getTotalAppointments() {

        return appointmentRepository.count();
    }


    public long getScheduledAppointments() {

        return appointmentRepository.countByStatus(
                AppointmentStatus.SCHEDULED
        );
    }


    public long getCompletedAppointments() {

        return appointmentRepository.countByStatus(
                AppointmentStatus.COMPLETED
        );
    }


    public long getCancelledAppointments() {

        return appointmentRepository.countByStatus(
                AppointmentStatus.CANCELLED
        );
    }


    /*
     * =========================================================
     * BILLING SUMMARY
     * =========================================================
     */

    public long getTotalBillings() {

        return billingRepository.count();
    }


    public long getPaidBillings() {

        return billingRepository.countByPaymentStatus(
                PaymentStatus.PAID
        );
    }


    public long getPartiallyPaidBillings() {

        return billingRepository.countByPaymentStatus(
                PaymentStatus.PARTIALLY_PAID
        );
    }


    public long getUnpaidBillings() {

        return billingRepository.countByPaymentStatus(
                PaymentStatus.UNPAID
        );
    }


    /*
     * =========================================================
     * FINANCIAL SUMMARY
     * =========================================================
     */

    public BigDecimal getTotalBilledAmount() {

        return safeAmount(
                billingRepository
                        .getTotalBilledAmount()
        );
    }


    public BigDecimal getTotalCollectedAmount() {

        return safeAmount(
                billingRepository
                        .getTotalCollectedAmount()
        );
    }


    public BigDecimal getOutstandingAmount() {

        BigDecimal totalBilled =
                getTotalBilledAmount();

        BigDecimal totalCollected =
                getTotalCollectedAmount();


        BigDecimal outstanding =
                totalBilled.subtract(
                        totalCollected
                );


        if (
                outstanding.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            return BigDecimal.ZERO;
        }


        return outstanding;
    }


    /*
     * =========================================================
     * DENTIST WORKLOAD
     * =========================================================
     */

    public List<DentistWorkloadReport>
    getDentistWorkload() {

        return appointmentRepository
                .getDentistWorkloadReport();
    }


    /*
     * =========================================================
     * TREATMENT USAGE
     * =========================================================
     */

    public List<TreatmentUsageReport>
    getTreatmentUsage() {

        return appointmentRepository
                .getTreatmentUsageReport();
    }


    /*
     * =========================================================
     * INTERNAL HELPERS
     * =========================================================
     */

    private BigDecimal safeAmount(
            BigDecimal amount
    ) {

        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }
}