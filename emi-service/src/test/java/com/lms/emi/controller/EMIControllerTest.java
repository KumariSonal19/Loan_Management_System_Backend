package com.lms.emi.controller;

import com.lms.emi.dto.EMIScheduleDTO;
import com.lms.emi.dto.PaymentRequestDTO;
import com.lms.emi.dto.RepaymentDTO;
import com.lms.emi.entity.EMISchedule;
import com.lms.emi.entity.EMIStatus;
import com.lms.emi.repository.EMIScheduleRepository;
import com.lms.emi.service.EMIService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EMIController.class)
@AutoConfigureMockMvc(addFilters = false)
class EMIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EMIService emiService;

    @MockBean
    private EMIScheduleRepository emiScheduleRepository;

    @Test
    void healthCheck_success() throws Exception {
        mockMvc.perform(get("/api/emis/health"))
                .andExpect(status().isOk());
    }

    @Test
    void getTotalOutstanding_success() throws Exception {
        when(emiScheduleRepository.findByStatus(EMIStatus.PENDING))
                .thenReturn(List.of(buildSchedule("1000"), buildSchedule("2000")));

        mockMvc.perform(get("/api/emis/total-outstanding"))
                .andExpect(status().isOk())
                .andExpect(content().string("3000"));
    }

    @Test
    void getTotalOverdue_success() throws Exception {
        when(emiScheduleRepository.findByStatus(EMIStatus.OVERDUE))
                .thenReturn(List.of(buildSchedule("1500")));

        mockMvc.perform(get("/api/emis/total-overdue"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500"));
    }

    @Test
    void getOverdueCount_success() throws Exception {
        when(emiScheduleRepository.findByStatus(EMIStatus.OVERDUE))
                .thenReturn(List.of(new EMISchedule(), new EMISchedule()));

        mockMvc.perform(get("/api/emis/overdue-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void getTotalDisbursed_success() throws Exception {
        EMISchedule s1 = new EMISchedule();
        s1.setPrincipalAmount(new BigDecimal("5000"));

        EMISchedule s2 = new EMISchedule();
        s2.setPrincipalAmount(new BigDecimal("3000"));

        when(emiScheduleRepository.findAll()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/emis/total-disbursed"))
                .andExpect(status().isOk())
                .andExpect(content().string("8000"));
    }

    @Test
    void generateEMISchedule_success() throws Exception {
        when(emiService.generateEMISchedule(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new EMIScheduleDTO()));

        mockMvc.perform(post("/api/emis/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "loanApplicationId": 1,
                          "principal": 10000,
                          "annualRate": 12,
                          "months": 12
                        }
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    void recordRepayment_success() throws Exception {
        when(emiService.recordRepayment(any(), any(), any()))
                .thenReturn(new RepaymentDTO());

        mockMvc.perform(post("/api/emis/repay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "emiScheduleId": 1,
                          "amountPaid": 5000,
                          "paymentMode": "UPI"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getOutstandingBalance_success() throws Exception {
        when(emiService.getOutstandingBalance(1L))
                .thenReturn(new BigDecimal("4500"));

        mockMvc.perform(get("/api/emis/outstanding/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("4500"));
    }

    @Test
    void getOverdueEMIs_success() throws Exception {
        when(emiService.getOverdueEMIs())
                .thenReturn(List.of(new EMIScheduleDTO()));

        mockMvc.perform(get("/api/emis/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    private EMISchedule buildSchedule(String amount) {
        EMISchedule s = new EMISchedule();
        s.setEmiAmount(new BigDecimal(amount));
        return s;
    }
}
