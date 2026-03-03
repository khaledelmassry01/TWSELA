package com.twsela.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecureRandomServiceTest {

    private final SecureRandomService secureRandomService = new SecureRandomService();

    @Test
    @DisplayName("توليد OTP بالطول المطلوب")
    void generateOtp_correctLength() {
        String otp = secureRandomService.generateOtp(6);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    @DisplayName("توليد OTP بطول غير صالح — خطأ")
    void generateOtp_invalidLength_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> secureRandomService.generateOtp(0));
        assertThrows(IllegalArgumentException.class, () -> secureRandomService.generateOtp(11));
    }

    @Test
    @DisplayName("توليد كلمة مرور قوية")
    void generatePassword_containsAllTypes() {
        String password = secureRandomService.generatePassword(12);
        assertEquals(12, password.length());
        assertTrue(password.matches(".*[A-Z].*"), "يجب أن تحتوي على حرف كبير");
        assertTrue(password.matches(".*[a-z].*"), "يجب أن تحتوي على حرف صغير");
        assertTrue(password.matches(".*[0-9].*"), "يجب أن تحتوي على رقم");
        assertTrue(password.matches(".*[!@#$%^&*()\\-_=+].*"), "يجب أن تحتوي على رمز خاص");
    }

    @Test
    @DisplayName("كلمة مرور قصيرة جداً — خطأ")
    void generatePassword_tooShort_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> secureRandomService.generatePassword(5));
    }

    @Test
    @DisplayName("توليد token عشوائي")
    void generateToken_correctLength() {
        String token = secureRandomService.generateToken(32);
        assertEquals(32, token.length());
        assertTrue(token.matches("[A-Za-z0-9]{32}"));
    }

    @Test
    @DisplayName("توليد tokens فريدة")
    void generateToken_uniqueness() {
        String token1 = secureRandomService.generateToken(32);
        String token2 = secureRandomService.generateToken(32);
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("توليد بايتات عشوائية")
    void generateBytes_correctLength() {
        byte[] bytes = secureRandomService.generateBytes(16);
        assertEquals(16, bytes.length);
    }
}
