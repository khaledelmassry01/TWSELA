package com.twsela.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Unified Application Utilities
 * Consolidates all utility functions to eliminate code duplication
 * 
 * This class combines:
 * - Response handling utilities
 * - DateTime operations
 * - Validation patterns
 * 
 * Following DRY principle and separation of concerns
 */
public class AppUtils {
    
    // ==================== RESPONSE UTILITIES ====================
    
    /**
     * Create success response with data
     */
    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }
    
    /**
     * Create success response with message
     */
    public static ResponseEntity<Map<String, Object>> success(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create success response with data and message
     */
    public static <T> ResponseEntity<Map<String, Object>> success(String message, T data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create error response
     */
    public static ResponseEntity<Map<String, Object>> error(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }
    
    /**
     * Create error response with status
     */
    public static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Create not found response
     */
    public static ResponseEntity<Map<String, Object>> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }
    
    /**
     * Create unauthorized response
     */
    public static ResponseEntity<Map<String, Object>> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }
    
    /**
     * Create forbidden response
     */
    public static ResponseEntity<Map<String, Object>> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message);
    }
    
    /**
     * Create validation error response
     */
    public static ResponseEntity<Map<String, Object>> validationError(Map<String, String> errors) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Create no content response
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
    
    // ==================== DATE/TIME UTILITIES ====================
    
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    
    /**
     * Get current timestamp
     */
    public static Instant now() {
        return Instant.now();
    }
    
    /**
     * Get current date
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
    
    /**
     * Get start of today as Instant
     */
    public static Instant todayStart() {
        return LocalDate.now().atStartOfDay().atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get end of today as Instant
     */
    public static Instant todayEnd() {
        return LocalDate.now().plusDays(1).atStartOfDay().atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get start of month as Instant
     */
    public static Instant monthStart() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay().atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get start of date range as Instant
     */
    public static Instant toInstant(LocalDate date) {
        return date.atStartOfDay().atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get end of date range as Instant
     */
    public static Instant toInstantEnd(LocalDate date) {
        return date.plusDays(1).atStartOfDay().atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Check if instant is within date range
     */
    public static boolean isWithinRange(Instant instant, LocalDate startDate, LocalDate endDate) {
        Instant start = toInstant(startDate);
        Instant end = toInstantEnd(endDate);
        return instant.isAfter(start) && instant.isBefore(end);
    }
    
    /**
     * Check if instant is today
     */
    public static boolean isToday(Instant instant) {
        return isWithinRange(instant, LocalDate.now(), LocalDate.now());
    }
    
    /**
     * Check if instant is this month
     */
    public static boolean isThisMonth(Instant instant) {
        LocalDate now = LocalDate.now();
        return isWithinRange(instant, now.withDayOfMonth(1), now);
    }
    
    // ==================== VALIDATION UTILITIES ====================
    
    // Phone number validation pattern
    public static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    
    // OTP validation pattern
    public static final Pattern OTP_PATTERN = Pattern.compile("^[0-9]{6}$");
    
    // Password validation pattern
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );
    
    // Name validation pattern (Arabic and English letters)
    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\u0600-\\u06FF\\s]+$");
    
    /**
     * Validates phone number format
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validates OTP format
     */
    public static boolean isValidOtp(String otp) {
        return otp != null && OTP_PATTERN.matcher(otp).matches();
    }
    
    /**
     * Validates password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validates name format
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }
    
    /**
     * Validates password confirmation
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}
