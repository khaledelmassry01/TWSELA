package com.twsela.service;

import org.springframework.stereotype.Service;

/**
 * خدمة تنظيف المدخلات من XSS وSQL Injection.
 */
@Service
public class InputSanitizationService {

    private static final String[] XSS_PATTERNS = {
            "<script", "</script", "javascript:", "onerror=", "onload=",
            "onclick=", "onmouseover=", "onfocus=", "onblur=",
            "<iframe", "<object", "<embed", "<form", "expression(",
            "eval(", "alert(", "document.cookie", "document.location"
    };

    private static final String[] SQL_PATTERNS = {
            "' OR ", "'; DROP", "1=1", "' UNION", "SELECT * FROM",
            "INSERT INTO", "DELETE FROM", "UPDATE SET", "EXEC(",
            "xp_cmdshell", "sp_executesql"
    };

    /**
     * تنظيف نص من محتوى XSS محتمل.
     */
    public String sanitizeXss(String input) {
        if (input == null) return null;
        String sanitized = input;
        // Escape HTML entities
        sanitized = sanitized.replace("&", "&amp;");
        sanitized = sanitized.replace("<", "&lt;");
        sanitized = sanitized.replace(">", "&gt;");
        sanitized = sanitized.replace("\"", "&quot;");
        sanitized = sanitized.replace("'", "&#x27;");
        return sanitized;
    }

    /**
     * فحص هل النص يحتوي على أنماط XSS.
     */
    public boolean containsXss(String input) {
        if (input == null || input.isBlank()) return false;
        String lower = input.toLowerCase();
        for (String pattern : XSS_PATTERNS) {
            if (lower.contains(pattern.toLowerCase())) return true;
        }
        return false;
    }

    /**
     * فحص هل النص يحتوي على أنماط SQL Injection.
     */
    public boolean containsSqlInjection(String input) {
        if (input == null || input.isBlank()) return false;
        String lower = input.toLowerCase();
        for (String pattern : SQL_PATTERNS) {
            if (lower.contains(pattern.toLowerCase())) return true;
        }
        return false;
    }

    /**
     * تنظيف شامل للمدخلات.
     */
    public String sanitize(String input) {
        if (input == null) return null;
        return sanitizeXss(input.trim());
    }

    /**
     * فحص أمان النص.
     */
    public boolean isSafe(String input) {
        return !containsXss(input) && !containsSqlInjection(input);
    }
}
