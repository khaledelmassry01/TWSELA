/**
 * Twsela CMS - Warehouse Fulfillment Page Handler
 * Manages fulfillment orders and picker assignment
 */

class WarehouseFulfillmentHandler extends BasePageHandler {
    constructor() {
        super('Warehouse Fulfillment');
        this.orders = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await this.loadOrders();
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseFulfillment');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const createBtn = document.getElementById('createFulfillmentBtn');
        if (createBtn) {
            createBtn.addEventListener('click', () => this.showCreateModal());
        }

        const createForm = document.getElementById('createFulfillmentForm');
        if (createForm) {
            createForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleCreateOrder();
            });
        }

        const assignForm = document.getElementById('assignPickerForm');
        if (assignForm) {
            assignForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleAssignPicker();
            });
        }

        const applyBtn = document.getElementById('applyFulfillmentFilters');
        if (applyBtn) {
            applyBtn.addEventListener('click', () => this.loadOrders());
        }

        const searchInput = document.getElementById('fulfillmentSearch');
        if (searchInput) {
            let timeout;
            searchInput.addEventListener('input', () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => this.loadOrders(), 500);
            });
        }
    }

    /**
     * Load fulfillment orders
     */
    async loadOrders() {
        try {
            const params = {};
            const status = document.getElementById('fulfillmentStatusFilter')?.value;
            const priority = document.getElementById('priorityFilter')?.value;
            const search = document.getElementById('fulfillmentSearch')?.value;

            if (status) params.status = status;
            if (priority) params.priority = priority;
            if (search) params.search = search;

            const response = await this.services.api.getFulfillmentOrders(params);
            if (response?.success) {
                this.orders = response.data || [];
            } else {
                this.orders = [];
            }
            this.renderTable();
            this.updateKPIs();
        } catch (error) {
            ErrorHandler.handle(error, 'LoadFulfillmentOrders');
            this.orders = [];
            this.renderTable();
        }
    }

    /**
     * Update KPI cards
     */
    updateKPIs() {
        const pending = this.orders.filter(o => o.status === 'PENDING').length;
        const processing = this.orders.filter(o => ['ASSIGNED', 'PICKING', 'PACKING'].includes(o.status)).length;
        const completed = this.orders.filter(o => o.status === 'COMPLETED').length;
        const assigned = this.orders.filter(o => o.pickerId).length;

        const el = (id, val) => {
            const e = document.getElementById(id);
            if (e) e.textContent = val;
        };

        el('pendingFulfillment', pending);
        el('processingFulfillment', processing);
        el('completedFulfillment', completed);
        el('assignedPickers', assigned);
    }

    /**
     * Render fulfillment table
     */
    renderTable() {
        const tbody = document.getElementById('fulfillmentTableBody');
        if (!tbody) return;

        if (this.orders.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="fas fa-box-open fa-2x mb-2 d-block"></i>
                        لا توجد أوامر تجهيز
                    </td>
                </tr>`;
            return;
        }

        const statusLabels = {
            'PENDING': '<span class="badge bg-warning">في الانتظار</span>',
            'ASSIGNED': '<span class="badge bg-info">معين</span>',
            'PICKING': '<span class="badge bg-primary">جاري الالتقاط</span>',
            'PACKING': '<span class="badge bg-secondary">جاري التعبئة</span>',
            'COMPLETED': '<span class="badge bg-success">مكتمل</span>',
            'CANCELLED': '<span class="badge bg-danger">ملغي</span>'
        };

        const priorityLabels = {
            'HIGH': '<span class="badge bg-danger">عالية</span>',
            'MEDIUM': '<span class="badge bg-warning">متوسطة</span>',
            'LOW': '<span class="badge bg-secondary">منخفضة</span>'
        };

        tbody.innerHTML = this.orders.map(order => `
            <tr>
                <td><strong>#${escapeHtml(order.orderNumber || order.id?.toString() || '')}</strong></td>
                <td>${escapeHtml(order.shipmentNumber || '-')}</td>
                <td>${priorityLabels[order.priority] || order.priority || '-'}</td>
                <td>${escapeHtml(order.pickerName || 'غير معين')}</td>
                <td>${statusLabels[order.status] || order.status}</td>
                <td>${order.createdAt ? new Date(order.createdAt).toLocaleDateString('ar-EG') : '-'}</td>
                <td>
                    <div class="btn-group btn-group-sm">
                        ${order.status === 'PENDING' ? `
                            <button class="btn btn-outline-info" onclick="window.warehouseFulfillmentHandler.showAssignModal(${order.id})" title="تعيين مجهز">
                                <i class="fas fa-user-plus"></i>
                            </button>` : ''}
                        ${['ASSIGNED', 'PICKING', 'PACKING'].includes(order.status) ? `
                            <button class="btn btn-outline-success" onclick="window.warehouseFulfillmentHandler.completeOrder(${order.id})" title="إكمال">
                                <i class="fas fa-check"></i>
                            </button>` : ''}
                    </div>
                </td>
            </tr>`).join('');
    }

    /**
     * Show create fulfillment modal
     */
    showCreateModal() {
        const form = document.getElementById('createFulfillmentForm');
        if (form) form.reset();
        const modal = new bootstrap.Modal(document.getElementById('createFulfillmentModal'));
        modal.show();
    }

    /**
     * Handle create fulfillment order
     */
    async handleCreateOrder() {
        try {
            const orderData = {
                shipmentId: document.getElementById('shipmentId')?.value,
                priority: document.getElementById('fulfillmentPriority')?.value,
                notes: document.getElementById('fulfillmentNotes')?.value
            };

            const response = await this.services.api.createFulfillmentOrder(orderData);
            if (response?.success) {
                this.services.notification.success('تم إنشاء أمر التجهيز بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('createFulfillmentModal'))?.hide();
                await this.loadOrders();
            } else {
                this.services.notification.error(response?.message || 'فشل في إنشاء أمر التجهيز');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CreateFulfillmentOrder');
        }
    }

    /**
     * Show assign picker modal
     */
    showAssignModal(orderId) {
        document.getElementById('assignOrderId').value = orderId;
        const modal = new bootstrap.Modal(document.getElementById('assignPickerModal'));
        modal.show();
    }

    /**
     * Handle assign picker
     */
    async handleAssignPicker() {
        try {
            const orderId = document.getElementById('assignOrderId')?.value;
            const pickerId = document.getElementById('pickerId')?.value;

            if (!orderId || !pickerId) return;

            const response = await this.services.api.assignPicker(orderId, pickerId);
            if (response?.success) {
                this.services.notification.success('تم تعيين المجهز بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('assignPickerModal'))?.hide();
                await this.loadOrders();
            } else {
                this.services.notification.error(response?.message || 'فشل في تعيين المجهز');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'AssignPicker');
        }
    }

    /**
     * Complete fulfillment order
     */
    async completeOrder(orderId) {
        try {
            const response = await this.services.api.updateFulfillmentStatus(orderId, 'COMPLETED');
            if (response?.success) {
                this.services.notification.success('تم إكمال أمر التجهيز');
                await this.loadOrders();
            } else {
                this.services.notification.error(response?.message || 'فشل في تحديث الحالة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CompleteFulfillment');
        }
    }
}

// Create global instance
window.warehouseFulfillmentHandler = new WarehouseFulfillmentHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/warehouse/fulfillment.html')) {
        setTimeout(() => {
            window.warehouseFulfillmentHandler.init();
        }, 200);
    }
});
