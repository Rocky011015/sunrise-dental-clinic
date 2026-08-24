package com.sunrisedental.clinic.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}