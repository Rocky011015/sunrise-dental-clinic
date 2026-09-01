package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.domain.Treatment;
import com.sunrisedental.clinic.repository.TreatmentRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceTest {

    @Mock
    private TreatmentRepository treatmentRepository;

    private TreatmentService treatmentService;

    @BeforeEach
    void setUp() {
        treatmentService =
                new TreatmentService(treatmentRepository);
    }

    @Test
    void shouldCreateTreatmentWhenCodeIsUnique() {

        Treatment treatment = new Treatment();

        treatment.setTreatmentCode(" trt-006 ");
        treatment.setTreatmentName("Root Canal Treatment");
        treatment.setDescription(
                "Root canal treatment procedure"
        );
        treatment.setBasePrice(
                new BigDecimal("15000.00")
        );
        treatment.setEstimatedDurationMinutes(60);
        treatment.setActive(true);

        when(treatmentRepository
                .existsByTreatmentCodeIgnoreCase(
                        "TRT-006"
                ))
                .thenReturn(false);

        when(treatmentRepository.save(treatment))
                .thenReturn(treatment);

        Treatment result =
                treatmentService.createTreatment(treatment);

        assertSame(treatment, result);

        assertEquals(
                "TRT-006",
                result.getTreatmentCode()
        );

        verify(treatmentRepository)
                .save(treatment);
    }

    @Test
    void shouldRejectDuplicateTreatmentCode() {

        Treatment treatment = new Treatment();

        treatment.setTreatmentCode("TRT-001");

        when(treatmentRepository
                .existsByTreatmentCodeIgnoreCase(
                        "TRT-001"
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> treatmentService
                                .createTreatment(treatment)
                );

        assertTrue(
                exception.getMessage()
                        .contains("TRT-001")
        );

        verify(
                treatmentRepository,
                never()
        ).save(treatment);
    }

    @Test
    void shouldRejectBlankTreatmentCode() {

        Treatment treatment = new Treatment();

        treatment.setTreatmentCode("   ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> treatmentService
                                .createTreatment(treatment)
                );

        assertTrue(
                exception.getMessage()
                        .contains("required")
        );
    }

    @Test
    void shouldReturnTreatmentWhenIdExists() {

        Treatment treatment = new Treatment();

        when(treatmentRepository.findById(1L))
                .thenReturn(
                        Optional.of(treatment)
                );

        Treatment result =
                treatmentService.getTreatmentById(1L);

        assertSame(treatment, result);
    }

    @Test
    void shouldRejectMissingTreatmentId() {

        when(treatmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> treatmentService
                                .getTreatmentById(99L)
                );

        assertTrue(
                exception.getMessage()
                        .contains("99")
        );
    }

    @Test
    void shouldReturnTreatmentByCode() {

        Treatment treatment = new Treatment();

        treatment.setTreatmentCode("TRT-001");

        when(treatmentRepository
                .findByTreatmentCodeIgnoreCase(
                        "trt-001"
                ))
                .thenReturn(
                        Optional.of(treatment)
                );

        Treatment result =
                treatmentService
                        .getTreatmentByCode(
                                " trt-001 "
                        );

        assertSame(treatment, result);
    }

    @Test
    void shouldSearchTreatments() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Treatment treatment =
                new Treatment();

        Page<Treatment> expectedPage =
                new PageImpl<>(
                        List.of(treatment)
                );

        when(treatmentRepository
                .findByTreatmentCodeContainingIgnoreCaseOrTreatmentNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "clean",
                        "clean",
                        "clean",
                        pageable
                ))
                .thenReturn(expectedPage);

        Page<Treatment> result =
                treatmentService.searchTreatments(
                        " clean ",
                        pageable
                );

        assertSame(expectedPage, result);
    }

    @Test
    void shouldReturnAllTreatmentsWhenSearchIsBlank() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Treatment treatment =
                new Treatment();

        Page<Treatment> expectedPage =
                new PageImpl<>(
                        List.of(treatment)
                );

        when(treatmentRepository
                .findAll(pageable))
                .thenReturn(expectedPage);

        Page<Treatment> result =
                treatmentService.searchTreatments(
                        "   ",
                        pageable
                );

        assertSame(expectedPage, result);

        verify(treatmentRepository)
                .findAll(pageable);
    }

    @Test
    void shouldReturnActiveTreatments() {

        Treatment first =
                new Treatment();

        Treatment second =
                new Treatment();

        List<Treatment> expected =
                List.of(first, second);

        when(treatmentRepository
                .findByActiveTrueOrderByTreatmentNameAsc())
                .thenReturn(expected);

        List<Treatment> result =
                treatmentService
                        .getActiveTreatments();

        assertSame(expected, result);
    }

    @Test
    void shouldUpdateExistingTreatment() {

        Treatment existingTreatment =
                new Treatment();

        existingTreatment
                .setTreatmentCode("TRT-001");

        existingTreatment
                .setTreatmentName("Old Treatment");

        existingTreatment
                .setDescription("Old description");

        existingTreatment
                .setBasePrice(
                        new BigDecimal("2500.00")
                );

        existingTreatment
                .setEstimatedDurationMinutes(30);

        existingTreatment.setActive(true);


        Treatment updatedTreatment =
                new Treatment();

        updatedTreatment
                .setTreatmentCode(" trt-001 ");

        updatedTreatment
                .setTreatmentName(
                        "Updated Dental Treatment"
                );

        updatedTreatment
                .setDescription(
                        "Updated description"
                );

        updatedTreatment
                .setBasePrice(
                        new BigDecimal("4500.00")
                );

        updatedTreatment
                .setEstimatedDurationMinutes(45);

        updatedTreatment.setActive(true);


        when(treatmentRepository.findById(1L))
                .thenReturn(
                        Optional.of(
                                existingTreatment
                        )
                );

        when(treatmentRepository
                .existsByTreatmentCodeIgnoreCaseAndIdNot(
                        "TRT-001",
                        1L
                ))
                .thenReturn(false);

        when(treatmentRepository
                .save(existingTreatment))
                .thenReturn(existingTreatment);


        Treatment result =
                treatmentService.updateTreatment(
                        1L,
                        updatedTreatment
                );


        assertSame(
                existingTreatment,
                result
        );

        assertEquals(
                "TRT-001",
                result.getTreatmentCode()
        );

        assertEquals(
                "Updated Dental Treatment",
                result.getTreatmentName()
        );

        assertEquals(
                "Updated description",
                result.getDescription()
        );

        assertEquals(
                new BigDecimal("4500.00"),
                result.getBasePrice()
        );

        assertEquals(
                45,
                result.getEstimatedDurationMinutes()
        );

        assertTrue(result.isActive());

        verify(treatmentRepository)
                .save(existingTreatment);
    }

    @Test
    void shouldRejectDuplicateCodeWhenUpdating() {

        Treatment existingTreatment =
                new Treatment();

        existingTreatment
                .setTreatmentCode("TRT-001");

        Treatment updatedTreatment =
                new Treatment();

        updatedTreatment
                .setTreatmentCode("TRT-002");

        when(treatmentRepository.findById(1L))
                .thenReturn(
                        Optional.of(
                                existingTreatment
                        )
                );

        when(treatmentRepository
                .existsByTreatmentCodeIgnoreCaseAndIdNot(
                        "TRT-002",
                        1L
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> treatmentService
                                .updateTreatment(
                                        1L,
                                        updatedTreatment
                                )
                );

        assertTrue(
                exception.getMessage()
                        .contains("TRT-002")
        );

        verify(
                treatmentRepository,
                never()
        ).save(existingTreatment);
    }

    @Test
    void shouldDeactivateTreatment() {

        Treatment treatment =
                new Treatment();

        treatment.setActive(true);

        when(treatmentRepository.findById(1L))
                .thenReturn(
                        Optional.of(treatment)
                );

        when(treatmentRepository.save(treatment))
                .thenReturn(treatment);

        Treatment result =
                treatmentService
                        .setTreatmentActive(
                                1L,
                                false
                        );

        assertSame(treatment, result);

        assertFalse(result.isActive());

        verify(treatmentRepository)
                .save(treatment);
    }

    @Test
    void shouldActivateTreatment() {

        Treatment treatment =
                new Treatment();

        treatment.setActive(false);

        when(treatmentRepository.findById(1L))
                .thenReturn(
                        Optional.of(treatment)
                );

        when(treatmentRepository.save(treatment))
                .thenReturn(treatment);

        Treatment result =
                treatmentService
                        .setTreatmentActive(
                                1L,
                                true
                        );

        assertSame(treatment, result);

        assertTrue(result.isActive());

        verify(treatmentRepository)
                .save(treatment);
    }
}