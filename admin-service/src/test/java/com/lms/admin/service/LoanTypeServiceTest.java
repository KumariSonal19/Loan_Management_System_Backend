package com.lms.admin.service;

import com.lms.admin.dto.LoanTypeDTO;
import com.lms.admin.entity.LoanType;
import com.lms.admin.exception.ResourceNotFoundException;
import com.lms.admin.repository.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanTypeServiceTest {

    private final LoanTypeRepository loanTypeRepository;

    public LoanTypeDTO createLoanType(LoanTypeDTO dto) {
        LoanType loanType = new LoanType();
        loanType.setTypeName(dto.getTypeName());
        loanType.setMinAmount(dto.getMinAmount());
        loanType.setMaxAmount(dto.getMaxAmount());
        loanType.setBaseInterestRate(dto.getBaseInterestRate());
        loanType.setMinTenure(dto.getMinTenure());
        loanType.setMaxTenure(dto.getMaxTenure());
        loanType.setDescription(dto.getDescription());
        loanType.setIsActive(true); // Default to true on creation

        LoanType savedLoanType = loanTypeRepository.save(loanType);
        return mapToDTO(savedLoanType);
    }

    public LoanTypeDTO updateLoanType(Long id, LoanTypeDTO dto) {
        LoanType loanType = loanTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan type not found with id: " + id));

        loanType.setTypeName(dto.getTypeName());
        loanType.setMinAmount(dto.getMinAmount());
        loanType.setMaxAmount(dto.getMaxAmount());
        loanType.setBaseInterestRate(dto.getBaseInterestRate());
        loanType.setMinTenure(dto.getMinTenure());
        loanType.setMaxTenure(dto.getMaxTenure());
        loanType.setDescription(dto.getDescription());
        
        
        if(dto.getIsActive() != null) {
            loanType.setIsActive(dto.getIsActive());
        }

        return mapToDTO(loanTypeRepository.save(loanType));
    }

    public List<LoanTypeDTO> getAllActiveLoanTypes() {
        return loanTypeRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public LoanTypeDTO getLoanTypeById(Long id) {
        LoanType loanType = loanTypeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan type not found with id: " + id
                        )
                );

        return mapToDTO(loanType);
    }

    public LoanTypeDTO deactivateLoanType(Long id) {
        LoanType loanType = loanTypeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan type not found with id: " + id
                        )
                );

        loanType.setIsActive(false);
        return mapToDTO(loanTypeRepository.save(loanType));
    }

    public void deleteLoanType(Long id) {
        if (!loanTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Loan type not found with id: " + id
            );
        }
        loanTypeRepository.deleteById(id);
    }

    private LoanTypeDTO mapToDTO(LoanType loanType) {
        return LoanTypeDTO.builder()
                .id(loanType.getId())
                .typeName(loanType.getTypeName())
                .minAmount(loanType.getMinAmount())
                .maxAmount(loanType.getMaxAmount())
                .baseInterestRate(loanType.getBaseInterestRate())
                .minTenure(loanType.getMinTenure())
                .maxTenure(loanType.getMaxTenure())
                .description(loanType.getDescription())
                .isActive(loanType.getIsActive())
                .build();
    }
}