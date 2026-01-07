package com.lms.authservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_ShouldReturnBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "username", "Username is required");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertEquals("Username is required", details.get("username"));
    }

    @Test
    void handleBusinessExceptions_ShouldReturnBadRequest_ForUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBusinessExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleBusinessExceptions_ShouldReturnBadRequest_ForDuplicateUser() {
       
        DuplicateUserException ex = new DuplicateUserException("Email already exists");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBusinessExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already exists", response.getBody().get("message"));
    }

    @Test
    void handleBusinessExceptions_ShouldReturnBadRequest_ForInvalidCredentials() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Wrong password");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBusinessExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Wrong password", response.getBody().get("message"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("Unexpected database error");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().get("message"));
        assertEquals("Unexpected database error", response.getBody().get("details"));
    }
}