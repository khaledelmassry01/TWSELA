package com.twsela.web;

import com.twsela.web.dto.ApiResponse;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.InvalidOperationException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler — HTTP status & response body")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("ResourceNotFoundException → 404")
    void resourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 99L);
        ResponseEntity<ApiResponse<Void>> resp = handler.handleResourceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertFalse(resp.getBody().isSuccess());
    }

    @Test
    @DisplayName("BusinessRuleException → 400")
    void businessRule() {
        BusinessRuleException ex = new BusinessRuleException("Order already placed");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleBusinessRule(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertFalse(resp.getBody().isSuccess());
    }

    @Test
    @DisplayName("DuplicateResourceException → 409")
    void duplicateResource() {
        DuplicateResourceException ex = new DuplicateResourceException("User", "phone", "0501234567");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleDuplicateResource(ex);
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertFalse(resp.getBody().isSuccess());
    }

    @Test
    @DisplayName("InvalidOperationException → 400")
    void invalidOperation() {
        InvalidOperationException ex = new InvalidOperationException("Cannot cancel delivered shipment");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleInvalidOperation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    @DisplayName("IllegalArgumentException → 400")
    void illegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleIllegalArgument(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("bad arg", resp.getBody().getMessage());
    }

    @Test
    @DisplayName("RuntimeException with 'غير موجودة' → 404")
    void runtimeNotFound() {
        RuntimeException ex = new RuntimeException("الشحنة غير موجودة");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleRuntimeException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    @DisplayName("RuntimeException generic → 400")
    void runtimeGeneric() {
        RuntimeException ex = new RuntimeException("some error");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleRuntimeException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    @DisplayName("Generic Exception → 500")
    void genericException() {
        Exception ex = new Exception("unexpected");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleGenericException(ex, webRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    }

    @Test
    @DisplayName("Generic Exception on login path → 401")
    void genericExceptionLoginPath() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/auth/login");
        Exception ex = new Exception("something");
        ResponseEntity<ApiResponse<Void>> resp = handler.handleGenericException(ex, webRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }
}
