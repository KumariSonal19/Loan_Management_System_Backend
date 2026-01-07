package com.lms.emi.controller;

import com.lms.emi.dto.EMIGenerationRequest;
import com.lms.emi.dto.EMIScheduleDTO;
import com.lms.emi.dto.PaymentRequestDTO;
import com.lms.emi.dto.RepaymentDTO;
import com.lms.emi.entity.EMISchedule;
import com.lms.emi.entity.EMIStatus;
import com.lms.emi.repository.EMIScheduleRepository;
import com.lms.emi.service.EMIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emis")
@Slf4j
public class EMIController {

    @Autowired
    private EMIService emiService;

    @Autowired
    private EMIScheduleRepository emiScheduleRepository; 

    @GetMapping("/total-outstanding")
    public ResponseEntity<BigDecimal> getTotalOutstandingEMI() {
        List<EMISchedule> pending = emiScheduleRepository.findByStatus(EMIStatus.PENDING);
        BigDecimal total = pending.stream()
                .map(EMISchedule::getEmiAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/total-overdue")
    public ResponseEntity<BigDecimal> getTotalOverdueEMI() {
        List<EMISchedule> overdue = emiScheduleRepository.findByStatus(EMIStatus.OVERDUE);
        BigDecimal total = overdue.stream()
                .map(EMISchedule::getEmiAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/overdue-count")
    public ResponseEntity<Long> getOverdueEMIsCount() {
        long count = emiScheduleRepository.findByStatus(EMIStatus.OVERDUE).size();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-disbursed")
    public ResponseEntity<BigDecimal> getTotalEMIDisbursed() {
        List<EMISchedule> all = emiScheduleRepository.findAll();
        BigDecimal total = all.stream()
                .map(EMISchedule::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ResponseEntity.ok(total);
    }

    @PostMapping("/generate")
    public ResponseEntity<List<EMIScheduleDTO>> generateEMISchedule(
            @RequestBody EMIGenerationRequest request) {

        List<EMIScheduleDTO> schedule = emiService.generateEMISchedule(
                request.getLoanApplicationId(),
                request.getPrincipal(),
                request.getAnnualRate(),
                request.getMonths(),
                LocalDate.now()  
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    @GetMapping("/schedule/{loanId}")
    public ResponseEntity<List<EMIScheduleDTO>> getEMISchedule(@PathVariable Long loanId) {
        List<EMIScheduleDTO> schedule = emiService.getEMISchedule(loanId);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/repay")
    public ResponseEntity<Map<String, Object>> recordRepayment(@RequestBody PaymentRequestDTO request) {
        
        RepaymentDTO responseDTO = emiService.recordRepayment(
                request.getEmiScheduleId(),
                request.getAmountPaid(),
                request.getPaymentMode()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment of " + request.getAmountPaid() + " received successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outstanding/{loanId}")
    public ResponseEntity<BigDecimal> getOutstandingBalance(@PathVariable Long loanId) {
        BigDecimal outstanding = emiService.getOutstandingBalance(loanId);
        return ResponseEntity.ok(outstanding);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<EMIScheduleDTO>> getOverdueEMIs() {
        List<EMIScheduleDTO> overdueEMIs = emiService.getOverdueEMIs();
        return ResponseEntity.ok(overdueEMIs);
    }


    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("EMI Service is running");
    }
    
 
    @PostMapping("/total-outstanding-batch")
    public ResponseEntity<BigDecimal> getTotalOutstandingByLoanIds(@RequestBody List<Long> loanIds) {
        if (loanIds == null || loanIds.isEmpty()) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }
        
        BigDecimal total = emiScheduleRepository.sumPendingAmountByLoanIds(loanIds);
        return ResponseEntity.ok(total);
    }
}
