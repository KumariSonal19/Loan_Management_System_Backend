package com.lms.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lms.admin.client.LoanClient;
import com.lms.admin.dto.AdminStatsDTO;

@Service
public class DashboardService {

    @Autowired
    private LoanClient loanClient;

    public AdminStatsDTO getAdminStats() {
        
        AdminStatsDTO loanStats = loanClient.getLoanStats();
        
        return AdminStatsDTO.builder()
                .totalLoans(loanStats.getTotalLoans())
                .approvedLoans(loanStats.getApprovedLoans())
                .pendingLoans(loanStats.getPendingLoans())
                .rejectedLoans(loanStats.getRejectedLoans())
                .totalCollected(loanStats.getTotalCollected()) 
                .pendingCollections(loanStats.getPendingCollections())
                .totalUsers(10) 
                .activeUsers(5)
                .build();
    }
}
