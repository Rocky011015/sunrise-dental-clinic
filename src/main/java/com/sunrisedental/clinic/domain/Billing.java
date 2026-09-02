package com.sunrisedental.clinic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "billings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_billings_number",
                        columnNames = "billing_number"
                ),
                @UniqueConstraint(
                        name = "uk_billings_appointment",
                        columnNames = "appointment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_billings_payment_status",
                        columnList = "payment_status"
                ),
                @Index(
                        name = "idx_billings_created_at",
                        columnList = "created_at"
                )
        }
)
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Billing number is required")
    @Size(
            max = 30,
            message = "Billing number must not exceed 30 characters"
    )
    @Column(
            name = "billing_number",
            nullable = false,
            length = 30
    )
    private String billingNumber;


    @NotNull(message = "Appointment is required")
    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "appointment_id",
            nullable = false,
            unique = true
    )
    private Appointment appointment;


    @NotNull(message = "Total amount is required")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Total amount cannot be negative"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Total amount must contain a maximum of 2 decimal places"
    )
    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;


    @NotNull(message = "Amount paid is required")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Amount paid cannot be negative"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Amount paid must contain a maximum of 2 decimal places"
    )
    @Column(
            name = "amount_paid",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amountPaid = BigDecimal.ZERO;


    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            nullable = false,
            length = 30
    )
    private PaymentStatus paymentStatus =
            PaymentStatus.UNPAID;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            length = 30
    )
    private PaymentMethod paymentMethod;


    @Size(
            max = 500,
            message = "Notes must not exceed 500 characters"
    )
    @Column(length = 500)
    private String notes;


    @NotNull(message = "Created by user is required")
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "created_by",
            nullable = false
    )
    private AppUser createdBy;


    @Version
    @Column(nullable = false)
    private Long version;


    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime updatedAt;


    public Billing() {
    }


    public Billing(
            String billingNumber,
            Appointment appointment,
            BigDecimal totalAmount,
            AppUser createdBy
    ) {
        this.billingNumber = billingNumber;
        this.appointment = appointment;
        this.totalAmount = totalAmount;
        this.amountPaid = BigDecimal.ZERO;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.createdBy = createdBy;
    }


    public Long getId() {
        return id;
    }


    public String getBillingNumber() {
        return billingNumber;
    }

    public void setBillingNumber(
            String billingNumber
    ) {
        this.billingNumber = billingNumber;
    }


    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(
            Appointment appointment
    ) {
        this.appointment = appointment;
    }


    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount
    ) {
        this.totalAmount = totalAmount;
    }


    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(
            BigDecimal amountPaid
    ) {
        this.amountPaid = amountPaid;
    }


    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(
            PaymentStatus paymentStatus
    ) {
        this.paymentStatus = paymentStatus;
    }


    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            PaymentMethod paymentMethod
    ) {
        this.paymentMethod = paymentMethod;
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(
            String notes
    ) {
        this.notes = notes;
    }


    public AppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(
            AppUser createdBy
    ) {
        this.createdBy = createdBy;
    }


    public Long getVersion() {
        return version;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    /**
     * Calculated outstanding balance.
     * This value is not stored separately in the database.
     */
    public BigDecimal getBalance() {

        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }

        if (amountPaid == null) {
            return totalAmount;
        }

        BigDecimal balance =
                totalAmount.subtract(amountPaid);

        return balance.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO
                : balance;
    }
}