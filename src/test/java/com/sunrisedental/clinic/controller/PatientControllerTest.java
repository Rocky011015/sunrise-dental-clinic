package com.sunrisedental.clinic.controller;

import com.sunrisedental.clinic.domain.Patient;
import com.sunrisedental.clinic.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class PatientControllerTest {

    private PatientService patientService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        patientService = mock(PatientService.class);

        PatientController patientController =
                new PatientController(patientService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(patientController)
                .build();
    }

    @Test
    void shouldDisplayPatientList() throws Exception {
        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("fullName").ascending()
        );

        when(patientService.searchPatients("", pageable))
                .thenReturn(Page.empty(pageable));

        mockMvc.perform(get("/patients")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(model().attribute("searchTerm", ""));

        verify(patientService).searchPatients("", pageable);
    }
    @Test
    void shouldDisplayNewPatientForm() throws Exception {
        mockMvc.perform(get("/patients/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attributeExists("patient"));
    }
    @Test
    void shouldRejectInvalidPatientForm() throws Exception {
        mockMvc.perform(post("/patients")
                        .param("patientCode", "")
                        .param("fullName", "")
                        .param("address", "")
                        .param("contactNumber", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attributeHasFieldErrors(
                        "patient",
                        "patientCode",
                        "fullName",
                        "address",
                        "contactNumber"
                ));
    }
    @Test
    void shouldCreatePatientAndRedirectToList() throws Exception {
        mockMvc.perform(post("/patients")
                        .param("patientCode", "P001")
                        .param("fullName", "John Silva")
                        .param("address", "Colombo")
                        .param("contactNumber", "0771234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));

        verify(patientService).createPatient(any(Patient.class));
    }
    @Test
    void shouldDisplayPatientDetails() throws Exception {
        Patient patient = new Patient();
        patient.setPatientCode("P001");
        patient.setFullName("John Silva");
        patient.setAddress("Colombo");
        patient.setContactNumber("0771234567");

        when(patientService.getPatientById(1L))
                .thenReturn(patient);

        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/details"))
                .andExpect(model().attribute("patient", patient));

        verify(patientService).getPatientById(1L);
    }
    @Test
    void shouldDeletePatientAndRedirectToList() throws Exception {
        mockMvc.perform(post("/patients/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));

        verify(patientService).deletePatient(1L);
    }
    @Test
    void shouldDisplayEditPatientForm() throws Exception {
        Patient patient = new Patient();
        patient.setPatientCode("P001");
        patient.setFullName("John Silva");
        patient.setAddress("Colombo");
        patient.setContactNumber("0771234567");

        when(patientService.getPatientById(1L))
                .thenReturn(patient);

        mockMvc.perform(get("/patients/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attribute("patient", patient));

        verify(patientService).getPatientById(1L);
    }
}