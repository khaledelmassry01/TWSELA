/**
 * Twsela CMS - Warehouse Receiving Page Handler
 * Manages incoming shipments receiving process
 */

class WarehouseReceivingHandler extends BasePageHandler {
    constructor() {
        super('Warehouse Receiving');
        this.orders = [];
        this.zones = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await Promise.all([
                this.loadOrders(),
                this.loadZonesForDropdown()
            ]);
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseReceiving');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const createBtn = document.getElementById('createReceivingBtn');
        if (createBtn) {
            createBtn.addEventListener('click', () => this.showCreateModal());
        }

        const form = document.getElementById('createReceivingForm');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleCreateOrder();
            });
        }

        const applyFiltersBtn = document.getElementById('applyFiltersBtn');
        if (applyFiltersBtn) {
            applyFiltersBtn.addEventListener('click', () => this.loadOrders());
        }

        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            let timeout;
            searchInput.addEventListener('input', () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => this.loadOrders(), 500);
            });
        }
    }

    /**
     * Load receiving orders
     */
    async loadOrders() {
        try {
            const params = {};
            const status = document.getElementById('statusFilter')?.value;
            const date = document.getElementById('dateFilter')?.value;
            const search = document.getElementById('searchInput')?.value;

            if (status) params.status = status;
            if (date) params.date = date;
            if (search) params.search = search;

            const response = await this.services.api.getReceivingOrders(params);
            if (response?.success) {
                this.orders = response.data || [];
            } else {
                this.orders = [];
            }
            this.renderTable();
            this.updateKPIs();
        } catch (error) {
            ErrorHandler.handle(error, 'LoadReceivingOrders');
            this.orders = [];
            this.renderTable();
        }
    }

    /**
     * Load zones for dropdown
     */
    async loadZonesForDropdown() {
        try {
            const response = await this.services.api.getWarehouseZones({ type: 'RECEIVING' });
            if (response?.success) {
                this.zones = response.data || [];
                const select = document.getElementById('receivingZone');
                if (select) {
                    this.zones.forEach(zone => {
                        const option = document.createElement('option');
                        option.value = zone.id;
                        option.textContent = zone.name;
                        select.appendChild(option);
                    });
                }
            }
        } catch (error) {
            // Non-critical, continue
        }
    }

    /**
     * Update KPI cards
     */
    updateKPIs() {
        const pending = this.orders.filter(o => o.status === 'PENDING').length;
        const inProgress = this.orders.filter(o => o.status === 'IN_PROGRESS').length;
        const completed = this.orders.filter(o => o.status === 'COMPLETED').length;

        const today = new Date().toISOString().split('T')[0];
        const todayReceived = this.orders.filter(o =>
            o.status === 'COMPLETED' && o.completedAt?.startsWith(today)
        ).length;

        const el = (id, val) => {
            const e = document.getElementById(id);
            if (e) e.textContent = val;
        };

        el('pendingCount', pending);
        el('inProgressCount', inProgress);
        el('completedCount', completed);
        el('todayReceived', todayReceived);
    }

    /**
     * Render receiving orders table
     */
    renderTable() {
        const tbody = document.getElementById('receivingTableBody');
        if (!tbody) return;

        if (this.orders.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                        لا توجد طلبات استلام
                    </td>
                </tr>`;
            return;
        }

        const statusLabels = {
            'PENDING': '<span class="badge bg-warning">في الانتظار</span>',
            'IN_PROGRESS': '<span class="badge bg-info">قيد المعالجة</span>',
            'COMPLETED': '<span class="badge bg-success">مكتمل</span>',
            'CANCELLED': '<span class="badge bg-danger">ملغي</span>'
        };

        tbody.innerHTML = this.orders.map(order => `
            <tr>
                <td><strong>#${escapeHtml(order.orderNumber || order.id?.toString() || '')}</strong></td>
                <td>${escapeHtml(order.supplierName || '-')}</td>
                <td>${order.expectedItems || 0}</td>
                <td>${escapeHtml(order.zoneName || '-')}</td>
                <td>${order.createdAt ? new Date(order.createdAt).toLocaleDateString('ar-EG') : '-'}</td>
                <td>${statusLabels[order.status] || order.status}</td>
                <td>
                    <div class="btn-group btn-group-sm">
                        ${order.status === 'PENDING' ? `
                            <button class="btn btn-outline-info" onclick="window.warehouseReceivingHandler.startProcessing(${order.id})" title="بدء المعالجة">
                                <i class="fas fa-play"></i>
                            </button>` : ''}
                        ${order.status === 'IN_PROGRESS' ? `
                            <button class="btn btn-outline-success" onclick="window.warehouseReceivingHandler.completeOrder(${order.id})" title="إكمال">
                                <i class="fas fa-check"></i>
                            </button>` : ''}
                    </div>
                </td>
            </tr>`).join('');
    }

    /**
     * Show create receiving modal
     */
    showCreateModal() {
        const form = document.getElementById('createReceivingForm');
        if (form) form.reset();
        const modal = new bootstrap.Modal(document.getElementById('createReceivingModal'));
        modal.show();
    }

    /**
     * Handle create receiving order
     */
    async handleCreateOrder() {
        try {
            const orderData = {
                supplierName: document.getElementById('supplierName')?.value,
                expectedItems: parseInt(document.getElementById('expectedItems')?.value) || 0,
                zoneId: document.getElementById('receivingZone')?.value,
                notes: document.getElementById('receivingNotes')?.value
            };

            const response = await this.services.api.createReceivingOrder(orderData);
            if (response?.success) {
                this.services.notification.success('تم إنشاء طلب الاستلام بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('createReceivingModal'))?.hide();
                await this.loadOrders();
            } else {
                this.services.notification.error(response?.message || 'فشل في إنشاء طلب الاستلام');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CreateReceivingOrder');
        }
    }

    /**
     * Start processing a receiving order
     */
    async startProcessing(orderId) {
        try {
            const response = await this.services.api.updateReceivingStatus(orderId, 'IN_PROGRESS');
            if (response?.success) {
                this.services.notification.success('تم بدء معالجة طلب الاستلام');
                await this.loadOrders();
            } else {
                this.services.notification.error(response?.message || 'فشل في تحديث الحالة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'StartProcessing');
        }
    }

    /**
     * Complete a receiving order
     */
    async completeOrder(orderId) {
        try {
            const response = await this.services.api.updateReceivingStatus(orderId, 'COMPLETED');
            if (response?.success) {
                this.services.notification.success('تم اكتمال طلب الاستلام');
                await this.loadOrders();
            } else {
                this.services.notification.error(response?.message || 'فشل في تحديث الحالة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CompleteOrder');
        }
    }
}

// Create global instance
window.warehouseReceivingHandler = new WarehouseReceivingHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/warehouse/receiving.html')) {
        setTimeout(() => {
            window.warehouseReceivingHandler.init();
        }, 200);
    }
});
