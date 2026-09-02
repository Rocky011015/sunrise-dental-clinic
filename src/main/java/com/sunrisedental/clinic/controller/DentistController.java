package com.sunrisedental.clinic.controller;

import com.sunrisedental.clinic.domain.Dentist;
import com.sunrisedental.clinic.service.DentistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dentists")
public class DentistController {

    private static final int PAGE_SIZE = 10;

    private final DentistService dentistService;

    public DentistController(
            DentistService dentistService
    ) {
        this.dentistService = dentistService;
    }

    @GetMapping
    public String listDentists(
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
                        PAGE_SIZE,
                        Sort.by(
                                Sort.Direction.ASC,
                                "fullName"
                        )
                );

        Page<Dentist> dentists =
                dentistService.searchDentists(
                        search,
                        pageable
                );

        model.addAttribute(
                "dentists",
                dentists
        );

        model.addAttribute(
                "search",
                search
        );

        return "dentists/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {

        Dentist dentist = new Dentist();
        dentist.setActive(true);

        model.addAttribute("dentist", dentist);
        model.addAttribute("editMode", false);

        return "dentists/form";
    }

    @PostMapping
    public String createDentist(
            @Valid
            @ModelAttribute("dentist")
            Dentist dentist,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "editMode",
                    false
            );

            return "dentists/form";
        }

        try {

            Dentist savedDentist =
                    dentistService.createDentist(
                            dentist
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Dentist " +
                            savedDentist.getFullName() +
                            " was created successfully."
            );

            return "redirect:/dentists";

        } catch (IllegalArgumentException exception) {

            bindingResult.rejectValue(
                    "dentistCode",
                    "dentistCode.invalid",
                    exception.getMessage()
            );

            model.addAttribute(
                    "editMode",
                    false
            );

            return "dentists/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model
    ) {

        Dentist dentist =
                dentistService.getDentistById(id);

        model.addAttribute(
                "dentist",
                dentist
        );

        model.addAttribute(
                "editMode",
                true
        );

        return "dentists/form";
    }

    @PostMapping("/{id}")
    public String updateDentist(
            @PathVariable Long id,

            @Valid
            @ModelAttribute("dentist")
            Dentist dentist,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "editMode",
                    true
            );

            return "dentists/form";
        }

        try {

            Dentist updatedDentist =
                    dentistService.updateDentist(
                            id,
                            dentist
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Dentist " +
                            updatedDentist.getFullName() +
                            " was updated successfully."
            );

            return "redirect:/dentists";

        } catch (IllegalArgumentException exception) {

            bindingResult.rejectValue(
                    "dentistCode",
                    "dentistCode.invalid",
                    exception.getMessage()
            );

            model.addAttribute(
                    "editMode",
                    true
            );

            return "dentists/form";
        }
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateDentist(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        Dentist dentist =
                dentistService.setDentistActive(
                        id,
                        false
                );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                dentist.getFullName() +
                        " was deactivated successfully."
        );

        return "redirect:/dentists";
    }

    @PostMapping("/{id}/activate")
    public String activateDentist(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        Dentist dentist =
                dentistService.setDentistActive(
                        id,
                        true
                );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                dentist.getFullName() +
                        " was activated successfully."
        );

        return "redirect:/dentists";
    }
}