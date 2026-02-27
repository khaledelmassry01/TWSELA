import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-dashboard-page');

/**
 * Twsela CMS - Merchant Dashboard Page Handler
 * Handles merchant dashboard functionality
 */

class MerchantDashboardHandler extends BasePageHandler {
    constructor() {
        super('Merchant Dashboard');
        this.shipments = [];
        this.currentPage = 1;
        this.pageSize = 10;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
        
            // Load merchant data
            await this.loadMerchantData();
            
            // Load recent shipments
            await this.loadRecentShipments();
            
            // Initialize charts
            this.initializeCharts();
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantDashboard');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load merchant data
     */
    async loadMerchantData() {
        try {

            
            const user = this.getCurrentUser();
            if (!user) {

                return;
            }

            // Update merchant info display
            this.updateMerchantInfo(user);
            
        } catch (error) { ErrorHandler.handle(error, 'MerchantDashboard.merchantData'); }
    }

    /**
     * Update merchant info display
     */
    updateMerchantInfo(user) {
        const merchantNameEl = document.getElementById('merchantName');
        if (merchantNameEl) {
            merchantNameEl.textContent = user.name || 'Ø§Ù„ØªØ§Ø¬Ø±';
        }

        const merchantPhoneEl = document.getElementById('merchantPhone');
        if (merchantPhoneEl) {
            merchantPhoneEl.textContent = user.phone || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        }

        const merchantStatusEl = document.getElementById('merchantStatus');
        if (merchantStatusEl) {
            merchantStatusEl.innerHTML = `<span class="badge bg-${this.getStatusColor(user.status)}">${escapeHtml(user.status?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</span>`;
        }
    }

    /**
     * Load recent shipments
     */
    async loadRecentShipments() {
        try {

            
            const response = await this.services.api.getShipments({ 
                limit: 5,
                merchantId: this.getCurrentUser()?.id
            });
            
            if (response.success) {
                this.shipments = response.data || [];
                this.updateRecentShipmentsTable();
            } else {
                UIUtils.showEmptyState('#recentShipmentsTable', 'لا توجد شحنات حديثة', 'box');
            }
            
        } catch (error) { ErrorHandler.handle(error, 'MerchantDashboard.shipments'); }
    }

    /**
     * Update recent shipments table
     */
    updateRecentShipmentsTable() {
        const tbody = document.querySelector('#recentShipmentsTable tbody');
        if (!tbody) return;

        // Clear existing rows
        tbody.innerHTML = '';

        if (!this.shipments || this.shipments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Ù„Ø§ ØªÙˆØ¬Ø¯ Ø´Ø­Ù†Ø§Øª Ø­Ø¯ÙŠØ«Ø©</td></tr>';
            return;
        }

        // Add shipment rows
        this.shipments.forEach(shipment => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${escapeHtml(shipment.trackingNumber || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                <td>${escapeHtml(shipment.courier?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                <td><span class="badge bg-${this.getStatusColor(shipment.status)}">${escapeHtml(shipment.status?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</span></td>
                <td>${this.formatCurrency(shipment.totalAmount)}</td>
                <td>${this.formatDate(shipment.createdAt)}</td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Initialize charts
     */
    initializeCharts() {
        try {

            
            // Initialize shipments chart
            this.initShipmentsChart();
            
            // Initialize revenue chart
            this.initRevenueChart();
            
        } catch (error) { ErrorHandler.handle(error, 'MerchantDashboard.charts'); }
    }

    /**
     * Initialize shipments chart
     */
    async initShipmentsChart() {
        const ctx = document.getElementById('shipmentsChart');
        if (!ctx) return;

        try {
            // Load merchant shipments status data from API
            const statusData = await this.loadMerchantShipmentsStatusData();
            
            this.charts.shipments = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: statusData.labels || [],
                    datasets: [{
                        data: statusData.values || [],
                        backgroundColor: [
                            '#ffc107',
                            '#17a2b8',
                            '#007bff',
                            '#28a745',
                            '#dc3545'
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        }
                    }
                }
            });
        } catch (error) {
            // Chart will not be initialized if data loading fails
        }
    }

    /**
     * Load merchant shipments status data from API
     */
    async loadMerchantShipmentsStatusData() {
        try {
            const response = await this.services.api.getMerchantShipmentsStatusSummary();
            if (response.success) {
                return response.data;
            }
        } catch (error) {
            // Return empty data on error
        }
        return { labels: [], values: [] };
    }

    /**
     * Initialize revenue chart
     */
    async initRevenueChart() {
        const ctx = document.getElementById('revenueChart');
        if (!ctx) return;

        try {
            // Load merchant revenue data from API
            const revenueData = await this.loadMerchantRevenueData();
            
            this.charts.revenue = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: revenueData.labels || [],
                    datasets: [{
                        label: 'Ø§Ù„Ø¥ÙŠØ±Ø§Ø¯Ø§Øª',
                        data: revenueData.values || [],
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        } catch (error) {
            // Chart will not be initialized if data loading fails
        }
    }

    /**
     * Load merchant revenue data from API
     */
    async loadMerchantRevenueData() {
        try {
            const response = await this.services.api.getMerchantRevenueData();
            if (response.success) {
                return response.data;
            }
        } catch (error) {
            // Return empty data on error
        }
        return { labels: [], values: [] };
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {

        
        // Create shipment button
        const createShipmentBtn = document.getElementById('createShipmentBtn');
        if (createShipmentBtn) {
            createShipmentBtn.addEventListener('click', () => {
                this.createShipment();
            });
        }

        // View all shipments button
        const viewAllShipmentsBtn = document.getElementById('viewAllShipmentsBtn');
        if (viewAllShipmentsBtn) {
            viewAllShipmentsBtn.addEventListener('click', () => {
                this.viewAllShipments();
            });
        }

        // Logout button
        const logoutBtn = document.querySelector('.logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                this.handleLogout();
            });
        }
    }

    /**
     * Create new shipment
     */
    createShipment() {

        window.location.href = '/merchant/create-shipment.html';
    }

    /**
     * View all shipments
     */
    viewAllShipments() {

        window.location.href = '/merchant/shipments.html';
    }
}

// Create global instance

window.merchantDashboardHandler = new MerchantDashboardHandler();


// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {


    
    // Only initialize if this is the merchant dashboard page
    if (window.location.pathname.includes('/merchant/dashboard.html')) {

        setTimeout(() => {

            window.merchantDashboardHandler.init();
        }, 200);
    } else {

    }
});
