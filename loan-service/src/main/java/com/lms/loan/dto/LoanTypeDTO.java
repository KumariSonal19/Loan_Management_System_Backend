package com.lms.loan.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanTypeDTO {
    private Long id;
    private String typeName;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal baseInterestRate;
    private Integer minTenure;
    private Integer maxTenure;
}