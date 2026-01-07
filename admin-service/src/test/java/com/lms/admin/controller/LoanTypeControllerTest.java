package com.lms.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.admin.dto.LoanTypeDTO;
import com.lms.admin.exception.GlobalExceptionHandler;
import com.lms.admin.service.LoanTypeService;
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

    private LoanTypeDTO sampleDTO() {
        return LoanTypeDTO.builder()
                .id(1L)
                .typeName("Home Loan")
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(5000000))
                .baseInterestRate(BigDecimal.valueOf(8.5))
                .minTenure(12)
                .maxTenure(240)
                .description("Housing loan")
                .isActive(true)
                .build();
    }

    @Test
    void getAllActiveLoanTypes_success() throws Exception {
        Mockito.when(loanTypeService.getAllActiveLoanTypes())
                .thenReturn(List.of(sampleDTO()));

        mockMvc.perform(get("/api/admin/loan-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].typeName").value("Home Loan"));
    }

    @Test
    void getLoanTypeById_success() throws Exception {
        Mockito.when(loanTypeService.getLoanTypeById(1L))
                .thenReturn(sampleDTO());

        mockMvc.perform(get("/api/admin/loan-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeName").value("Home Loan"));
    }

    @Test
    void createLoanType_success() throws Exception {
        Mockito.when(loanTypeService.createLoanType(Mockito.any(LoanTypeDTO.class)))
                .thenReturn(sampleDTO());

        mockMvc.perform(post("/api/admin/loan-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeName").value("Home Loan"));
    }

    @Test
    void createLoanType_validationFailure() throws Exception {
        LoanTypeDTO invalid = new LoanTypeDTO();

        mockMvc.perform(post("/api/admin/loan-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deleteLoanType_success() throws Exception {
        Mockito.doNothing().when(loanTypeService).deleteLoanType(1L);

        mockMvc.perform(delete("/api/admin/loan-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Loan type deleted successfully"));
    }

    @Test
    void healthCheck_success() throws Exception {
        mockMvc.perform(get("/api/admin/loan-types/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin Service is running"));
    }
}
