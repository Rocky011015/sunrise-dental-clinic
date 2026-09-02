package com.sunrisedental.clinic.controller;

import com.sunrisedental.clinic.domain.Treatment;
import com.sunrisedental.clinic.service.TreatmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(
            TreatmentService treatmentService
    ) {
        this.treatmentService = treatmentService;
    }


    // =====================================================
    // LIST + SEARCH
    // =====================================================

    @GetMapping
    public String listTreatments(
            @RequestParam(
                    name = "search",
                    required = false,
                    defaultValue = ""
            )
            String search,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            int page,

            Model model
    ) {

        int safePage = Math.max(page, 0);

        Pageable pageable =
                PageRequest.of(
                        safePage,
                        10
                );

        Page<Treatment> treatmentPage =
                treatmentService.searchTreatments(
                        search,
                        pageable
                );

        model.addAttribute(
                "treatmentPage",
                treatmentPage
        );

        model.addAttribute(
                "treatments",
                treatmentPage.getContent()
        );

        model.addAttribute(
                "search",
                search
        );

        return "treatments/list";
    }


    // =====================================================
    // CREATE FORM
    // =====================================================

    @GetMapping("/new")
    public String showCreateForm(
            Model model
    ) {

        Treatment treatment =
                new Treatment();

        treatment.setActive(true);

        model.addAttribute(
                "treatment",
                treatment
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "treatments/form";
    }


    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping
    public String createTreatment(
            @Valid Treatment treatment,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "editMode",
                    false
            );

            return "treatments/form";
        }

        try {

            Treatment savedTreatment =
                    treatmentService
                            .createTreatment(
                                    treatment
                            );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "Treatment "
                                    + savedTreatment
                                    .getTreatmentName()
                                    + " was created successfully."
                    );

            return "redirect:/treatments";

        } catch (IllegalArgumentException ex) {

            bindingResult.reject(
                    "treatment.error",
                    ex.getMessage()
            );

            model.addAttribute(
                    "editMode",
                    false
            );

            return "treatments/form";
        }
    }


    // =====================================================
    // EDIT FORM
    // =====================================================

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Treatment treatment =
                    treatmentService
                            .getTreatmentById(id);

            model.addAttribute(
                    "treatment",
                    treatment
            );

            model.addAttribute(
                    "editMode",
                    true
            );

            return "treatments/form";

        } catch (NoSuchElementException ex) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            ex.getMessage()
                    );

            return "redirect:/treatments";
        }
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @PostMapping("/{id}")
    public String updateTreatment(
            @PathVariable Long id,
            @Valid Treatment treatment,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "editMode",
                    true
            );

            return "treatments/form";
        }

        try {

            Treatment updatedTreatment =
                    treatmentService
                            .updateTreatment(
                                    id,
                                    treatment
                            );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "Treatment "
                                    + updatedTreatment
                                    .getTreatmentName()
                                    + " was updated successfully."
                    );

            return "redirect:/treatments";

        } catch (
                IllegalArgumentException |
                NoSuchElementException ex
        ) {

            bindingResult.reject(
                    "treatment.error",
                    ex.getMessage()
            );

            model.addAttribute(
                    "editMode",
                    true
            );

            return "treatments/form";
        }
    }


    // =====================================================
    // DEACTIVATE
    // =====================================================

    @PostMapping("/{id}/deactivate")
    public String deactivateTreatment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Treatment treatment =
                    treatmentService
                            .setTreatmentActive(
                                    id,
                                    false
                            );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            treatment.getTreatmentName()
                                    + " was deactivated successfully."
                    );

        } catch (NoSuchElementException ex) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            ex.getMessage()
                    );
        }

        return "redirect:/treatments";
    }


    // =====================================================
    // ACTIVATE
    // =====================================================

    @PostMapping("/{id}/activate")
    public String activateTreatment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Treatment treatment =
                    treatmentService
                            .setTreatmentActive(
                                    id,
                                    true
                            );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            treatment.getTreatmentName()
                                    + " was activated successfully."
                    );

        } catch (NoSuchElementException ex) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            ex.getMessage()
                    );
        }

        return "redirect:/treatments";
    }
}