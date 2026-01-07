package com.lms.loan.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void runtimeExceptionHandled() {
    	ResponseEntity<Map<String, Object>> res = handler.handleRuntime(new RuntimeException("error"));
        assertEquals(400, res.getStatusCodeValue());
    }
    
}
