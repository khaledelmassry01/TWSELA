package com.twsela.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * خدمة سياسة كلمات المرور.
 */
@Service
public class PasswordPolicyService {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()\\-_=+\\[\\]{}|;:',.<>?/~`]");

    private static final List<String> COMMON_PASSWORDS = List.of(
            "password", "123456", "12345678", "qwerty", "abc123",
            "password1", "admin123", "letmein", "welcome", "monkey"
    );

    /**
     * التحقق من صحة كلمة المرور وفقاً للسياسة.
     * يعيد قائمة فارغة إذا كانت صالحة، أو قائمة بالمخالفات.
     */
    public List<String> validate(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null || password.isBlank()) {
            violations.add("كلمة المرور مطلوبة");
            return violations;
        }

        if (password.length() < MIN_LENGTH) {
            violations.add("كلمة المرور يجب أن تكون " + MIN_LENGTH + " أحرف على الأقل");
        }

        if (password.length() > MAX_LENGTH) {
            violations.add("كلمة المرور لا يجب أن تتجاوز " + MAX_LENGTH + " حرف");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add("يجب أن تحتوي على حرف كبير واحد على الأقل");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            violations.add("يجب أن تحتوي على حرف صغير واحد على الأقل");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            violations.add("يجب أن تحتوي على رقم واحد على الأقل");
        }

        if (!SPECIAL_PATTERN.matcher(password).find()) {
            violations.add("يجب أن تحتوي على رمز خاص واحد على الأقل");
        }

        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            violations.add("كلمة المرور شائعة جداً — اختر كلمة أقوى");
        }

        return violations;
    }

    /**
     * فحص هل كلمة المرور تطابق سياسة القوة.
     */
    public boolean isStrong(String password) {
        return validate(password).isEmpty();
    }

    /**
     * فحص هل كلمة المرور موجودة في التاريخ (لمنع إعادة الاستخدام).
     */
    public boolean isInHistory(String rawPassword, List<String> previousHashedPasswords,
                                org.springframework.security.crypto.password.PasswordEncoder encoder) {
        if (previousHashedPasswords == null) return false;
        return previousHashedPasswords.stream()
                .anyMatch(hashed -> encoder.matches(rawPassword, hashed));
    }
}
