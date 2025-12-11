package com.twsela.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Twilio SMS Service Implementation
 * Sends SMS using Twilio API
 */
@Service
@Primary
@ConditionalOnProperty(
    name = "sms.provider", 
    havingValue = "twilio",
    matchIfMissing = true
)
public class TwilioSmsService implements SmsService {
    
    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);
    
    @Value("${twilio.account-sid:}")
    private String accountSid;
    
    @Value("${twilio.auth-token:}")
    private String authToken;
    
    @Value("${twilio.phone-number:}")
    private String fromPhoneNumber;
    
    @Value("${twilio.timeout:30000}")
    private int timeout;
    
    @Value("${twilio.retry-attempts:3}")
    private int retryAttempts;
    
    @Value("${twilio.webhook-url:}")
    private String webhookUrl;
    
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;
    
    private boolean initialized = false;
    
    /**
     * Initialize Twilio client
     */
    private void initializeTwilio() {
        if (!initialized && smsEnabled) {
            if (accountSid.isEmpty() || authToken.isEmpty() || fromPhoneNumber.isEmpty()) {
                logger.warn("Twilio credentials not configured. SMS will not be sent.");
                return;
            }
            
            try {
                Twilio.init(accountSid, authToken);
                initialized = true;
                logger.info("Twilio SMS service initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Twilio: {}", e.getMessage());
            }
        }
    }
    
    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            logger.info("SMS is disabled. Would send to {}: {}", phoneNumber, message);
            return false;
        }
        
        initializeTwilio();
        
        if (!initialized) {
            logger.error("Twilio not initialized. Cannot send SMS.");
            return false;
        }
        
        // Retry logic
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                // Ensure phone number has country code
                String formattedNumber = formatPhoneNumber(phoneNumber);
                
                Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(fromPhoneNumber),
                    message
                ).create();
                
                logger.info("SMS sent successfully to {} on attempt {}", phoneNumber, attempt);
                return true;
                
            } catch (Exception e) {
                logger.warn("SMS send attempt {} failed for {}: {}", attempt, phoneNumber, e.getMessage());
                
                if (attempt == retryAttempts) {
                    logger.error("All SMS send attempts failed for {}: {}", phoneNumber, e.getMessage());
                    return false;
                }
                
                // Wait before retry (exponential backoff)
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Format phone number to E.164 format
     * Supports multiple countries with proper country codes
     */
    private String formatPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        
        // If already starts with country code, return as is
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }
        
        // Handle different country formats
        if (digits.startsWith("0")) {
            // Remove leading zero and add country code
            String withoutZero = digits.substring(1);
            
            // Determine country code based on length and patterns
            if (withoutZero.length() == 10) {
                // Egypt: +20
                return "+20" + withoutZero;
            } else if (withoutZero.length() == 9) {
                // Saudi Arabia: +966
                return "+966" + withoutZero;
            } else if (withoutZero.length() == 8) {
                // UAE: +971
                return "+971" + withoutZero;
            } else {
                // Default to Egypt
                return "+20" + withoutZero;
            }
        } else if (digits.length() >= 10) {
            // Already has country code, just add +
            return "+" + digits;
        } else {
            // Assume Egypt for short numbers
            return "+20" + digits;
        }
    }
}

