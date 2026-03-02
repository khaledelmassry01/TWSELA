/**
 * sanitizer.js — XSS protection utilities for Twsela frontend.
 * Escapes HTML entities in user-provided strings before DOM insertion.
 */
(function () {
    'use strict';

    var escapeMap = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#x27;',
        '/': '&#x2F;'
    };

    var escapeRegex = /[&<>"'/]/g;

    /**
     * Escape HTML special characters in a string.
     * @param {string} str - Raw string
     * @returns {string} Escaped string safe for innerHTML
     */
    function escapeHtml(str) {
        if (str == null) return '';
        return String(str).replace(escapeRegex, function (ch) {
            return escapeMap[ch];
        });
    }

    /**
     * Sanitize an object's string values (shallow).
     * @param {Object} obj - Object with string values
     * @returns {Object} New object with escaped string values
     */
    function sanitizeObject(obj) {
        if (!obj || typeof obj !== 'object') return obj;
        var result = {};
        Object.keys(obj).forEach(function (key) {
            result[key] = typeof obj[key] === 'string' ? escapeHtml(obj[key]) : obj[key];
        });
        return result;
    }

    /**
     * Set text content safely (no HTML interpretation).
     * @param {HTMLElement} el - Target element
     * @param {string} text - Text to set
     */
    function safeText(el, text) {
        if (el) el.textContent = text != null ? String(text) : '';
    }

    /**
     * Set innerHTML with escaped content.
     * @param {HTMLElement} el - Target element
     * @param {string} html - HTML string (will be escaped)
     */
    function safeHtml(el, html) {
        if (el) el.innerHTML = escapeHtml(html);
    }

    // Expose globally
    window.Sanitizer = {
        escapeHtml: escapeHtml,
        sanitizeObject: sanitizeObject,
        safeText: safeText,
        safeHtml: safeHtml
    };
})();
