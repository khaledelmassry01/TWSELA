/**
 * Twsela CMS - Courier Dashboard Page Handler
 * Handles courier dashboard functionality
 */

class CourierDashboardHandler extends BasePageHandler {
    constructor() {
        super('Courier Dashboard');
        this.deliveries = [];
        this.currentPage = 1;
        this.pageSize = 10;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {

        
        // Load courier data
        await this.loadCourierData();
        
        // Load today's deliveries
        await this.loadTodaysDeliveries();
        
        // Load manifest
        await this.loadManifest();
        
        // Initialize charts
        this.initializeCharts();
    }

    /**
     * Load courier data
     */
    async loadCourierData() {
        try {

            
            const user = this.getCurrentUser();
            if (!user) {

                return;
            }

            // Update courier info display
            this.updateCourierInfo(user);
            
        } catch (error) { console.error('Unhandled error:', error); }
    }

    /**
     * Update courier info display
     */
    updateCourierInfo(user) {
        const courierNameEl = document.getElementById('courierName');
        if (courierNameEl) {
            courierNameEl.textContent = user.name || 'الساعي';
        }

        const courierPhoneEl = document.getElementById('courierPhone');
        if (courierPhoneEl) {
            courierPhoneEl.textContent = user.phone || 'غير محدد';
        }

        const courierStatusEl = document.getElementById('courierStatus');
        if (courierStatusEl) {
            courierStatusEl.innerHTML = `<span class="badge bg-${this.getStatusColor(user.status)}">${escapeHtml(user.status?.name || 'غير محدد')}</span>`;
        }
    }

    /**
     * Load today's deliveries
     */
    async loadTodaysDeliveries() {
        try {

            
            const today = new Date().toISOString().split('T')[0];
            const response = await this.services.api.getShipments({ 
                limit: 10,
                courierId: this.getCurrentUser()?.id,
                deliveryDate: today
            });
            
            if (response.success) {
                this.deliveries = response.data || [];
                this.updateTodaysDeliveriesTable();
            } else {

            }
            
        } catch (error) { console.error('Unhandled error:', error); }
    }

    /**
     * Update today's deliveries table
     */
    updateTodaysDeliveriesTable() {
        const tbody = document.querySelector('#todaysDeliveriesTable tbody');
        if (!tbody) return;

        // Clear existing rows
        tbody.innerHTML = '';

        if (!this.deliveries || this.deliveries.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">لا توجد توصيلات اليوم</td></tr>';
            return;
        }

        // Add delivery rows
        this.deliveries.forEach(delivery => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${delivery.trackingNumber || 'غير محدد'}</td>
                <td>${delivery.merchant?.name || 'غير محدد'}</td>
                <td>${delivery.customerName || 'غير محدد'}</td>
                <td>${delivery.customerPhone || 'غير محدد'}</td>
                <td><span class="badge bg-${this.getStatusColor(delivery.status)}">${delivery.status?.name || 'غير محدد'}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-success" onclick="courierDashboardHandler.markAsDelivered(${delivery.id})">
                        <i class="fas fa-check"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Load manifest
     */
    async loadManifest() {
        try {

            
            const response = await this.services.api.getCourierManifest();
            
            if (response.success) {
                this.updateManifestDisplay(response.data);
            } else {

            }
            
        } catch (error) { console.error('Unhandled error:', error); }
    }

    /**
     * Update manifest display
     */
    updateManifestDisplay(manifest) {
        const manifestIdEl = document.getElementById('manifestId');
        if (manifestIdEl) {
            manifestIdEl.textContent = manifest.id || 'غير محدد';
        }

        const manifestDateEl = document.getElementById('manifestDate');
        if (manifestDateEl) {
            manifestDateEl.textContent = this.formatDate(manifest.createdAt);
        }

        const manifestStatusEl = document.getElementById('manifestStatus');
        if (manifestStatusEl) {
            manifestStatusEl.innerHTML = `<span class="badge bg-${this.getStatusColor(manifest.status)}">${manifest.status?.name || 'غير محدد'}</span>`;
        }

        const manifestShipmentsEl = document.getElementById('manifestShipments');
        if (manifestShipmentsEl) {
            manifestShipmentsEl.textContent = manifest.shipments?.length || 0;
        }
    }

    /**
     * Initialize charts
     */
    initializeCharts() {
        try {

            
            // Initialize deliveries chart
            this.initDeliveriesChart();
            
            // Initialize performance chart
            this.initPerformanceChart();
            
        } catch (error) { console.error('Unhandled error:', error); }
    }

    /**
     * Initialize deliveries chart
     */
    async initDeliveriesChart() {
        const ctx = document.getElementById('deliveriesChart');
        if (!ctx) return;

        try {
            // Load courier deliveries data from API
            const deliveriesData = await this.loadCourierDeliveriesData();
            
            this.charts.deliveries = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: deliveriesData.labels || [],
                    datasets: [{
                        label: 'التوصيلات',
                        data: deliveriesData.values || [],
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
     * Load courier deliveries data from API
     */
    async loadCourierDeliveriesData() {
        try {
            const response = await this.services.api.getCourierDeliveriesData();
            if (response.success) {
                return response.data;
            }
        } catch (error) {
            // Return empty data on error
        }
        return { labels: [], values: [] };
    }

    /**
     * Initialize performance chart
     */
    async initPerformanceChart() {
        const ctx = document.getElementById('performanceChart');
        if (!ctx) return;

        try {
            // Load courier performance data from API
            const performanceData = await this.loadCourierPerformanceData();
            
            this.charts.performance = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: performanceData.labels || [],
                    datasets: [{
                        label: 'معدل التوصيل',
                        data: performanceData.values || [],
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100
                        }
                    }
                }
            });
        } catch (error) {
            // Chart will not be initialized if data loading fails
        }
    }

    /**
     * Load courier performance data from API
     */
    async loadCourierPerformanceData() {
        try {
            const response = await this.services.api.getCourierPerformanceData();
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

        
        // View manifest button
        const viewManifestBtn = document.getElementById('viewManifestBtn');
        if (viewManifestBtn) {
            viewManifestBtn.addEventListener('click', () => {
                this.viewManifest();
            });
        }

        // Start delivery button
        const startDeliveryBtn = document.getElementById('startDeliveryBtn');
        if (startDeliveryBtn) {
            startDeliveryBtn.addEventListener('click', () => {
                this.startDelivery();
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
     * Mark delivery as delivered
     */
    async markAsDelivered(shipmentId) {
        try {

            
            if (confirm('هل أنت متأكد من تسليم هذه الشحنة؟')) {
                const response = await this.services.api.updateShipmentStatus(shipmentId, 'DELIVERED');
                
                if (response.success) {
                    this.showSuccess('تم تسليم الشحنة بنجاح');
                    this.loadTodaysDeliveries(); // Reload data
                } else {
                    
                }
            }
        } catch (error) { console.error('Unhandled error:', error); }
    }

    /**
     * View manifest
     */
    viewManifest() {

        window.location.href = '/courier/manifest.html';
    }

    /**
     * Start delivery
     */
    startDelivery() {

        this.showInfo('بدء التوصيل قيد التطوير');
    }
}

// Create global instance

window.courierDashboardHandler = new CourierDashboardHandler();


// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {


    
    // Only initialize if this is the courier dashboard page
    if (window.location.pathname.includes('/courier/dashboard.html')) {

        setTimeout(() => {

            window.courierDashboardHandler.init();
        }, 200);
    } else {

    }
});
