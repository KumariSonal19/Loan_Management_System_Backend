package com.lms.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.loan.client.EmiClient;
import com.lms.loan.dto.LoanApplicationDTO;
import com.lms.loan.dto.LoanApprovalRequestDTO;
import com.lms.loan.entity.LoanApplication; // ✅ Added Missing Import
import com.lms.loan.entity.LoanStatus;
import com.lms.loan.repository.LoanApplicationRepository;
import com.lms.loan.service.LoanApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoanApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanApplicationService loanApplicationService;

    @MockBean
    private LoanApplicationRepository loanApplicationRepository;

    @MockBean
    private EmiClient emiClient;

    @Test
    void health_success() throws Exception {
        mockMvc.perform(get("/api/loans/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan Service is running"));
    }

    @Test
    void applyLoan_success() throws Exception {
        LoanApplicationDTO response = new LoanApplicationDTO();
        response.setId(1L);

        when(loanApplicationService.applyLoan(eq(10L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/loans/apply")
                        .header("X-User-Id", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "loanTypeId": 1,
                          "loanAmount": 100000,
                          "tenure": 12,
                          "annualIncome": 500000,
                          "employmentScore": 85
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").value(1));
    }

    @Test
    void getLoan_success() throws Exception {
        when(loanApplicationService.getLoanById(1L))
                .thenReturn(new LoanApplicationDTO());

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCustomerLoans_success() throws Exception {
        when(loanApplicationService.getCustomerLoans(5L))
                .thenReturn(List.of(new LoanApplicationDTO()));

        mockMvc.perform(get("/api/loans/customer/list/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void getLoansByStatus_success() throws Exception {
        when(loanApplicationService.getLoansByStatusList(LoanStatus.APPLIED))
                .thenReturn(List.of(new LoanApplicationDTO()));

        mockMvc.perform(get("/api/loans/status/APPLIED"))
                .andExpect(status().isOk());
    }

    @Test
    void getLoanCountByStatus_success() throws Exception {
        when(loanApplicationService.getLoansCountByStatus(LoanStatus.APPROVED))
                .thenReturn(3L);

        mockMvc.perform(get("/api/loans/count/APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void getTotalLoans_success() throws Exception {
        when(loanApplicationRepository.count()).thenReturn(10L);

        mockMvc.perform(get("/api/loans/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }
    
    @Test
    void applyLoan_validationFailure() throws Exception {
        mockMvc.perform(post("/api/loans/apply")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
    
    @Test
    void dashboardSummary_noApprovedLoans() throws Exception {
        when(loanApplicationRepository.findByCustomerId(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/loans/dashboard/summary")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLoans").value(0))
                .andExpect(jsonPath("$.activeLoans").value(0))
                .andExpect(jsonPath("$.pendingEmi").value(0));
    }

    @Test
    void dashboardSummary_withApprovedLoans_success() throws Exception {
     
        LoanApplication loan = new LoanApplication();
        loan.setId(10L);
        loan.setStatus(LoanStatus.APPROVED);

        when(loanApplicationRepository.findByCustomerId(1L))
                .thenReturn(List.of(loan));

        when(emiClient.getOutstandingBalanceForLoans(any()))
                .thenReturn(BigDecimal.valueOf(5000));

        mockMvc.perform(get("/api/loans/dashboard/summary")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLoans").value(1))
                .andExpect(jsonPath("$.activeLoans").value(1))
                .andExpect(jsonPath("$.pendingEmi").value(5000));
    }
    
    @Test
    void dashboardSummary_emiFailure_shouldReturnZero() throws Exception {
        LoanApplication loan = new LoanApplication();
        loan.setId(10L);
        loan.setStatus(LoanStatus.APPROVED);

        when(loanApplicationRepository.findByCustomerId(1L))
                .thenReturn(List.of(loan));

        when(emiClient.getOutstandingBalanceForLoans(any()))
                .thenThrow(new RuntimeException("EMI Service Down"));

        mockMvc.perform(get("/api/loans/dashboard/summary")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingEmi").value(0)); 
    }
}