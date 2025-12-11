/**
 * Twsela CMS - Unified Notification Service
 * Simplified notification access for all handlers
 * Follows Single Responsibility Principle - ONLY notification operations
 */

// NotificationService initialization - console.log removed for cleaner console

class NotificationService {
    /**
     * Show notification - Simplified access
     * @param {string} message - Notification message
     * @param {string} type - Notification type (success, error, warning, info)
     * @param {Object} options - Additional options
     */
    static show(message, type = 'info', options = {}) {
        if (window.notificationManager) {
            return window.notificationManager.show({ message, type, ...options });
        } else {
            // Fallback to alert for backward compatibility
            alert(message);
        }
    }

    /**
     * Show success notification
     * @param {string} message - Success message
     * @param {Object} options - Additional options
     */
    static success(message, options = {}) {
        return this.show(message, 'success', options);
    }

    /**
     * Show error notification
     * @param {string} message - Error message
     * @param {Object} options - Additional options
     */
    static error(message, options = {}) {
        return this.show(message, 'error', options);
    }

    /**
     * Show warning notification
     * @param {string} message - Warning message
     * @param {Object} options - Additional options
     */
    static warning(message, options = {}) {
        return this.show(message, 'warning', options);
    }

    /**
     * Show info notification
     * @param {string} message - Info message
     * @param {Object} options - Additional options
     */
    static info(message, options = {}) {
        return this.show(message, 'info', options);
    }

    /**
     * Show loading notification
     * @param {string} message - Loading message
     * @param {Object} options - Additional options
     */
    static loading(message = 'جاري التحميل...', options = {}) {
        return this.show(message, 'info', { 
            icon: 'fas fa-spinner fa-spin', 
            persistent: true, 
            ...options 
        });
    }

    /**
     * Hide all notifications
     */
    static hideAll() {
        if (window.notificationManager) {
            window.notificationManager.hideAll();
        }
    }

    /**
     * Show confirmation notification
     * @param {string} message - Confirmation message
     * @param {Function} onConfirm - Confirm callback
     * @param {Function} onCancel - Cancel callback
     * @param {Object} options - Additional options
     */
    static confirm(message, onConfirm, onCancel, options = {}) {
        if (window.notificationManager) {
            return window.notificationManager.confirm(message, onConfirm, onCancel, options);
        } else {
            // Fallback to confirm dialog
            if (confirm(message)) {
                if (onConfirm) onConfirm();
            } else {
                if (onCancel) onCancel();
            }
        }
    }
}

// Create global instance
window.NotificationService = NotificationService;

// Convenience functions for backward compatibility
window.showNotification = (message, type, options) => {
    return NotificationService.show(message, type, options);
};

window.showSuccess = (message, options) => {
    return NotificationService.success(message, options);
};

window.showWarning = (message, options) => {
    return NotificationService.warning(message, options);
};

window.showInfo = (message, options) => {
    return NotificationService.info(message, options);
};

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = NotificationService;
}

// NotificationService loaded - console.log removed for cleaner console
