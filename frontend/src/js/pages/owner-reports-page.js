import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('owner-reports-page');

/**
 * Twsela CMS - Owner Reports Page Handler
 * Handles reports and analytics for owner
 */

class OwnerReportsHandler extends BasePageHandler {
    constructor() {
        super('Owner Reports');
        this.charts = {};
        this.reportData = {};
        this.currentReportType = 'overview';
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
        
            // Load report data
            await this.loadReportData();
            
            // Initialize charts
            this.initializeCharts();
            
            // Setup report type selector
            this.setupReportTypeSelector();
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerReports');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load report data
     */
    async loadReportData() {
        try {

            
            // Load different types of reports based on current type
            switch (this.currentReportType) {
                case 'overview':
                    await this.loadOverviewReport();
                    break;
                case 'shipments':
                    await this.loadShipmentsReport();
                    break;
                case 'couriers':
                    await this.loadCouriersReport();
                    break;
                case 'merchants':
                    await this.loadMerchantsReport();
                    break;
                case 'revenue':
                    await this.loadRevenueReport();
                    break;
                default:
                    await this.loadOverviewReport();
            }
            
        } catch (error) { ErrorHandler.handle(error, 'OwnerReports.loadData'); }
    }

    /**
     * Load overview report
     */
    async loadOverviewReport() {
        try {

            
            const [shipmentsResponse, couriersResponse, merchantsResponse] = await Promise.allSettled([
                this.services.api.getShipments({ limit: 100 }),
                this.services.api.getCouriers({ limit: 100 }),
                this.services.api.getMerchants({ limit: 100 })
            ]);

            this.reportData.overview = {
                totalShipments: shipmentsResponse.status === 'fulfilled' && shipmentsResponse.value.success 
                    ? shipmentsResponse.value.data?.length || 0 : 0,
                totalCouriers: couriersResponse.status === 'fulfilled' && couriersResponse.value.success 
                    ? couriersResponse.value.data?.length || 0 : 0,
                totalMerchants: merchantsResponse.status === 'fulfilled' && merchantsResponse.value.success 
                    ? merchantsResponse.value.data?.length || 0 : 0
            };

            this.updateOverviewDisplay();
            
        } catch (error) { ErrorHandler.handle(error, 'OwnerReports.overview'); }
    }

    /**
     * Load shipments report
     */
    async loadShipmentsReport() {
        try {

            
            const response = await this.services.api.getShipments({ limit: 1000 });
            
            if (response.success) {
                this.reportData.shipments = response.data || [];
                this.updateShipmentsReport();
            }
            
        } catch (error) { ErrorHandler.handle(error, 'OwnerReports.shipments'); }
    }

    /**
     * Load couriers report
     */
    async loadCouriersReport() {
        try {

            
            const response = await this.services.api.getCouriers({ limit: 1000 });
            
            if (response.success) {
                this.reportData.couriers = response.data || [];
                this.updateCouriersReport();
            }
            
        } catch (error) { ErrorHandler.handle(error, 'OwnerReports.couriers'); }
    }

    /**
     * Load merchants report
     */
    async loadMerchantsReport() {
        try {

            
            const response = await this.services.api.getMerchants({ limit: 1000 });
            
            if (response.success) {
                this.reportData.merchants = response.data || [];
                this.updateMerchantsReport();
            }
            
        } catch (error) { ErrorHandler.handle(error, 'OwnerReports.merchants'); }
    }

    /**
     * Load revenue report
     */
    async loadRevenueReport() {
        try {

            
            const response = await this.services.api.getShipments({ limit: 1000 });
            
            if (response.success) {
                const shipments = response.data || [];
                const totalRevenue = shipments.reduce((sum, shipment) => {
                    return sum + (shipment.totalAmount || 0);
                }, 0);
                
                this.reportData.revenue = {
                    totalRevenue,
                    shipments: shipments
                };
                
                this.updateRevenueReport();
            }
            
        } catch (error) { ErrorHandler.handle(error, 'OwnerReports.revenue'); }
    }

    /**
     * Update overview display
     */
    updateOverviewDisplay() {
        const data = this.reportData.overview;
        if (!data) return;

        // Update overview cards
        const totalShipmentsEl = document.getElementById('totalShipmentsOverview');
        if (totalShipmentsEl) {
            totalShipmentsEl.textContent = data.totalShipments;
        }

        const totalCouriersEl = document.getElementById('totalCouriersOverview');
        if (totalCouriersEl) {
            totalCouriersEl.textContent = data.totalCouriers;
        }

        const totalMerchantsEl = document.getElementById('totalMerchantsOverview');
        if (totalMerchantsEl) {
            totalMerchantsEl.textContent = data.totalMerchants;
        }
    }

    /**
     * Update shipments report
     */
    updateShipmentsReport() {
        const data = this.reportData.shipments;
        if (!data) return;

        // Update shipments table
        const tbody = document.querySelector('#shipmentsReportTable tbody');
        if (tbody) {
            tbody.innerHTML = '';
            
            data.forEach(shipment => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${escapeHtml(shipment.trackingNumber || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                    <td>${escapeHtml(shipment.merchant?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                    <td>${escapeHtml(shipment.courier?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                    <td><span class="badge bg-${this.getStatusColor(shipment.status)}">${escapeHtml(shipment.status?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</span></td>
                    <td>${this.formatCurrency(shipment.totalAmount)}</td>
                    <td>${this.formatDate(shipment.createdAt)}</td>
                `;
                tbody.appendChild(row);
            });
        }
    }

    /**
     * Update couriers report
     */
    updateCouriersReport() {
        const data = this.reportData.couriers;
        if (!data) return;

        // Update couriers table
        const tbody = document.querySelector('#couriersReportTable tbody');
        if (tbody) {
            tbody.innerHTML = '';
            
            data.forEach(courier => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${escapeHtml(courier.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                    <td>${escapeHtml(courier.phone || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                    <td><span class="badge bg-${this.getStatusColor(courier.status)}">${escapeHtml(courier.status?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</span></td>
                    <td>${courier.totalDeliveries || 0}</td>
                    <td>${this.formatDate(courier.createdAt)}</td>
                `;
                tbody.appendChild(row);
            });
        }
    }

    /**
     * Update merchants report
     */
    updateMerchantsReport() {
        const data = this.reportData.merchants;
        if (!data) return;

        // Update merchants table
        const tbody = document.querySelector('#merchantsReportTable tbody');
        if (tbody) {
            tbody.innerHTML = '';
            
            data.forEach(merchant => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${escapeHtml(merchant.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                    <td>${escapeHtml(merchant.phone || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                    <td><span class="badge bg-${this.getStatusColor(merchant.status)}">${escapeHtml(merchant.status?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</span></td>
                    <td>${merchant.totalShipments || 0}</td>
                    <td>${this.formatDate(merchant.createdAt)}</td>
                `;
                tbody.appendChild(row);
            });
        }
    }

    /**
     * Update revenue report
     */
    updateRevenueReport() {
        const data = this.reportData.revenue;
        if (!data) return;

        // Update revenue display
        const totalRevenueEl = document.getElementById('totalRevenueReport');
        if (totalRevenueEl) {
            totalRevenueEl.textContent = this.formatCurrency(data.totalRevenue);
        }
    }

    /**
     * Setup report type selector
     */
    setupReportTypeSelector() {
        const reportTypeSelect = document.getElementById('reportTypeSelect');
        if (reportTypeSelect) {
            reportTypeSelect.addEventListener('change', (e) => {
                this.currentReportType = e.target.value;
                this.loadReportData();
            });
        }
    }

    /**
     * Initialize charts
     */
    initializeCharts() {
        try {

            
            // Initialize overview chart
            this.initOverviewChart();
            
            // Initialize shipments chart
            this.initShipmentsChart();
            
            // Initialize revenue chart
            this.initRevenueChart();
            
        } catch (error) { ErrorHandler.handle(error, 'OwnerReports.charts'); }
    }

    /**
     * Initialize overview chart
     */
    initOverviewChart() {
        const ctx = document.getElementById('overviewChart');
        if (!ctx) return;

        this.charts.overview = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Ø§Ù„Ø´Ø­Ù†Ø§Øª', 'Ø§Ù„Ø³Ø¹Ø§Ø©', 'Ø§Ù„ØªØ¬Ø§Ø±'],
                datasets: [{
                    data: [
                        this.reportData.overview?.totalShipments || 0,
                        this.reportData.overview?.totalCouriers || 0,
                        this.reportData.overview?.totalMerchants || 0
                    ],
                    backgroundColor: [
                        '#007bff',
                        '#28a745',
                        '#ffc107'
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
    }

    /**
     * Initialize shipments chart
     */
    async initShipmentsChart() {
        const ctx = document.getElementById('shipmentsChart');
        if (!ctx) return;

        try {
            // Load shipments report data from API
            const shipmentsData = await this.loadShipmentsReportData();
            
            this.charts.shipments = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: shipmentsData.labels || [],
                    datasets: [{
                        label: 'Ø§Ù„Ø´Ø­Ù†Ø§Øª',
                        data: shipmentsData.values || [],
                        backgroundColor: '#007bff'
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
     * Load shipments report data from API
     */
    async loadShipmentsReportData() {
        try {
            const response = await window.apiService.getShipmentsReportData();
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
            // Load revenue report data from API
            const revenueData = await this.loadRevenueReportData();
            
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
     * Load revenue report data from API
     */
    async loadRevenueReportData() {
        try {
            const response = await window.apiService.getRevenueReportData();
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

        
        // Export button
        const exportBtn = document.getElementById('exportReportBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => {
                this.exportReport();
            });
        }

        // Refresh button
        const refreshBtn = document.getElementById('refreshReportBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                this.loadReportData();
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
     * Export report
     */
    async exportReport() {
        try {

            // TODO: Implement export functionality
            this.showInfo('تصدير التقرير قيد التطوير');
        } catch (error) { ErrorHandler.handle(error, 'OwnerReports.export'); }
    }
}

// Create global instance

window.ownerReportsHandler = new OwnerReportsHandler();


// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {


    
    // Only initialize if this is the owner reports page
    if (window.location.pathname.includes('/owner/reports.html')) {

        setTimeout(() => {

            window.ownerReportsHandler.init();
        }, 200);
    } else {

    }
});
