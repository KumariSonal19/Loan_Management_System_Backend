package com.lms.loan.client;

import com.lms.loan.dto.LoanTypeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "admin-service")
public interface AdminClient {
    @GetMapping("/api/admin/loan-types/{id}")
    LoanTypeDTO getLoanTypeById(@PathVariable("id") Long id);
}