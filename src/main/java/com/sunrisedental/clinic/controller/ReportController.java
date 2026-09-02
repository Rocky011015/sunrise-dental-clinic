package com.sunrisedental.clinic.controller;

import com.sunrisedental.clinic.service.ReportService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;


    public ReportController(
            ReportService reportService
    ) {
        this.reportService = reportService;
    }


    @GetMapping
    public String showReports(
            Model model
    ) {

        /*
         * ===============================================
         * APPOINTMENT SUMMARY
         * ===============================================
         */

        model.addAttribute(
                "totalAppointments",
                reportService.getTotalAppointments()
        );

        model.addAttribute(
                "scheduledAppointments",
                reportService.getScheduledAppointments()
        );

        model.addAttribute(
                "completedAppointments",
                reportService.getCompletedAppointments()
        );

        model.addAttribute(
                "cancelledAppointments",
                reportService.getCancelledAppointments()
        );


        /*
         * ===============================================
         * BILLING SUMMARY
         * ===============================================
         */

        model.addAttribute(
                "totalBillings",
                reportService.getTotalBillings()
        );

        model.addAttribute(
                "paidBillings",
                reportService.getPaidBillings()
        );

        model.addAttribute(
                "partiallyPaidBillings",
                reportService.getPartiallyPaidBillings()
        );

        model.addAttribute(
                "unpaidBillings",
                reportService.getUnpaidBillings()
        );


        /*
         * ===============================================
         * FINANCIAL SUMMARY
         * ===============================================
         */

        model.addAttribute(
                "totalBilledAmount",
                reportService.getTotalBilledAmount()
        );

        model.addAttribute(
                "totalCollectedAmount",
                reportService.getTotalCollectedAmount()
        );

        model.addAttribute(
                "outstandingAmount",
                reportService.getOutstandingAmount()
        );


        /*
         * ===============================================
         * PERFORMANCE REPORTS
         * ===============================================
         */

        model.addAttribute(
                "dentistWorkload",
                reportService.getDentistWorkload()
        );

        model.addAttribute(
                "treatmentUsage",
                reportService.getTreatmentUsage()
        );


        return "reports/dashboard";
    }
}