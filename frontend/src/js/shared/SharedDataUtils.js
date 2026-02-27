/**
 * Twsela CMS - Shared Data Utilities
 * Consolidated data formatting and manipulation functions
 * Follows Single Responsibility Principle - ONLY data operations
 */

// SharedDataUtils initialization - console.log removed for cleaner console

class SharedDataUtils {
    /**
     * Escape HTML to prevent XSS attacks
     * @param {string} str - Raw string (potentially from user/server data)
     * @returns {string} HTML-safe string
     */
    static escapeHtml(str) {
        if (str == null) return '';
        const s = String(str);
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return s.replace(/[&<>"']/g, c => map[c]);
    }

    /**
     * Get status badge class
     * @param {string} status - Status value
     * @returns {string} CSS class for badge
     */
    static getStatusBadgeClass(status) {
        const statusMap = {
            'PENDING': 'bg-warning',
            'PICKED_UP': 'bg-info',
            'IN_TRANSIT': 'bg-primary',
            'OUT_FOR_DELIVERY': 'bg-info',
            'DELIVERED': 'bg-success',
            'FAILED_DELIVERY': 'bg-danger',
            'RETURNED': 'bg-secondary',
            'ACTIVE': 'bg-success',
            'INACTIVE': 'bg-secondary',
            'MAINTENANCE': 'bg-warning',
            'APPROVED': 'bg-success',
            'REJECTED': 'bg-danger',
            'PAID': 'bg-info'
        };
        return statusMap[status] || 'bg-secondary';
    }

    /**
     * Get status text in Arabic
     * @param {string} status - Status value
     * @returns {string} Arabic status text
     */
    static getStatusText(status) {
        const statusMap = {
            'PENDING': 'معلق',
            'PICKED_UP': 'تم الاستلام',
            'IN_TRANSIT': 'في الطريق',
            'OUT_FOR_DELIVERY': 'خارج للتوصيل',
            'DELIVERED': 'تم التسليم',
            'FAILED_DELIVERY': 'فشل التسليم',
            'RETURNED': 'مرتجع',
            'ACTIVE': 'نشط',
            'INACTIVE': 'غير نشط',
            'MAINTENANCE': 'صيانة',
            'APPROVED': 'موافق عليه',
            'REJECTED': 'مرفوض',
            'PAID': 'مدفوع'
        };
        return statusMap[status] || status;
    }

    /**
     * Get role badge class
     * @param {string} role - Role value
     * @returns {string} CSS class for badge
     */
    static getRoleBadgeClass(role) {
        const roleMap = {
            'OWNER': 'bg-primary',
            'ADMIN': 'bg-info',
            'MERCHANT': 'bg-success',
            'COURIER': 'bg-warning',
            'WAREHOUSE_MANAGER': 'bg-secondary',
            'WAREHOUSE': 'bg-secondary'
        };
        return roleMap[role] || 'bg-secondary';
    }

    /**
     * Get role text in Arabic
     * @param {string} role - Role value
     * @returns {string} Arabic role text
     */
    static getRoleText(role) {
        const roleMap = {
            'OWNER': 'مالك',
            'ADMIN': 'مدير',
            'MERCHANT': 'تاجر',
            'COURIER': 'سائق',
            'WAREHOUSE_MANAGER': 'مدير مستودع',
            'WAREHOUSE': 'مستودع'
        };
        return roleMap[role] || role;
    }

    /**
     * Get payment method text in Arabic
     * @param {string} method - Payment method
     * @returns {string} Arabic payment method text
     */
    static getPaymentMethodText(method) {
        const methodMap = {
            'CASH': 'نقدي',
            'CARD': 'بطاقة ائتمان',
            'BANK_TRANSFER': 'تحويل بنكي',
            'COD': 'الدفع عند الاستلام',
            'ONLINE': 'دفع إلكتروني'
        };
        return methodMap[method] || method;
    }

    /**
     * Format currency with Arabic locale
     * @param {number} amount - Amount to format
     * @param {string} currency - Currency code
     * @returns {string} Formatted currency
     */
    static formatCurrency(amount, currency = 'SAR') {
        return new Intl.NumberFormat('ar-SA', {
            style: 'currency',
            currency: currency
        }).format(amount);
    }

    /**
     * Format date with Arabic locale
     * @param {Date|string} date - Date to format
     * @param {Object} options - Formatting options
     * @returns {string} Formatted date
     */
    static formatDate(date, options = {}) {
        const defaultOptions = {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        };
        
        return new Intl.DateTimeFormat('ar-SA', { ...defaultOptions, ...options }).format(new Date(date));
    }

    /**
     * Format phone number for display
     * @param {string} phone - Phone number
     * @returns {string} Formatted phone number
     */
    static formatPhone(phone) {
        if (!phone) return '';
        
        // Remove all non-digit characters
        const cleaned = phone.replace(/\D/g, '');
        
        // Handle Egyptian phone numbers
        if (cleaned.startsWith('20')) {
            return `+${cleaned}`;
        } else if (cleaned.startsWith('0')) {
            return `+20${cleaned.substring(1)}`;
        } else if (cleaned.length === 10) {
            return `+20${cleaned}`;
        }
        
        return phone;
    }

    /**
     * Create status badge HTML
     * @param {string} status - Status value
     * @returns {Object} Badge object with class and text
     */
    static createStatusBadge(status) {
        return {
            class: this.getStatusBadgeClass(status),
            text: this.getStatusText(status)
        };
    }

    /**
     * Create role badge HTML
     * @param {string} role - Role value
     * @returns {Object} Badge object with class and text
     */
    static createRoleBadge(role) {
        return {
            class: this.getRoleBadgeClass(role),
            text: this.getRoleText(role)
        };
    }

    /**
     * Format number with Arabic locale
     * @param {number} number - Number to format
     * @param {Object} options - Formatting options
     * @returns {string} Formatted number
     */
    static formatNumber(number, options = {}) {
        return new Intl.NumberFormat('ar-SA', options).format(number);
    }

    /**
     * Get relative time
     * @param {Date|string} date - Date to compare
     * @returns {string} Relative time
     */
    static getRelativeTime(date) {
        const now = new Date();
        const target = new Date(date);
        const diffInSeconds = Math.floor((now - target) / 1000);
        
        if (diffInSeconds < 60) return 'منذ لحظات';
        if (diffInSeconds < 3600) return `منذ ${Math.floor(diffInSeconds / 60)} دقيقة`;
        if (diffInSeconds < 86400) return `منذ ${Math.floor(diffInSeconds / 3600)} ساعة`;
        if (diffInSeconds < 2592000) return `منذ ${Math.floor(diffInSeconds / 86400)} يوم`;
        
        return this.formatDate(date);
    }

    /**
     * Validate phone number
     * @param {string} phone - Phone number to validate
     * @returns {boolean} Validation result
     */
    static validatePhone(phone) {
        if (!phone) return false;
        
        const cleaned = phone.replace(/\D/g, '');
        
        // Egyptian phone number validation
        if (cleaned.startsWith('20') && cleaned.length === 12) {
            return true;
        } else if (cleaned.startsWith('0') && cleaned.length === 11) {
            return true;
        } else if (cleaned.length === 10) {
            return true;
        }
        
        return false;
    }

    /**
     * Validate email address
     * @param {string} email - Email to validate
     * @returns {boolean} Validation result
     */
    static validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    /**
     * Format file size
     * @param {number} bytes - File size in bytes
     * @returns {string} Formatted file size
     */
    static formatFileSize(bytes) {
        if (bytes === 0) return '0 بايت';
        
        const k = 1024;
        const sizes = ['بايت', 'كيلوبايت', 'ميجابايت', 'جيجابايت'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    /**
     * Get file extension
     * @param {string} filename - File name
     * @returns {string} File extension
     */
    static getFileExtension(filename) {
        return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2);
    }

    /**
     * Check if file type is allowed
     * @param {string} filename - File name
     * @param {Array} allowedTypes - Allowed file types
     * @returns {boolean} Allowed status
     */
    static isFileTypeAllowed(filename, allowedTypes) {
        const extension = this.getFileExtension(filename).toLowerCase();
        return allowedTypes.includes(extension);
    }

    /**
     * Sanitize HTML string
     * @param {string} str - HTML string
     * @returns {string} Sanitized string
     */
    static sanitizeHTML(str) {
        const temp = document.createElement('div');
        temp.textContent = str;
        return temp.innerHTML;
    }

    /**
     * Escape HTML string
     * @param {string} str - String to escape
     * @returns {string} Escaped string
     */
    static escapeHTML(str) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return str.replace(/[&<>"']/g, m => map[m]);
    }

    /**
     * Generate UUID v4
     * @returns {string} UUID
     */
    static generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }

    /**
     * Generate random string
     * @param {number} length - String length
     * @returns {string} Random string
     */
    static generateRandomString(length = 10) {
        const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let result = '';
        for (let i = 0; i < length; i++) {
            result += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        return result;
    }

    /**
     * Generate OTP code
     * @param {number} length - OTP length
     * @returns {string} OTP code
     */
    static generateOTP(length = 6) {
        let otp = '';
        for (let i = 0; i < length; i++) {
            otp += Math.floor(Math.random() * 10);
        }
        return otp;
    }

    /**
     * Deep clone object
     * @param {Object} obj - Object to clone
     * @returns {Object} Cloned object
     */
    static deepClone(obj) {
        if (obj === null || typeof obj !== 'object') return obj;
        if (obj instanceof Date) return new Date(obj.getTime());
        if (obj instanceof Array) return obj.map(item => this.deepClone(item));
        if (typeof obj === 'object') {
            const clonedObj = {};
            for (const key in obj) {
                if (obj.hasOwnProperty(key)) {
                    clonedObj[key] = this.deepClone(obj[key]);
                }
            }
            return clonedObj;
        }
    }

    /**
     * Check if object is empty
     * @param {Object} obj - Object to check
     * @returns {boolean} Empty status
     */
    static isEmpty(obj) {
        if (obj == null) return true;
        if (Array.isArray(obj) || typeof obj === 'string') return obj.length === 0;
        if (typeof obj === 'object') return Object.keys(obj).length === 0;
        return false;
    }

    /**
     * Get query parameter value
     * @param {string} name - Parameter name
     * @returns {string|null} Parameter value
     */
    static getQueryParam(name) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(name);
    }

    /**
     * Set query parameter
     * @param {string} name - Parameter name
     * @param {string} value - Parameter value
     */
    static setQueryParam(name, value) {
        const url = new URL(window.location);
        url.searchParams.set(name, value);
        window.history.replaceState({}, '', url);
    }

    /**
     * Remove query parameter
     * @param {string} name - Parameter name
     */
    static removeQueryParam(name) {
        const url = new URL(window.location);
        url.searchParams.delete(name);
        window.history.replaceState({}, '', url);
    }

    /**
     * Copy text to clipboard
     * @param {string} text - Text to copy
     * @returns {Promise<boolean>} Success status
     */
    static async copyToClipboard(text) {
        try {
            if (navigator.clipboard && window.isSecureContext) {
                await navigator.clipboard.writeText(text);
                return true;
            } else {
                // Fallback for older browsers
                const textArea = document.createElement('textarea');
                textArea.value = text;
                textArea.style.position = 'fixed';
                textArea.style.left = '-999999px';
                textArea.style.top = '-999999px';
                document.body.appendChild(textArea);
                textArea.focus();
                textArea.select();
                const result = document.execCommand('copy');
                document.body.removeChild(textArea);
                return result;
            }
        } catch (error) {

            return false;
        }
    }

    /**
     * Download file
     * @param {string} data - File data
     * @param {string} filename - File name
     * @param {string} type - MIME type
     */
    static downloadFile(data, filename, type = 'text/plain') {
        const blob = new Blob([data], { type });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    }

    /**
     * Sleep function
     * @param {number} ms - Milliseconds to sleep
     * @returns {Promise} Promise that resolves after delay
     */
    static sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * Retry function with exponential backoff
     * @param {Function} fn - Function to retry
     * @param {number} retries - Number of retries
     * @param {number} delay - Initial delay
     * @returns {Promise} Promise that resolves with function result
     */
    static async retry(fn, retries = 3, delay = 1000) {
        try {
            return await fn();
        } catch (error) {
            if (retries > 0) {
                await this.sleep(delay);
                return this.retry(fn, retries - 1, delay * 2);
            }
            throw error;
        }
    }

    /**
     * Debounce function
     * @param {Function} func - Function to debounce
     * @param {number} wait - Wait time in milliseconds
     * @returns {Function} Debounced function
     */
    static debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    /**
     * Throttle function
     * @param {Function} func - Function to throttle
     * @param {number} limit - Time limit in milliseconds
     * @returns {Function} Throttled function
     */
    static throttle(func, limit) {
        let inThrottle;
        return function(...args) {
            if (!inThrottle) {
                func.apply(this, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    /**
     * Check if device is mobile
     * @returns {boolean} Mobile status
     */
    static isMobile() {
        return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    }

    /**
     * Check if device is tablet
     * @returns {boolean} Tablet status
     */
    static isTablet() {
        return /iPad|Android/i.test(navigator.userAgent) && window.innerWidth >= 768;
    }

    /**
     * Get device type
     * @returns {string} Device type
     */
    static getDeviceType() {
        if (this.isMobile()) return 'mobile';
        if (this.isTablet()) return 'tablet';
        return 'desktop';
    }
}

// Create global instance
window.SharedDataUtils = SharedDataUtils;
// Shorthand for XSS-safe HTML escaping
window.escapeHtml = SharedDataUtils.escapeHtml;

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = SharedDataUtils;
}

// SharedDataUtils loaded - console.log removed for cleaner console
