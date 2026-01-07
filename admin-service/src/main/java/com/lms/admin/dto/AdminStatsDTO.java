package com.lms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private long totalLoans;
    private long approvedLoans;
    private long pendingLoans;
    private long rejectedLoans;
    
    private long totalUsers;
    private long activeUsers;
    
    private double totalCollected;
    private double pendingCollections;
}