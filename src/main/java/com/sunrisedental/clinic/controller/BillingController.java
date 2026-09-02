package com.sunrisedental.clinic.controller;

import com.sunrisedental.clinic.domain.AppUser;
import com.sunrisedental.clinic.domain.Billing;
import com.sunrisedental.clinic.domain.PaymentMethod;
import com.sunrisedental.clinic.repository.AppUserRepository;
import com.sunrisedental.clinic.service.BillingService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.NoSuchElementException;


@Controller
@RequestMapping("/billings")
public class BillingController {

    private final BillingService billingService;
    private final AppUserRepository appUserRepository;


    public BillingController(
            BillingService billingService,
            AppUserRepository appUserRepository
    ) {
        this.billingService = billingService;
        this.appUserRepository = appUserRepository;
    }


    /*
     * =========================================================
     * BILLING LIST
     * =========================================================
     */

    @GetMapping
    public String listBillings(

            @RequestParam(
                    name = "search",
                    required = false,
                    defaultValue = ""
            )
            String searchTerm,

            @RequestParam(
                    name = "page",
                    required = false,
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    name = "size",
                    required = false,
                    defaultValue = "10"
            )
            int size,

            Model model
    ) {

        int safePage =
                Math.max(
                        page,
                        0
                );

        int safeSize =
                Math.min(
                        Math.max(
                                size,
                                5
                        ),
                        50
                );


        Pageable pageable =
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(
                                Sort.Order.desc(
                                        "createdAt"
                                )
                        )
                );


        Page<Billing> billingPage =
                billingService.findBillings(
                        searchTerm,
                        pageable
                );


        model.addAttribute(
                "billingPage",
                billingPage
        );

        model.addAttribute(
                "billings",
                billingPage.getContent()
        );

        model.addAttribute(
                "searchTerm",
                searchTerm
        );


        return "billings/list";
    }


    /*
     * =========================================================
     * BILLING DETAILS
     * =========================================================
     */

    @GetMapping("/{id}")
    public String showBillingDetails(
            @PathVariable Long id,
            Model model
    ) {

        Billing billing =
                billingService.getBilling(
                        id
                );


        model.addAttribute(
                "billing",
                billing
        );

        model.addAttribute(
                "paymentMethods",
                PaymentMethod.values()
        );


        return "billings/details";
    }


    /*
     * =========================================================
     * CREATE BILL FROM APPOINTMENT
     * =========================================================
     */

    @PostMapping("/appointment/{appointmentId}")
    public String createBillingFromAppointment(

            @PathVariable
            Long appointmentId,

            Authentication authentication,

            RedirectAttributes redirectAttributes
    ) {

        try {

            AppUser createdBy =
                    getAuthenticatedUser(
                            authentication
                    );


            Billing billing =
                    billingService.createBilling(
                            appointmentId,
                            createdBy
                    );


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Billing "
                            + billing.getBillingNumber()
                            + " created successfully."
            );


            return "redirect:/billings/"
                    + billing.getId();


        } catch (
                IllegalArgumentException
                |
                NoSuchElementException exception
        ) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );


            return "redirect:/appointments/"
                    + appointmentId;
        }
    }


    /*
     * =========================================================
     * RECORD PAYMENT
     * =========================================================
     */

    @PostMapping("/{id}/payment")
    public String recordPayment(

            @PathVariable
            Long id,

            @RequestParam(
                    name = "paymentAmount"
            )
            BigDecimal paymentAmount,

            @RequestParam(
                    name = "paymentMethod"
            )
            PaymentMethod paymentMethod,

            RedirectAttributes redirectAttributes
    ) {

        try {

            Billing billing =
                    billingService.recordPayment(
                            id,
                            paymentAmount,
                            paymentMethod
                    );


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment recorded successfully for "
                            + billing.getBillingNumber()
                            + "."
            );


        } catch (
                IllegalArgumentException
                |
                NoSuchElementException exception
        ) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }


        return "redirect:/billings/"
                + id;
    }


    /*
     * =========================================================
     * AUTHENTICATED USER
     * =========================================================
     */

    private AppUser getAuthenticatedUser(
            Authentication authentication
    ) {

        if (
                authentication == null
                        ||
                        authentication.getName() == null
                        ||
                        authentication
                                .getName()
                                .isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Authenticated user is required"
            );
        }


        String username =
                authentication
                        .getName()
                        .trim();


        return appUserRepository
                .findByUsernameIgnoreCase(
                        username
                )
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Authenticated user account not found: "
                                                + username
                                )
                );
    }
}