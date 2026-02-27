import { Logger } from './Logger.js';
const log = Logger.getLogger('BasePageHandler');

/**
 * Twsela CMS - Base Page Handler
 * Common functionality for all pages
 */

// BasePageHandler initialization - console.log removed for cleaner console

class BasePageHandler {
    constructor(pageName) {
        this.pageName = pageName;
        this.isInitialized = false;
        this.services = {};
        this.init();
    }

    /**
     * Initialize the page
     */
    async init() {
        // Initializing page - console.log removed for cleaner console
        
        if (this.isInitialized) {
            return;
        }

        try {
            // Wait for services to be available
            await this.waitForServices();
            
            // ÙØ­Øµ Ø¥Ø°Ø§ ÙƒØ§Ù†Øª Ø§Ù„ØµÙØ­Ø© ØªØªØ¹Ø§Ù…Ù„ Ù…Ø¹ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ø¨Ù†ÙØ³Ù‡Ø§
            if (this.shouldSkipAuthCheck()) {
                log.debug(`ðŸ“„ ${this.pageName} handles its own authentication, skipping BasePageHandler auth check`);
                // Initialize page-specific functionality without auth check
                await this.initializePage();
                this.setupEventListeners();
                this.updateUserInfo();
                this.isInitialized = true;
                return;
            }
            
            // Verify authentication
            const isAuthenticated = await this.verifyAuthentication();
            if (!isAuthenticated) {
                this.redirectToLogin();
                return;
            }
            
            // Initialize page-specific functionality
            await this.initializePage();
            
            // Setup event listeners
            this.setupEventListeners();
            
            // Update user info
            this.updateUserInfo();
            
            this.isInitialized = true;
            // Page initialized successfully - console.log removed for cleaner console
            
        } catch (error) {
            log.error(`âŒ BasePageHandler Error - ${this.pageName} Initialization:`, {
                pageName: this.pageName,
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
        }
    }

    /**
     * Check if page should skip authentication check
     * @returns {boolean} Whether to skip auth check
     */
    shouldSkipAuthCheck() {
        // Ù‚Ø§Ø¦Ù…Ø© Ø§Ù„ØµÙØ­Ø§Øª Ø§Ù„ØªÙŠ ØªØªØ¹Ø§Ù…Ù„ Ù…Ø¹ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ø¨Ù†ÙØ³Ù‡Ø§
        const pagesWithOwnAuth = [
            'Owner Zones',
            'Owner Dashboard',
            'Merchant Dashboard',
            'Courier Dashboard',
            'Warehouse Dashboard'
        ];
        
        return pagesWithOwnAuth.includes(this.pageName);
    }

    /**
     * Wait for required services to be available
     */
    async waitForServices() {
        let attempts = 0;
        const maxAttempts = 50; // 5 seconds max
        
        log.debug('ðŸ”„ Waiting for services to be available...');
        
        while (attempts < maxAttempts) {
            if (window.authService && window.apiService && window.NotificationService) {
                this.services.auth = window.authService;
                this.services.api = window.apiService;
                this.services.notification = window.NotificationService;
                log.debug('âœ… All required services are available');
                return;
            }
            
            if (attempts % 10 === 0) { // Log every 10 attempts
                log.debug(`â³ Waiting for services... (attempt ${attempts + 1}/${maxAttempts})`);
                log.debug('Available services:', {
                    authService: !!window.authService,
                    apiService: !!window.apiService,
                    notificationService: !!window.NotificationService
                });
            }
            
            await new Promise(resolve => setTimeout(resolve, 100));
            attempts++;
        }
        
        log.error('âŒ Services not available after timeout');
        log.error('Available services:', {
            authService: !!window.authService,
            apiService: !!window.apiService,
            notificationService: !!window.NotificationService
        });
        
        throw new Error('Services not available after timeout');
    }

    /**
     * Verify user authentication
     */
    async verifyAuthentication() {
        try {
            // ÙØ­Øµ ÙˆØ¬ÙˆØ¯ Ø®Ø¯Ù…Ø© Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø©
            if (!this.services || !this.services.auth) {
                log.error('âŒ Auth service not available');
                this.redirectToLogin();
                return false;
            }

            const token = this.services.auth.getToken();
            if (!token) {
                log.warn('âš ï¸ No authentication token found');
                this.redirectToLogin();
                return false;
            }

            log.debug('ðŸ”„ Verifying authentication...');
            const response = await fetch(`${this.services.auth.getApiBaseUrl()}/api/auth/me`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.status === 401) {
                log.warn('âš ï¸ Authentication failed (401), clearing auth data');
                this.services.auth.clearAuthData();
                this.redirectToLogin();
                return false;
            }

            if (response.status === 403) {
                log.warn('âš ï¸ Access forbidden (403)');
                this.redirectToLogin();
                return false;
            }

            if (response.ok) {
                const user = await response.json();
                
                // Store updated user data
                this.services.auth.storeUserData(user);
                this.currentUser = user;
                log.debug('âœ… Authentication verified successfully');
                return true;
            }

            log.warn('âš ï¸ Authentication verification failed with status:', response.status);
            this.redirectToLogin();
            return false;

        } catch (error) {
            log.error(`âŒ BasePageHandler Error - Authentication Verification:`, {
                pageName: this.pageName,
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
            this.redirectToLogin();
            return false;
        }
    }

    /**
     * Initialize page-specific functionality (to be overridden)
     */
    async initializePage() {
        // Override in child classes
    }

    /**
     * Setup event listeners (to be overridden)
     */
    setupEventListeners() {
        // Override in child classes
    }

    /**
     * Update user info in header
     */
    updateUserInfo() {
        try {
            if (this.currentUser) {
                const userNameEl = document.querySelector('.user-name');
                if (userNameEl) {
                    userNameEl.textContent = this.currentUser.name || 'Ø§Ù„Ù…Ø³ØªØ®Ø¯Ù…';
                }
            }
        } catch (error) {
            log.error(`âŒ BasePageHandler Error - Update User Info:`, {
                pageName: this.pageName,
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
        }
    }

    /**
     * Show error message
     */
    showError(message) {
        // Error logging only - no user notification
    }

    /**
     * Show success message
     */
    showSuccess(message) {
        if (this.services.notification) {
            this.services.notification.success(message);
        }
    }

    /**
     * Show info message
     */
    showInfo(message) {
        if (this.services.notification) {
            this.services.notification.info(message);
        }
    }

    /**
     * Redirect to login page
     */
    redirectToLogin() {
        window.location.replace('/login.html');
    }

    /**
     * Handle logout
     */
    async handleLogout() {
        try {
            await this.services.auth.logout();
        } catch (error) {
            log.error(`âŒ BasePageHandler Error - Update User Info:`, {
                pageName: this.pageName,
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
        }
    }

    /**
     * Get current user
     */
    getCurrentUser() {
        return this.currentUser || this.services.auth.getCurrentUser();
    }

    /**
     * Check if user has specific role
     */
    hasRole(role) {
        const user = this.getCurrentUser();
        return user && user.role && user.role.name === role;
    }

    /**
     * Format date for display
     */
    formatDate(dateString) {
        if (!dateString) return 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('ar-SA', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch (error) {
            return 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        }
    }

    /**
     * Format currency for display
     */
    formatCurrency(amount) {
        if (!amount) return '0.00 Ø±.Ø³';
        
        try {
            return new Intl.NumberFormat('ar-SA', {
                style: 'currency',
                currency: 'SAR'
            }).format(amount);
        } catch (error) {
            return `${amount} Ø±.Ø³`;
        }
    }

    /**
     * Get status color for badge
     */
    getStatusColor(status) {
        const statusColors = {
            'PENDING': 'warning',
            'CONFIRMED': 'info',
            'IN_TRANSIT': 'primary',
            'DELIVERED': 'success',
            'CANCELLED': 'danger',
            'RETURNED': 'secondary',
            'ACTIVE': 'success',
            'INACTIVE': 'secondary',
            'SUSPENDED': 'warning'
        };
        return statusColors[status?.name] || statusColors[status] || 'secondary';
    }

    /**
     * Refresh page data
     */
    async refresh() {
        await this.initializePage();
    }
}

// Export for use in other files
window.BasePageHandler = BasePageHandler;

// BasePageHandler loaded - console.log removed for cleaner console
