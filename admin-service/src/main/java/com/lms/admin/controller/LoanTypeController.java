package com.lms.admin.controller;

import com.lms.admin.dto.LoanTypeDTO;
import com.lms.admin.service.LoanTypeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/loan-types")
@Slf4j
public class LoanTypeController {

    @Autowired
    private LoanTypeService loanTypeService;

    @GetMapping
    public ResponseEntity<List<LoanTypeDTO>> getAllActiveLoanTypes() {
        log.info("GET all active loan types");
        List<LoanTypeDTO> loanTypes = loanTypeService.getAllActiveLoanTypes();
        return ResponseEntity.ok(loanTypes);
    }

    @GetMapping("/all")
    public ResponseEntity<List<LoanTypeDTO>> getAllLoanTypes() {
        List<LoanTypeDTO> loanTypes = loanTypeService.getAllLoanTypes();
        return ResponseEntity.ok(loanTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanTypeDTO> getLoanType(@PathVariable Long id) {
        log.info("GET loan type: {}", id);
        LoanTypeDTO loanType = loanTypeService.getLoanTypeById(id);
        return ResponseEntity.ok(loanType);
    }

    @GetMapping("/name/{typeName}")
    public ResponseEntity<LoanTypeDTO> getLoanTypeByName(@PathVariable String typeName) {
        log.info("GET loan type by name: {}", typeName);
        LoanTypeDTO loanType = loanTypeService.getLoanTypeByName(typeName);
        return ResponseEntity.ok(loanType);
    }

    @PostMapping
    public ResponseEntity<LoanTypeDTO> createLoanType(@Valid @RequestBody LoanTypeDTO request) {
        log.info("CREATE loan type: {}", request.getTypeName());
        LoanTypeDTO response = loanTypeService.createLoanType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanTypeDTO> updateLoanType(
            @PathVariable Long id,
            @Valid @RequestBody LoanTypeDTO request) {
        log.info("UPDATE loan type: {}", id);
        LoanTypeDTO response = loanTypeService.updateLoanType(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<LoanTypeDTO> deactivateLoanType(@PathVariable Long id) {
        log.info("DEACTIVATE loan type: {}", id);
        LoanTypeDTO response = loanTypeService.deactivateLoanType(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<LoanTypeDTO> activateLoanType(@PathVariable Long id) {
        log.info("ACTIVATE loan type: {}", id);
        LoanTypeDTO response = loanTypeService.activateLoanType(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLoanType(@PathVariable Long id) {
        log.info("DELETE loan type: {}", id);
        loanTypeService.deleteLoanType(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Loan type deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Admin Service is running");
    }
}
