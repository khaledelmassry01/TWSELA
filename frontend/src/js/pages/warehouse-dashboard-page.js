/**
 * Twsela CMS - Warehouse Dashboard Page Handler
 * Handles warehouse operations: incoming/outgoing shipments, inventory, activity
 */

class WarehouseDashboardHandler extends BasePageHandler {
    constructor() {
        super('Warehouse Dashboard');
        this.incomingShipments = [];
        this.outgoingShipments = [];
        this.inventory = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();

            // Setup event listeners for quick actions
            this.setupQuickActions();
            this.setupSearchFilters();

            // Load all dashboard data
            await this.loadDashboardData();
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseDashboard');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load all dashboard data in parallel
     */
    async loadDashboardData() {
        try {
            const [warehouseRes, shipmentsRes] = await Promise.allSettled([
                window.apiService.getWarehouseOperations(),
                window.apiService.getShipments({ limit: 50 })
            ]);

            // Process warehouse operations
            if (warehouseRes.status === 'fulfilled' && warehouseRes.value?.success) {
                const operations = warehouseRes.value.data || [];
                this.processOperations(operations);
            }

            // Process shipments
            if (shipmentsRes.status === 'fulfilled' && shipmentsRes.value?.success) {
                const shipments = shipmentsRes.value.data || [];
                this.categorizeShipments(shipments);
            }

            // Update all displays
            this.updateKPIs();
            this.renderIncomingTable();
            this.renderOutgoingTable();
            this.renderInventoryTable();
            this.renderRecentActivity();

        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseDashboard.loadData');
        }
    }

    /**
     * Process warehouse operations data
     */
    processOperations(operations) {
        this.recentActivity = operations.slice(0, 20);
    }

    /**
     * Categorize shipments into incoming, outgoing, and inventory
     */
    categorizeShipments(shipments) {
        this.incomingShipments = shipments.filter(s =>
            ['CREATED', 'PICKED_UP'].includes(s.status)
        );
        this.outgoingShipments = shipments.filter(s =>
            ['IN_TRANSIT', 'OUT_FOR_DELIVERY'].includes(s.status)
        );
        this.inventory = shipments.filter(s =>
            ['AT_WAREHOUSE', 'PROCESSING'].includes(s.status)
        );
    }

    /**
     * Update KPI display cards
     */
    updateKPIs() {
        const kpiValues = document.querySelectorAll('.kpi-value');
        if (kpiValues.length >= 4) {
            kpiValues[0].textContent = this.incomingShipments.length;
            kpiValues[1].textContent = this.outgoingShipments.length;
            kpiValues[2].textContent = this.inventory.length;
            kpiValues[3].textContent = this.incomingShipments.length + this.outgoingShipments.length + this.inventory.length;
        }
    }

    /**
     * Render incoming shipments table
     */
    renderIncomingTable() {
        const tbody = document.querySelector('#incomingShipmentsTable tbody');
        if (!tbody) return;

        if (this.incomingShipments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">\u0644\u0627 \u062a\u0648\u062c\u062f \u0634\u062d\u0646\u0627\u062a \u0648\u0627\u0631\u062f\u0629</td></tr>';
            return;
        }

        tbody.innerHTML = this.incomingShipments.map(shipment => `
            <tr>
                <td>${escapeHtml(shipment.trackingNumber || '')}</td>
                <td>${escapeHtml(shipment.merchantName || '')}</td>
                <td>${escapeHtml(shipment.recipientName || '')}</td>
                <td><span class="badge bg-${SharedDataUtils.getStatusColor(shipment.status)}">${escapeHtml(SharedDataUtils.getStatusText(shipment.status))}</span></td>
                <td>${SharedDataUtils.formatDate(shipment.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary action-btn receive-btn" data-shipment-id="${shipment.id}" title="\u0627\u0633\u062a\u0644\u0627\u0645">
                        <i class="fas fa-check"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    /**
     * Render outgoing shipments table
     */
    renderOutgoingTable() {
        const tbody = document.querySelector('#outgoingShipmentsTable tbody');
        if (!tbody) return;

        if (this.outgoingShipments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">\u0644\u0627 \u062a\u0648\u062c\u062f \u0634\u062d\u0646\u0627\u062a \u0635\u0627\u062f\u0631\u0629</td></tr>';
            return;
        }

        tbody.innerHTML = this.outgoingShipments.map(shipment => `
            <tr>
                <td>${escapeHtml(shipment.trackingNumber || '')}</td>
                <td>${escapeHtml(shipment.recipientName || '')}</td>
                <td>${escapeHtml(shipment.courierName || '')}</td>
                <td><span class="badge bg-${SharedDataUtils.getStatusColor(shipment.status)}">${escapeHtml(SharedDataUtils.getStatusText(shipment.status))}</span></td>
                <td>${SharedDataUtils.formatDate(shipment.updatedAt)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-success action-btn release-btn" data-shipment-id="${shipment.id}" title="\u0625\u0637\u0644\u0627\u0642">
                        <i class="fas fa-arrow-up"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    /**
     * Render inventory table
     */
    renderInventoryTable() {
        const tbody = document.querySelector('#inventoryTable tbody');
        if (!tbody) return;

        if (this.inventory.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">\u0644\u0627 \u062a\u0648\u062c\u062f \u0634\u062d\u0646\u0627\u062a \u0641\u064a \u0627\u0644\u0645\u062e\u0632\u0646</td></tr>';
            return;
        }

        tbody.innerHTML = this.inventory.map(shipment => `
            <tr>
                <td>${escapeHtml(shipment.trackingNumber || '')}</td>
                <td>${escapeHtml(shipment.merchantName || '')}</td>
                <td>${escapeHtml(shipment.recipientName || '')}</td>
                <td><span class="badge bg-${SharedDataUtils.getStatusColor(shipment.status)}">${escapeHtml(SharedDataUtils.getStatusText(shipment.status))}</span></td>
                <td>${SharedDataUtils.formatDate(shipment.receivedAt || shipment.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-info action-btn view-btn" data-shipment-id="${shipment.id}" title="\u0639\u0631\u0636">
                        <i class="fas fa-eye"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    /**
     * Render recent activity list
     */
    renderRecentActivity() {
        const container = document.getElementById('recentActivity');
        if (!container) return;

        const activities = this.recentActivity || [];
        if (activities.length === 0) {
            container.innerHTML = '<p class="text-center text-muted">\u0644\u0627 \u062a\u0648\u062c\u062f \u0623\u0646\u0634\u0637\u0629 \u062d\u062f\u064a\u062b\u0629</p>';
            return;
        }

        container.innerHTML = activities.map(activity => `
            <div class="activity-item">
                <div class="activity-icon">
                    <i class="fas fa-${this.getActivityIcon(activity.type)}"></i>
                </div>
                <div class="activity-content">
                    <p class="activity-text">${escapeHtml(activity.description || activity.notes || '')}</p>
                    <small class="activity-time">${SharedDataUtils.formatDate(activity.timestamp)}</small>
                </div>
            </div>
        `).join('');
    }

    /**
     * Get icon for activity type
     */
    getActivityIcon(type) {
        const icons = {
            'RECEIVE': 'arrow-down',
            'RELEASE': 'arrow-up',
            'SCAN': 'barcode',
            'UPDATE': 'edit',
            'TRANSFER': 'exchange-alt'
        };
        return icons[type] || 'info-circle';
    }

    /**
     * Setup quick action buttons
     */
    setupQuickActions() {
        document.querySelectorAll('.quick-action-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const action = btn.dataset.action;
                this.handleQuickAction(action);
            });
        });
    }

    /**
     * Handle quick action button clicks
     */
    async handleQuickAction(action) {
        switch (action) {
            case 'receiveShipment':
                await this.receiveIncomingShipment();
                break;
            case 'releaseOutgoing':
                await this.releaseOutgoingShipment();
                break;
            case 'generateReport':
                this.generateWarehouseReport();
                break;
            case 'updateInventory':
                await this.loadDashboardData();
                NotificationService.success('\u062a\u0645 \u062a\u062d\u062f\u064a\u062b \u0627\u0644\u0628\u064a\u0627\u0646\u0627\u062a');
                break;
        }
    }

    /**
     * Receive an incoming shipment
     */
    async receiveIncomingShipment() {
        const { value: trackingNumber } = await Swal.fire({
            title: '\u0627\u0633\u062a\u0644\u0627\u0645 \u0634\u062d\u0646\u0629',
            input: 'text',
            inputLabel: '\u0631\u0642\u0645 \u0627\u0644\u062a\u062a\u0628\u0639',
            inputPlaceholder: '\u0623\u062f\u062e\u0644 \u0631\u0642\u0645 \u0627\u0644\u062a\u062a\u0628\u0639...',
            showCancelButton: true,
            confirmButtonText: '\u0627\u0633\u062a\u0644\u0627\u0645',
            cancelButtonText: '\u0625\u0644\u063a\u0627\u0621',
            inputValidator: (value) => {
                if (!value) return '\u064a\u0631\u062c\u0649 \u0625\u062f\u062e\u0627\u0644 \u0631\u0642\u0645 \u0627\u0644\u062a\u062a\u0628\u0639';
            }
        });

        if (!trackingNumber) return;

        try {
            UIUtils.showLoading();
            await window.apiService.updateShipmentStatus(trackingNumber, 'AT_WAREHOUSE');
            NotificationService.success('\u062a\u0645 \u0627\u0633\u062a\u0644\u0627\u0645 \u0627\u0644\u0634\u062d\u0646\u0629 \u0628\u0646\u062c\u0627\u062d');
            await this.loadDashboardData();
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseDashboard.receive');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Release an outgoing shipment
     */
    async releaseOutgoingShipment() {
        const { value: trackingNumber } = await Swal.fire({
            title: '\u0625\u0637\u0644\u0627\u0642 \u0634\u062d\u0646\u0629',
            input: 'text',
            inputLabel: '\u0631\u0642\u0645 \u0627\u0644\u062a\u062a\u0628\u0639',
            inputPlaceholder: '\u0623\u062f\u062e\u0644 \u0631\u0642\u0645 \u0627\u0644\u062a\u062a\u0628\u0639...',
            showCancelButton: true,
            confirmButtonText: '\u0625\u0637\u0644\u0627\u0642',
            cancelButtonText: '\u0625\u0644\u063a\u0627\u0621',
            inputValidator: (value) => {
                if (!value) return '\u064a\u0631\u062c\u0649 \u0625\u062f\u062e\u0627\u0644 \u0631\u0642\u0645 \u0627\u0644\u062a\u062a\u0628\u0639';
            }
        });

        if (!trackingNumber) return;

        try {
            UIUtils.showLoading();
            await window.apiService.updateShipmentStatus(trackingNumber, 'IN_TRANSIT');
            NotificationService.success('\u062a\u0645 \u0625\u0637\u0644\u0627\u0642 \u0627\u0644\u0634\u062d\u0646\u0629 \u0628\u0646\u062c\u0627\u062d');
            await this.loadDashboardData();
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseDashboard.release');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Generate warehouse report
     */
    generateWarehouseReport() {
        NotificationService.info('\u062c\u0627\u0631\u064a \u0625\u0639\u062f\u0627\u062f \u0627\u0644\u062a\u0642\u0631\u064a\u0631...');
        // Report generation would integrate with the reports API
        window.location.href = '/owner/reports/warehouse.html';
    }

    /**
     * Setup search/filter inputs for tables
     */
    setupSearchFilters() {
        document.querySelectorAll('.search-input').forEach(input => {
            input.addEventListener('input', (e) => {
                const tableId = e.target.dataset.table;
                const query = e.target.value.toLowerCase();
                this.filterTable(tableId, query);
            });
        });

        // Setup action buttons via event delegation
        document.addEventListener('click', (e) => {
            const receiveBtn = e.target.closest('.receive-btn');
            if (receiveBtn) {
                const id = receiveBtn.dataset.shipmentId;
                this.receiveShipmentById(id);
                return;
            }

            const releaseBtn = e.target.closest('.release-btn');
            if (releaseBtn) {
                const id = releaseBtn.dataset.shipmentId;
                this.releaseShipmentById(id);
                return;
            }
        });
    }

    /**
     * Filter table rows by search query
     */
    filterTable(tableId, query) {
        const table = document.getElementById(tableId);
        if (!table) return;
        const rows = table.querySelectorAll('tbody tr');
        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(query) ? '' : 'none';
        });
    }

    /**
     * Receive specific shipment by ID
     */
    async receiveShipmentById(shipmentId) {
        try {
            UIUtils.showLoading();
            await window.apiService.updateShipmentStatus(shipmentId, 'AT_WAREHOUSE');
            NotificationService.success('\u062a\u0645 \u0627\u0633\u062a\u0644\u0627\u0645 \u0627\u0644\u0634\u062d\u0646\u0629');
            await this.loadDashboardData();
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseDashboard.receiveById');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Release specific shipment by ID
     */
    async releaseShipmentById(shipmentId) {
        try {
            UIUtils.showLoading();
            await window.apiService.updateShipmentStatus(shipmentId, 'IN_TRANSIT');
            NotificationService.success('\u062a\u0645 \u0625\u0637\u0644\u0627\u0642 \u0627\u0644\u0634\u062d\u0646\u0629');
            await this.loadDashboardData();
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseDashboard.releaseById');
        } finally {
            UIUtils.hideLoading();
        }
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (typeof BasePageHandler !== 'undefined') {
        window.warehouseDashboardHandler = new WarehouseDashboardHandler();
    }
});
