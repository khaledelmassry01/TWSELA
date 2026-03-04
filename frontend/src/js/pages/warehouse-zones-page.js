/**
 * Twsela CMS - Warehouse Zones Page Handler
 * Manages warehouse zones and bins
 */

class WarehouseZonesHandler extends BasePageHandler {
    constructor() {
        super('Warehouse Zones');
        this.zones = [];
        this.selectedZoneId = null;
        this.bins = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            this.setupFilters();
            await this.loadZones();
        } catch (error) {
            ErrorHandler.handle(error, 'WarehouseZones');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Add zone button
        const addZoneBtn = document.getElementById('addZoneBtn');
        if (addZoneBtn) {
            addZoneBtn.addEventListener('click', () => this.showAddZoneModal());
        }

        // Add zone form submit
        const addZoneForm = document.getElementById('addZoneForm');
        if (addZoneForm) {
            addZoneForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleCreateZone();
            });
        }

        // Add bin button
        const addBinBtn = document.getElementById('addBinBtn');
        if (addBinBtn) {
            addBinBtn.addEventListener('click', () => this.showAddBinModal());
        }

        // Add bin form submit
        const addBinForm = document.getElementById('addBinForm');
        if (addBinForm) {
            addBinForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleCreateBin();
            });
        }
    }

    /**
     * Setup filter listeners
     */
    setupFilters() {
        const zoneTypeFilter = document.getElementById('zoneTypeFilter');
        if (zoneTypeFilter) {
            zoneTypeFilter.addEventListener('change', () => this.loadZones());
        }
    }

    /**
     * Load zones from API
     */
    async loadZones() {
        try {
            const typeFilter = document.getElementById('zoneTypeFilter')?.value;
            const params = {};
            if (typeFilter) params.type = typeFilter;

            const response = await this.services.api.getWarehouseZones(params);
            if (response?.success) {
                this.zones = response.data || [];
                this.renderZonesTable();
                this.updateStats();
            } else {
                this.zones = [];
                this.renderZonesTable();
            }
        } catch (error) {
            ErrorHandler.handle(error, 'LoadZones');
            this.zones = [];
            this.renderZonesTable();
        }
    }

    /**
     * Update zone statistics
     */
    updateStats() {
        const totalEl = document.getElementById('totalZones');
        const binsEl = document.getElementById('totalBins');
        const activeEl = document.getElementById('activeZones');
        const utilEl = document.getElementById('utilizationRate');

        if (totalEl) totalEl.textContent = this.zones.length;

        const totalBins = this.zones.reduce((sum, z) => sum + (z.binCount || 0), 0);
        if (binsEl) binsEl.textContent = totalBins;

        const activeCount = this.zones.filter(z => z.status === 'ACTIVE').length;
        if (activeEl) activeEl.textContent = activeCount;

        const totalCapacity = this.zones.reduce((sum, z) => sum + (z.capacity || 0), 0);
        const totalUsed = this.zones.reduce((sum, z) => sum + (z.currentUsage || 0), 0);
        const rate = totalCapacity > 0 ? Math.round((totalUsed / totalCapacity) * 100) : 0;
        if (utilEl) utilEl.textContent = rate + '%';
    }

    /**
     * Render zones table
     */
    renderZonesTable() {
        const tbody = document.getElementById('zonesTableBody');
        if (!tbody) return;

        if (this.zones.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="fas fa-map-marked-alt fa-2x mb-2 d-block"></i>
                        لا توجد مناطق حالياً
                    </td>
                </tr>`;
            return;
        }

        const zoneTypeLabels = {
            'RECEIVING': 'استلام',
            'STORAGE': 'تخزين',
            'PACKING': 'تعبئة',
            'DISPATCH': 'إرسال',
            'RETURNS': 'مرتجعات',
            'QUARANTINE': 'حجر',
            'COLD_STORAGE': 'تخزين بارد'
        };

        tbody.innerHTML = this.zones.map(zone => {
            const usage = zone.capacity > 0 ? Math.round((zone.currentUsage / zone.capacity) * 100) : 0;
            const statusClass = zone.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary';
            const statusLabel = zone.status === 'ACTIVE' ? 'نشطة' : 'غير نشطة';
            const typeLabel = zoneTypeLabels[zone.type] || zone.type;

            return `
                <tr>
                    <td><strong>${escapeHtml(zone.name || '')}</strong></td>
                    <td><span class="badge bg-info">${escapeHtml(typeLabel)}</span></td>
                    <td>${zone.capacity || 0}</td>
                    <td>
                        <div class="progress" style="height: 20px;">
                            <div class="progress-bar ${usage > 80 ? 'bg-danger' : usage > 50 ? 'bg-warning' : 'bg-success'}" 
                                 style="width: ${usage}%">${usage}%</div>
                        </div>
                    </td>
                    <td>${zone.binCount || 0}</td>
                    <td><span class="badge ${statusClass}">${statusLabel}</span></td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-primary" onclick="window.warehouseZonesHandler.viewBins(${zone.id})" title="عرض الحاويات">
                                <i class="fas fa-cube"></i>
                            </button>
                            <button class="btn btn-outline-warning" onclick="window.warehouseZonesHandler.editZone(${zone.id})" title="تعديل">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-outline-danger" onclick="window.warehouseZonesHandler.deleteZone(${zone.id})" title="حذف">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>`;
        }).join('');
    }

    /**
     * Show add zone modal
     */
    showAddZoneModal() {
        const form = document.getElementById('addZoneForm');
        if (form) form.reset();
        const modal = new bootstrap.Modal(document.getElementById('addZoneModal'));
        modal.show();
    }

    /**
     * Handle create zone
     */
    async handleCreateZone() {
        try {
            const zoneData = {
                name: document.getElementById('zoneName')?.value,
                type: document.getElementById('zoneType')?.value,
                capacity: parseInt(document.getElementById('zoneCapacity')?.value) || 0,
                description: document.getElementById('zoneDescription')?.value
            };

            const response = await this.services.api.createWarehouseZone(zoneData);
            if (response?.success) {
                this.services.notification.success('تم إضافة المنطقة بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('addZoneModal'))?.hide();
                await this.loadZones();
            } else {
                this.services.notification.error(response?.message || 'فشل في إضافة المنطقة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CreateZone');
        }
    }

    /**
     * View bins for a zone
     */
    async viewBins(zoneId) {
        try {
            this.selectedZoneId = zoneId;
            const zone = this.zones.find(z => z.id === zoneId);
            const nameEl = document.getElementById('selectedZoneName');
            if (nameEl && zone) nameEl.textContent = zone.name;

            const binsSection = document.getElementById('binsSection');
            if (binsSection) binsSection.classList.remove('d-none');

            const response = await this.services.api.getZoneBins(zoneId);
            if (response?.success) {
                this.bins = response.data || [];
                this.renderBinsTable();
            }
        } catch (error) {
            ErrorHandler.handle(error, 'ViewBins');
        }
    }

    /**
     * Render bins table
     */
    renderBinsTable() {
        const tbody = document.getElementById('binsTableBody');
        if (!tbody) return;

        if (this.bins.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted py-3">لا توجد حاويات</td>
                </tr>`;
            return;
        }

        tbody.innerHTML = this.bins.map(bin => {
            const statusClass = bin.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary';
            const statusLabel = bin.status === 'ACTIVE' ? 'نشطة' : 'غير نشطة';
            return `
                <tr>
                    <td><strong>${escapeHtml(bin.code || '')}</strong></td>
                    <td>${escapeHtml(bin.type || '')}</td>
                    <td>${bin.capacity || 0}</td>
                    <td>${bin.currentCount || 0}</td>
                    <td><span class="badge ${statusClass}">${statusLabel}</span></td>
                </tr>`;
        }).join('');
    }

    /**
     * Show add bin modal
     */
    showAddBinModal() {
        const form = document.getElementById('addBinForm');
        if (form) form.reset();
        const modal = new bootstrap.Modal(document.getElementById('addBinModal'));
        modal.show();
    }

    /**
     * Handle create bin
     */
    async handleCreateBin() {
        try {
            if (!this.selectedZoneId) return;

            const binData = {
                code: document.getElementById('binCode')?.value,
                type: document.getElementById('binType')?.value,
                capacity: parseInt(document.getElementById('binCapacity')?.value) || 0
            };

            const response = await this.services.api.createBin(this.selectedZoneId, binData);
            if (response?.success) {
                this.services.notification.success('تم إضافة الحاوية بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('addBinModal'))?.hide();
                await this.viewBins(this.selectedZoneId);
                await this.loadZones();
            } else {
                this.services.notification.error(response?.message || 'فشل في إضافة الحاوية');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CreateBin');
        }
    }

    /**
     * Edit zone
     */
    async editZone(zoneId) {
        const zone = this.zones.find(z => z.id === zoneId);
        if (!zone) return;

        document.getElementById('zoneName').value = zone.name || '';
        document.getElementById('zoneType').value = zone.type || '';
        document.getElementById('zoneCapacity').value = zone.capacity || '';
        document.getElementById('zoneDescription').value = zone.description || '';

        const form = document.getElementById('addZoneForm');
        if (form) {
            form.onsubmit = async (e) => {
                e.preventDefault();
                await this.handleUpdateZone(zoneId);
            };
        }

        const modal = new bootstrap.Modal(document.getElementById('addZoneModal'));
        modal.show();
    }

    /**
     * Handle update zone
     */
    async handleUpdateZone(zoneId) {
        try {
            const zoneData = {
                name: document.getElementById('zoneName')?.value,
                type: document.getElementById('zoneType')?.value,
                capacity: parseInt(document.getElementById('zoneCapacity')?.value) || 0,
                description: document.getElementById('zoneDescription')?.value
            };

            const response = await this.services.api.updateWarehouseZone(zoneId, zoneData);
            if (response?.success) {
                this.services.notification.success('تم تحديث المنطقة بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('addZoneModal'))?.hide();
                await this.loadZones();
            } else {
                this.services.notification.error(response?.message || 'فشل في تحديث المنطقة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'UpdateZone');
        }
    }

    /**
     * Delete zone
     */
    async deleteZone(zoneId) {
        if (!confirm('هل أنت متأكد من حذف هذه المنطقة؟')) return;

        try {
            const response = await this.services.api.deleteWarehouseZone(zoneId);
            if (response?.success) {
                this.services.notification.success('تم حذف المنطقة');
                await this.loadZones();
            } else {
                this.services.notification.error(response?.message || 'فشل في حذف المنطقة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'DeleteZone');
        }
    }
}

// Create global instance
window.warehouseZonesHandler = new WarehouseZonesHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/warehouse/zones.html')) {
        setTimeout(() => {
            window.warehouseZonesHandler.init();
        }, 200);
    }
});
