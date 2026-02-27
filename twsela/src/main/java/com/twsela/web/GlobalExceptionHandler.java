package com.twsela.web;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.twsela.util.AppUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return AppUtils.validationError(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        return AppUtils.error("Invalid input data");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        return AppUtils.unauthorized("Authentication failed");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        return AppUtils.unauthorized("Invalid credentials");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        return AppUtils.forbidden("Access denied");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        return AppUtils.error(ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElementException(
            NoSuchElementException ex, WebRequest request) {
        
        log.warn("NoSuchElementException: {}", ex.getMessage());
        return AppUtils.error(HttpStatus.NOT_FOUND, "البيانات المطلوبة غير موجودة في النظام");
    }

    // ===== New exception handlers added in Sprint 3 =====

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        log.warn("Method not supported: {} for {}", ex.getMethod(), request.getDescription(false));
        return AppUtils.error(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method غير مدعوم: " + ex.getMethod());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        return AppUtils.error(HttpStatus.BAD_REQUEST, "معامل مطلوب مفقود: " + ex.getParameterName());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        
        return AppUtils.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "نوع المحتوى غير مدعوم: " + ex.getContentType());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        log.error("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return AppUtils.error(HttpStatus.CONFLICT, "تعارض في البيانات — قد تكون القيمة مكررة أو مرتبطة بسجلات أخرى");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, WebRequest request) {
        
        return AppUtils.error(HttpStatus.PAYLOAD_TOO_LARGE, "حجم الملف يتجاوز الحد المسموح");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        return AppUtils.error(HttpStatus.BAD_REQUEST, "صيغة الطلب غير صالحة — تحقق من JSON المرسل");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        log.warn("RuntimeException: {}", ex.getMessage());
        
        // Check if it's a data initialization error
        if (ex.getMessage() != null && ex.getMessage().contains("غير موجودة")) {
            return AppUtils.error(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        
        // Don't leak internal error details to client
        return AppUtils.error(HttpStatus.BAD_REQUEST, "حدث خطأ أثناء معالجة الطلب");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        // Log the actual exception for debugging
        log.error("Unexpected error", ex);
        
        // CRITICAL FIX: Check if this is an authentication-related error
        String requestPath = request.getDescription(false);
        if (requestPath.contains("/api/auth/login")) {
            // For login endpoint, always return 401 instead of 500
            return AppUtils.unauthorized("Authentication failed");
        }
        
        return AppUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error has occurred");
    }
}
