import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('contact');

/**
 * Twsela CMS - Contact Page Handler
 * Handles contact form submission and office locations display
 */

class ContactPageHandler {
    constructor() {
        this.form = null;
        this.isSubmitting = false;
        this.init();
    }

    /**
     * Initialize the contact page
     */
    init() {
        this.form = document.getElementById('contactForm');
        if (!this.form) {
            log.warn('Contact form not found');
            return;
        }

        this.setupEventListeners();
        this.loadOfficeLocations();
    }

    /**
     * Setup form event listeners
     */
    setupEventListeners() {
        this.form.addEventListener('submit', (e) => this.handleSubmit(e));

        // Live validation
        const requiredFields = this.form.querySelectorAll('[required]');
        requiredFields.forEach(field => {
            field.addEventListener('blur', () => this.validateField(field));
            field.addEventListener('input', () => this.clearFieldError(field));
        });
    }

    /**
     * Handle form submission
     * @param {Event} e - Submit event
     */
    async handleSubmit(e) {
        e.preventDefault();

        if (this.isSubmitting) return;
        if (!this.validateForm()) return;

        this.isSubmitting = true;
        this.setSubmitLoading(true);

        try {
            const formData = this.getFormData();
            const apiBaseUrl = this.getApiBaseUrl();

            const response = await fetch(`${apiBaseUrl}/api/public/contact`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                this.showSuccess(result.message || 'ØªÙ… Ø¥Ø±Ø³Ø§Ù„ Ø±Ø³Ø§Ù„ØªÙƒ Ø¨Ù†Ø¬Ø§Ø­! Ø³Ù†ØªÙˆØ§ØµÙ„ Ù…Ø¹Ùƒ Ù‚Ø±ÙŠØ¨Ø§Ù‹.');
                this.form.reset();
            } else {
                this.showError(result.message || 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø¥Ø±Ø³Ø§Ù„ Ø§Ù„Ø±Ø³Ø§Ù„Ø©. ÙŠØ±Ø¬Ù‰ Ø§Ù„Ù…Ø­Ø§ÙˆÙ„Ø© Ù…Ø±Ø© Ø£Ø®Ø±Ù‰.');
            }
        } catch (error) {
            log.error('Contact form submission failed:', error);
            this.showError('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø§ØªØµØ§Ù„ Ø¨Ø§Ù„Ø®Ø§Ø¯Ù…. ÙŠØ±Ø¬Ù‰ Ø§Ù„Ù…Ø­Ø§ÙˆÙ„Ø© Ù„Ø§Ø­Ù‚Ø§Ù‹.');
        } finally {
            this.isSubmitting = false;
            this.setSubmitLoading(false);
        }
    }

    /**
     * Get form data as object
     * @returns {Object} Form data
     */
    getFormData() {
        return {
            firstName: document.getElementById('firstName')?.value?.trim() || '',
            lastName: document.getElementById('lastName')?.value?.trim() || '',
            email: document.getElementById('email')?.value?.trim() || '',
            subject: document.getElementById('subject')?.value || '',
            message: document.getElementById('message')?.value?.trim() || ''
        };
    }

    /**
     * Validate the entire form
     * @returns {boolean} True if valid
     */
    validateForm() {
        let isValid = true;
        const requiredFields = this.form.querySelectorAll('[required]');
        requiredFields.forEach(field => {
            if (!this.validateField(field)) {
                isValid = false;
            }
        });
        return isValid;
    }

    /**
     * Validate a single field
     * @param {HTMLElement} field
     * @returns {boolean} True if valid
     */
    validateField(field) {
        const value = field.value?.trim();
        if (!value) {
            this.showFieldError(field, 'Ù‡Ø°Ø§ Ø§Ù„Ø­Ù‚Ù„ Ù…Ø·Ù„ÙˆØ¨');
            return false;
        }
        if (field.type === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
            this.showFieldError(field, 'Ø§Ù„Ø¨Ø±ÙŠØ¯ Ø§Ù„Ø¥Ù„ÙƒØªØ±ÙˆÙ†ÙŠ ØºÙŠØ± ØµØ­ÙŠØ­');
            return false;
        }
        this.clearFieldError(field);
        return true;
    }

    /**
     * Show field error
     */
    showFieldError(field, message) {
        this.clearFieldError(field);
        field.classList.add('is-invalid');
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback';
        errorDiv.textContent = message;
        field.parentNode.appendChild(errorDiv);
    }

    /**
     * Clear field error
     */
    clearFieldError(field) {
        field.classList.remove('is-invalid');
        const feedback = field.parentNode.querySelector('.invalid-feedback');
        if (feedback) feedback.remove();
    }

    /**
     * Set submit button loading state
     * @param {boolean} loading
     */
    setSubmitLoading(loading) {
        const btn = this.form.querySelector('button[type="submit"]');
        if (!btn) return;
        if (loading) {
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Ø¬Ø§Ø±ÙŠ Ø§Ù„Ø¥Ø±Ø³Ø§Ù„...';
        } else {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-paper-plane"></i> Ø¥Ø±Ø³Ø§Ù„ Ø§Ù„Ø±Ø³Ø§Ù„Ø©';
        }
    }

    /**
     * Show success message
     * @param {string} message
     */
    showSuccess(message) {
        this.showAlert(message, 'success');
    }

    /**
     * Show error message
     * @param {string} message
     */
    showError(message) {
        this.showAlert(message, 'danger');
    }

    /**
     * Show alert above the form
     * @param {string} message
     * @param {string} type - Bootstrap alert type
     */
    showAlert(message, type) {
        // Remove existing alerts
        const existingAlerts = this.form.parentNode.querySelectorAll('.alert');
        existingAlerts.forEach(a => a.remove());

        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible fade show`;
        alert.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        this.form.parentNode.insertBefore(alert, this.form);

        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            if (alert.parentNode) alert.remove();
        }, 5000);
    }

    /**
     * Load office locations
     */
    async loadOfficeLocations() {
        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const response = await fetch(`${apiBaseUrl}/api/public/contact/offices`);
            if (!response.ok) return;
            // Office locations are currently static in the HTML
        } catch (error) {
            // Silently fail - office locations are shown statically
        }
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
    new ContactPageHandler();
});
