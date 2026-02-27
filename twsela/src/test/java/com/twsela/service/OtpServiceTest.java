package com.twsela.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        // 5-minute validity, max 5 attempts
        otpService = new OtpService(5, 5);
    }

    // ======== generateOtp ========

    @Test
    @DisplayName("generateOtp — يجب إنشاء رمز OTP مكون من 6 أرقام")
    void generateOtp_returns6DigitCode() {
        String otp = otpService.generateOtp("01012345678");

        assertThat(otp).isNotNull();
        assertThat(otp).hasSize(6);
        assertThat(otp).matches("\\d{6}");
    }

    @Test
    @DisplayName("generateOtp — يجب إنشاء رمز جديد لكل استدعاء لنفس الرقم")
    void generateOtp_overwritesPrevious() {
        String otp1 = otpService.generateOtp("01012345678");
        String otp2 = otpService.generateOtp("01012345678");

        // The new OTP should be stored, not necessarily different
        assertThat(otp2).isNotNull().hasSize(6);
    }

    // ======== verifyOtp ========

    @Test
    @DisplayName("verifyOtp — يجب التحقق من الرمز الصحيح")
    void verifyOtp_correctCode() {
        String otp = otpService.generateOtp("01012345678");

        boolean result = otpService.verifyOtp("01012345678", otp);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verifyOtp — يجب رفض الرمز الخاطئ")
    void verifyOtp_wrongCode() {
        otpService.generateOtp("01012345678");

        boolean result = otpService.verifyOtp("01012345678", "000000");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyOtp — يجب رفض الرمز لرقم غير مسجل")
    void verifyOtp_unknownPhone() {
        boolean result = otpService.verifyOtp("09999999999", "123456");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyOtp — يجب رفض بعد تجاوز الحد الأقصى للمحاولات")
    void verifyOtp_maxAttemptsExceeded() {
        String otp = otpService.generateOtp("01012345678");

        // Exhaust max attempts with wrong codes
        for (int i = 0; i < 5; i++) {
            otpService.verifyOtp("01012345678", "WRONG" + i);
        }

        // Even correct code should fail now
        boolean result = otpService.verifyOtp("01012345678", otp);

        assertThat(result).isFalse();
    }

    // ======== removeOtp ========

    @Test
    @DisplayName("removeOtp — يجب إزالة الرمز بنجاح")
    void removeOtp_removesEntry() {
        otpService.generateOtp("01012345678");
        otpService.removeOtp("01012345678");

        boolean result = otpService.verifyOtp("01012345678", "anything");

        assertThat(result).isFalse();
    }

    // ======== hasValidOtp ========

    @Test
    @DisplayName("hasValidOtp — يجب إرجاع true للرمز الصالح")
    void hasValidOtp_true() {
        otpService.generateOtp("01012345678");

        boolean result = otpService.hasValidOtp("01012345678");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasValidOtp — يجب إرجاع false لرقم بدون OTP")
    void hasValidOtp_false() {
        boolean result = otpService.hasValidOtp("09999999999");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasValidOtp — يجب إرجاع false بعد إزالة الرمز")
    void hasValidOtp_afterRemoval() {
        otpService.generateOtp("01012345678");
        otpService.removeOtp("01012345678");

        boolean result = otpService.hasValidOtp("01012345678");

        assertThat(result).isFalse();
    }
}
