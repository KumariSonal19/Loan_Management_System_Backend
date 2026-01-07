package com.lms.loan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.AllArgsConstructor;

@FeignClient(name = "emi-service")
public interface EmiClient {

    @PostMapping("/api/emis/generate")
    void generateEMISchedule(@RequestBody EMIGenerationRequest request);

    @PostMapping("/api/emis/total-outstanding-batch")
    BigDecimal getOutstandingBalanceForLoans(@RequestBody List<Long> loanIds);
    
    @Data
    @AllArgsConstructor
    class EMIGenerationRequest {
        private Long loanApplicationId;
        private BigDecimal principal;
        private BigDecimal annualRate;
        private Integer months;
    }
}