package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.Dentist;
import com.sunrisedental.clinic.repository.DentistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class DentistServiceTest {

    @Mock
    private DentistRepository dentistRepository;

    @Mock
    private Dentist dentist;

    private DentistService dentistService;

    @BeforeEach
    void setUp() {
        dentistService =
                new DentistService(dentistRepository);
    }

    @Test
    void shouldCreateDentistWhenCodeIsUnique() {

        when(dentist.getDentistCode())
                .thenReturn(" den-001 ");

        when(dentistRepository
                .existsByDentistCodeIgnoreCase("DEN-001"))
                .thenReturn(false);

        when(dentistRepository.save(dentist))
                .thenReturn(dentist);

        Dentist savedDentist =
                dentistService.createDentist(dentist);

        assertSame(dentist, savedDentist);

        verify(dentist)
                .setDentistCode("DEN-001");

        verify(dentistRepository)
                .save(dentist);
    }

    @Test
    void shouldRejectDuplicateDentistCode() {

        when(dentist.getDentistCode())
                .thenReturn("DEN-001");

        when(dentistRepository
                .existsByDentistCodeIgnoreCase("DEN-001"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                dentistService
                                        .createDentist(dentist)
                );

        assertTrue(
                exception.getMessage()
                        .contains("DEN-001")
        );

        verify(dentistRepository, never())
                .save(any(Dentist.class));
    }

    @Test
    void shouldRejectDentistWithBlankCode() {

        when(dentist.getDentistCode())
                .thenReturn("   ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                dentistService
                                        .createDentist(dentist)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Dentist code is required")
        );

        verify(dentistRepository, never())
                .save(any(Dentist.class));
    }

    @Test
    void shouldReturnDentistWhenIdExists() {

        when(dentistRepository.findById(1L))
                .thenReturn(Optional.of(dentist));

        Dentist result =
                dentistService.getDentistById(1L);

        assertSame(dentist, result);
    }

    @Test
    void shouldRejectMissingDentistId() {

        when(dentistRepository.findById(99L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () ->
                                dentistService
                                        .getDentistById(99L)
                );

        assertTrue(
                exception.getMessage()
                        .contains("99")
        );
    }

    @Test
    void shouldReturnAllDentistsWhenSearchIsBlank() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Dentist> expectedPage =
                new PageImpl<>(
                        List.of(dentist)
                );

        when(dentistRepository.findAll(pageable))
                .thenReturn(expectedPage);

        Page<Dentist> result =
                dentistService.searchDentists(
                        "   ",
                        pageable
                );

        assertSame(expectedPage, result);

        verify(dentistRepository)
                .findAll(pageable);
    }

    @Test
    void shouldSearchByCodeNameOrSpecialization() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Dentist> expectedPage =
                new PageImpl<>(
                        List.of(dentist)
                );

        when(
                dentistRepository
                        .findByDentistCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrSpecializationContainingIgnoreCase(
                                "nimal",
                                "nimal",
                                "nimal",
                                pageable
                        )
        ).thenReturn(expectedPage);

        Page<Dentist> result =
                dentistService.searchDentists(
                        " nimal ",
                        pageable
                );

        assertSame(expectedPage, result);
    }

    @Test
    void shouldUpdateExistingDentist() {

        Dentist existingDentist =
                new Dentist();

        Dentist updatedDentist =
                new Dentist();

        updatedDentist.setDentistCode(
                " den-002 "
        );

        updatedDentist.setFullName(
                "Dr. Kasun Silva"
        );

        updatedDentist.setSpecialization(
                "Oral Surgery"
        );

        updatedDentist.setConsultationFee(
                new BigDecimal("6500.00")
        );

        updatedDentist.setActive(true);

        when(dentistRepository.findById(1L))
                .thenReturn(
                        Optional.of(existingDentist)
                );

        when(
                dentistRepository
                        .existsByDentistCodeIgnoreCaseAndIdNot(
                                "DEN-002",
                                1L
                        )
        ).thenReturn(false);

        when(
                dentistRepository.save(
                        existingDentist
                )
        ).thenReturn(existingDentist);

        Dentist result =
                dentistService.updateDentist(
                        1L,
                        updatedDentist
                );

        assertSame(existingDentist, result);

        assertEquals(
                "DEN-002",
                existingDentist.getDentistCode()
        );

        assertEquals(
                "Dr. Kasun Silva",
                existingDentist.getFullName()
        );

        assertEquals(
                "Oral Surgery",
                existingDentist.getSpecialization()
        );

        assertEquals(
                new BigDecimal("6500.00"),
                existingDentist.getConsultationFee()
        );

        assertTrue(
                existingDentist.isActive()
        );

        verify(dentistRepository)
                .save(existingDentist);
    }

    @Test
    void shouldRejectDuplicateCodeWhenUpdatingDentist() {

        Dentist existingDentist =
                new Dentist();

        Dentist updatedDentist =
                new Dentist();

        updatedDentist.setDentistCode(
                "DEN-003"
        );

        when(dentistRepository.findById(1L))
                .thenReturn(
                        Optional.of(existingDentist)
                );

        when(
                dentistRepository
                        .existsByDentistCodeIgnoreCaseAndIdNot(
                                "DEN-003",
                                1L
                        )
        ).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                dentistService
                                        .updateDentist(
                                                1L,
                                                updatedDentist
                                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("DEN-003")
        );

        verify(dentistRepository, never())
                .save(any(Dentist.class));
    }

    @Test
    void shouldDeactivateDentist() {

        Dentist existingDentist =
                new Dentist();

        existingDentist.setActive(true);

        when(dentistRepository.findById(1L))
                .thenReturn(
                        Optional.of(existingDentist)
                );

        when(
                dentistRepository.save(
                        existingDentist
                )
        ).thenReturn(existingDentist);

        Dentist result =
                dentistService.setDentistActive(
                        1L,
                        false
                );

        assertSame(existingDentist, result);

        assertFalse(result.isActive());

        verify(dentistRepository)
                .save(existingDentist);
    }
}