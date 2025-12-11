package com.twsela.service;

/**
 * SMS Service Interface
 * Defines contract for sending SMS messages
 */
public interface SmsService {
    
    /**
     * Send SMS message to a phone number
     * 
     * @param phoneNumber Recipient phone number (with country code)
     * @param message Message content
     * @return true if sent successfully, false otherwise
     */
    boolean sendSms(String phoneNumber, String message);
    
    /**
     * Send OTP SMS to a phone number
     * 
     * @param phoneNumber Recipient phone number (with country code)
     * @param otp OTP code
     * @return true if sent successfully, false otherwise
     */
    default boolean sendOtp(String phoneNumber, String otp) {
        String message = String.format(
            "رمز التحقق الخاص بك هو: %s\nصالح لمدة 5 دقائق.\nلا تشارك هذا الرمز مع أحد.",
            otp
        );
        return sendSms(phoneNumber, message);
    }
}

