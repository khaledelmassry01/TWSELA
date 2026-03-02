package com.twsela.web.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordValidator — @ValidPassword constraint")
class PasswordValidatorTest {

    private PasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("null and empty → invalid")
    void nullAndEmpty(String pwd) {
        assertFalse(validator.isValid(pwd, null));
    }

    @Test
    @DisplayName("blank (spaces only) → invalid")
    void blank() {
        assertFalse(validator.isValid("      ", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab1", "x2", "a1"})
    @DisplayName("too short (<6) → invalid")
    void tooShort(String pwd) {
        assertFalse(validator.isValid(pwd, null));
    }

    @Test
    @DisplayName("only letters → invalid")
    void onlyLetters() {
        assertFalse(validator.isValid("abcdefgh", null));
    }

    @Test
    @DisplayName("only digits → invalid")
    void onlyDigits() {
        assertFalse(validator.isValid("12345678", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc123", "123abc", "Pass1word", "a1b2c3"})
    @DisplayName("valid passwords — letters + digits, 6+ chars")
    void valid(String pwd) {
        assertTrue(validator.isValid(pwd, null));
    }
}
