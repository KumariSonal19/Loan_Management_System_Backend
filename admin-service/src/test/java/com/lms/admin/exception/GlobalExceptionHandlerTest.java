package com.lms.admin.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

  @Test
    void handleResourceNotFoundException_success() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/test");

        var response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Not found"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
