import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('settings');

/**
 * Twsela CMS - Settings Page Handler
 * Handles user settings, notification preferences, and password reset
 */

class SettingsPageHandler {
    constructor() {
        this.settings = {};
        this.isDirty = false;
        this.init();
    }

    /**
     * Initialize the settings page
     */
    async init() {
        try {
            this.setupEventListeners();
            this.setupTabs();
            await this.loadSettings();
        } catch (error) {
            log.error('Settings initialization failed:', error);
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Save settings button
        const saveBtn = document.getElementById('saveSettingsBtn');
        if (saveBtn) {
            saveBtn.addEventListener('click', () => this.saveSettings());
        }

        // Reset settings button
        const resetBtn = document.getElementById('resetSettingsBtn');
        if (resetBtn) {
            resetBtn.addEventListener('click', () => this.resetSettings());
        }

        // Password reset form
        const passwordForm = document.getElementById('passwordResetForm');
        if (passwordForm) {
            passwordForm.addEventListener('submit', (e) => this.handlePasswordReset(e));
        }

        // Send OTP button
        const sendOtpBtn = document.getElementById('sendOtpBtn');
        if (sendOtpBtn) {
            sendOtpBtn.addEventListener('click', () => this.sendOtp());
        }

        // Logout link
        const logoutLink = document.getElementById('logoutLink');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                this.handleLogout();
            });
        }

        // Clear cache button
        const clearCacheBtn = document.getElementById('clearCacheBtn');
        if (clearCacheBtn) {
            clearCacheBtn.addEventListener('click', () => this.clearCache());
        }

        // Track form changes
        document.querySelectorAll('select, input[type="checkbox"]').forEach(el => {
            el.addEventListener('change', () => { this.isDirty = true; });
        });
    }

    /**
     * Setup settings tabs navigation
     */
    setupTabs() {
        const tabLinks = document.querySelectorAll('.settings-tab-link');
        tabLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const targetPanel = link.getAttribute('data-target') || link.getAttribute('href')?.replace('#', '');
                if (targetPanel) {
                    this.switchTab(targetPanel);
                }
            });
        });
    }

    /**
     * Switch to a specific settings tab
     * @param {string} panelId
     */
    switchTab(panelId) {
        // Deactivate all tabs and panels
        document.querySelectorAll('.settings-tab-link').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.settings-panel').forEach(p => p.classList.remove('active'));

        // Activate selected
        const targetLink = document.querySelector(`[data-target="${panelId}"]`) ||
                           document.querySelector(`[href="#${panelId}"]`);
        if (targetLink) targetLink.classList.add('active');

        const targetPanel = document.getElementById(panelId);
        if (targetPanel) targetPanel.classList.add('active');
    }

    /**
     * Load settings from API
     */
    async loadSettings() {
        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const token = sessionStorage.getItem('authToken');
            if (!token) return;

            const response = await fetch(`${apiBaseUrl}/api/settings`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) return;

            this.settings = await response.json();
            this.applySettings(this.settings);
        } catch (error) {
            log.error('Failed to load settings:', error);
        }
    }

    /**
     * Apply settings to form elements
     * @param {Object} settings
     */
    applySettings(settings) {
        if (!settings) return;

        // General settings
        this.setSelectValue('language', settings.language);
        this.setSelectValue('timezone', settings.timezone);
        this.setSelectValue('dateFormat', settings.dateFormat);
        this.setSelectValue('timeFormat', settings.timeFormat);
        this.setSelectValue('currency', settings.currency);

        // Notification settings
        this.setCheckboxValue('emailNotifications', settings.emailNotifications);
        this.setCheckboxValue('smsNotifications', settings.smsNotifications);
        this.setCheckboxValue('pushNotifications', settings.pushNotifications);
    }

    /**
     * Save settings to API
     */
    async saveSettings() {
        try {
            const settingsData = this.collectSettingsData();
            const apiBaseUrl = this.getApiBaseUrl();
            const token = sessionStorage.getItem('authToken');
            if (!token) {
                this.showNotification('ÙŠØ¬Ø¨ ØªØ³Ø¬ÙŠÙ„ Ø§Ù„Ø¯Ø®ÙˆÙ„ Ø£ÙˆÙ„Ø§Ù‹', 'danger');
                return;
            }

            const response = await fetch(`${apiBaseUrl}/api/settings`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(settingsData)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                this.isDirty = false;
                this.showNotification(result.message || 'ØªÙ… Ø­ÙØ¸ Ø§Ù„Ø¥Ø¹Ø¯Ø§Ø¯Ø§Øª Ø¨Ù†Ø¬Ø§Ø­', 'success');
            } else {
                this.showNotification(result.message || 'ÙØ´Ù„ ÙÙŠ Ø­ÙØ¸ Ø§Ù„Ø¥Ø¹Ø¯Ø§Ø¯Ø§Øª', 'danger');
            }
        } catch (error) {
            log.error('Save settings failed:', error);
            this.showNotification('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø­ÙØ¸ Ø§Ù„Ø¥Ø¹Ø¯Ø§Ø¯Ø§Øª', 'danger');
        }
    }

    /**
     * Collect current settings from form elements
     * @returns {Object}
     */
    collectSettingsData() {
        return {
            language: document.getElementById('language')?.value || 'ar',
            timezone: document.getElementById('timezone')?.value || 'Africa/Cairo',
            dateFormat: document.getElementById('dateFormat')?.value || 'DD/MM/YYYY',
            timeFormat: document.getElementById('timeFormat')?.value || '12',
            currency: document.getElementById('currency')?.value || 'EGP',
            emailNotifications: document.getElementById('emailNotifications')?.checked ?? true,
            smsNotifications: document.getElementById('smsNotifications')?.checked ?? false,
            pushNotifications: document.getElementById('pushNotifications')?.checked ?? true
        };
    }

    /**
     * Reset settings to defaults
     */
    async resetSettings() {
        if (!confirm('Ù‡Ù„ Ø£Ù†Øª Ù…ØªØ£ÙƒØ¯ Ù…Ù† Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† Ø¬Ù…ÙŠØ¹ Ø§Ù„Ø¥Ø¹Ø¯Ø§Ø¯Ø§ØªØŸ')) return;

        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const token = sessionStorage.getItem('authToken');
            if (!token) return;

            const response = await fetch(`${apiBaseUrl}/api/settings/reset`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });

            const result = await response.json();
            if (response.ok && result.success) {
                await this.loadSettings();
                this.showNotification('ØªÙ… Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† Ø§Ù„Ø¥Ø¹Ø¯Ø§Ø¯Ø§Øª', 'success');
            } else {
                this.showNotification(result.message || 'ÙØ´Ù„ Ø¥Ø¹Ø§Ø¯Ø© Ø§Ù„ØªØ¹ÙŠÙŠÙ†', 'danger');
            }
        } catch (error) {
            log.error('Reset settings failed:', error);
            this.showNotification('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø¥Ø¹Ø§Ø¯Ø© Ø§Ù„ØªØ¹ÙŠÙŠÙ†', 'danger');
        }
    }

    /**
     * Send OTP for password reset
     */
    async sendOtp() {
        const phoneInput = document.getElementById('resetPhone');
        const phone = phoneInput?.value?.trim();
        if (!phone) {
            this.showNotification('ÙŠØ±Ø¬Ù‰ Ø¥Ø¯Ø®Ø§Ù„ Ø±Ù‚Ù… Ø§Ù„Ù‡Ø§ØªÙ Ø£ÙˆÙ„Ø§Ù‹', 'warning');
            return;
        }

        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const response = await fetch(`${apiBaseUrl}/api/public/forgot-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ phone })
            });

            const result = await response.json();
            if (response.ok && result.success) {
                this.showNotification('ØªÙ… Ø¥Ø±Ø³Ø§Ù„ Ø±Ù…Ø² Ø§Ù„ØªØ­Ù‚Ù‚ Ø¥Ù„Ù‰ Ù‡Ø§ØªÙÙƒ', 'success');
            } else {
                this.showNotification(result.message || 'ÙØ´Ù„ Ø¥Ø±Ø³Ø§Ù„ Ø±Ù…Ø² Ø§Ù„ØªØ­Ù‚Ù‚', 'danger');
            }
        } catch (error) {
            log.error('Send OTP failed:', error);
            this.showNotification('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø¥Ø±Ø³Ø§Ù„ Ø±Ù…Ø² Ø§Ù„ØªØ­Ù‚Ù‚', 'danger');
        }
    }

    /**
     * Handle password reset form submission
     * @param {Event} e
     */
    async handlePasswordReset(e) {
        e.preventDefault();

        const phone = document.getElementById('resetPhone')?.value?.trim();
        const otp = document.getElementById('resetOtp')?.value?.trim();
        const newPassword = document.getElementById('newPassword')?.value;
        const confirmPassword = document.getElementById('confirmNewPassword')?.value;

        if (!phone || !otp || !newPassword) {
            this.showNotification('ÙŠØ±Ø¬Ù‰ Ù…Ù„Ø¡ Ø¬Ù…ÙŠØ¹ Ø§Ù„Ø­Ù‚ÙˆÙ„ Ø§Ù„Ù…Ø·Ù„ÙˆØ¨Ø©', 'warning');
            return;
        }

        if (newPassword !== confirmPassword) {
            this.showNotification('ÙƒÙ„Ù…ØªØ§ Ø§Ù„Ù…Ø±ÙˆØ± ØºÙŠØ± Ù…ØªØ·Ø§Ø¨Ù‚ØªÙŠÙ†', 'danger');
            return;
        }

        if (newPassword.length < 6) {
            this.showNotification('ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ± ÙŠØ¬Ø¨ Ø£Ù† ØªÙƒÙˆÙ† 6 Ø£Ø­Ø±Ù Ø¹Ù„Ù‰ Ø§Ù„Ø£Ù‚Ù„', 'danger');
            return;
        }

        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const response = await fetch(`${apiBaseUrl}/api/public/reset-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ phone, otp, newPassword })
            });

            const result = await response.json();
            if (response.ok && result.success) {
                this.showNotification('ØªÙ… ØªØºÙŠÙŠØ± ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ± Ø¨Ù†Ø¬Ø§Ø­', 'success');
                document.getElementById('passwordResetForm')?.reset();
            } else {
                this.showNotification(result.message || 'ÙØ´Ù„ ØªØºÙŠÙŠØ± ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±', 'danger');
            }
        } catch (error) {
            log.error('Password reset failed:', error);
            this.showNotification('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ ØªØºÙŠÙŠØ± ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±', 'danger');
        }
    }

    /**
     * Handle logout
     */
    handleLogout() {
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('userData');
        window.location.href = '/login.html';
    }

    /**
     * Clear application cache
     */
    clearCache() {
        if (!confirm('Ù‡Ù„ Ø£Ù†Øª Ù…ØªØ£ÙƒØ¯ Ù…Ù† Ù…Ø³Ø­ Ø§Ù„Ø°Ø§ÙƒØ±Ø© Ø§Ù„Ù…Ø¤Ù‚ØªØ©ØŸ')) return;
        sessionStorage.clear();
        this.showNotification('ØªÙ… Ù…Ø³Ø­ Ø§Ù„Ø°Ø§ÙƒØ±Ø© Ø§Ù„Ù…Ø¤Ù‚ØªØ©', 'success');
    }

    /**
     * Helper: Set select element value
     */
    setSelectValue(id, value) {
        const el = document.getElementById(id);
        if (el && value) el.value = value;
    }

    /**
     * Helper: Set checkbox value
     */
    setCheckboxValue(id, value) {
        const el = document.getElementById(id);
        if (el && value !== undefined) el.checked = !!value;
    }

    /**
     * Show notification toast/alert
     * @param {string} message
     * @param {string} type - success, danger, warning, info
     */
    showNotification(message, type = 'info') {
        // Remove existing notifications
        document.querySelectorAll('.settings-notification').forEach(n => n.remove());

        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show settings-notification`;
        notification.style.cssText = 'position: fixed; top: 20px; left: 50%; transform: translateX(-50%); z-index: 9999; min-width: 300px;';
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        document.body.appendChild(notification);

        setTimeout(() => {
            if (notification.parentNode) notification.remove();
        }, 4000);
    }

    /**
     * Get API base URL
     * @returns {string}
     */
    getApiBaseUrl() {
        return window.getApiBaseUrl();
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new SettingsPageHandler();
});
