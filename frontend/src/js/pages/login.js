import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('login');

/**
 * Twsela CMS - Login Page Handler
 * Handles all login page functionality including form validation, authentication, and UI interactions
 * Follows DRY principle with unified login page logic
 */

class LoginPageHandler {
    constructor() {
        this.form = null;
        this.isSubmitting = false;
        this.redirectTimeout = null;
        this.isActive = true;
        this.init();
    }

    /**
     * Initialize login page
     */
    init() {
        
        // Prevent App.js from interfering with login process
        if (window.twselaApp) {
            window.twselaApp.isLoginPage = true;
        }
        
        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setup());
        } else {
            this.setup();
        }
    }

    /**
     * Setup login page
     */
    setup() {
        this.form = document.getElementById('loginForm');
        if (!this.form) {

            return;
        }

        this.setupEventListeners();
        this.setupFormValidation();
        this.setupPasswordToggle();
        this.setupForgotPassword();
        this.setupRegisterLink();
        this.checkExistingAuth();
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Form submission
        this.form.addEventListener('submit', (e) => this.handleSubmit(e));

        // Input validation on blur
        const inputs = this.form.querySelectorAll('input[required]');
        inputs.forEach(input => {
            input.addEventListener('blur', () => this.validateField(input));
            input.addEventListener('input', () => this.clearFieldError(input));
        });

        // Enter key on inputs
        inputs.forEach(input => {
            input.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    this.handleSubmit(e);
                }
            });
        });
        
    }

    /**
     * Setup form validation
     */
    setupFormValidation() {
        // Phone number validation
        const phoneInput = document.getElementById('phone');
        if (phoneInput) {
            phoneInput.addEventListener('input', (e) => {
                this.formatPhoneInput(e.target);
            });
        }

        // Password validation
        const passwordInput = document.getElementById('password');
        if (passwordInput) {
            passwordInput.addEventListener('input', (e) => {
                this.validatePasswordStrength(e.target.value);
            });
        }
        
    }

    /**
     * Setup password toggle functionality
     */
    setupPasswordToggle() {
        const toggleButton = document.getElementById('togglePassword');
        const passwordInput = document.getElementById('password');

        if (toggleButton && passwordInput) {
            toggleButton.addEventListener('click', () => {
                const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
                passwordInput.setAttribute('type', type);
                
                const icon = toggleButton.querySelector('i');
                if (icon) {
                    icon.classList.toggle('fa-eye');
                    icon.classList.toggle('fa-eye-slash');
                }
            });
        } else {

        }
    }

    /**
     * Setup forgot password functionality
     */
    setupForgotPassword() {
        const forgotPasswordLink = document.getElementById('forgotPasswordLink');
        const forgotPasswordModal = document.getElementById('forgotPasswordModal');
        const resetForm = document.getElementById('resetForm');
        const sendResetBtn = document.getElementById('sendResetBtn');

        if (forgotPasswordLink && forgotPasswordModal) {
            forgotPasswordLink.addEventListener('click', (e) => {
                e.preventDefault();
                this.showForgotPasswordModal();
            });
        }

        if (sendResetBtn && resetForm) {
            sendResetBtn.addEventListener('click', () => {
                this.handlePasswordReset();
            });
        }
        
    }

    /**
     * Setup register link
     */
    setupRegisterLink() {
        const registerLink = document.getElementById('registerLink');
        if (registerLink) {
            registerLink.addEventListener('click', (e) => {
                e.preventDefault();
                this.handleRegisterClick();
            });
        } else {

        }
    }

    /**
     * Check if user is already authenticated
     */
    checkExistingAuth() {
        const token = sessionStorage.getItem('authToken');
        if (token) {
            // Only validate token if we're not on login page AND login is not in progress
            if (!window.location.pathname.includes('login.html') && 
                (!window.authService || !window.authService.isLoginInProgress)) {
                // Add a small delay to ensure DOM is fully loaded
                setTimeout(() => {
                    this.validateExistingToken();
                }, 100);
            }
        }
    }

    /**
     * Validate existing token
     */
    async validateExistingToken() {
        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const response = await fetch(`${apiBaseUrl}/api/auth/me`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const user = await response.json();
                if (user) {
                    // User is already authenticated, redirect to dashboard
                    const userRole = user.role ? user.role.name : null;
                    if (userRole) {
                        this.redirectToDashboard(userRole);
                    } else {

                        sessionStorage.removeItem('authToken');
                        sessionStorage.removeItem('userData');
            } else {
                // Token is invalid, clear it
                sessionStorage.removeItem('authToken');
                sessionStorage.removeItem('userData');
            }
        } catch (error) {

            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('userData');
        }
    }

    /**
     * Handle form submission
     * @param {Event} e - Form submit event
     */
    async handleSubmit(e) {
        e.preventDefault();

        if (this.isSubmitting) {
            return;
        }

        // Validate form
        if (!this.validateForm()) {
            return;
        }

        this.isSubmitting = true;
        this.setSubmitButtonLoading(true);

        try {
            const formData = this.getFormData();
            
            const result = await this.performLogin(formData);

            if (result.success) {
                this.handleLoginSuccess(result.data);
            } else {
                this.handleLoginError(result.message, result.errors);
            }
        } catch (error) {

            this.handleLoginError('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø§ØªØµØ§Ù„ Ø¨Ø§Ù„Ø®Ø§Ø¯Ù…');
        } finally {
            this.isSubmitting = false;
            this.setSubmitButtonLoading(false);
        }
    }

    /**
     * Validate form
     * @returns {boolean} Validation result
     */
    validateForm() {
        let isValid = true;

        // Clear previous errors
        this.clearAllErrors();

        // Validate phone number
        const phoneInput = document.getElementById('phone');
        if (phoneInput) {
            const phoneValue = phoneInput.value.trim();
            if (!phoneValue) {
                this.showFieldError(phoneInput, 'Ø±Ù‚Ù… Ø§Ù„Ù‡Ø§ØªÙ Ù…Ø·Ù„ÙˆØ¨');
                isValid = false;
            } else if (!this.validatePhone(phoneValue)) {
                this.showFieldError(phoneInput, 'Ø±Ù‚Ù… Ø§Ù„Ù‡Ø§ØªÙ ØºÙŠØ± ØµØ­ÙŠØ­');
                isValid = false;
            }
        }

        // Validate password
        const passwordInput = document.getElementById('password');
        if (passwordInput) {
            const passwordValue = passwordInput.value;
            if (!passwordValue) {
                this.showFieldError(passwordInput, 'ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ± Ù…Ø·Ù„ÙˆØ¨Ø©');
                isValid = false;
            } else if (!this.validatePassword(passwordValue)) {
                this.showFieldError(passwordInput, 'ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ± Ù…Ø·Ù„ÙˆØ¨Ø©');
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Validate phone number - Use centralized utility
     * @param {string} phone - Phone number
     * @returns {boolean} Validation result
     */
    validatePhone(phone) {
        // Use centralized phone validation from utilities
        if (window.Utils && window.Utils.validatePhone) {
            return window.Utils.validatePhone(phone);
        }
        
        // Fallback if utilities not available
        if (!phone) return false;
        const cleaned = phone.replace(/\D/g, '');
        return (cleaned.startsWith('20') && cleaned.length === 12) ||
               (cleaned.startsWith('0') && cleaned.length === 11) ||
               (cleaned.length === 10) ||
               (cleaned.length >= 10 && cleaned.length <= 12);
    }

    /**
     * Validate password
     * @param {string} password - Password
     * @returns {boolean} Validation result
     */
    validatePassword(password) {
        const isValid = password && password.length > 0;
        return isValid;
    }

    /**
     * Validate individual field
     * @param {HTMLElement} field - Field element
     */
    validateField(field) {
        if (!field) {

            return;
        }
        
        const value = field.value.trim();
        let isValid = true;
        let errorMessage = '';

        if (field.hasAttribute('required') && !value) {
            isValid = false;
            errorMessage = 'Ù‡Ø°Ø§ Ø§Ù„Ø­Ù‚Ù„ Ù…Ø·Ù„ÙˆØ¨';
        } else if (field.type === 'tel' && value && !this.validatePhone(value)) {
            isValid = false;
            errorMessage = 'Ø±Ù‚Ù… Ø§Ù„Ù‡Ø§ØªÙ ØºÙŠØ± ØµØ­ÙŠØ­';
        }

        if (isValid) {
            this.clearFieldError(field);
        } else {
            this.showFieldError(field, errorMessage);
        }
    }

    /**
     * Show field error
     * @param {HTMLElement} field - Field element
     * @param {string} message - Error message
     */
    showFieldError(field, message) {
        if (!field) {

            return;
        }
        
        field.classList.add('is-invalid');
        
        // Remove existing error message
        const existingError = field.parentNode.querySelector('.invalid-feedback');
        if (existingError) {
            existingError.remove();
        }

        // Add new error message
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback';
        errorDiv.textContent = message;
        field.parentNode.appendChild(errorDiv);
        
    }

    /**
     * Clear field error
     * @param {HTMLElement} field - Field element
     */
    clearFieldError(field) {
        if (!field) {

            return;
        }
        
        field.classList.remove('is-invalid');
        const errorDiv = field.parentNode.querySelector('.invalid-feedback');
        if (errorDiv) {
            errorDiv.remove();
        }
    }

    /**
     * Clear all errors
     */
    clearAllErrors() {
        if (!this.form) {

            return;
        }
        
        const invalidFields = this.form.querySelectorAll('.is-invalid');
        invalidFields.forEach(field => {
            this.clearFieldError(field);
        });
    }

    /**
     * Format phone input - Keep original format as entered by user
     * @param {HTMLElement} input - Phone input element
     */
    formatPhoneInput(input) {
        if (!input) {

            return;
        }
        
        // Don't modify the input value - keep it as entered by user
        // Only remove non-digit characters for validation purposes
        const value = input.value;
        
        // Store the original value for validation
        input.dataset.originalValue = value;
        
    }

    /**
     * Validate password strength
     * @param {string} password - Password
     */
    validatePasswordStrength(password) {
        // This could be enhanced with visual password strength indicator
        // For now, just basic validation
        const isValid = password.length >= 8;
        return isValid;
    }

    /**
     * Get form data - Keep phone number as entered by user
     * @returns {Object} Form data
     */
    getFormData() {
        const formData = new FormData(this.form);
        
        // Return data exactly as entered by user without any formatting
        const phone = formData.get('phone');
        const password = formData.get('password');
        
        
        return {
            phone: phone ? phone.trim() : '', // Keep original format
            password: password || ''
        };
    }

    /**
     * Perform login
     * @param {Object} credentials - Login credentials
     * @returns {Promise<Object>} Login result
     */
    async performLogin(credentials) {
        try {
            // Use the correct API base URL
            const apiBaseUrl = this.getApiBaseUrl();
            
            const response = await fetch(`${apiBaseUrl}/api/auth/login`, {
                method: 'POST',
                mode: 'cors',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(credentials)
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return {
                    success: true,
                    data: data.data || data,
                    message: data.message
                };
            } else {
                return {
                    success: false,
                    message: data.message || 'ÙØ´Ù„ ÙÙŠ ØªØ³Ø¬ÙŠÙ„ Ø§Ù„Ø¯Ø®ÙˆÙ„',
                    errors: data.errors || []
                };
            }
        } catch (error) {
            let errorMessage = 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø§ØªØµØ§Ù„ Ø¨Ø§Ù„Ø®Ø§Ø¯Ù…';
            
            if (error.name === 'TypeError' && error.message.includes('Failed to fetch')) {
                errorMessage = 'Ù„Ø§ ÙŠÙ…ÙƒÙ† Ø§Ù„Ø§ØªØµØ§Ù„ Ø¨Ø§Ù„Ø®Ø§Ø¯Ù…. ØªØ£ÙƒØ¯ Ù…Ù† Ø£Ù† Ø§Ù„Ø¨Ø§Ùƒ Ø¥Ù†Ø¯ ÙŠØ¹Ù…Ù„ Ø¹Ù„Ù‰ Ø§Ù„Ù…Ù†ÙØ° 8080';
            } else if (error.name === 'NetworkError') {
                errorMessage = 'Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø´Ø¨ÙƒØ©. ØªØ­Ù‚Ù‚ Ù…Ù† Ø§ØªØµØ§Ù„Ùƒ Ø¨Ø§Ù„Ø¥Ù†ØªØ±Ù†Øª';
            }
            
            return {
                success: false,
                message: errorMessage
            };
        }
    }

    /**
     * Get API base URL - Use centralized utility
     * @returns {string} API base URL
     */
    getApiBaseUrl() {
        return window.getApiBaseUrl();
    }

    /**
     * Handle successful login
     * @param {Object} data - Login data
     */
    handleLoginSuccess(data) {
        
        // Store authentication data using AuthService
        if (window.authService) {
            
            // Handle different data structures
            if (data.data) {
                // API response has data wrapper
                window.authService.storeAuthData(data.data);
            } else {
                // Direct data
                window.authService.storeAuthData(data);
            }
            
            // Verify data was stored
            const storedToken = window.authService.getToken();
            const storedUser = window.authService.getCurrentUser();
        } else {
            // Fallback if AuthService not available
            if (data.token) {
                sessionStorage.setItem('authToken', data.token);
            }
            if (data.user) {
                sessionStorage.setItem('userData', JSON.stringify(data.user));
            }
        }

        // Show success message
        this.showSuccessMessage('ØªÙ… ØªØ³Ø¬ÙŠÙ„ Ø§Ù„Ø¯Ø®ÙˆÙ„ Ø¨Ù†Ø¬Ø§Ø­');

        // Set login in progress flag to prevent double redirect
        if (window.authService) {
            window.authService.isLoginInProgress = true;
        }
        
        // Prevent App.js from interfering
        if (window.twselaApp) {
            window.twselaApp.isLoginPage = true;
        }
        
        // Set session flag to indicate just logged in
        sessionStorage.setItem('justLoggedIn', 'true');

        // Redirect to appropriate dashboard with proper error handling
        // Clear any existing timeout first
        if (this.redirectTimeout) {
            clearTimeout(this.redirectTimeout);
        }
        
        // Add delay to ensure data is fully stored and App.js doesn't interfere
        this.redirectTimeout = setTimeout(() => {
            try {
                
                // Check for role - API returns role at top level
                let userRole = null;
                
                // First check top-level role (actual API response structure)
                if (data.role) {
                    userRole = typeof data.role === 'string' ? data.role : data.role.name;
                } else if (data.user && data.user.role) {
                    // Fallback for nested role structure
                    userRole = typeof data.user.role === 'string' ? data.user.role : data.user.role.name;
                }
                
                
                if (userRole) {
                    this.redirectToDashboard(userRole);
                } else {

                    if (data.user) {
                    }
                    // Redirect to login page instead of index
                    window.location.replace('/login.html');
                }
            } catch (error) {

                window.location.replace('/login.html');
            }
        }, 1500); // Increased timeout to ensure proper redirect
    }

    /**
     * Handle login error
     * @param {string} message - Error message
     * @param {Array} errors - Field errors
     */
    handleLoginError(message, errors = []) {
        

        // Show field-specific errors
        if (errors && errors.length > 0) {
            errors.forEach(error => {
                const field = this.form.querySelector(`[name="${error.field}"]`);
                if (field) {
                    this.showFieldError(field, error.message);
                }
            });
        }
        
    }

    /**
     * Show success message - Use centralized utility
     * @param {string} message - Success message
     */
    showSuccessMessage(message) {
        NotificationService.success(message);
    }

    /**
     * Set submit button loading state
     * @param {boolean} loading - Loading state
     */
    setSubmitButtonLoading(loading) {
        const submitButton = document.getElementById('loginBtn');
        if (!submitButton) {

            return;
        }

        const buttonText = submitButton.querySelector('.btn-text');
        const buttonSpinner = submitButton.querySelector('.btn-spinner');

        if (loading) {
            submitButton.disabled = true;
            if (buttonText) buttonText.classList.add('d-none');
            if (buttonSpinner) buttonSpinner.classList.remove('d-none');
        } else {
            submitButton.disabled = false;
            if (buttonText) buttonText.classList.remove('d-none');
            if (buttonSpinner) buttonSpinner.classList.add('d-none');
        }
        
    }

    /**
     * Redirect to dashboard based on user role - Use centralized utility
     * @param {string} role - User role
     */
    redirectToDashboard(role) {
        try {
            
            // Mark login page as inactive to prevent App.js interference
            this.isActive = false;
            
            // Reset login in progress flag before redirect
            if (window.authService) {
                window.authService.isLoginInProgress = false;
            }
            
            // Disable App.js authentication check
            if (window.twselaApp) {
                window.twselaApp.isLoginPage = true;
            }
            
            // Use centralized redirect function from utilities
            if (window.Utils && window.Utils.redirectToDashboard) {
                window.Utils.redirectToDashboard(role);
                return;
            }
            
            // Fallback if utilities not available
            const roleRedirects = {
                'OWNER': '/owner/dashboard.html',
                'ADMIN': '/admin/dashboard.html',
                'MERCHANT': '/merchant/dashboard.html',
                'COURIER': '/courier/dashboard.html',
                'WAREHOUSE_MANAGER': '/warehouse/dashboard.html',
                'WAREHOUSE': '/warehouse/dashboard.html'
            };

            const redirectUrl = roleRedirects[role] || '/login.html';
            
            // Clear any existing timeouts to prevent multiple redirects
            if (this.redirectTimeout) {
                clearTimeout(this.redirectTimeout);
                this.redirectTimeout = null;
            }
            
            // Use replace instead of href to prevent back button issues
            window.location.replace(redirectUrl);
        } catch (error) {

            window.location.replace('/login.html');
        }
    }

    /**
     * Show forgot password modal
     */
    showForgotPasswordModal() {
        const modal = document.getElementById('forgotPasswordModal');
        if (modal && window.bootstrap) {
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
        } else {

        }
    }

    /**
     * Handle password reset
     */
    async handlePasswordReset() {
        const phoneInput = document.getElementById('resetPhone');
        if (!phoneInput) {

            return;
        }

        const phone = phoneInput.value.trim();
        if (!phone) {
            
            return;
        }

        if (!this.validatePhone(phone)) {
            
            return;
        }

        try {
            // Here you would implement the password reset logic
            this.showSuccessMessage('ØªÙ… Ø¥Ø±Ø³Ø§Ù„ Ø±Ù…Ø² Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±');
        } catch (error) { log.error('Unhandled error:', error); }
    }

    /**
     * Handle register link click
     */
    handleRegisterClick() {
        // For now, just show a message
        this.showInfoMessage('Ù…ÙŠØ²Ø© Ø¥Ù†Ø´Ø§Ø¡ Ø§Ù„Ø­Ø³Ø§Ø¨ Ø³ØªÙƒÙˆÙ† Ù…ØªØ§Ø­Ø© Ù‚Ø±ÙŠØ¨Ø§Ù‹');
    }

    /**
     * Show info message
     * @param {string} message - Info message
     */
    showInfoMessage(message) {
        if (window.UIUtils) {
            window.UIUtils.showInfo(message);
        } else {
            alert(message);
        }
    }
}

// Initialize login page when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.loginPageHandler = new LoginPageHandler();
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LoginPageHandler;
}
