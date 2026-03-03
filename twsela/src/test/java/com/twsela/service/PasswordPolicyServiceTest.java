package com.twsela.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyServiceTest {

    private final PasswordPolicyService passwordPolicyService = new PasswordPolicyService();

    @Test
    @DisplayName("كلمة مرور قوية — بدون مخالفات")
    void validate_strongPassword_noViolations() {
        List<String> violations = passwordPolicyService.validate("StrongPass1!");
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("كلمة مرور قصيرة — مخالفة")
    void validate_tooShort_hasViolation() {
        List<String> violations = passwordPolicyService.validate("Aa1!");
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("بدون حرف كبير — مخالفة")
    void validate_noUppercase_hasViolation() {
        List<String> violations = passwordPolicyService.validate("strongpass1!");
        assertTrue(violations.stream().anyMatch(v -> v.contains("حرف كبير")));
    }

    @Test
    @DisplayName("بدون رقم — مخالفة")
    void validate_noDigit_hasViolation() {
        List<String> violations = passwordPolicyService.validate("StrongPass!");
        assertTrue(violations.stream().anyMatch(v -> v.contains("رقم")));
    }

    @Test
    @DisplayName("بدون رمز خاص — مخالفة")
    void validate_noSpecialChar_hasViolation() {
        List<String> violations = passwordPolicyService.validate("StrongPass1");
        assertTrue(violations.stream().anyMatch(v -> v.contains("رمز خاص")));
    }

    @Test
    @DisplayName("كلمة مرور شائعة — مخالفة")
    void validate_commonPassword_hasViolation() {
        List<String> violations = passwordPolicyService.validate("password");
        assertTrue(violations.stream().anyMatch(v -> v.contains("شائعة")));
    }

    @Test
    @DisplayName("كلمة مرور فارغة — مخالفة")
    void validate_nullPassword_hasViolation() {
        List<String> violations = passwordPolicyService.validate(null);
        assertFalse(violations.isEmpty());
        assertTrue(violations.get(0).contains("مطلوبة"));
    }

    @Test
    @DisplayName("isStrong — كلمة قوية")
    void isStrong_validPassword_returnsTrue() {
        assertTrue(passwordPolicyService.isStrong("MySecure@99"));
    }

    @Test
    @DisplayName("isInHistory — كلمة مرور مكررة")
    void isInHistory_matchesPrevious_returnsTrue() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "OldPassword1!";
        String hashed = encoder.encode(raw);

        assertTrue(passwordPolicyService.isInHistory(raw, List.of(hashed), encoder));
    }

    @Test
    @DisplayName("isInHistory — كلمة مرور جديدة")
    void isInHistory_noMatch_returnsFalse() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode("OldPassword1!");

        assertFalse(passwordPolicyService.isInHistory("NewPassword2@", List.of(hashed), encoder));
    }
}
