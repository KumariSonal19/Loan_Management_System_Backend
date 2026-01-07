package com.lms.admin.controller;

import com.lms.admin.client.LoanClient;
import com.lms.admin.dto.LoanApplicationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/loans")
public class AdminLoanController {

    @Autowired
    private LoanClient loanClient;

    @GetMapping
    public ResponseEntity<List<LoanApplicationDTO>> getAllLoans() {
        return ResponseEntity.ok(loanClient.getAllLoans());
    }
}