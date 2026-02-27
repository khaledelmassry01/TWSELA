import { Logger } from './shared/Logger.js';
const log = Logger.getLogger('app');

/**
 * Twsela CMS - Unified Main Application
 * Consolidated main application entry point with all initialization logic
 * Follows DRY principle with unified application management
 * This file contains all app logic merged from app.js and app_base.js
 */

// App initialization - console.log removed for cleaner console

/**
 * Main Application Class
 * Provides unified access to all services and utilities
 */
class TwselaApp {
    constructor() {
        this.isInitialized = false;
        this.services = {};
        this.handlers = {};
        this.currentUser = null;
        this.apiBaseUrl = this.getApiBaseUrl();
        this.init();
    }

    /**
     * Initialize the application
     */
    async init() {
        
        if (this.isInitialized) {
            return;
        }
        
        try {
            // Wait for DOM to be ready
            if (document.readyState === 'loading') {
                await new Promise(resolve => {
                    document.addEventListener('DOMContentLoaded', resolve);
                });
            }

            // Check if this is a login page - if so, skip initialization
            if (window.location.pathname.includes('login.html') || 
                (window.loginPageHandler && window.loginPageHandler.isActive)) {
                return;
            }

            // Initialize page - check authentication and setup routing
            await this.initializePage();
            
            // Initialize services
            this.initializeServices();
            
            // Initialize page handlers
            this.initializePageHandlers();
            
            // Setup global listeners
            this.setupGlobalListeners();
            
            this.isInitialized = true;
        } catch (error) {
            // Failed to initialize TwselaApp
        }
    }

    /**
     * Initialize page - check authentication and setup routing
     */
    async initializePage() {
        
        // Check if login is in progress to prevent double redirect
        if (window.authService && window.authService.isLoginInProgress) {
            // Wait a bit for login to complete
            setTimeout(() => {
                this.setupPageSpecificHandlers();
            }, 3000); // Increased timeout
            return;
        }
        
        // Additional check: if we just logged in, skip auth check
        const justLoggedIn = sessionStorage.getItem('justLoggedIn');
        if (justLoggedIn) {
            sessionStorage.removeItem('justLoggedIn');
            this.setupPageSpecificHandlers();
            return;
        }
        
        // Skip authentication check for login page and public pages
        const currentPage = this.getCurrentPage();
        const publicPages = ['login', 'index', '404', 'contact'];
        
        // Check if this is a login page or if LoginPageHandler is active
        if (publicPages.includes(currentPage) || 
            window.location.pathname.includes('login.html') ||
            this.isLoginPage ||
            (window.loginPageHandler && window.loginPageHandler.isActive)) {
            return;
        }
        
        // Additional check: if we're on login page, don't redirect
        if (window.location.pathname === '/login.html' || window.location.pathname.endsWith('/login.html')) {
            return;
        }

        // ÙØ­Øµ Ø¥Ø°Ø§ ÙƒØ§Ù†Øª Ø§Ù„ØµÙØ­Ø© ØªÙ‚ÙˆÙ… Ø¨ÙØ­Øµ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ø¨Ù†ÙØ³Ù‡Ø§
        if (this.isPageHandlingAuth(currentPage)) {
            log.debug(`ðŸ“„ Page ${currentPage} handles its own authentication, skipping app.js auth check`);
            this.setupPageSpecificHandlers();
            return;
        }

        // Always check authentication status with backend
        try {
            // Use AuthService to check authentication status
            let isValid = false;
            if (window.authService) {
                // ÙØ­Øµ Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª Ø§Ù„Ù…Ø­Ù„ÙŠØ© Ø£ÙˆÙ„Ø§Ù‹ Ù„ØªØ¬Ù†Ø¨ Ø§Ø³ØªØ¯Ø¹Ø§Ø¡ auth/me
                const localUser = window.authService.getCurrentUser();
                if (localUser && localUser.id) {
                    log.debug('âœ… Using local user data, skipping auth/me call');
                    isValid = true;
                } else {
                    isValid = await window.authService.checkAuthStatus();
                }
            } else {
                isValid = await this.checkAuthStatus();
            }
            
            if (!isValid) {
                this.redirectToLogin();
                return;
            }

            // Load user data
            await this.loadUserData();
            
            // Setup page-specific functionality
            this.setupPageSpecificHandlers();
            
        } catch (error) {
            // Error during authentication check
            this.redirectToLogin();
        }
    }

    /**
     * Check if page handles its own authentication
     * @param {string} pageName - Page name
     * @returns {boolean} Whether page handles auth
     */
    isPageHandlingAuth(pageName) {
        const pagesWithOwnAuth = [
            'owner-zones',
            'owner-dashboard',
            'merchant-dashboard',
            'courier-dashboard',
            'warehouse-dashboard'
        ];
        
        return pagesWithOwnAuth.includes(pageName);
    }

    /**
     * Setup global event listeners
     */
    setupGlobalListeners() {
        // Logout functionality
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-action="logout"]')) {
                this.handleLogout();
            }
        });

        // Window resize handler
        window.addEventListener('resize', this.debounce(() => {
            this.handleWindowResize();
        }, 250));

        // Global error handler
        window.addEventListener('error', (e) => {
            log.error('[Twsela Global Error]', e.message, '\nFile:', e.filename, '\nLine:', e.lineno, '\nCol:', e.colno);
        });

        // Unhandled promise rejection handler
        window.addEventListener('unhandledrejection', (e) => {
            log.error('[Twsela Unhandled Rejection]', e.reason);
        });
    }

    /**
     * Setup page-specific handlers based on current page
     */
    setupPageSpecificHandlers() {
        const currentPage = this.getCurrentPage();
        
        switch (currentPage) {
            case 'merchant-create-shipment':
                this.initializeMerchantCreateShipment();
                break;
            case 'owner-zones':
                this.initializeOwnerZones();
                break;
            case 'owner-payouts':
                this.initializeOwnerPayouts();
                break;
            case 'courier-manifest':
                this.initializeCourierManifest();
                break;
            default:
                // Dashboard pages are handled by their specific handlers

        }
    }

    /**
     * Check authentication status with backend
     */
    async checkAuthStatus() {
        try {
            // Use AuthService to get token consistently
            let token = null;
            if (window.authService) {
                token = window.authService.getToken();
            } else {
                token = sessionStorage.getItem('authToken');
            }
            
            
            if (!token) {
                return false;
            }

            const response = await fetch(`${this.apiBaseUrl}/api/auth/me`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            
            
            if (response.ok) {
                const user = await response.json();
                if (user) {
                    this.currentUser = user;
                    return true;
                }
            } else {
            }
            
            return false;
        } catch (error) {
            // Auth check error
            return false;
        }
    }

    /**
     * Load user data
     */
    async loadUserData() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/api/auth/me`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                if (data.success && data.data && data.data.user) {
                    this.currentUser = data.data.user;
                    this.updateUserInterface();
                }
            }
        } catch (error) {
            // Failed to load user data
        }
    }

    /**
     * Update user interface with current user data
     */
    updateUserInterface() {
        if (!this.currentUser) return;

        // Update user name in header
        const userNameElements = document.querySelectorAll('.user-name');
        userNameElements.forEach(el => {
            el.textContent = this.currentUser.name || this.currentUser.username;
        });

        // Update user role
        const userRoleElements = document.querySelectorAll('.user-role');
        userRoleElements.forEach(el => {
            el.textContent = this.getRoleDisplayName(this.currentUser.role);
        });

        // Update profile avatar
        const avatarElements = document.querySelectorAll('.profile-avatar img');
        avatarElements.forEach(el => {
            if (this.currentUser.avatar) {
                el.src = this.currentUser.avatar;
            } else {
                el.src = this.generateAvatar(this.currentUser.name || this.currentUser.username);
            }
        });
    }

    /**
     * Get role display name in Arabic
     */
    getRoleDisplayName(role) {
        const roleMap = {
            'OWNER': 'Ù…Ø§Ù„Ùƒ Ø§Ù„Ù†Ø¸Ø§Ù…',
            'MERCHANT': 'ØªØ§Ø¬Ø±',
            'COURIER': 'Ø³Ø§Ø¦Ù‚ ØªÙˆØµÙŠÙ„',
            'WAREHOUSE': 'Ù…ÙˆØ¸Ù Ù…Ø³ØªÙˆØ¯Ø¹',
            'ADMIN': 'Ù…Ø¯ÙŠØ±'
        };
        return roleMap[role] || role;
    }

    /**
     * Generate avatar from name
     */
    generateAvatar(name) {
        const initials = name.split(' ').map(n => n[0]).join('').toUpperCase();
        const colors = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6'];
        const color = colors[name.length % colors.length];
        
        return `data:image/svg+xml,${encodeURIComponent(`
            <svg width="40" height="40" xmlns="http://www.w3.org/2000/svg">
                <rect width="40" height="40" fill="${color}"/>
                <text x="20" y="25" text-anchor="middle" fill="white" font-family="Arial" font-size="16" font-weight="bold">${initials}</text>
            </svg>
        `)}`;
    }

    /**
     * Handle logout
     */
    handleLogout() {
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('userData');
        this.currentUser = null;
        this.redirectToLogin();
    }

    /**
     * Redirect to login page - Use centralized utility
     */
    redirectToLogin() {
        // Use centralized redirect function from utilities
        if (window.Utils && window.Utils.redirectToLogin) {
            window.Utils.redirectToLogin();
            return;
        }
        
        // Fallback if utilities not available
        // Use replace instead of href to prevent back button issues
        window.location.replace('/login.html');
    }

    /**
     * Handle window resize
     */
    handleWindowResize() {
        // Update sidebar visibility on mobile
        const sidebar = document.querySelector('.sidebar');
        if (window.innerWidth <= 768) {
            sidebar?.classList.remove('show');
        }
    }

    /**
     * Get API base URL - delegates to global config utility
     */
    getApiBaseUrl() {
        return window.getApiBaseUrl();
    }

    /**
     * Debounce function
     */
    debounce(func, wait) {
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
     * Initialize all services
     */
    initializeServices() {
        // AuthService is available globally
        this.services.auth = window.authService;
        
        // ApiService is available globally
        this.services.api = window.apiService;
        
        // UIUtils is available globally
        this.services.ui = window.UIUtils;
        
        // AppBase is available globally
        this.services.app = window.appBase;
    }

    /**
     * Initialize page handlers based on current page
     */
    initializePageHandlers() {
        const currentPage = this.getCurrentPage();
        
        // Initialize page-specific handlers
        switch (currentPage) {
            case 'merchant-create-shipment':
                this.handlers.merchantCreateShipment = window.merchantCreateShipmentHandler;
                break;
            case 'owner-zones':
                this.handlers.ownerZones = window.ownerZonesHandler;
                break;
            case 'owner-payouts':
                this.handlers.ownerPayouts = window.ownerPayoutsHandler;
                break;
            case 'courier-manifest':
                this.handlers.courierManifest = window.courierManifestHandler;
                break;
        }
    }

    /**
     * Get current page identifier
     */
    getCurrentPage() {
        const path = window.location.pathname;
        const filename = path.split('/').pop().replace('.html', '');
        
        // Handle special cases first
        if (filename === 'login' || path.includes('login.html')) {
            return 'login';
        }
        if (filename === 'index' || path === '/' || path === '/index.html') {
            return 'index';
        }
        if (filename === '404' || path.includes('404.html')) {
            return '404';
        }
        if (filename === 'contact' || path.includes('contact.html')) {
            return 'contact';
        }
        
        const pageMap = {
            'create-shipment': 'merchant-create-shipment',
            'zones': 'owner-zones',
            'payouts': 'owner-payouts',
            'manifest': 'courier-manifest',
            'dashboard': 'dashboard'
        };
        
        return pageMap[filename] || 'dashboard';
    }

    /**
     * Get service by name
     */
    getService(serviceName) {
        return this.services[serviceName];
    }

    /**
     * Get handler by name
     */
    getHandler(handlerName) {
        return this.handlers[handlerName];
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return this.services.auth ? this.services.auth.isAuthenticated() : false;
    }

    /**
     * Get current user
     */
    getCurrentUser() {
        return this.services.auth ? this.services.auth.getCurrentUser() : null;
    }

    /**
     * Show notification
     */
    showNotification(message, type = 'info', duration = 5000) {
        if (this.services.ui) {
            this.services.ui.showNotification(message, type, duration);
        } else {
        }
    }

    /**
     * Show loading
     */
    showLoading(container = null) {
        if (this.services.ui) {
            this.services.ui.showLoading(container);
        }
    }

    /**
     * Hide loading
     */
    hideLoading(container = null) {
        if (this.services.ui) {
            this.services.ui.hideLoading(container);
        }
    }

    /**
     * Make API request
     */
    async apiRequest(endpoint, options = {}) {
        if (this.services.api) {
            return this.services.api.request(endpoint, options);
        } else {
            throw new Error('ApiService not available');
        }
    }

    /**
     * Login user
     */
    async login(credentials) {
        if (this.services.auth) {
            return this.services.auth.login(credentials);
        } else {
            throw new Error('AuthService not available');
        }
    }

    /**
     * Logout user
     */
    async logout() {
        if (this.services.auth) {
            return this.services.auth.logout();
        } else {
            throw new Error('AuthService not available');
        }
    }

    /**
     * Check authentication status
     */
    async checkAuthStatus() {
        if (this.services.auth) {
            return this.services.auth.checkAuthStatus();
        } else {
            return false;
        }
    }

    /**
     * Get application statistics
     */
    getAppStats() {
        return {
            isInitialized: this.isInitialized,
            servicesCount: Object.keys(this.services).length,
            handlersCount: Object.keys(this.handlers).length,
            currentPage: this.getCurrentPage(),
            isAuthenticated: this.isAuthenticated()
        };
    }

    /**
     * Reload current page
     */
    reload() {
        window.location.reload();
    }

    /**
     * Navigate to page
     */
    navigateTo(page) {
        window.location.href = page;
    }

    /**
     * Get application version
     */
    getVersion() {
        return '1.0.0';
    }

    /**
     * Get application info
     */
    getAppInfo() {
        return {
            name: 'Twsela CMS',
            version: this.getVersion(),
            description: 'Ù†Ø¸Ø§Ù… Ø¥Ø¯Ø§Ø±Ø© Ø§Ù„Ø´Ø­Ù†Ø§Øª Ø§Ù„Ù…ØªÙƒØ§Ù…Ù„',
            author: 'Twsela Team',
            buildDate: '2024-01-16'
        };
    }
}

// Create global application instance
// Create global instance - delay initialization to ensure all scripts are loaded
window.twselaApp = null;

// Initialize app after all scripts are loaded
document.addEventListener('DOMContentLoaded', () => {
    
    // Add small delay to ensure all services are ready
    setTimeout(() => {
        window.twselaApp = new TwselaApp();
    }, 100);
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = TwselaApp;
}

// Convenience functions for global access
window.showNotification = (message, type, duration) => {
    window.twselaApp.showNotification(message, type, duration);
};

window.showLoading = (container) => {
    window.twselaApp.showLoading(container);
};

window.hideLoading = (container) => {
    window.twselaApp.hideLoading(container);
};

window.isAuthenticated = () => {
    return window.twselaApp.isAuthenticated();
};

window.getCurrentUser = () => {
    return window.twselaApp.getCurrentUser();
};

// Handle logout links
document.addEventListener('click', (e) => {
    if (e.target.closest('.logout-link')) {
        e.preventDefault();
        if (window.authService) {
            window.authService.logout();
        } else {
            // Fallback logout
            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('userData');
            window.location.href = '/login.html';
        }
    }
});

// App initialization complete
// App initialization complete - console.log removed for cleaner console
