package com.lms.loan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lms.loan.dto.LoanStatsDTO;
import com.lms.loan.entity.LoanStatus;
import com.lms.loan.repository.LoanApplicationRepository;

@RestController
@RequestMapping("/api/loans/stats")
public class LoanStatsController {

    @Autowired
    private LoanApplicationRepository loanRepo;

    @GetMapping
    public ResponseEntity<LoanStatsDTO> getLoanStats() {
        LoanStatsDTO stats = LoanStatsDTO.builder()
                .totalLoans(loanRepo.count())
                .approvedLoans(loanRepo.countByStatus(LoanStatus.APPROVED))
                .pendingLoans(loanRepo.countByStatus(LoanStatus.APPLIED))
                .rejectedLoans(loanRepo.countByStatus(LoanStatus.REJECTED))
                .totalDisbursed(loanRepo.sumTotalDisbursed())
                .pendingAmount(loanRepo.sumPendingAmount())
                .build();
        return ResponseEntity.ok(stats);
    }
}
