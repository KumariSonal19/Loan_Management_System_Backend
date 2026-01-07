package com.lms.admin.service;

import com.lms.admin.dto.LoanTypeDTO;
import com.lms.admin.entity.LoanType;
import com.lms.admin.exception.ResourceNotFoundException;
import com.lms.admin.repository.LoanTypeRepository;
import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class LoanTypeServiceTest {

    @Mock
    private LoanTypeRepository repository;

    @InjectMocks
    private LoanTypeService service;

    private LoanType loanType;
    private LoanTypeDTO dto;

    @BeforeEach
    void setup() {
        loanType = new LoanType();
        loanType.setId(1L);
        loanType.setTypeName("Home Loan");
        loanType.setMinAmount(BigDecimal.valueOf(100000));
        loanType.setMaxAmount(BigDecimal.valueOf(5000000));
        loanType.setBaseInterestRate(BigDecimal.valueOf(8.5));
        loanType.setMinTenure(12);
        loanType.setMaxTenure(240);
        loanType.setIsActive(true);

        dto = LoanTypeDTO.builder()
                .typeName("Home Loan")
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(5000000))
                .baseInterestRate(BigDecimal.valueOf(8.5))
                .minTenure(12)
                .maxTenure(240)
                .build();
    }


    @Test
    void getLoanTypeById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(loanType));

        LoanTypeDTO result = service.getLoanTypeById(1L);

        assertEquals("Home Loan", result.getTypeName());
    }

    @Test
    void getLoanTypeById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getLoanTypeById(1L));
    }

    @Test
    void getLoanTypeByName_notFound() {
        when(repository.findByTypeName("X")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getLoanTypeByName("X"));
    }


    @Test
    void createLoanType_success() {
        when(repository.findByTypeName("Home Loan")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(loanType);

        LoanTypeDTO result = service.createLoanType(dto);

        assertEquals("Home Loan", result.getTypeName());
    }

    @Test
    void createLoanType_duplicateName() {
        when(repository.findByTypeName("Home Loan"))
                .thenReturn(Optional.of(loanType));

        assertThrows(RuntimeException.class,
                () -> service.createLoanType(dto));
    }

    @Test
    void createLoanType_invalidTenure() {
        dto.setMinTenure(24);
        dto.setMaxTenure(12);

        assertThrows(RuntimeException.class,
                () -> service.createLoanType(dto));
    }

    @Test
    void createLoanType_invalidAmount() {
        dto.setMinAmount(BigDecimal.valueOf(500000));
        dto.setMaxAmount(BigDecimal.valueOf(100000));

        assertThrows(RuntimeException.class,
                () -> service.createLoanType(dto));
    }

    @Test
    void updateLoanType_confirmDuplicateName() {
        LoanType existing = new LoanType();
        existing.setTypeName("Old Name");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByTypeNameAndIdNot("Home Loan", 1L))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> service.updateLoanType(1L, dto));
    }

    @Test
    void updateLoanType_invalidTenure() {
        when(repository.findById(1L)).thenReturn(Optional.of(loanType));

        dto.setMinTenure(30);
        dto.setMaxTenure(12);

        assertThrows(RuntimeException.class,
                () -> service.updateLoanType(1L, dto));
    }

    @Test
    void updateLoanType_invalidAmount() {
        when(repository.findById(1L)).thenReturn(Optional.of(loanType));

        dto.setMinAmount(BigDecimal.valueOf(900000));
        dto.setMaxAmount(BigDecimal.valueOf(100000));

        assertThrows(RuntimeException.class,
                () -> service.updateLoanType(1L, dto));
    }

    @Test
    void deactivateLoanType_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(loanType));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoanTypeDTO result = service.deactivateLoanType(1L);

        assertFalse(result.getIsActive());
    }

    @Test
    void activateLoanType_success() {
        loanType.setIsActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(loanType));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoanTypeDTO result = service.activateLoanType(1L);

        assertTrue(result.getIsActive());
    }


    @Test
    void deleteLoanType_success() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteLoanType(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteLoanType_notFound() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> service.deleteLoanType(1L));
    }
}
