import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('courier-pickups-page');

/**
 * Twsela CMS - Courier Pickups Page Handler
 * Handles today's pickup tasks for couriers (start/complete flow)
 */

class CourierPickupsHandler extends BasePageHandler {
    constructor() {
        super('Courier Pickups');
        this.pickups = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await this.loadTodayPickups();
        } catch (error) {
            ErrorHandler.handle(error, 'CourierPickups');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load today's pickups
     */
    async loadTodayPickups() {
        try {
            const response = await this.services.api.getTodayPickups();

            if (response.success) {
                this.pickups = response.data || [];
                this.updateStats();
                this.renderPickups();
            } else {
                this.showNoPickups();
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CourierPickups.loadTodayPickups');
            this.showNoPickups();
        }
    }

    /**
     * Show no pickups state
     */
    showNoPickups() {
        const cards = document.getElementById('pickupCards');
        const empty = document.getElementById('noPickupsState');
        if (cards) cards.innerHTML = '';
        if (empty) empty.classList.remove('d-none');
    }

    /**
     * Update stats cards
     */
    updateStats() {
        const totalEl = document.getElementById('totalTodayPickups');
        if (totalEl) totalEl.textContent = this.pickups.length;

        const pendingEl = document.getElementById('pendingTodayPickups');
        if (pendingEl) pendingEl.textContent = this.pickups.filter(p =>
            p.status === 'ASSIGNED' || p.status === 'SCHEDULED'
        ).length;

        const completedEl = document.getElementById('completedTodayPickups');
        if (completedEl) completedEl.textContent = this.pickups.filter(p => p.status === 'COMPLETED').length;
    }

    /**
     * Render pickup cards
     */
    renderPickups() {
        const container = document.getElementById('pickupCards');
        const empty = document.getElementById('noPickupsState');

        if (!this.pickups || this.pickups.length === 0) {
            this.showNoPickups();
            return;
        }

        if (empty) empty.classList.add('d-none');
        if (!container) return;

        container.innerHTML = '';

        this.pickups.forEach(pickup => {
            const card = document.createElement('div');
            card.className = 'content-card mb-3';
            card.innerHTML = `
                <div class="card-content">
                    <div class="d-flex justify-content-between align-items-start">
                        <div class="flex-grow-1">
                            <div class="d-flex align-items-center mb-2">
                                <span class="badge bg-${this.getStatusColor(pickup.status)} me-2">${this.getStatusLabel(pickup.status)}</span>
                                <span class="badge bg-light text-dark">${this.getTimeSlotLabel(pickup.timeSlot)}</span>
                            </div>
                            <h6 class="mb-1"><i class="fas fa-store me-1"></i>${escapeHtml(pickup.merchantName || 'التاجر')}</h6>
                            <p class="text-muted mb-1"><i class="fas fa-map-marker-alt me-1"></i>${escapeHtml(pickup.address || '-')}</p>
                            <small class="text-muted"><i class="fas fa-boxes me-1"></i>عدد الشحنات المتوقع: ${pickup.estimatedShipments || 0}</small>
                            ${pickup.notes ? `<p class="text-muted small mt-1 mb-0"><i class="fas fa-sticky-note me-1"></i>${escapeHtml(pickup.notes)}</p>` : ''}
                        </div>
                        <div class="d-flex flex-column gap-2">
                            ${pickup.status === 'ASSIGNED' ? `
                                <button class="btn btn-sm btn-primary start-pickup" data-id="${pickup.id}">
                                    <i class="fas fa-play me-1"></i> بدء الاستلام
                                </button>
                            ` : ''}
                            ${pickup.status === 'IN_PROGRESS' ? `
                                <button class="btn btn-sm btn-success complete-pickup" data-id="${pickup.id}">
                                    <i class="fas fa-check me-1"></i> إتمام
                                </button>
                            ` : ''}
                            ${pickup.status === 'COMPLETED' ? `
                                <span class="text-success"><i class="fas fa-check-circle fa-lg"></i></span>
                            ` : ''}
                        </div>
                    </div>
                </div>
            `;
            container.appendChild(card);
        });
    }

    /**
     * Start a pickup
     */
    async startPickup(id) {
        try {
            const result = await Swal.fire({
                title: 'بدء الاستلام',
                text: 'هل أنت متأكد من بدء مهمة الاستلام؟',
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#3085d6',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'نعم، ابدأ',
                cancelButtonText: 'تراجع'
            });

            if (result.isConfirmed) {
                UIUtils.showLoading();
                const response = await this.services.api.startPickup(id);
                if (response.success) {
                    this.services.notification.success('تم بدء مهمة الاستلام');
                    await this.loadTodayPickups();
                } else {
                    ErrorHandler.handle(response, 'CourierPickups.startPickup');
                }
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CourierPickups.startPickup');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Complete a pickup
     */
    async completePickup(id) {
        try {
            const result = await Swal.fire({
                title: 'إتمام الاستلام',
                text: 'هل تم استلام جميع الشحنات بنجاح؟',
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#22c55e',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'نعم، تم الاستلام',
                cancelButtonText: 'تراجع'
            });

            if (result.isConfirmed) {
                UIUtils.showLoading();
                const response = await this.services.api.completePickup(id);
                if (response.success) {
                    this.services.notification.success('تم إتمام مهمة الاستلام بنجاح');
                    await this.loadTodayPickups();
                } else {
                    ErrorHandler.handle(response, 'CourierPickups.completePickup');
                }
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CourierPickups.completePickup');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Get time slot label in Arabic
     */
    getTimeSlotLabel(slot) {
        const labels = {
            'MORNING': 'صباحاً (9-12)',
            'AFTERNOON': 'ظهراً (12-3)',
            'EVENING': 'مساءً (3-6)',
            'LATE_EVENING': 'ليلاً (6-9)'
        };
        return labels[slot] || slot || '-';
    }

    /**
     * Get status badge color
     */
    getStatusColor(status) {
        const colors = {
            'SCHEDULED': 'secondary',
            'ASSIGNED': 'warning',
            'IN_PROGRESS': 'primary',
            'COMPLETED': 'success',
            'CANCELLED': 'dark'
        };
        return colors[status] || 'secondary';
    }

    /**
     * Get status label in Arabic
     */
    getStatusLabel(status) {
        const labels = {
            'SCHEDULED': 'مجدولة',
            'ASSIGNED': 'معيّنة',
            'IN_PROGRESS': 'قيد التنفيذ',
            'COMPLETED': 'مكتملة',
            'CANCELLED': 'ملغاة'
        };
        return labels[status] || status || 'غير محدد';
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const refreshBtn = document.getElementById('refreshPickupsBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', async () => {
                UIUtils.showLoading();
                await this.loadTodayPickups();
                UIUtils.hideLoading();
            });
        }

        // Event delegation for pickup actions
        const container = document.getElementById('pickupCards');
        if (container) {
            container.addEventListener('click', (e) => {
                const startBtn = e.target.closest('.start-pickup');
                if (startBtn) {
                    this.startPickup(parseInt(startBtn.dataset.id));
                }
                const completeBtn = e.target.closest('.complete-pickup');
                if (completeBtn) {
                    this.completePickup(parseInt(completeBtn.dataset.id));
                }
            });
        }
    }
}

// Create global instance
window.courierPickupsHandler = new CourierPickupsHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/courier/pickups.html')) {
        setTimeout(() => {
            window.courierPickupsHandler.init();
        }, 200);
    }
});
