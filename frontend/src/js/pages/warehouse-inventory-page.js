/**
 * Twsela CMS - Warehouse Inventory Page Handler
 * Manages inventory movements and stock tracking
 */

class WarehouseInventoryHandler extends BasePageHandler {
    constructor() {
        super('Warehouse Inventory');
        this.movements = [];
        this.zones = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await Promise.all([
                this.loadMovements(),
                this.loadZonesDropdown(),
                this.loadSummary()
            ]);
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseInventory');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const addBtn = document.getElementById('addMovementBtn');
        if (addBtn) {
            addBtn.addEventListener('click', () => this.showAddModal());
        }

        const form = document.getElementById('addMovementForm');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleCreateMovement();
            });
        }

        const applyBtn = document.getElementById('applyInventoryFilters');
        if (applyBtn) {
            applyBtn.addEventListener('click', () => this.loadMovements());
        }
    }

    /**
     * Load zones for dropdowns
     */
    async loadZonesDropdown() {
        try {
            const response = await this.services.api.getWarehouseZones();
            if (response?.success) {
                this.zones = response.data || [];
                ['inventoryZoneFilter', 'movementZone'].forEach(selectId => {
                    const select = document.getElementById(selectId);
                    if (select) {
                        this.zones.forEach(zone => {
                            const option = document.createElement('option');
                            option.value = zone.id;
                            option.textContent = zone.name;
                            select.appendChild(option);
                        });
                    }
                });
            }
        } catch (error) {
            // Non-critical
        }
    }

    /**
     * Load inventory summary
     */
    async loadSummary() {
        try {
            const response = await this.services.api.getInventorySummary();
            if (response?.success && response.data) {
                const data = response.data;
                const el = (id, val) => {
                    const e = document.getElementById(id);
                    if (e) e.textContent = val;
                };
                el('totalInbound', data.totalInbound || 0);
                el('totalOutbound', data.totalOutbound || 0);
                el('totalTransfers', data.totalTransfers || 0);
                el('currentStock', data.currentStock || 0);
            }
        } catch (error) {
            // Non-critical
        }
    }

    /**
     * Load inventory movements
     */
    async loadMovements() {
        try {
            const params = {};
            const type = document.getElementById('movementTypeFilter')?.value;
            const zone = document.getElementById('inventoryZoneFilter')?.value;
            const date = document.getElementById('inventoryDateFilter')?.value;

            if (type) params.type = type;
            if (zone) params.zoneId = zone;
            if (date) params.date = date;

            const response = await this.services.api.getInventoryMovements(params);
            if (response?.success) {
                this.movements = response.data || [];
            } else {
                this.movements = [];
            }
            this.renderTable();
        } catch (error) {
            ErrorHandler.handle(error, 'LoadInventoryMovements');
            this.movements = [];
            this.renderTable();
        }
    }

    /**
     * Render movements table
     */
    renderTable() {
        const tbody = document.getElementById('movementsTableBody');
        if (!tbody) return;

        if (this.movements.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="fas fa-boxes fa-2x mb-2 d-block"></i>
                        لا توجد حركات مخزون
                    </td>
                </tr>`;
            return;
        }

        const typeLabels = {
            'RECEIPT': '<span class="badge bg-success">استلام</span>',
            'DISPATCH': '<span class="badge bg-danger">إرسال</span>',
            'TRANSFER': '<span class="badge bg-info">تحويل</span>',
            'ADJUSTMENT_ADD': '<span class="badge bg-primary">تعديل +</span>',
            'ADJUSTMENT_REMOVE': '<span class="badge bg-warning">تعديل -</span>',
            'RETURN': '<span class="badge bg-secondary">مرتجع</span>',
            'DAMAGE': '<span class="badge bg-dark">تالف</span>'
        };

        tbody.innerHTML = this.movements.map(mov => `
            <tr>
                <td><strong>#${mov.id || ''}</strong></td>
                <td>${typeLabels[mov.type] || mov.type}</td>
                <td>${mov.quantity || 0}</td>
                <td>${escapeHtml(mov.zoneName || '-')}</td>
                <td>${escapeHtml(mov.reference || '-')}</td>
                <td>${mov.createdAt ? new Date(mov.createdAt).toLocaleDateString('ar-EG') : '-'}</td>
                <td>${escapeHtml(mov.notes || '-')}</td>
            </tr>`).join('');
    }

    /**
     * Show add movement modal
     */
    showAddModal() {
        const form = document.getElementById('addMovementForm');
        if (form) form.reset();
        const modal = new bootstrap.Modal(document.getElementById('addMovementModal'));
        modal.show();
    }

    /**
     * Handle create movement
     */
    async handleCreateMovement() {
        try {
            const movementData = {
                type: document.getElementById('movementType')?.value,
                quantity: parseInt(document.getElementById('movementQuantity')?.value) || 0,
                zoneId: document.getElementById('movementZone')?.value,
                reference: document.getElementById('movementReference')?.value,
                notes: document.getElementById('movementNotes')?.value
            };

            const response = await this.services.api.createInventoryMovement(movementData);
            if (response?.success) {
                this.services.notification.success('تم تسجيل حركة المخزون بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('addMovementModal'))?.hide();
                await Promise.all([this.loadMovements(), this.loadSummary()]);
            } else {
                this.services.notification.error(response?.message || 'فشل في تسجيل الحركة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CreateInventoryMovement');
        }
    }
}

// Create global instance
window.warehouseInventoryHandler = new WarehouseInventoryHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/warehouse/inventory.html')) {
        setTimeout(() => {
            window.warehouseInventoryHandler.init();
        }, 200);
    }
});
