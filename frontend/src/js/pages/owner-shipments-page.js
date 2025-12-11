/**
 * Twsela CMS - Owner Shipments Page Handler
 * Handles shipment management for owner
 */

class OwnerShipmentsHandler extends BasePageHandler {
    constructor() {
        super('Owner Shipments');
        this.shipments = [];
        this.currentPage = 1;
        this.pageSize = 10;
        this.filters = {
            status: '',
            merchant: '',
            courier: '',
            dateFrom: '',
            dateTo: ''
        };
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {

        
        // Load shipments data
        await this.loadShipments();
        
        // Setup filters
        this.setupFilters();
        
        // Setup pagination
        this.setupPagination();
    }

    /**
     * Load shipments data
     */
    async loadShipments() {
        try {

            
            const params = {
                page: this.currentPage,
                size: this.pageSize,
                ...this.filters
            };
            
            const response = await this.services.api.getShipments(params);
            
            if (response.success) {
                this.shipments = response.data || [];
                this.updateShipmentsTable();
                this.updatePaginationInfo(response.totalElements || 0);
            } else {

                
            }
            
        } catch (error) {

            
        }
    }

    /**
     * Update shipments table
     */
    updateShipmentsTable() {
        const tbody = document.querySelector('#shipmentsTable tbody');
        if (!tbody) return;

        // Clear existing rows
        tbody.innerHTML = '';

        if (!this.shipments || this.shipments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">لا توجد شحنات</td></tr>';
            return;
        }

        // Add shipment rows
        this.shipments.forEach(shipment => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${shipment.trackingNumber || 'غير محدد'}</td>
                <td>${shipment.merchant?.name || 'غير محدد'}</td>
                <td>${shipment.courier?.name || 'غير محدد'}</td>
                <td><span class="badge bg-${this.getStatusColor(shipment.status)}">${shipment.status?.name || 'غير محدد'}</span></td>
                <td>${this.formatCurrency(shipment.totalAmount)}</td>
                <td>${this.formatDate(shipment.createdAt)}</td>
                <td>${this.formatDate(shipment.deliveryDate)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="ownerShipmentsHandler.viewShipment(${shipment.id})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-warning" onclick="ownerShipmentsHandler.editShipment(${shipment.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Setup filters
     */
    setupFilters() {
        // Status filter
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', (e) => {
                this.filters.status = e.target.value;
                this.currentPage = 1;
                this.loadShipments();
            });
        }

        // Merchant filter
        const merchantFilter = document.getElementById('merchantFilter');
        if (merchantFilter) {
            merchantFilter.addEventListener('change', (e) => {
                this.filters.merchant = e.target.value;
                this.currentPage = 1;
                this.loadShipments();
            });
        }

        // Courier filter
        const courierFilter = document.getElementById('courierFilter');
        if (courierFilter) {
            courierFilter.addEventListener('change', (e) => {
                this.filters.courier = e.target.value;
                this.currentPage = 1;
                this.loadShipments();
            });
        }

        // Date filters
        const dateFromFilter = document.getElementById('dateFromFilter');
        if (dateFromFilter) {
            dateFromFilter.addEventListener('change', (e) => {
                this.filters.dateFrom = e.target.value;
                this.currentPage = 1;
                this.loadShipments();
            });
        }

        const dateToFilter = document.getElementById('dateToFilter');
        if (dateToFilter) {
            dateToFilter.addEventListener('change', (e) => {
                this.filters.dateTo = e.target.value;
                this.currentPage = 1;
                this.loadShipments();
            });
        }

        // Clear filters button
        const clearFiltersBtn = document.getElementById('clearFiltersBtn');
        if (clearFiltersBtn) {
            clearFiltersBtn.addEventListener('click', () => {
                this.clearFilters();
            });
        }
    }

    /**
     * Clear all filters
     */
    clearFilters() {
        this.filters = {
            status: '',
            merchant: '',
            courier: '',
            dateFrom: '',
            dateTo: ''
        };

        // Reset filter inputs
        document.getElementById('statusFilter').value = '';
        document.getElementById('merchantFilter').value = '';
        document.getElementById('courierFilter').value = '';
        document.getElementById('dateFromFilter').value = '';
        document.getElementById('dateToFilter').value = '';

        this.currentPage = 1;
        this.loadShipments();
    }

    /**
     * Setup pagination
     */
    setupPagination() {
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        
        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                if (this.currentPage > 1) {
                    this.currentPage--;
                    this.loadShipments();
                }
            });
        }
        
        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                this.currentPage++;
                this.loadShipments();
            });
        }
    }

    /**
     * Update pagination info
     */
    updatePaginationInfo(totalElements) {
        const paginationInfo = document.getElementById('paginationInfo');
        if (paginationInfo) {
            const startItem = (this.currentPage - 1) * this.pageSize + 1;
            const endItem = Math.min(this.currentPage * this.pageSize, totalElements);
            paginationInfo.textContent = `عرض ${startItem}-${endItem} من ${totalElements}`;
        }
    }

    /**
     * View shipment details
     */
    async viewShipment(shipmentId) {
        try {

            // TODO: Implement view functionality
            this.showInfo('عرض تفاصيل الشحنة قيد التطوير');
        } catch (error) {

            
        }
    }

    /**
     * Edit shipment
     */
    async editShipment(shipmentId) {
        try {

            // TODO: Implement edit functionality
            this.showInfo('تعديل الشحنة قيد التطوير');
        } catch (error) {

            
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {

        
        // Export button
        const exportBtn = document.getElementById('exportBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => {
                this.exportShipments();
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
     * Export shipments
     */
    async exportShipments() {
        try {

            // TODO: Implement export functionality
            this.showInfo('تصدير الشحنات قيد التطوير');
        } catch (error) {

            
        }
    }
}

// Create global instance

window.ownerShipmentsHandler = new OwnerShipmentsHandler();


// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {


    
    // Only initialize if this is the owner shipments page
    if (window.location.pathname.includes('/owner/shipments.html')) {

        setTimeout(() => {

            window.ownerShipmentsHandler.init();
        }, 200);
    } else {

    }
});
