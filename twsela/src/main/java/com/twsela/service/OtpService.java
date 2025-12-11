package com.twsela.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    
    private static class OtpData {
        String otp;
        LocalDateTime expiryTime;
        int attempts;
        
        OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.attempts = 0;
        }
    }
    
    // Store OTP temporarily in memory (phone -> OtpData)
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    
    // OTP valid for 5 minutes
    private static final int OTP_VALIDITY_MINUTES = 5;
    // Maximum verification attempts
    private static final int MAX_ATTEMPTS = 5;
    
    /**
     * Generate and store OTP for a phone number
     */
    public String generateOtp(String phone) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        // Store OTP with expiry time
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
        otpStore.put(phone, new OtpData(otp, expiryTime));
        
        return otp;
    }
    
    /**
     * Verify OTP for a phone number
     */
    public boolean verifyOtp(String phone, String otp) {
        OtpData otpData = otpStore.get(phone);
        
        if (otpData == null) {
            return false; // No OTP found for this phone
        }
        
        // Check if OTP has expired
        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            otpStore.remove(phone); // Remove expired OTP
            return false;
        }
        
        // Check maximum attempts
        if (otpData.attempts >= MAX_ATTEMPTS) {
            otpStore.remove(phone); // Remove OTP after max attempts
            return false;
        }
        
        // Increment attempts
        otpData.attempts++;
        
        // Verify OTP
        if (otpData.otp.equals(otp)) {
            otpStore.remove(phone); // Remove OTP after successful verification
            return true;
        }
        
        return false;
    }
    
    /**
     * Remove OTP for a phone number
     */
    public void removeOtp(String phone) {
        otpStore.remove(phone);
    }
    
    /**
     * Check if OTP exists and is valid for a phone number
     */
    public boolean hasValidOtp(String phone) {
        OtpData otpData = otpStore.get(phone);
        if (otpData == null) {
            return false;
        }
        
        // Check if expired
        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            otpStore.remove(phone);
            return false;
        }
        
        return true;
    }
}

