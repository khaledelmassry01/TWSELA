/**
 * Twsela CMS - Warehouse Pick Waves Page Handler
 * Manages pick wave creation and tracking
 */

class WarehousePickWavesHandler extends BasePageHandler {
    constructor() {
        super('Warehouse Pick Waves');
        this.waves = [];
        this.zones = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await Promise.all([
                this.loadWaves(),
                this.loadZonesDropdown()
            ]);
        } catch (error) {
            ErrorHandler.handle(error, 'WarehousePickWaves');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const createBtn = document.getElementById('createWaveBtn');
        if (createBtn) {
            createBtn.addEventListener('click', () => this.showCreateModal());
        }

        const createForm = document.getElementById('createWaveForm');
        if (createForm) {
            createForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleCreateWave();
            });
        }

        const applyBtn = document.getElementById('applyWaveFilters');
        if (applyBtn) {
            applyBtn.addEventListener('click', () => this.loadWaves());
        }
    }

    /**
     * Load zones for dropdown
     */
    async loadZonesDropdown() {
        try {
            const response = await this.services.api.getWarehouseZones();
            if (response?.success) {
                this.zones = response.data || [];
                const select = document.getElementById('waveZone');
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
            // Non-critical
        }
    }

    /**
     * Load pick waves
     */
    async loadWaves() {
        try {
            const params = {};
            const status = document.getElementById('waveStatusFilter')?.value;
            const date = document.getElementById('waveDateFilter')?.value;

            if (status) params.status = status;
            if (date) params.date = date;

            const response = await this.services.api.getPickWaves(params);
            if (response?.success) {
                this.waves = response.data || [];
            } else {
                this.waves = [];
            }
            this.renderTable();
            this.updateKPIs();
        } catch (error) {
            ErrorHandler.handle(error, 'LoadPickWaves');
            this.waves = [];
            this.renderTable();
        }
    }

    /**
     * Update KPI cards
     */
    updateKPIs() {
        const pending = this.waves.filter(w => w.status === 'PENDING').length;
        const active = this.waves.filter(w => w.status === 'IN_PROGRESS').length;
        const completed = this.waves.filter(w => w.status === 'COMPLETED').length;
        const totalItems = this.waves.reduce((sum, w) => sum + (w.orderCount || 0), 0);

        const el = (id, val) => {
            const e = document.getElementById(id);
            if (e) e.textContent = val;
        };

        el('pendingWaves', pending);
        el('activeWaves', active);
        el('completedWaves', completed);
        el('totalItems', totalItems);
    }

    /**
     * Render waves table
     */
    renderTable() {
        const tbody = document.getElementById('wavesTableBody');
        if (!tbody) return;

        if (this.waves.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="fas fa-tasks fa-2x mb-2 d-block"></i>
                        لا توجد موجات التقاط
                    </td>
                </tr>`;
            return;
        }

        const statusLabels = {
            'PENDING': '<span class="badge bg-warning">في الانتظار</span>',
            'IN_PROGRESS': '<span class="badge bg-info">جاري التنفيذ</span>',
            'COMPLETED': '<span class="badge bg-success">مكتمل</span>',
            'PARTIAL': '<span class="badge bg-secondary">مكتمل جزئياً</span>',
            'CANCELLED': '<span class="badge bg-danger">ملغي</span>'
        };

        tbody.innerHTML = this.waves.map(wave => {
            const progress = wave.totalOrders > 0 ? Math.round((wave.completedOrders / wave.totalOrders) * 100) : 0;

            return `
                <tr>
                    <td><strong>${escapeHtml(wave.name || wave.id?.toString() || '')}</strong></td>
                    <td>${wave.orderCount || 0}</td>
                    <td>${escapeHtml(wave.zoneName || '-')}</td>
                    <td>${escapeHtml(wave.pickerName || 'غير معين')}</td>
                    <td>
                        <div class="progress" style="height: 20px;">
                            <div class="progress-bar ${progress === 100 ? 'bg-success' : 'bg-info'}" 
                                 style="width: ${progress}%">${progress}%</div>
                        </div>
                    </td>
                    <td>${statusLabels[wave.status] || wave.status}</td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            ${wave.status === 'PENDING' ? `
                                <button class="btn btn-outline-info" onclick="window.warehousePickWavesHandler.startWave(${wave.id})" title="بدء">
                                    <i class="fas fa-play"></i>
                                </button>` : ''}
                            ${wave.status === 'IN_PROGRESS' ? `
                                <button class="btn btn-outline-success" onclick="window.warehousePickWavesHandler.completeWave(${wave.id})" title="إكمال">
                                    <i class="fas fa-check"></i>
                                </button>` : ''}
                        </div>
                    </td>
                </tr>`;
        }).join('');
    }

    /**
     * Show create wave modal
     */
    showCreateModal() {
        const form = document.getElementById('createWaveForm');
        if (form) form.reset();
        const modal = new bootstrap.Modal(document.getElementById('createWaveModal'));
        modal.show();
    }

    /**
     * Handle create wave
     */
    async handleCreateWave() {
        try {
            const waveData = {
                name: document.getElementById('waveName')?.value,
                zoneId: document.getElementById('waveZone')?.value,
                notes: document.getElementById('waveNotes')?.value
            };

            const response = await this.services.api.createPickWave(waveData);
            if (response?.success) {
                this.services.notification.success('تم إنشاء موجة الالتقاط بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('createWaveModal'))?.hide();
                await this.loadWaves();
            } else {
                this.services.notification.error(response?.message || 'فشل في إنشاء الموجة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CreatePickWave');
        }
    }

    /**
     * Start a pick wave
     */
    async startWave(waveId) {
        try {
            const response = await this.services.api.startPickWave(waveId);
            if (response?.success) {
                this.services.notification.success('تم بدء موجة الالتقاط');
                await this.loadWaves();
            } else {
                this.services.notification.error(response?.message || 'فشل في بدء الموجة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'StartPickWave');
        }
    }

    /**
     * Complete a pick wave
     */
    async completeWave(waveId) {
        try {
            const response = await this.services.api.completePickWave(waveId);
            if (response?.success) {
                this.services.notification.success('تم إكمال موجة الالتقاط');
                await this.loadWaves();
            } else {
                this.services.notification.error(response?.message || 'فشل في إكمال الموجة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CompletePickWave');
        }
    }
}

// Create global instance
window.warehousePickWavesHandler = new WarehousePickWavesHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/warehouse/pick-waves.html')) {
        setTimeout(() => {
            window.warehousePickWavesHandler.init();
        }, 200);
    }
});
