package com.sunrisedental.clinic.repository;

public interface TreatmentUsageReport {

    Long getTreatmentId();

    String getTreatmentName();

    Long getAppointmentCount();
}