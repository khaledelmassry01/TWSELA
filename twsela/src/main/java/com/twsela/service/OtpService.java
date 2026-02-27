package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    
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
    
    private final int otpValidityMinutes;
    private final int maxAttempts;

    public OtpService(
            @Value("${app.otp.validity-minutes:5}") int otpValidityMinutes,
            @Value("${app.otp.max-attempts:5}") int maxAttempts
    ) {
        this.otpValidityMinutes = otpValidityMinutes;
        this.maxAttempts = maxAttempts;
    }
    
    /**
     * Generate and store OTP for a phone number
     */
    public String generateOtp(String phone) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", new SecureRandom().nextInt(1000000));
        
        // Store OTP with expiry time
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpValidityMinutes);
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
        if (otpData.attempts >= maxAttempts) {
            otpStore.remove(phone); // Remove OTP after max attempts
            return false;
        }
        
        // Increment attempts
        otpData.attempts++;
        
        // Verify OTP using constant-time comparison (prevents timing attacks)
        if (MessageDigest.isEqual(otpData.otp.getBytes(), otp.getBytes())) {
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

