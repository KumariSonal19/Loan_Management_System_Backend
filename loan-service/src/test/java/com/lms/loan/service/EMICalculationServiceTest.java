package com.lms.loan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class EMICalculationServiceTest {

    private final EMICalculationService service = new EMICalculationService();

    @Test
    void calculateEMI_success() {
        BigDecimal emi = service.calculateEMI(
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(12),
                12
        );
        assertNotNull(emi);
    }

    @Test
    void calculateEMI_invalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateEMI(null, BigDecimal.TEN, 12));
    }

    @Test
    void calculateTotalAmount_success() {
        BigDecimal total = service.calculateTotalAmount(
                BigDecimal.valueOf(1000), 12);
        assertEquals(BigDecimal.valueOf(12000).setScale(2), total);
    }

    @Test
    void calculateTotalInterest_success() {
        BigDecimal interest = service.calculateTotalInterest(
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(12000));
        assertEquals(BigDecimal.valueOf(2000).setScale(2), interest);
    }
}

