package com.lms.loan.controller;

import com.lms.loan.client.EmiClient;
import com.lms.loan.dto.LoanApplicationDTO;
import com.lms.loan.dto.LoanApprovalRequestDTO;
import com.lms.loan.entity.LoanApplication;
import com.lms.loan.entity.LoanStatus;
import com.lms.loan.repository.LoanApplicationRepository;
import com.lms.loan.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@Slf4j
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService loanApplicationService;

    @Autowired 
    private LoanApplicationRepository loanApplicationRepository;
    
    @Autowired
    private EmiClient emiClient;

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Long>> applyLoan(@RequestHeader("X-User-Id") Long customerId, @Valid @RequestBody LoanApplicationDTO request) {
        
        log.info("Apply loan request from customer: {}", customerId);
        
        LoanApplicationDTO response = loanApplicationService.applyLoan(customerId, request);
        
        Map<String, Long> responseBody = new HashMap<>();
        responseBody.put("loanId", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationDTO> getLoan(@PathVariable Long id) {
        LoanApplicationDTO loan = loanApplicationService.getLoanById(id);
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/customer/list/{customerId}")
    public ResponseEntity<List<LoanApplicationDTO>> getCustomerLoans(@PathVariable Long customerId) {
        List<LoanApplicationDTO> loans = loanApplicationService.getCustomerLoans(customerId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanApplicationDTO>> getLoansByStatus(@PathVariable String status) {
        LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
        List<LoanApplicationDTO> loans = loanApplicationService.getLoansByStatusList(loanStatus);
        return ResponseEntity.ok(loans);
    }

    @PutMapping("/review")
    public ResponseEntity<LoanApplicationDTO> reviewLoan(
            @RequestHeader("X-User-Id") Long officerId,
            @Valid @RequestBody LoanApprovalRequestDTO request) {
        
        log.info("Loan review request - ID: {}, Status Update: {}", request.getLoanId(), request.getStatus());
       
        LoanApplicationDTO response = loanApplicationService.updateLoanStatus(request, officerId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/{status}")
    public ResponseEntity<Map<String, Object>> getLoansCountByStatus(@PathVariable String status) {
        LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
        Long count = loanApplicationService.getLoansCountByStatus(loanStatus);

        Map<String, Object> response = new HashMap<>();
        response.put("status", loanStatus.name());
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Loan Service is running");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotalLoansCount() {
        long count = loanApplicationRepository.count();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getCustomerDashboardSummary(@RequestHeader("X-User-Id") Long customerId) {
        
        List<LoanApplication> allLoans = loanApplicationRepository.findByCustomerId(customerId);

        long totalLoans = allLoans.size();
        long activeLoans = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.APPROVED)
                .count();

        List<Long> approvedLoanIds = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.APPROVED)
                .map(LoanApplication::getId)
                .collect(Collectors.toList());

        BigDecimal totalPendingEmi = BigDecimal.ZERO;
        if (!approvedLoanIds.isEmpty()) {
            try {
                totalPendingEmi = emiClient.getOutstandingBalanceForLoans(approvedLoanIds);
            } catch (Exception e) {
                log.error("Failed to fetch EMI balance", e);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalLoans", totalLoans);
        response.put("activeLoans", activeLoans);
        response.put("pendingEmi", totalPendingEmi);

        return ResponseEntity.ok(response);
    }
    

    @GetMapping("/all")
    public ResponseEntity<List<LoanApplicationDTO>> getAllLoans() {
        List<LoanApplicationDTO> loans = loanApplicationService.getAllLoans(); 
        return ResponseEntity.ok(loans);
    }
}