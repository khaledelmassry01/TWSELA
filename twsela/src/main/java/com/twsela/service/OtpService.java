package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final String REDIS_OTP_PREFIX = "otp:";
    private static final String REDIS_ATTEMPTS_PREFIX = "otp:attempts:";

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

    // Fallback in-memory store when Redis is unavailable
    private final Map<String, OtpData> fallbackStore = new ConcurrentHashMap<>();

    private final int otpValidityMinutes;
    private final int maxAttempts;
    private final StringRedisTemplate redisTemplate;

    public OtpService(
            @Value("${app.otp.validity-minutes:5}") int otpValidityMinutes,
            @Value("${app.otp.max-attempts:5}") int maxAttempts,
            @Nullable StringRedisTemplate redisTemplate
    ) {
        this.otpValidityMinutes = otpValidityMinutes;
        this.maxAttempts = maxAttempts;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generate and store OTP for a phone number.
     * Stores in Redis with TTL; falls back to in-memory if Redis is unavailable.
     */
    public String generateOtp(String phone) {
        String otp = String.format("%06d", new SecureRandom().nextInt(1000000));

        if (tryRedisStore(phone, otp)) {
            return otp;
        }

        // Fallback to in-memory
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpValidityMinutes);
        fallbackStore.put(phone, new OtpData(otp, expiryTime));
        return otp;
    }

    /**
     * Verify OTP for a phone number.
     * Checks Redis first, then the in-memory fallback.
     */
    public boolean verifyOtp(String phone, String otp) {
        // Try Redis first
        Boolean redisResult = tryRedisVerify(phone, otp);
        if (redisResult != null) {
            return redisResult;
        }

        // Fallback to in-memory
        OtpData otpData = fallbackStore.get(phone);
        if (otpData == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            fallbackStore.remove(phone);
            return false;
        }

        if (otpData.attempts >= maxAttempts) {
            fallbackStore.remove(phone);
            return false;
        }

        otpData.attempts++;

        if (MessageDigest.isEqual(otpData.otp.getBytes(), otp.getBytes())) {
            fallbackStore.remove(phone);
            return true;
        }

        return false;
    }

    /**
     * Remove OTP for a phone number (from both Redis and fallback).
     */
    public void removeOtp(String phone) {
        tryRedisDelete(phone);
        fallbackStore.remove(phone);
    }

    /**
     * Alias for {@link #removeOtp(String)}.
     */
    public void clearOtp(String phone) {
        removeOtp(phone);
    }

    /**
     * Check if OTP exists and is valid for a phone number.
     */
    public boolean hasValidOtp(String phone) {
        Boolean redisResult = tryRedisHasValid(phone);
        if (redisResult != null) {
            return redisResult;
        }

        // Fallback
        OtpData otpData = fallbackStore.get(phone);
        if (otpData == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            fallbackStore.remove(phone);
            return false;
        }

        return true;
    }

    // ---- Scheduled cleanup for the in-memory fallback store ----

    /**
     * Periodically removes expired entries from the fallback map.
     * Redis entries expire automatically via TTL.
     */
    @Scheduled(fixedRateString = "${app.otp.cleanup-interval-ms:60000}")
    public void cleanupExpiredOtps() {
        if (fallbackStore.isEmpty()) {
            return;
        }

        int removed = 0;
        Iterator<Map.Entry<String, OtpData>> it = fallbackStore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OtpData> entry = it.next();
            if (LocalDateTime.now().isAfter(entry.getValue().expiryTime)) {
                it.remove();
                removed++;
            }
        }

        if (removed > 0) {
            log.debug("Cleaned up {} expired OTP(s) from fallback store", removed);
        }
    }

    // ---- Redis helper methods (all failures fall back silently) ----

    private boolean tryRedisStore(String phone, String otp) {
        if (redisTemplate == null) {
            return false;
        }
        try {
            String otpKey = REDIS_OTP_PREFIX + phone;
            String attemptsKey = REDIS_ATTEMPTS_PREFIX + phone;
            redisTemplate.opsForValue().set(otpKey, otp, otpValidityMinutes, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(attemptsKey, "0", otpValidityMinutes, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            log.warn("Redis store failed, falling back to in-memory: {}", e.getMessage());
            return false;
        }
    }

    /**
     * @return {@code true}/{@code false} if Redis handled the request,
     *         {@code null} if the OTP was not found in Redis (caller should check fallback).
     */
    private Boolean tryRedisVerify(String phone, String otp) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            String otpKey = REDIS_OTP_PREFIX + phone;
            String attemptsKey = REDIS_ATTEMPTS_PREFIX + phone;

            String storedOtp = redisTemplate.opsForValue().get(otpKey);
            if (storedOtp == null) {
                return null; // Not in Redis — might be in fallback
            }

            // Check attempts
            String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
            int attempts = (attemptsStr != null) ? Integer.parseInt(attemptsStr) : 0;
            if (attempts >= maxAttempts) {
                redisTemplate.delete(otpKey);
                redisTemplate.delete(attemptsKey);
                return false;
            }

            // Increment attempts
            redisTemplate.opsForValue().increment(attemptsKey);

            // Constant-time comparison (prevents timing attacks)
            if (MessageDigest.isEqual(storedOtp.getBytes(), otp.getBytes())) {
                redisTemplate.delete(otpKey);
                redisTemplate.delete(attemptsKey);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("Redis verify failed, falling back to in-memory: {}", e.getMessage());
            return null;
        }
    }

    private void tryRedisDelete(String phone) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(REDIS_OTP_PREFIX + phone);
            redisTemplate.delete(REDIS_ATTEMPTS_PREFIX + phone);
        } catch (Exception e) {
            log.warn("Redis delete failed: {}", e.getMessage());
        }
    }

    private Boolean tryRedisHasValid(String phone) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            String storedOtp = redisTemplate.opsForValue().get(REDIS_OTP_PREFIX + phone);
            if (storedOtp != null) {
                return true;
            }
            // Key not in Redis — might be in fallback or not exist at all
            return null;
        } catch (Exception e) {
            log.warn("Redis check failed, falling back to in-memory: {}", e.getMessage());
            return null;
        }
    }
}

