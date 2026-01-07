package com.lms.loan.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanStatsDTO {
    private long totalLoans;
    private long approvedLoans;
    private long pendingLoans;
    private long rejectedLoans;
    private double totalDisbursed;
    private double pendingAmount;
}