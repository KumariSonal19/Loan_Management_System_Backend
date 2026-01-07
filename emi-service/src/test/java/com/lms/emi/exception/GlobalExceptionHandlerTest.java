package com.lms.emi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void handleRuntimeException_success() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException ex = new RuntimeException("Test error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleRuntimeException(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Test error", response.getBody().get("message"));
    }
}
