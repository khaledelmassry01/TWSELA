/**
 * Twsela CMS - Owner Dashboard Page Handler
 * Handles data loading and display for the owner dashboard
 */

class OwnerDashboardHandler extends BasePageHandler {
    constructor() {
        super('Owner Dashboard');
        this.charts = {};
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            // Load dashboard data
            await this.loadDashboardData();
            
            // Initialize charts
            this.initializeCharts();
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerDashboard');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load dashboard data from API
     */
    async loadDashboardData() {
        try {
            // Load dashboard statistics using existing endpoints
            const [shipmentsResponse, couriersResponse, merchantsResponse] = await Promise.allSettled([
                window.apiService.getShipments({ limit: 1 }), // Just to get count
                window.apiService.getCouriers({ limit: 1 }),   // Just to get count
                window.apiService.getMerchants({ limit: 1 })  // Just to get count
            ]);

            // Extract counts from responses
            const totalShipments = shipmentsResponse.status === 'fulfilled' && shipmentsResponse.value.success 
                ? shipmentsResponse.value.data?.length || 0 : 0;
            
            const activeCouriers = couriersResponse.status === 'fulfilled' && couriersResponse.value.success 
                ? couriersResponse.value.data?.length || 0 : 0;
            
            const activeMerchants = merchantsResponse.status === 'fulfilled' && merchantsResponse.value.success 
                ? merchantsResponse.value.data?.length || 0 : 0;

            // Update statistics with real data
            this.updateStatistics({
                totalShipments,
                activeCouriers,
                activeMerchants,
                totalRevenue: 0 // Will be calculated later
            });

            // Load recent shipments
            await this.loadRecentShipments();
            
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerDashboard');
            // Set default values on error
            this.updateStatistics({
                totalShipments: 0,
                activeCouriers: 0,
                activeMerchants: 0,
                totalRevenue: 0
            });
        }
    }

    /**
     * Update statistics display
     */
    updateStatistics(stats) {
        // Update total shipments
        const totalShipmentsEl = document.getElementById('totalShipments');
        if (totalShipmentsEl) {
            totalShipmentsEl.textContent = stats.totalShipments || 0;
        }

        // Update active couriers
        const activeCouriersEl = document.getElementById('activeCouriers');
        if (activeCouriersEl) {
            activeCouriersEl.textContent = stats.activeCouriers || 0;
        }

        // Update active merchants
        const activeMerchantsEl = document.getElementById('activeMerchants');
        if (activeMerchantsEl) {
            activeMerchantsEl.textContent = stats.activeMerchants || 0;
        }

        // Update total revenue
        const totalRevenueEl = document.getElementById('totalRevenue');
        if (totalRevenueEl) {
            totalRevenueEl.textContent = stats.totalRevenue || 0;
        }
    }

    /**
     * Load recent shipments
     */
    async loadRecentShipments() {
        try {
            const shipmentsResponse = await window.apiService.getShipments({ limit: 5 });
            if (shipmentsResponse.success) {
                this.updateRecentShipmentsTable(shipmentsResponse.data);
            } else {
                UIUtils.showEmptyState('#recentShipmentsTable', 'لا توجد شحنات حديثة', 'box');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerDashboard.recentShipments');
        }
    }

    /**
     * Update recent shipments table
     */
    updateRecentShipmentsTable(shipments) {
        const tbody = document.querySelector('#recentShipmentsTable tbody');
        if (!tbody) return;

        // Clear existing rows
        tbody.innerHTML = '';

        // Ensure shipments is an array
        const shipmentsArray = Array.isArray(shipments) ? shipments : (shipments?.content || []);
        
        if (!shipmentsArray || shipmentsArray.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">لا توجد شحنات حديثة</td></tr>';
            return;
        }

        // Add shipment rows
        shipmentsArray.forEach(shipment => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${escapeHtml(shipment.trackingNumber || 'غير محدد')}</td>
                <td>${escapeHtml(shipment.merchant?.name || 'غير محدد')}</td>
                <td><span class="badge bg-primary">${escapeHtml(shipment.status?.name || 'غير محدد')}</span></td>
                <td>${shipment.createdAt ? new Date(shipment.createdAt).toLocaleDateString('ar-SA') : 'غير محدد'}</td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Initialize charts
     */
    initializeCharts() {
        try {
            // Initialize revenue chart
            this.initRevenueChart();
            
            // Initialize shipments chart
            this.initShipmentsChart();
            
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerDashboard.charts');
        }
    }

    /**
     * Initialize revenue chart
     */
    async initRevenueChart() {
        const ctx = document.getElementById('revenueChart');
        if (!ctx) return;

        try {
            // Load revenue data from API
            const revenueData = await this.loadRevenueData();
            
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
                    plugins: {
                        legend: {
                            display: false
                        }
                    },
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
     * Load revenue data from API
     */
    async loadRevenueData() {
        try {
            const response = await window.apiService.getDashboardRevenue();
            if (response.success) {
                return response.data;
            }
        } catch (error) {
            // Return empty data on error
        }
        return { labels: [], values: [] };
    }

    /**
     * Initialize shipments chart
     */
    async initShipmentsChart() {
        const ctx = document.getElementById('shipmentsChart');
        if (!ctx) return;

        try {
            // Load shipments status data from API
            const statusData = await this.loadShipmentsStatusData();
            
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
     * Load shipments status data from API
     */
    async loadShipmentsStatusData() {
        try {
            const response = await window.apiService.getShipmentsStatusSummary();
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
        // Quick action buttons
        document.querySelectorAll('.quick-action-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const action = e.currentTarget.dataset.action;
                this.handleQuickAction(action);
            });
        });

        // Logout button
        const logoutBtn = document.querySelector('.logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                this.handleLogout();
            });
        }
    }

    /**
     * Handle quick actions
     */
    handleQuickAction(action) {
        switch (action) {
            case 'addEmployee':
                window.location.href = '/owner/employees.html';
                break;
            case 'addZone':
                window.location.href = '/owner/zones.html';
                break;
            case 'viewReports':
                window.location.href = '/owner/reports.html';
                break;
            case 'managePricing':
                window.location.href = '/owner/pricing.html';
                break;
            case 'systemSettings':
                window.location.href = '/owner/settings.html';
                break;
            case 'auditLog':
                this.notificationService.info('سجل التدقيق قيد التطوير');
                break;
            default:
                // Unknown action
        }
    }
}

// Create global instance
window.ownerDashboardHandler = new OwnerDashboardHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Only initialize if this is the owner dashboard page
    if (window.location.pathname.includes('/owner/dashboard.html')) {
        setTimeout(() => {
            window.ownerDashboardHandler.init();
        }, 200);
    }
});

// Force initialization after a longer delay as backup
setTimeout(() => {
    if (window.location.pathname.includes('/owner/dashboard.html') && 
        window.ownerDashboardHandler && 
        !window.ownerDashboardHandler.isInitialized) {
        window.ownerDashboardHandler.init();
    }
}, 1000);
