package com.sunrisedental.clinic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "treatments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_treatments_code",
                        columnNames = "treatment_code"
                ),
                @UniqueConstraint(
                        name = "uk_treatments_name",
                        columnNames = "treatment_name"
                )
        }
)
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Treatment code is required")
    @Size(max = 20, message = "Treatment code must not exceed 20 characters")
    @Column(name = "treatment_code", nullable = false, length = 20)
    private String treatmentCode;

    @NotBlank(message = "Treatment name is required")
    @Size(max = 120, message = "Treatment name must not exceed 120 characters")
    @Column(name = "treatment_name", nullable = false, length = 120)
    private String treatmentName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Base price must be zero or greater"
    )
    @Column(
            name = "base_price",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal basePrice;

    @Min(
            value = 1,
            message = "Estimated duration must be at least 1 minute"
    )
    @Column(name = "estimated_duration_minutes", nullable = false)
    private int estimatedDurationMinutes = 30;

    @Column(nullable = false)
    private boolean active = true;

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

    public Treatment() {
    }

    public Treatment(
            String treatmentCode,
            String treatmentName,
            String description,
            BigDecimal basePrice,
            int estimatedDurationMinutes
    ) {
        this.treatmentCode = treatmentCode;
        this.treatmentName = treatmentName;
        this.description = description;
        this.basePrice = basePrice;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getTreatmentCode() {
        return treatmentCode;
    }

    public void setTreatmentCode(String treatmentCode) {
        this.treatmentCode = treatmentCode;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}