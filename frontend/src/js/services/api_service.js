import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('api_service');

/**
 * Twsela CMS - Unified API Service
 * Consolidated API service handling all 80+ API endpoints
 * Follows DRY principle with consistent API communication
 * This is the ONLY API service file - all others have been merged here
 */

// ApiService initialization - console.log removed for cleaner console

class ApiService {
    constructor() {
        this.apiBaseUrl = this.getApiBaseUrl();
        this.defaultHeaders = {
            'Content-Type': 'application/json'
        };
    }

    /**
     * Get API base URL - delegates to global config utility
     */
    getApiBaseUrl() {
        return window.getApiBaseUrl();
    }

    /**
     * Get authorization headers
     */
    getAuthHeaders() {
        const token = sessionStorage.getItem('authToken');
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    /**
     * Make API request
     * @param {string} endpoint - API endpoint
     * @param {Object} options - Request options
     * @returns {Promise<Object>} API response
     */
    async request(endpoint, options = {}) {
        let url = '';
        try {
            // ÙØ­Øµ ÙˆØ¬ÙˆØ¯ Ø§Ù„ØªÙˆÙƒÙ† Ù‚Ø¨Ù„ Ø§Ù„Ø·Ù„Ø¨
            const token = sessionStorage.getItem('authToken');
            if (!token && !endpoint.includes('/auth/')) {
                log.warn('âš ï¸ No authentication token found for protected endpoint:', endpoint);
                return {
                    success: false,
                    message: 'No authentication token',
                    status: 401,
                    data: null
                };
            }

            url = `${this.apiBaseUrl}${endpoint}`;
            const headers = {
                ...this.defaultHeaders,
                ...this.getAuthHeaders(),
                ...options.headers
            };

            const config = {
                ...options,
                headers
            };

            log.debug(`ðŸ”„ API Request: ${options.method || 'GET'} ${url}`);
            const response = await fetch(url, config);
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            let data;
            
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = { message: await response.text() };
            }

            // Ù…Ø¹Ø§Ù„Ø¬Ø© Ø£Ø®Ø·Ø§Ø¡ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ø¨Ø´ÙƒÙ„ Ø®Ø§Øµ
            if (response.status === 401) {
                log.warn('âš ï¸ Authentication failed (401), clearing auth data');
                this.clearAuthData();
                return {
                    success: false,
                    message: 'Authentication failed',
                    status: 401,
                    data: null,
                    requiresLogin: true
                };
            }

            if (response.status === 403) {
                log.warn('âš ï¸ Access forbidden (403)');
                return {
                    success: false,
                    message: 'Access forbidden',
                    status: 403,
                    data: null,
                    requiresLogin: true
                };
            }

            if (!response.ok) {
                log.error('ðŸŒ API Service Error - Request Failed:', {
                    url: url,
                    method: options.method || 'GET',
                    status: response.status,
                    statusText: response.statusText,
                    response: data,
                    timestamp: new Date().toISOString()
                });
                
                return {
                    success: false,
                    message: data.message || `HTTP ${response.status}`,
                    status: response.status,
                    data: null
                };
            }

            log.debug(`âœ… API Request successful: ${options.method || 'GET'} ${url}`);
            return {
                success: true,
                data: data.data || data,
                message: data.message,
                status: response.status
            };
        } catch (error) {
            log.error('🌐 API Service Error - Network/Request Error:', {
                url: url,
                method: options.method || 'GET',
                error: error.message,
                stack: error.stack,
                timestamp: new Date().toISOString()
            });
            
            return {
                success: false,
                message: error.message || 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø§ØªØµØ§Ù„ Ø¨Ø§Ù„Ø®Ø§Ø¯Ù…',
                error: error,
                data: null
            };
        }
    }

    // ==========================================================================
    // AUTHENTICATION ENDPOINTS
    // ==========================================================================

    /**
     * Login user
     */
    async login(credentials) {
        return this.request('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify(credentials)
        });
    }

    /**
     * Logout user
     */
    async logout() {
        return this.request('/api/auth/logout', {
            method: 'POST'
        });
    }

    /**
     * Verify token
     */
    async verifyToken() {
        try {
            const url = `${this.apiBaseUrl}/api/auth/me`;
            const headers = {
                ...this.defaultHeaders,
                ...this.getAuthHeaders()
            };

            const response = await fetch(url, {
                method: 'GET',
                headers
            });

            if (!response.ok) {

                return {
                    success: false,
                    message: `HTTP ${response.status}`,
                    status: response.status,
                    data: null
                };
            }

            const user = await response.json();
            return {
                success: true,
                data: user,
                message: 'Token verified successfully',
                status: response.status
            };
        } catch (error) {
            log.error('🌐 API Service Error - verifyToken:', {
                url: '/api/auth/me',
                method: 'GET',
                error: error.message,
                timestamp: new Date().toISOString()
            });
            
            return {
                success: false,
                message: error.message || 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø§ØªØµØ§Ù„ Ø¨Ø§Ù„Ø®Ø§Ø¯Ù…',
                error: error,
                data: null
            };
        }
    }

    /**
     * Get current user
     */
    async getCurrentUser() {
        try {
            const url = `${this.apiBaseUrl}/api/auth/me`;
            const headers = {
                ...this.defaultHeaders,
                ...this.getAuthHeaders()
            };

            const response = await fetch(url, {
                method: 'GET',
                headers
            });

            if (!response.ok) {

                return {
                    success: false,
                    message: `HTTP ${response.status}`,
                    status: response.status,
                    data: null
                };
            }

            const user = await response.json();
            return {
                success: true,
                data: user,
                message: 'User data retrieved successfully',
                status: response.status
            };
        } catch (error) {
            log.error('ðŸŒ API Service Error - getCurrentUser:', {
                url: '/api/auth/me',
                method: 'GET',
                error: error.message,
                timestamp: new Date().toISOString()
            });
            
            return {
                success: false,
                message: error.message || 'Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø§ØªØµØ§Ù„ Ø¨Ø§Ù„Ø®Ø§Ø¯Ù…',
                error: error,
                data: null
            };
        }
    }

    /**
     * Change password
     */
    async changePassword(passwordData) {
        return this.request('/api/auth/change-password', {
            method: 'POST',
            body: JSON.stringify(passwordData)
        });
    }

    /**
     * Forgot password
     */
    async forgotPassword(email) {
        return this.request('/api/public/forgot-password', {
            method: 'POST',
            body: JSON.stringify({ email })
        });
    }

    /**
     * Reset password
     */
    async resetPassword(resetData) {
        return this.request('/api/public/reset-password', {
            method: 'POST',
            body: JSON.stringify(resetData)
        });
    }

    // ==========================================================================
    // USER MANAGEMENT ENDPOINTS - Unified CRUD
    // ==========================================================================

    /**
     * Unified CRUD operation for users
     * @param {string} operation - Operation type (get, getAll, create, update, delete)
     * @param {*} data - Data for the operation
     * @param {Object} params - Additional parameters
     */
    async userCRUD(operation, data = null, params = {}) {
        const baseEndpoint = '/api/users';
        
        switch (operation) {
            case 'getAll':
                const queryString = new URLSearchParams(params).toString();
                return this.request(`${baseEndpoint}?${queryString}`, { method: 'GET' });
            
            case 'get':
                return this.request(`${baseEndpoint}/${data}`, { method: 'GET' });
            
            case 'create':
                return this.request(baseEndpoint, {
                    method: 'POST',
                    body: JSON.stringify(data)
                });
            
            case 'update':
                return this.request(`${baseEndpoint}/${data.id}`, {
                    method: 'PUT',
                    body: JSON.stringify(data)
                });
            
            case 'delete':
                return this.request(`${baseEndpoint}/${data}`, { method: 'DELETE' });
            
            default:
                throw new Error(`Unknown operation: ${operation}`);
        }
    }

    /**
     * Get all users - Convenience method
     */
    async getUsers(params = {}) {
        return this.userCRUD('getAll', null, params);
    }

    /**
     * Get user by ID - Convenience method
     */
    async getUser(userId) {
        return this.userCRUD('get', userId);
    }

    /**
     * Create user - Convenience method
     */
    async createUser(userData) {
        return this.userCRUD('create', userData);
    }

    /**
     * Update user - Convenience method
     */
    async updateUser(userId, userData) {
        return this.userCRUD('update', { id: userId, ...userData });
    }

    /**
     * Delete user - Convenience method
     */
    async deleteUser(userId) {
        return this.userCRUD('delete', userId);
    }

    /**
     * Update user profile
     */
    async updateProfile(profileData) {
        return this.request('/api/users/profile', {
            method: 'PUT',
            body: JSON.stringify(profileData)
        });
    }

    // ==========================================================================
    // SHIPMENT ENDPOINTS - Unified CRUD
    // ==========================================================================

    /**
     * Unified CRUD operation for shipments
     * @param {string} operation - Operation type (get, getAll, create, update, delete)
     * @param {*} data - Data for the operation
     * @param {Object} params - Additional parameters
     */
    async shipmentCRUD(operation, data = null, params = {}) {
        const baseEndpoint = '/api/shipments';
        
        switch (operation) {
            case 'getAll':
                const queryString = new URLSearchParams(params).toString();
                return this.request(`${baseEndpoint}?${queryString}`, { method: 'GET' });
            
            case 'get':
                return this.request(`${baseEndpoint}/${data}`, { method: 'GET' });
            
            case 'create':
                return this.request(baseEndpoint, {
                    method: 'POST',
                    body: JSON.stringify(data)
                });
            
            case 'update':
                return this.request(`${baseEndpoint}/${data.id}`, {
                    method: 'PUT',
                    body: JSON.stringify(data)
                });
            
            case 'delete':
                return this.request(`${baseEndpoint}/${data}`, { method: 'DELETE' });
            
            default:
                throw new Error(`Unknown operation: ${operation}`);
        }
    }

    /**
     * Get all shipments - Convenience method
     */
    async getShipments(params = {}) {
        return this.shipmentCRUD('getAll', null, params);
    }

    /**
     * Get shipment by ID - Convenience method
     */
    async getShipment(shipmentId) {
        return this.shipmentCRUD('get', shipmentId);
    }

    /**
     * Create shipment - Convenience method
     */
    async createShipment(shipmentData) {
        return this.shipmentCRUD('create', shipmentData);
    }

    /**
     * Update shipment - Convenience method
     */
    async updateShipment(shipmentId, shipmentData) {
        return this.shipmentCRUD('update', { id: shipmentId, ...shipmentData });
    }

    /**
     * Delete shipment - Convenience method
     */
    async deleteShipment(shipmentId) {
        return this.shipmentCRUD('delete', shipmentId);
    }

    /**
     * Update shipment status
     */
    async updateShipmentStatus(shipmentId, status) {
        return this.request(`/api/shipments/${shipmentId}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
    }

    /**
     * Assign courier to shipment
     */
    async assignCourier(shipmentId, courierId) {
        return this.request(`/api/shipments/${shipmentId}/assign`, {
            method: 'POST',
            body: JSON.stringify({ courierId })
        });
    }

    /**
     * Track shipment
     */
    async trackShipment(trackingNumber) {
        return this.request(`/api/shipments/tracking/${trackingNumber}`, {
            method: 'GET'
        });
    }

    /**
     * Return shipment
     */
    async returnShipment(shipmentId) {
        return this.request(`/api/shipments/${shipmentId}/return`, {
            method: 'POST'
        });
    }

    // ==========================================================================
    // ZONE MANAGEMENT ENDPOINTS - Unified CRUD
    // ==========================================================================

    /**
     * Unified CRUD operation for zones
     * @param {string} operation - Operation type (get, getAll, create, update, delete)
     * @param {*} data - Data for the operation
     * @param {Object} params - Additional parameters
     */
    async zoneCRUD(operation, data = null, params = {}) {
        const baseEndpoint = '/api/master-data/zones';
        
        switch (operation) {
            case 'getAll':
                const queryString = new URLSearchParams(params).toString();
                return this.request(`${baseEndpoint}?${queryString}`, { method: 'GET' });
            
            case 'get':
                return this.request(`${baseEndpoint}/${data}`, { method: 'GET' });
            
            case 'create':
                return this.request(baseEndpoint, {
                    method: 'POST',
                    body: JSON.stringify(data)
                });
            
            case 'update':
                return this.request(`${baseEndpoint}/${data.id}`, {
                    method: 'PUT',
                    body: JSON.stringify(data)
                });
            
            case 'delete':
                return this.request(`${baseEndpoint}/${data}`, { method: 'DELETE' });
            
            default:
                throw new Error(`Unknown operation: ${operation}`);
        }
    }

    /**
     * Get all zones - Convenience method
     */
    async getZones(params = {}) {
        return this.zoneCRUD('getAll', null, params);
    }

    /**
     * Get zone by ID - Convenience method
     */
    async getZone(zoneId) {
        return this.zoneCRUD('get', zoneId);
    }

    /**
     * Create zone - Convenience method
     */
    async createZone(zoneData) {
        return this.zoneCRUD('create', zoneData);
    }

    /**
     * Update zone - Convenience method
     */
    async updateZone(zoneId, zoneData) {
        return this.zoneCRUD('update', { id: zoneId, ...zoneData });
    }

    /**
     * Delete zone - Convenience method
     */
    async deleteZone(zoneId) {
        return this.zoneCRUD('delete', zoneId);
    }

    // ==========================================================================
    // FINANCIAL ENDPOINTS
    // ==========================================================================

    /**
     * Get all payouts
     */
    async getPayouts(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/financial/payouts?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get payout by ID
     */
    async getPayout(payoutId) {
        return this.request(`/api/financial/payouts/${payoutId}`, {
            method: 'GET'
        });
    }

    /**
     * Mark payout as paid
     */
    async approvePayout(payoutId) {
        return this.request(`/api/financial/payouts/${payoutId}/pay`, {
            method: 'PUT'
        });
    }

    /**
     * Reject payout
     */
    async rejectPayout(payoutId, reason) {
        return this.request(`/api/financial/payouts/${payoutId}/reject`, {
            method: 'PUT',
            body: JSON.stringify({ reason })
        });
    }

    /**
     * Get financial summary
     */
    async getFinancialReports(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/financial/summary?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Create payout
     */
    async createPayout(payoutData) {
        return this.request('/api/financial/payouts', {
            method: 'POST',
            body: JSON.stringify(payoutData)
        });
    }

    /**
     * Get merchant financial summary
     */
    async getFinancialMerchantSummary(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/financial/merchant-summary?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get courier earnings
     */
    async getFinancialCourierEarnings(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/financial/courier-earnings?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get financial revenue
     */
    async getFinancialRevenue(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/financial/revenue?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get pricing
     */
    async getPricing(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/pricing?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Create pricing
     */
    async createPricing(pricingData) {
        return this.request('/api/pricing', {
            method: 'POST',
            body: JSON.stringify(pricingData)
        });
    }

    /**
     * Update pricing
     */
    async updatePricing(pricingId, pricingData) {
        return this.request(`/api/pricing/${pricingId}`, {
            method: 'PUT',
            body: JSON.stringify(pricingData)
        });
    }

    /**
     * Delete pricing
     */
    async deletePricing(pricingId) {
        return this.request(`/api/pricing/${pricingId}`, {
            method: 'DELETE'
        });
    }

    // ==========================================================================
    // MANIFEST ENDPOINTS
    // ==========================================================================

    /**
     * Get all manifests
     */
    async getManifests(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/manifests?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get manifest by ID
     */
    async getManifest(manifestId) {
        return this.request(`/api/manifests/${manifestId}`, {
            method: 'GET'
        });
    }

    /**
     * Create manifest
     */
    async createManifest(manifestData) {
        return this.request('/api/manifests', {
            method: 'POST',
            body: JSON.stringify(manifestData)
        });
    }

    /**
     * Update manifest
     */
    async updateManifest(manifestId, manifestData) {
        return this.request(`/api/manifests/${manifestId}`, {
            method: 'PUT',
            body: JSON.stringify(manifestData)
        });
    }

    /**
     * Update manifest status
     */
    async updateManifestStatus(manifestId, status) {
        return this.request(`/api/manifests/${manifestId}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
    }

    /**
     * Dispatch manifest
     */
    async dispatchManifest(manifestId) {
        return this.request(`/api/manifests/${manifestId}/dispatch`, {
            method: 'POST'
        });
    }

    /**
     * Complete manifest
     */
    async completeManifest(manifestId) {
        return this.request(`/api/manifests/${manifestId}/complete`, {
            method: 'POST'
        });
    }

    /**
     * Add shipments to manifest
     */
    async addShipmentsToManifest(manifestId, shipmentIds) {
        return this.request(`/api/manifests/${manifestId}/shipments`, {
            method: 'POST',
            body: JSON.stringify({ shipmentIds })
        });
    }

    /**
     * Get current courier manifest
     */
    async getCourierManifest(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/manifests/current?${queryString}`, {
            method: 'GET'
        });
    }

    // ==========================================================================
    // COURIER ENDPOINTS
    // ==========================================================================

    /**
     * Get all couriers
     */
    async getCouriers(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/couriers?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get courier by ID
     */
    async getCourier(courierId) {
        return this.request(`/api/couriers/${courierId}`, {
            method: 'GET'
        });
    }

    /**
     * Create courier
     */
    async createCourier(courierData) {
        return this.request('/api/couriers', {
            method: 'POST',
            body: JSON.stringify(courierData)
        });
    }

    /**
     * Update courier
     */
    async updateCourier(courierId, courierData) {
        return this.request(`/api/couriers/${courierId}`, {
            method: 'PUT',
            body: JSON.stringify(courierData)
        });
    }

    /**
     * Delete courier
     */
    async deleteCourier(courierId) {
        return this.request(`/api/couriers/${courierId}`, {
            method: 'DELETE'
        });
    }

    /**
     * Get courier location
     */
    async getCourierLocation(courierId) {
        return this.request(`/api/couriers/${courierId}/location`, {
            method: 'GET'
        });
    }

    /**
     * Update courier location
     */
    async updateCourierLocation(courierId, locationData) {
        return this.request(`/api/couriers/${courierId}/location`, {
            method: 'PUT',
            body: JSON.stringify(locationData)
        });
    }

    /**
     * Get courier assignments
     */
    async getCourierAssignments(courierId, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/couriers/${courierId}/assignments?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Update assignment status
     */
    async updateAssignmentStatus(assignmentId, status) {
        return this.request(`/api/couriers/assignments/${assignmentId}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
    }

    // ==========================================================================
    // MERCHANT ENDPOINTS
    // ==========================================================================

    /**
     * Get all merchants
     */
    async getMerchants(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/merchants?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get merchant by ID
     */
    async getMerchant(merchantId) {
        return this.request(`/api/merchants/${merchantId}`, {
            method: 'GET'
        });
    }

    /**
     * Create merchant
     */
    async createMerchant(merchantData) {
        return this.request('/api/merchants', {
            method: 'POST',
            body: JSON.stringify(merchantData)
        });
    }

    /**
     * Update merchant
     */
    async updateMerchant(merchantId, merchantData) {
        return this.request(`/api/merchants/${merchantId}`, {
            method: 'PUT',
            body: JSON.stringify(merchantData)
        });
    }

    /**
     * Delete merchant
     */
    async deleteMerchant(merchantId) {
        return this.request(`/api/merchants/${merchantId}`, {
            method: 'DELETE'
        });
    }

    // ==========================================================================
    // EMPLOYEE ENDPOINTS
    // ==========================================================================

    /**
     * Get all employees
     */
    async getEmployees(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/employees?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get employee by ID
     */
    async getEmployee(employeeId) {
        return this.request(`/api/employees/${employeeId}`, {
            method: 'GET'
        });
    }

    /**
     * Create employee
     */
    async createEmployee(employeeData) {
        return this.request('/api/employees', {
            method: 'POST',
            body: JSON.stringify(employeeData)
        });
    }

    /**
     * Update employee
     */
    async updateEmployee(employeeId, employeeData) {
        return this.request(`/api/employees/${employeeId}`, {
            method: 'PUT',
            body: JSON.stringify(employeeData)
        });
    }

    /**
     * Delete employee
     */
    async deleteEmployee(employeeId) {
        return this.request(`/api/employees/${employeeId}`, {
            method: 'DELETE'
        });
    }

    // ==========================================================================
    // TELEMETRY ENDPOINTS
    // ==========================================================================

    /**
     * Post telemetry data
     */
    async postTelemetry(telemetryData) {
        return this.request('/api/telemetry', {
            method: 'POST',
            body: JSON.stringify(telemetryData)
        });
    }

    /**
     * Get telemetry data
     */
    async getTelemetry(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/telemetry?${queryString}`, {
            method: 'GET'
        });
    }

    // ==========================================================================
    // REPORTING ENDPOINTS
    // ==========================================================================

    /**
     * Get dashboard statistics
     */
    async getDashboardStats(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/dashboard?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get dashboard revenue data (for charts)
     */
    async getDashboardRevenue(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/dashboard/revenue?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get shipments status summary (for charts)
     */
    async getShipmentsStatusSummary(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/dashboard/shipments-status?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get courier reports
     */
    async getCourierReports(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/couriers?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get couriers report (alias for getCourierReports)
     */
    async getCouriersReport(params = {}) {
        return this.getCourierReports(params);
    }

    /**
     * Get courier performance data (for charts)
     */
    async getCourierPerformanceData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/couriers/performance?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get courier distribution data (for charts)
     */
    async getCourierDistributionData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/couriers/distribution?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get merchant reports
     */
    async getMerchantReports(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/merchants?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get merchants report (alias for getMerchantReports)
     */
    async getMerchantsReport(params = {}) {
        return this.getMerchantReports(params);
    }

    /**
     * Get merchants performance data (for charts)
     */
    async getMerchantsPerformanceData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/merchants/performance?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get merchants distribution data (for charts)
     */
    async getMerchantsDistributionData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/merchants/distribution?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get merchants revenue data (for charts)
     */
    async getMerchantsRevenueData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/merchants/revenue?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get warehouse reports
     */
    async getWarehouseReports(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/warehouse?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get warehouse operations (alias for getWarehouseReports)
     */
    async getWarehouseOperations(params = {}) {
        return this.getWarehouseReports(params);
    }

    /**
     * Get warehouse operations data (for charts)
     */
    async getWarehouseOperationsData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/warehouse/operations?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get operations distribution data (for charts)
     */
    async getOperationsDistributionData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/warehouse/distribution?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get warehouse errors data (for charts)
     */
    async getErrorsData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/warehouse/errors?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Export data
     */
    async exportData(type, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/export/${type}?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get shipments report (for reports page)
     */
    async getShipmentsReport(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/shipments?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get shipments report data (for charts)
     */
    async getShipmentsReportData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/shipments?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get revenue report data (for charts)
     */
    async getRevenueReportData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/financial/revenue?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get courier deliveries data (for charts)
     */
    async getCourierDeliveriesData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/couriers?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get merchant shipments status summary (for charts)
     */
    async getMerchantShipmentsStatusSummary(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/reports/merchants?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get merchant revenue data (for charts)
     */
    async getMerchantRevenueData(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/financial/merchant-summary?${queryString}`, {
            method: 'GET'
        });
    }

    // ==========================================================================
    // NOTIFICATION ENDPOINTS
    // ==========================================================================

    /**
     * Get notifications
     */
    async getNotifications(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/notifications?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Mark notification as read
     */
    async markNotificationRead(notificationId) {
        return this.request(`/api/notifications/${notificationId}/read`, {
            method: 'PUT'
        });
    }

    /**
     * Mark all notifications as read
     */
    async markAllNotificationsRead() {
        return this.request('/api/notifications/read-all', {
            method: 'PUT'
        });
    }

    // ==========================================================================
    // SETTINGS ENDPOINTS
    // ==========================================================================

    /**
     * Get system settings
     */
    async getSettings() {
        return this.request('/api/settings', {
            method: 'GET'
        });
    }

    /**
     * Update system settings
     */
    async updateSettings(settingsData) {
        return this.request('/api/settings', {
            method: 'POST',
            body: JSON.stringify(settingsData)
        });
    }

    /**
     * Reset system settings to defaults
     */
    async resetSettings() {
        return this.request('/api/settings/reset', {
            method: 'POST'
        });
    }

    // ==========================================================================
    // HEALTH ENDPOINT
    // ==========================================================================

    /**
     * Get system health status
     */
    async getHealth() {
        return this.request('/api/health', {
            method: 'GET'
        });
    }

    // ==========================================================================
    // MASTER DATA ENDPOINTS
    // ==========================================================================

    /**
     * Get all statuses
     */
    async getStatuses() {
        return this.request('/api/master-data/statuses', {
            method: 'GET'
        });
    }

    /**
     * Get all roles
     */
    async getRoles() {
        return this.request('/api/master-data/roles', {
            method: 'GET'
        });
    }

    /**
     * Get all warehouses
     */
    async getWarehouses() {
        return this.request('/api/master-data/warehouses', {
            method: 'GET'
        });
    }

    // ==========================================================================
    // DASHBOARD ENDPOINT
    // ==========================================================================

    /**
     * Get dashboard data
     */
    async getDashboard() {
        return this.request('/api/dashboard', {
            method: 'GET'
        });
    }

    // ==========================================================================
    // PUBLIC ENDPOINTS
    // ==========================================================================

    /**
     * Submit contact form
     */
    async submitContactForm(contactData) {
        return this.request('/api/public/contact', {
            method: 'POST',
            body: JSON.stringify(contactData)
        });
    }

    // ==========================================================================
    // AUDIT ENDPOINTS
    // ==========================================================================

    /**
     * Get audit logs
     */
    async getAuditLogs(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/audit?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * Get audit log by ID
     */
    async getAuditLog(auditId) {
        return this.request(`/api/audit/${auditId}`, {
            method: 'GET'
        });
    }

    /**
     * Get audit statistics
     */
    async getAuditStats() {
        return this.request('/api/audit/stats', {
            method: 'GET'
        });
    }

    // ==========================================================================
    // SMS ENDPOINTS
    // ==========================================================================

    /**
     * Send SMS
     */
    async sendSms(smsData) {
        return this.request('/api/sms/send', {
            method: 'POST',
            body: JSON.stringify(smsData)
        });
    }

    /**
     * Send bulk SMS
     */
    async sendBulkSms(smsData) {
        return this.request('/api/sms/send-bulk', {
            method: 'POST',
            body: JSON.stringify(smsData)
        });
    }

    /**
     * Get SMS status
     */
    async getSmsStatus(messageId) {
        return this.request(`/api/sms/status/${messageId}`, {
            method: 'GET'
        });
    }

    /**
     * Send OTP via SMS
     */
    async sendOtp(otpData) {
        return this.request('/api/sms/send-otp', {
            method: 'POST',
            body: JSON.stringify(otpData)
        });
    }

    // ==========================================================================
    // BACKUP ENDPOINTS
    // ==========================================================================

    /**
     * Get all backups
     */
    async getBackups() {
        return this.request('/api/backup', {
            method: 'GET'
        });
    }

    /**
     * Create backup
     */
    async createBackup() {
        return this.request('/api/backup', {
            method: 'POST'
        });
    }

    /**
     * Get backup by ID
     */
    async getBackup(backupId) {
        return this.request(`/api/backup/${backupId}`, {
            method: 'GET'
        });
    }

    /**
     * Delete backup
     */
    async deleteBackup(backupId) {
        return this.request(`/api/backup/${backupId}`, {
            method: 'DELETE'
        });
    }

    // ==========================================================================
    // FILE UPLOAD ENDPOINTS
    // ==========================================================================

    /**
     * Upload file
     */
    async uploadFile(file, type = 'general') {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('type', type);

        return this.request('/api/upload', {
            method: 'POST',
            headers: {
                ...this.getAuthHeaders()
                // Don't set Content-Type for FormData
            },
            body: formData
        });
    }

    /**
     * Delete file
     */
    async deleteFile(fileId) {
        return this.request(`/api/upload/${fileId}`, {
            method: 'DELETE'
        });
    }

    // ==========================================================================
    // UTILITY METHODS
    // ==========================================================================

    /**
     * Handle API response
     */
    handleResponse(response) {
        if (response.success) {
            return response.data;
        } else {
            throw new Error(response.message || 'API request failed');
        }
    }

    /**
     * Handle API error
     */
    handleError(error) {
        throw error;
    }

    /**
     * Make authenticated request
     */
    async authenticatedRequest(endpoint, options = {}) {
        const token = sessionStorage.getItem('authToken');
        if (!token) {
            throw new Error('No authentication token found');
        }

        return this.request(endpoint, {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${token}`
            }
        });
    }

    /**
     * Get paginated data
     */
    async getPaginatedData(endpoint, page = 1, limit = 10, filters = {}) {
        const params = new URLSearchParams({
            page: page.toString(),
            limit: limit.toString(),
            ...Object.fromEntries(
                Object.entries(filters).map(([k, v]) => [k, String(v)])
            )
        });
        return this.request(`${endpoint}?${params.toString()}`, {
            method: 'GET'
        });
    }

    /**
     * Search data
     */
    async searchData(endpoint, query, filters = {}) {
        const params = new URLSearchParams({
            q: query,
            ...Object.fromEntries(
                Object.entries(filters).map(([k, v]) => [k, String(v)])
            )
        });
        return this.request(`${endpoint}?${params.toString()}`, {
            method: 'GET'
        });
    }

    /**
     * Clear authentication data
     */
    clearAuthData() {
        try {
            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('userData');
        } catch (error) {
            log.error('âŒ Error clearing auth data:', error);
        }
    }
}

// Create global instance
// Creating global ApiService instance - console.log removed for cleaner console
window.apiService = new ApiService();
// Global ApiService instance created - console.log removed for cleaner console

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ApiService;
}
