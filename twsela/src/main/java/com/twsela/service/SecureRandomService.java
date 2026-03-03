package com.twsela.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * خدمة توليد أرقام عشوائية آمنة.
 */
@Service
public class SecureRandomService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+";

    /**
     * توليد OTP رقمي.
     */
    public String generateOtp(int length) {
        if (length <= 0 || length > 10) {
            throw new IllegalArgumentException("OTP length must be between 1 and 10");
        }
        int bound = (int) Math.pow(10, length);
        int otp = SECURE_RANDOM.nextInt(bound);
        return String.format("%0" + length + "d", otp);
    }

    /**
     * توليد كلمة مرور عشوائية قوية.
     */
    public String generatePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8");
        }
        String allChars = ALPHANUMERIC + SPECIAL_CHARS;
        StringBuilder password = new StringBuilder(length);
        // Ensure at least one of each type
        password.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(26))); // uppercase
        password.append(ALPHANUMERIC.charAt(26 + SECURE_RANDOM.nextInt(26))); // lowercase
        password.append(ALPHANUMERIC.charAt(52 + SECURE_RANDOM.nextInt(10))); // digit
        password.append(SPECIAL_CHARS.charAt(SECURE_RANDOM.nextInt(SPECIAL_CHARS.length()))); // special

        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(SECURE_RANDOM.nextInt(allChars.length())));
        }
        // Shuffle
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * توليد token عشوائي.
     */
    public String generateToken(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Token length must be positive");
        }
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return token.toString();
    }

    /**
     * توليد بايتات عشوائية.
     */
    public byte[] generateBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }
}
