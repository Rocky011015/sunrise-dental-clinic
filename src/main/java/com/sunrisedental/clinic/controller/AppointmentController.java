package com.sunrisedental.clinic.controller;

import com.sunrisedental.clinic.controller.form.AppointmentForm;
import com.sunrisedental.clinic.domain.AppUser;
import com.sunrisedental.clinic.domain.Appointment;
import com.sunrisedental.clinic.domain.AppointmentStatus;
import com.sunrisedental.clinic.domain.Dentist;
import com.sunrisedental.clinic.domain.Patient;
import com.sunrisedental.clinic.domain.Treatment;
import com.sunrisedental.clinic.repository.AppUserRepository;
import com.sunrisedental.clinic.repository.DentistRepository;
import com.sunrisedental.clinic.repository.PatientRepository;
import com.sunrisedental.clinic.repository.TreatmentRepository;
import com.sunrisedental.clinic.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
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

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;
    private final AppUserRepository appUserRepository;

    public AppointmentController(
            AppointmentService appointmentService,
            PatientRepository patientRepository,
            DentistRepository dentistRepository,
            TreatmentRepository treatmentRepository,
            AppUserRepository appUserRepository
    ) {
        this.appointmentService = appointmentService;
        this.patientRepository = patientRepository;
        this.dentistRepository = dentistRepository;
        this.treatmentRepository = treatmentRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public String listAppointments(
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

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(
                Math.max(size, 5),
                50
        );

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        Sort.Order.asc(
                                "appointmentDate"
                        ),
                        Sort.Order.asc(
                                "appointmentTime"
                        )
                )
        );

        Page<Appointment> appointmentPage =
                appointmentService.searchAppointments(
                        searchTerm,
                        pageable
                );

        model.addAttribute(
                "appointmentPage",
                appointmentPage
        );

        model.addAttribute(
                "appointments",
                appointmentPage.getContent()
        );

        model.addAttribute(
                "searchTerm",
                searchTerm
        );

        return "appointments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(
            Model model
    ) {

        AppointmentForm form =
                new AppointmentForm();

        form.setStatus(
                AppointmentStatus.SCHEDULED
        );

        model.addAttribute(
                "appointmentForm",
                form
        );

        model.addAttribute(
                "editing",
                false
        );

        populateFormOptions(model);

        return "appointments/form";
    }

    @PostMapping
    public String createAppointment(
            @Valid
            @ModelAttribute("appointmentForm")
            AppointmentForm form,

            BindingResult bindingResult,

            Authentication authentication,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "editing",
                    false
            );

            populateFormOptions(model);

            return "appointments/form";
        }

        try {

            Patient patient =
                    getPatient(form.getPatientId());

            Dentist dentist =
                    getDentist(form.getDentistId());

            Treatment treatment =
                    getTreatment(
                            form.getTreatmentId()
                    );

            AppUser createdBy =
                    getAuthenticatedUser(
                            authentication
                    );

            Appointment appointment =
                    new Appointment(
                            "",
                            patient,
                            dentist,
                            treatment,
                            form.getAppointmentDate(),
                            form.getAppointmentTime(),
                            createdBy
                    );

            appointment.setStatus(
                    form.getStatus()
            );

            appointment.setNotes(
                    normaliseNotes(
                            form.getNotes()
                    )
            );

            Appointment savedAppointment =
                    appointmentService
                            .createAppointment(
                                    appointment
                            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Appointment "
                            + savedAppointment
                            .getAppointmentNumber()
                            + " created successfully."
            );

            return "redirect:/appointments/"
                    + savedAppointment.getId();

        } catch (
                IllegalArgumentException
                |
                NoSuchElementException exception
        ) {

            bindingResult.reject(
                    "appointment.error",
                    exception.getMessage()
            );

            model.addAttribute(
                    "editing",
                    false
            );

            populateFormOptions(model);

            return "appointments/form";
        }
    }

    @GetMapping("/{id}")
    public String showAppointmentDetails(
            @PathVariable Long id,
            Model model
    ) {

        Appointment appointment =
                appointmentService
                        .getAppointmentById(id);

        model.addAttribute(
                "appointment",
                appointment
        );

        return "appointments/details";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model
    ) {

        Appointment appointment =
                appointmentService
                        .getAppointmentById(id);

        AppointmentForm form =
                toForm(appointment);

        model.addAttribute(
                "appointmentForm",
                form
        );

        model.addAttribute(
                "appointment",
                appointment
        );

        model.addAttribute(
                "editing",
                true
        );

        populateFormOptions(model);

        return "appointments/form";
    }

    @PostMapping("/{id}")
    public String updateAppointment(
            @PathVariable Long id,

            @Valid
            @ModelAttribute("appointmentForm")
            AppointmentForm form,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "editing",
                    true
            );

            model.addAttribute(
                    "appointment",
                    appointmentService
                            .getAppointmentById(id)
            );

            populateFormOptions(model);

            return "appointments/form";
        }

        try {

            Patient patient =
                    getPatient(form.getPatientId());

            Dentist dentist =
                    getDentist(form.getDentistId());

            Treatment treatment =
                    getTreatment(
                            form.getTreatmentId()
                    );

            Appointment updatedAppointment =
                    new Appointment();

            updatedAppointment.setPatient(
                    patient
            );

            updatedAppointment.setDentist(
                    dentist
            );

            updatedAppointment.setTreatment(
                    treatment
            );

            updatedAppointment.setAppointmentDate(
                    form.getAppointmentDate()
            );

            updatedAppointment.setAppointmentTime(
                    form.getAppointmentTime()
            );

            updatedAppointment.setStatus(
                    form.getStatus()
            );

            updatedAppointment.setNotes(
                    normaliseNotes(
                            form.getNotes()
                    )
            );

            Appointment savedAppointment =
                    appointmentService
                            .updateAppointment(
                                    id,
                                    updatedAppointment
                            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Appointment "
                            + savedAppointment
                            .getAppointmentNumber()
                            + " updated successfully."
            );

            return "redirect:/appointments/"
                    + id;

        } catch (
                IllegalArgumentException
                |
                NoSuchElementException exception
        ) {

            bindingResult.reject(
                    "appointment.error",
                    exception.getMessage()
            );

            model.addAttribute(
                    "editing",
                    true
            );

            model.addAttribute(
                    "appointment",
                    appointmentService
                            .getAppointmentById(id)
            );

            populateFormOptions(model);

            return "appointments/form";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelAppointment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        Appointment appointment =
                appointmentService
                        .cancelAppointment(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Appointment "
                        + appointment
                        .getAppointmentNumber()
                        + " cancelled successfully."
        );

        return "redirect:/appointments/"
                + id;
    }

    private void populateFormOptions(
            Model model
    ) {

        model.addAttribute(
                "patients",
                patientRepository.findAll(
                        Sort.by(
                                Sort.Order.asc(
                                        "fullName"
                                )
                        )
                )
        );

        model.addAttribute(
                "dentists",
                dentistRepository
                        .findByActiveTrueOrderByFullNameAsc()
        );

        model.addAttribute(
                "treatments",
                treatmentRepository
                        .findByActiveTrueOrderByTreatmentNameAsc()
        );

        model.addAttribute(
                "statuses",
                AppointmentStatus.values()
        );

        model.addAttribute(
                "minimumDate",
                LocalDate.now()
        );
    }

    private Patient getPatient(
            Long patientId
    ) {

        if (patientId == null) {
            throw new IllegalArgumentException(
                    "Patient is required"
            );
        }

        return patientRepository
                .findById(patientId)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Patient not found with ID: "
                                                + patientId
                                )
                );
    }

    private Dentist getDentist(
            Long dentistId
    ) {

        if (dentistId == null) {
            throw new IllegalArgumentException(
                    "Dentist is required"
            );
        }

        return dentistRepository
                .findById(dentistId)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Dentist not found with ID: "
                                                + dentistId
                                )
                );
    }

    private Treatment getTreatment(
            Long treatmentId
    ) {

        if (treatmentId == null) {
            throw new IllegalArgumentException(
                    "Treatment is required"
            );
        }

        return treatmentRepository
                .findById(treatmentId)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Treatment not found with ID: "
                                                + treatmentId
                                )
                );
    }

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

    private AppointmentForm toForm(
            Appointment appointment
    ) {

        AppointmentForm form =
                new AppointmentForm();

        form.setPatientId(
                appointment
                        .getPatient()
                        .getId()
        );

        form.setDentistId(
                appointment
                        .getDentist()
                        .getId()
        );

        form.setTreatmentId(
                appointment
                        .getTreatment()
                        .getId()
        );

        form.setAppointmentDate(
                appointment
                        .getAppointmentDate()
        );

        form.setAppointmentTime(
                appointment
                        .getAppointmentTime()
        );

        form.setStatus(
                appointment
                        .getStatus()
        );

        form.setNotes(
                appointment
                        .getNotes()
        );

        return form;
    }

    private String normaliseNotes(
            String notes
    ) {

        if (notes == null) {
            return null;
        }

        String trimmedNotes =
                notes.trim();

        return trimmedNotes.isEmpty()
                ? null
                : trimmedNotes;
    }
}