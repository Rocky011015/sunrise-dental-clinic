package com.sunrisedental.clinic.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.sunrisedental.clinic.domain.Patient;
import com.sunrisedental.clinic.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public String listPatients(
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("fullName").ascending()
        );

        Page<Patient> patients =
                patientService.searchPatients(searchTerm, pageable);

        model.addAttribute("patients", patients);
        model.addAttribute("searchTerm", searchTerm);

        return "patients/list";
    }
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patients/form";
    }
    @PostMapping
    public String createPatient(
            @Valid @ModelAttribute("patient") Patient patient,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "patients/form";
        }

        patientService.createPatient(patient);
        return "redirect:/patients";
    }
    @GetMapping("/{id}")
    public String showPatientDetails(
            @PathVariable Long id,
            Model model
    ) {
        Patient patient = patientService.getPatientById(id);
        model.addAttribute("patient", patient);
        return "patients/details";
    }
    @PostMapping("/{id}/delete")
    public String deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return "redirect:/patients";
    }
}