package com.lms.admin.dto;

import com.lms.admin.entity.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDTO {

    private Long id;
    private Long loanTypeId;
    private BigDecimal loanAmount;
    private Integer tenure;
    private BigDecimal annualIncome;
    private BigDecimal employmentScore;
    
    private LoanStatus status; 
    
    private BigDecimal approvedAmount;
    private BigDecimal approvedInterestRate;
    private String approvalRemarks;
    private LocalDateTime appliedDate;
    private LocalDateTime approvalDate;
    private LocalDateTime closedDate;
}