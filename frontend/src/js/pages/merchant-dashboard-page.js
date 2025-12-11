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

        
        // Load merchant data
        await this.loadMerchantData();
        
        // Load recent shipments
        await this.loadRecentShipments();
        
        // Initialize charts
        this.initializeCharts();
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
            
        } catch (error) {

            
        }
    }

    /**
     * Update merchant info display
     */
    updateMerchantInfo(user) {
        const merchantNameEl = document.getElementById('merchantName');
        if (merchantNameEl) {
            merchantNameEl.textContent = user.name || 'التاجر';
        }

        const merchantPhoneEl = document.getElementById('merchantPhone');
        if (merchantPhoneEl) {
            merchantPhoneEl.textContent = user.phone || 'غير محدد';
        }

        const merchantStatusEl = document.getElementById('merchantStatus');
        if (merchantStatusEl) {
            merchantStatusEl.innerHTML = `<span class="badge bg-${this.getStatusColor(user.status)}">${user.status?.name || 'غير محدد'}</span>`;
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

            }
            
        } catch (error) {

        }
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
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">لا توجد شحنات حديثة</td></tr>';
            return;
        }

        // Add shipment rows
        this.shipments.forEach(shipment => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${shipment.trackingNumber || 'غير محدد'}</td>
                <td>${shipment.courier?.name || 'غير محدد'}</td>
                <td><span class="badge bg-${this.getStatusColor(shipment.status)}">${shipment.status?.name || 'غير محدد'}</span></td>
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
            
        } catch (error) {

        }
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
                        label: 'الإيرادات',
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
