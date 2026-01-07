package com.lms.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.admin.dto.LoanTypeDTO;
import com.lms.admin.exception.GlobalExceptionHandler;
import com.lms.admin.service.LoanTypeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoanTypeController.class)
@AutoConfigureMockMvc(addFilters = false)   
@Import(GlobalExceptionHandler.class)
class LoanTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanTypeService loanTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoanTypeDTO dto;

    @BeforeEach
    void setup() {
        dto = LoanTypeDTO.builder()
                .id(1L)
                .typeName("Home Loan")
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(5000000))
                .baseInterestRate(BigDecimal.valueOf(8.5))
                .minTenure(12)
                .maxTenure(240)
                .isActive(true)
                .build();
    }


    @Test
    void getAllActiveLoanTypes_success() throws Exception {
        when(loanTypeService.getAllActiveLoanTypes())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/loan-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].typeName").value("Home Loan"));
    }

    @Test
    void getAllLoanTypes_success() throws Exception {
        when(loanTypeService.getAllLoanTypes())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/loan-types/all"))
                .andExpect(status().isOk());
    }

    @Test
    void getLoanTypeById_success() throws Exception {
        when(loanTypeService.getLoanTypeById(1L))
                .thenReturn(dto);

        mockMvc.perform(get("/api/admin/loan-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeName").value("Home Loan"));
    }

    @Test
    void getLoanTypeById_notFound() throws Exception {
        when(loanTypeService.getLoanTypeById(99L))
                .thenThrow(new RuntimeException("Loan type not found"));

        mockMvc.perform(get("/api/admin/loan-types/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLoanTypeByName_success() throws Exception {
        when(loanTypeService.getLoanTypeByName("Home Loan"))
                .thenReturn(dto);

        mockMvc.perform(get("/api/admin/loan-types/name/Home Loan"))
                .andExpect(status().isOk());
    }


    @Test
    void createLoanType_success() throws Exception {
        when(loanTypeService.createLoanType(any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/admin/loan-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createLoanType_validationFailure() throws Exception {
        mockMvc.perform(post("/api/admin/loan-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateLoanType_success() throws Exception {
        when(loanTypeService.updateLoanType(eq(1L), any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/admin/loan-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateLoanType_validationFailure() throws Exception {
        mockMvc.perform(put("/api/admin/loan-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateLoanType_success() throws Exception {
        when(loanTypeService.deactivateLoanType(1L))
                .thenReturn(dto);

        mockMvc.perform(put("/api/admin/loan-types/1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    void activateLoanType_success() throws Exception {
        when(loanTypeService.activateLoanType(1L))
                .thenReturn(dto);

        mockMvc.perform(put("/api/admin/loan-types/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteLoanType_success() throws Exception {
        doNothing().when(loanTypeService).deleteLoanType(1L);

        mockMvc.perform(delete("/api/admin/loan-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Loan type deleted successfully"));
    }

   

    @Test
    void health_success() throws Exception {
        mockMvc.perform(get("/api/admin/loan-types/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin Service is running"));
    }
}
