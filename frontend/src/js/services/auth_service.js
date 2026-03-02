import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('auth_service');

/**
 * Twsela CMS - Unified Authentication Service
 * Consolidated authentication service handling all auth operations
 * Follows DRY principle with unified authentication logic
 * This is the ONLY auth service file - all others have been merged here
 */

// AuthService initialization - console.log removed for cleaner console

class AuthService {
    constructor() {
        this.apiBaseUrl = this.getApiBaseUrl();
        this.tokenKey = 'authToken';
        this.userDataKey = 'userData';
        this.isLoginInProgress = false;
        
        // Cache for auth/me response
        this.authCache = {
            lastCheck: null,
            isValid: false,
            userData: null,
            cacheTimeout: 5 * 60 * 1000 // 5 minutes
        };
        
        // Prevent multiple simultaneous auth/me calls
        this.authCheckInProgress = false;
        this.authCheckPromise = null;
        
        // Global flag to prevent multiple auth checks
        if (!window.authCheckInProgress) {
            window.authCheckInProgress = false;
        }
    }

    /**
     * Get API base URL - delegates to global config utility
     */
    getApiBaseUrl() {
        return window.getApiBaseUrl();
    }

    /**
     * Login user with credentials
     * @param {Object} credentials - Login credentials
     * @param {string} credentials.username - Username or email
     * @param {string} credentials.password - Password
     * @param {string} credentials.role - User role (optional)
     * @returns {Promise<Object>} Login response
     */
    async login(credentials) {
        try {
            this.isLoginInProgress = true;
            UIUtils.showLoading();
            
            const response = await fetch(`${this.apiBaseUrl}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(credentials)
            });

            const data = await response.json();

            if (response.ok && data.success) {
                // Store authentication data
                this.storeAuthData(data.data);
                
                UIUtils.showSuccess('ØªÙ… ØªØ³Ø¬ÙŠÙ„ Ø§Ù„Ø¯Ø®ÙˆÙ„ Ø¨Ù†Ø¬Ø§Ø­');
                
                // Don't redirect here - let LoginPageHandler handle it
                // This prevents double redirect
                
                return {
                    success: true,
                    data: data.data,
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
            log.error('ðŸ” Auth Service Error - Login:', {
                method: 'login',
                error: error.message,
                stack: error.stack,
                credentials: { phone: credentials.phone, role: credentials.role },
                timestamp: new Date().toISOString()
            });
            
            return {
                success: false,
                message: 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø§ØªØµØ§Ù„ Ø¨Ø§Ù„Ø®Ø§Ø¯Ù…'
            };
        } finally {
            this.isLoginInProgress = false;
            UIUtils.hideLoading();
        }
    }

    /**
     * Logout user
     * @returns {Promise<Object>} Logout response
     */
    async logout() {
        try {
            const token = this.getToken();
            if (token) {
                // Notify backend about logout
                await fetch(`${this.apiBaseUrl}/api/auth/logout`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
            }
        } catch (error) {
            log.error('ðŸ” Auth Service Error - Logout:', {
                method: 'logout',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
        } finally {
            // Clear local storage regardless of backend response
            this.clearAuthData();
            UIUtils.showSuccess('ØªÙ… ØªØ³Ø¬ÙŠÙ„ Ø§Ù„Ø®Ø±ÙˆØ¬ Ø¨Ù†Ø¬Ø§Ø­');
            this.redirectToLogin();
        }
    }

    /**
     * Check if auth cache is valid
     * @returns {boolean} Cache validity
     */
    isAuthCacheValid() {
        if (!this.authCache.lastCheck) {
            return false;
        }
        
        const now = Date.now();
        const timeDiff = now - this.authCache.lastCheck;
        
        return timeDiff < this.authCache.cacheTimeout && this.authCache.isValid;
    }

    /**
     * Update auth cache
     * @param {Object} userData - User data from server
     * @param {boolean} isValid - Authentication validity
     */
    updateAuthCache(userData, isValid) {
        this.authCache = {
            lastCheck: Date.now(),
            isValid: isValid,
            userData: userData,
            cacheTimeout: this.authCache.cacheTimeout
        };
    }

    /**
     * Clear auth cache
     */
    clearAuthCache() {
        this.authCache = {
            lastCheck: null,
            isValid: false,
            userData: null,
            cacheTimeout: 5 * 60 * 1000
        };
    }

    /**
     * Check authentication status
     * @returns {Promise<boolean>} Authentication status
     */
    async checkAuthStatus() {
        try {
            // Skip auth check if login is in progress
            if (this.isLoginInProgress) {
                return true;
            }
            
            // ÙØ­Øµ Ø§Ù„Ù€ global flag Ù„Ù…Ù†Ø¹ Ø§Ù„Ø§Ø³ØªØ¯Ø¹Ø§Ø¡Ø§Øª Ø§Ù„Ù…ØªØ¹Ø¯Ø¯Ø©
            if (window.authCheckInProgress) {
                log.debug('â³ Global auth check already in progress, waiting...');
                let attempts = 0;
                while (window.authCheckInProgress && attempts < 50) {
                    await new Promise(resolve => setTimeout(resolve, 100));
                    attempts++;
                }
                
                // ÙØ­Øµ Ø§Ù„Ù†ØªÙŠØ¬Ø© Ø¨Ø¹Ø¯ Ø§Ù„Ø§Ù†ØªØ¸Ø§Ø±
                const localUser = this.getCurrentUser();
                if (localUser && localUser.id) {
                    log.debug('âœ… Auth verified after waiting for global check');
                    return true;
                }
            }
            
            // Ù…Ù†Ø¹ Ø§Ù„Ø§Ø³ØªØ¯Ø¹Ø§Ø¡Ø§Øª Ø§Ù„Ù…ØªØ¹Ø¯Ø¯Ø© Ø§Ù„Ù…ØªØ²Ø§Ù…Ù†Ø©
            if (this.authCheckInProgress && this.authCheckPromise) {
                log.debug('â³ Auth check already in progress, waiting for result...');
                return await this.authCheckPromise;
            }
            
            const token = this.getToken();
            if (!token) {
                this.clearAuthCache();
                return false;
            }

            // ÙØ­Øµ Ø§Ù„Ù€ cache Ø£ÙˆÙ„Ø§Ù‹
            if (this.isAuthCacheValid()) {
                log.debug('âœ… Using cached auth data, skipping auth/me call');
                return true;
            }

            // ÙØ­Øµ Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª Ø§Ù„Ù…Ø­Ù„ÙŠØ© ÙƒØ¨Ø¯ÙŠÙ„ Ù„Ù„Ù€ cache
            const localUser = this.getCurrentUser();
            if (localUser && localUser.id) {
                log.debug('âœ… Using local user data, updating cache');
                this.updateAuthCache(localUser, true);
                return true;
            }

            // Ø¨Ø¯Ø¡ Ø¹Ù…Ù„ÙŠØ© ÙØ­Øµ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ù…Ø¹ Ù…Ù†Ø¹ Ø§Ù„Ø§Ø³ØªØ¯Ø¹Ø§Ø¡Ø§Øª Ø§Ù„Ù…ØªØ¹Ø¯Ø¯Ø©
            this.authCheckInProgress = true;
            window.authCheckInProgress = true; // ØªØ¹ÙŠÙŠÙ† Ø§Ù„Ù€ global flag
            this.authCheckPromise = this.performAuthCheck();
            
            const result = await this.authCheckPromise;
            this.authCheckInProgress = false;
            window.authCheckInProgress = false; // Ø¥Ø²Ø§Ù„Ø© Ø§Ù„Ù€ global flag
            this.authCheckPromise = null;
            
            return result;
        } catch (error) {
            this.authCheckInProgress = false;
            window.authCheckInProgress = false; // ØªÙ†Ø¸ÙŠÙ Ø§Ù„Ù€ global flag
            this.authCheckPromise = null;
            
            log.error('ðŸ” Auth Service Error - Check Auth Status:', {
                method: 'checkAuthStatus',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });

            // SECURITY FIX: Network errors should NOT keep user authenticated
            // This prevents unauthorized access when server is unreachable
            log.warn('⚠️ Network error during auth check, logging user out for safety');
            return false; // Return false to force re-authentication
        }
    }

    /**
     * Perform actual auth check with server
     * @returns {Promise<boolean>} Authentication status
     */
    async performAuthCheck() {
        try {
            log.debug('ðŸ”„ Checking authentication with server...');
            
            // Use getAuthHeader() to ensure proper headers are sent
            const authHeaders = this.getAuthHeader();
            
            const response = await fetch(`${this.apiBaseUrl}/api/auth/me`, {
                method: 'GET',
                headers: {
                    ...authHeaders,
                    'Content-Type': 'application/json'
                }
            });
            
            log.debug('ðŸ“¡ Auth/me response status:', response.status);

            // CRITICAL FIX: Only clear auth data if response is 401 (Unauthorized)
            if (response.status === 401) {
                log.warn('âš ï¸ Authentication failed (401), clearing auth data');
                this.clearAuthData();
                this.clearAuthCache();
                return false;
            }

            // If response is ok, update user data and cache
            if (response.ok) {
                const body = await response.json();
                const user = body.data || body;
                if (user) {
                    this.storeUserData(user);
                    this.updateAuthCache(user, true);
                    log.debug('âœ… User data updated from server and cached');
                    return true;
                }
            }

            // For other errors (network, server errors), don't clear auth data
            // but don't bypass authentication either
            log.warn('âš ï¸ Auth/me returned non-200 status, auth check inconclusive');
            return false; // Return false â€” require re-authentication on server errors
        } catch (error) {
            log.error('ðŸ” Auth Service Error - Perform Auth Check:', {
                method: 'performAuthCheck',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });

            // Don't clear auth data on network errors
            // but don't bypass authentication either
            log.warn('âš ï¸ Network error during auth check, auth check inconclusive');
            return false; // Return false â€” require re-authentication on network errors
        }
    }

    /**
     * Get current user data
     * @returns {Object|null} Current user data
     */
    getCurrentUser() {
        try {
            const userData = sessionStorage.getItem(this.userDataKey);
            return userData ? JSON.parse(userData) : null;
        } catch (error) {
            log.error('ðŸ” Auth Service Error - Get Current User:', {
                method: 'getCurrentUser',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });

            return null;
        }
    }

    /**
     * Get authentication token
     * @returns {string|null} Authentication token
     */
    getToken() {
        return sessionStorage.getItem(this.tokenKey);
    }

    /**
     * Check if user is authenticated
     * @returns {boolean} Authentication status
     */
    isAuthenticated() {
        const token = this.getToken();
        const user = this.getCurrentUser();
        return !!(token && user);
    }

    /**
     * Check if user has specific role
     * @param {string} role - Role to check
     * @returns {boolean} Role check result
     */
    hasRole(role) {
        const user = this.getCurrentUser();
        return user && user.role === role;
    }

    /**
     * Check if user has any of the specified roles
     * @param {string[]} roles - Roles to check
     * @returns {boolean} Role check result
     */
    hasAnyRole(roles) {
        const user = this.getCurrentUser();
        return user && roles.includes(user.role);
    }

    /**
     * Check if user has specific permission
     * @param {string} permission - Permission to check
     * @returns {boolean} Permission check result
     */
    hasPermission(permission) {
        const user = this.getCurrentUser();
        if (!user || !user.permissions) return false;
        
        return user.permissions.includes(permission);
    }

    /**
     * Refresh authentication token
     * @returns {Promise<boolean>} Refresh result
     */
    async refreshToken() {
        try {
            const token = this.getToken();
            if (!token) return false;

            const response = await fetch(`${this.apiBaseUrl}/api/auth/refresh`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success && data.data.token) {
                    this.storeToken(data.data.token);
                    return true;
                }
            }

            return false;
        } catch (error) {
            log.error('ðŸ” Auth Service Error - Verify Token:', {
                method: 'verifyToken',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });

            return false;
        }
    }

    /**
     * Change user password
     * @param {Object} passwordData - Password change data
     * @param {string} passwordData.currentPassword - Current password
     * @param {string} passwordData.newPassword - New password
     * @param {string} passwordData.confirmPassword - Confirm new password
     * @returns {Promise<Object>} Password change response
     */
    async changePassword(passwordData) {
        try {
            UIUtils.showLoading();

            const response = await fetch(`${this.apiBaseUrl}/api/auth/change-password`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(passwordData)
            });

            const data = await response.json();

            if (response.ok && data.success) {
                UIUtils.showSuccess('ØªÙ… ØªØºÙŠÙŠØ± ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ± Ø¨Ù†Ø¬Ø§Ø­');
                return {
                    success: true,
                    message: data.message
                };
            } else {
                
                return {
                    success: false,
                    message: data.message || 'ÙØ´Ù„ ÙÙŠ ØªØºÙŠÙŠØ± ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±',
                    errors: data.errors || []
                };
            }
        } catch (error) {
            log.error('ðŸ” Auth Service Error - Change Password:', {
                method: 'changePassword',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
            
            return {
                success: false,
                message: 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ ØªØºÙŠÙŠØ± ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±'
            };
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Request password reset
     * @param {string} email - User email
     * @returns {Promise<Object>} Password reset response
     */
    async requestPasswordReset(email) {
        try {
            UIUtils.showLoading();

            const response = await fetch(`${this.apiBaseUrl}/api/public/forgot-password`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                UIUtils.showSuccess('ØªÙ… Ø¥Ø±Ø³Ø§Ù„ Ø±Ø§Ø¨Ø· Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±');
                return {
                    success: true,
                    message: data.message
                };
            } else {
                
                return {
                    success: false,
                    message: data.message || 'ÙØ´Ù„ ÙÙŠ Ø¥Ø±Ø³Ø§Ù„ Ø±Ø§Ø¨Ø· Ø¥Ø¹Ø§Ø¯Ø© Ø§Ù„ØªØ¹ÙŠÙŠÙ†'
                };
            }
        } catch (error) {
            log.error('ðŸ” Auth Service Error - Forgot Password:', {
                method: 'forgotPassword',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
            
            return {
                success: false,
                message: 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø·Ù„Ø¨ Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±'
            };
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Reset password with token
     * @param {Object} resetData - Password reset data
     * @param {string} resetData.token - Reset token
     * @param {string} resetData.newPassword - New password
     * @param {string} resetData.confirmPassword - Confirm new password
     * @returns {Promise<Object>} Password reset response
     */
    async resetPassword(resetData) {
        try {
            UIUtils.showLoading();

            const response = await fetch(`${this.apiBaseUrl}/api/public/reset-password`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(resetData)
            });

            const data = await response.json();

            if (response.ok && data.success) {
                UIUtils.showSuccess('ØªÙ… Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ± Ø¨Ù†Ø¬Ø§Ø­');
                return {
                    success: true,
                    message: data.message
                };
            } else {
                
                return {
                    success: false,
                    message: data.message || 'ÙØ´Ù„ ÙÙŠ Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±',
                    errors: data.errors || []
                };
            }
        } catch (error) {
            log.error('ðŸ” Auth Service Error - Reset Password:', {
                method: 'resetPassword',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
            
            return {
                success: false,
                message: 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±'
            };
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Store authentication data
     * @param {Object} authData - Authentication data
     */
    storeAuthData(authData) {
        
        if (authData.token) {
            this.storeToken(authData.token);
        } else {

        }
        
        if (authData.user) {
            this.storeUserData(authData.user);
        } else {

        }
        
        // Verify storage
        const storedToken = this.getToken();
        const storedUser = this.getCurrentUser();
    }

    /**
     * Store authentication token
     * @param {string} token - Authentication token
     */
    storeToken(token) {
        sessionStorage.setItem(this.tokenKey, token);
    }

    /**
     * Store user data
     * @param {Object} user - User data
     */
    storeUserData(user) {
        sessionStorage.setItem(this.userDataKey, JSON.stringify(user));
    }

    /**
     * Clear authentication data
     */
    clearAuthData() {
        sessionStorage.removeItem(this.tokenKey);
        sessionStorage.removeItem(this.userDataKey);
        this.clearAuthCache();
    }

    /**
     * Redirect after successful login based on user role - Use centralized utility
     * @param {string} role - User role
     */
    redirectAfterLogin(role) {
        // Use centralized redirect function from utilities
        if (window.Utils && window.Utils.redirectToDashboard) {
            window.Utils.redirectToDashboard(role);
            return;
        }
        
        // Fallback if utilities not available
        const roleRedirects = {
            'OWNER': '/owner/dashboard.html',
            'MERCHANT': '/merchant/dashboard.html',
            'COURIER': '/courier/dashboard.html',
            'WAREHOUSE': '/warehouse/dashboard.html',
            'WAREHOUSE_MANAGER': '/warehouse/dashboard.html',
            'ADMIN': '/admin/dashboard.html'
        };

        const redirectUrl = roleRedirects[role] || '/login.html';
        
        // Use replace instead of href to prevent back button issues
        window.location.replace(redirectUrl);
    }

    /**
     * Redirect to login page - Simplified direct redirect
     */
    redirectToLogin() {
        // Direct redirect without complex logic
        window.location.replace('/login.html');
    }

    /**
     * Get authorization header
     * @returns {Object} Authorization header
     */
    getAuthHeader() {
        const token = this.getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    /**
     * Get user permissions
     * @returns {string[]} User permissions
     */
    getUserPermissions() {
        const user = this.getCurrentUser();
        return user ? (user.permissions || []) : [];
    }

    /**
     * Check if user can access specific page
     * @param {string} page - Page identifier
     * @returns {boolean} Access permission
     */
    canAccessPage(page) {
        const user = this.getCurrentUser();
        if (!user) return false;

        const pagePermissions = {
            'owner-dashboard': ['OWNER', 'ADMIN'],
            'merchant-dashboard': ['MERCHANT', 'OWNER', 'ADMIN'],
            'courier-dashboard': ['COURIER', 'OWNER', 'ADMIN'],
            'warehouse-dashboard': ['WAREHOUSE', 'OWNER', 'ADMIN'],
            'admin-dashboard': ['ADMIN', 'OWNER']
        };

        const allowedRoles = pagePermissions[page];
        return allowedRoles ? allowedRoles.includes(user.role) : false;
    }
}

// Create global instance
// Creating global AuthService instance - console.log removed for cleaner console
window.authService = new AuthService();
// Global AuthService instance created - console.log removed for cleaner console

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AuthService;
}
