import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-pickups-page');

/**
 * Twsela CMS - Merchant Pickups Page Handler
 * Handles pickup scheduling and management for merchants
 */

class MerchantPickupsHandler extends BasePageHandler {
    constructor() {
        super('Merchant Pickups');
        this.pickups = [];
        this.filteredPickups = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            this.setMinDate();
            await this.loadPickups();
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantPickups');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Set minimum date to today
     */
    setMinDate() {
        const dateInput = document.getElementById('pickupDate');
        if (dateInput) {
            const today = new Date().toISOString().split('T')[0];
            dateInput.min = today;
            dateInput.value = today;
        }
    }

    /**
     * Load merchant pickups
     */
    async loadPickups() {
        try {
            UIUtils.showTableLoading('#pickupsTable');
            const response = await this.services.api.getMyPickups();

            if (response.success) {
                this.pickups = response.data?.content || response.data || [];
                this.filteredPickups = [...this.pickups];
                this.updateStats();
                this.updatePickupsTable();
            } else {
                UIUtils.showEmptyState('#pickupsTable tbody', 'لا توجد طلبات استلام', 'truck-loading');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantPickups.loadPickups');
        }
    }

    /**
     * Update stats cards
     */
    updateStats() {
        const totalEl = document.getElementById('totalPickups');
        if (totalEl) totalEl.textContent = this.pickups.length;

        const scheduledEl = document.getElementById('scheduledPickups');
        if (scheduledEl) scheduledEl.textContent = this.pickups.filter(p =>
            p.status === 'SCHEDULED' || p.status === 'ASSIGNED'
        ).length;

        const inProgressEl = document.getElementById('inProgressPickups');
        if (inProgressEl) inProgressEl.textContent = this.pickups.filter(p => p.status === 'IN_PROGRESS').length;

        const completedEl = document.getElementById('completedPickups');
        if (completedEl) completedEl.textContent = this.pickups.filter(p => p.status === 'COMPLETED').length;
    }

    /**
     * Update pickups table
     */
    updatePickupsTable() {
        const tbody = document.querySelector('#pickupsTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!this.filteredPickups || this.filteredPickups.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">لا توجد طلبات استلام</td></tr>';
            return;
        }

        this.filteredPickups.forEach((pickup, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${this.formatDate(pickup.pickupDate)}</td>
                <td>${this.getTimeSlotLabel(pickup.timeSlot)}</td>
                <td>${escapeHtml(pickup.address || '-')}</td>
                <td>${pickup.estimatedShipments || 0}</td>
                <td><span class="badge bg-${this.getStatusColor(pickup.status)}">${this.getStatusLabel(pickup.status)}</span></td>
                <td>${escapeHtml(pickup.courierName || 'غير معين')}</td>
                <td>
                    ${pickup.status === 'SCHEDULED' ? `
                        <button class="btn btn-sm btn-outline-danger cancel-pickup" data-id="${pickup.id}" title="إلغاء">
                            <i class="fas fa-times"></i>
                        </button>
                    ` : ''}
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Schedule a new pickup
     */
    async schedulePickup() {
        try {
            const pickupDate = document.getElementById('pickupDate')?.value;
            const timeSlot = document.getElementById('timeSlot')?.value;
            const address = document.getElementById('pickupAddress')?.value;
            const estimatedShipments = document.getElementById('estimatedShipments')?.value;
            const notes = document.getElementById('pickupNotes')?.value;

            if (!pickupDate || !timeSlot || !address) {
                this.services.notification.warning('يرجى ملء جميع الحقول المطلوبة');
                return;
            }

            const btn = document.getElementById('submitPickupBtn');
            UIUtils.showButtonLoading(btn, 'جاري الجدولة...');

            const response = await this.services.api.schedulePickup({
                pickupDate,
                timeSlot,
                address,
                estimatedShipments: parseInt(estimatedShipments) || 1,
                notes: notes || ''
            });

            if (response.success) {
                this.services.notification.success('تم جدولة طلب الاستلام بنجاح');
                const modal = bootstrap.Modal.getInstance(document.getElementById('schedulePickupModal'));
                if (modal) modal.hide();
                document.getElementById('schedulePickupForm')?.reset();
                this.setMinDate();
                await this.loadPickups();
            } else {
                ErrorHandler.handle(response, 'MerchantPickups.schedulePickup');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantPickups.schedulePickup');
        } finally {
            const btn = document.getElementById('submitPickupBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Cancel a pickup
     */
    async cancelPickup(id) {
        try {
            const result = await Swal.fire({
                title: 'تأكيد الإلغاء',
                text: 'هل أنت متأكد من إلغاء طلب الاستلام؟',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#ef4444',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'نعم، ألغِ الطلب',
                cancelButtonText: 'تراجع'
            });

            if (result.isConfirmed) {
                UIUtils.showLoading();
                const response = await this.services.api.cancelPickup(id);
                if (response.success) {
                    this.services.notification.success('تم إلغاء طلب الاستلام');
                    await this.loadPickups();
                } else {
                    ErrorHandler.handle(response, 'MerchantPickups.cancelPickup');
                }
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantPickups.cancelPickup');
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
            'SCHEDULED': 'warning',
            'ASSIGNED': 'info',
            'IN_PROGRESS': 'primary',
            'COMPLETED': 'success',
            'CANCELLED': 'dark',
            'OVERDUE': 'danger'
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
            'CANCELLED': 'ملغاة',
            'OVERDUE': 'متأخرة'
        };
        return labels[status] || status || 'غير محدد';
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const statusFilter = document.getElementById('pickupStatusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', () => {
                const status = statusFilter.value;
                this.filteredPickups = status === 'all'
                    ? [...this.pickups]
                    : this.pickups.filter(p => p.status === status);
                this.updatePickupsTable();
            });
        }

        const search = document.getElementById('pickupSearch');
        if (search) {
            search.addEventListener('input', () => {
                const query = search.value.toLowerCase();
                this.filteredPickups = this.pickups.filter(p =>
                    (p.address || '').toLowerCase().includes(query) ||
                    (p.notes || '').toLowerCase().includes(query)
                );
                this.updatePickupsTable();
            });
        }

        const submitBtn = document.getElementById('submitPickupBtn');
        if (submitBtn) {
            submitBtn.addEventListener('click', () => this.schedulePickup());
        }

        const table = document.getElementById('pickupsTable');
        if (table) {
            table.addEventListener('click', (e) => {
                const cancelBtn = e.target.closest('.cancel-pickup');
                if (cancelBtn) {
                    this.cancelPickup(parseInt(cancelBtn.dataset.id));
                }
            });
        }
    }

    /**
     * Format date
     */
    formatDate(dateString) {
        if (!dateString) return 'غير محدد';
        try {
            return new Date(dateString).toLocaleDateString('ar-SA', {
                year: 'numeric', month: 'short', day: 'numeric'
            });
        } catch {
            return 'غير محدد';
        }
    }
}

// Create global instance
window.merchantPickupsHandler = new MerchantPickupsHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/merchant/pickups.html')) {
        setTimeout(() => {
            window.merchantPickupsHandler.init();
        }, 200);
    }
});
