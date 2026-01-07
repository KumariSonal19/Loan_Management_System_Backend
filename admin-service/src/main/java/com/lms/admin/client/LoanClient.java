package com.lms.admin.client;

import com.lms.admin.dto.AdminStatsDTO; 
import com.lms.admin.dto.LoanApplicationDTO;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "loan-service") 
public interface LoanClient {
    
    @GetMapping("/api/loans/stats")
    AdminStatsDTO getLoanStats(); 
 
    @GetMapping("/api/loans/all")
    List<LoanApplicationDTO> getAllLoans(); 
}