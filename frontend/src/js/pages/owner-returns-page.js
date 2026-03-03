import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('owner-returns-page');

/**
 * Twsela CMS - Owner Returns Management Page Handler
 * Handles return shipments management for owner/admin
 */

class OwnerReturnsHandler extends BasePageHandler {
    constructor() {
        super('Owner Returns');
        this.returns = [];
        this.filteredReturns = [];
        this.couriers = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();

            // Load returns
            await this.loadReturns();

            // Load couriers for assignment
            await this.loadCouriers();

        } catch (error) {
            ErrorHandler.handle(error, 'OwnerReturns');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load returns
     */
    async loadReturns() {
        try {
            UIUtils.showTableLoading('#returnsTable');
            const response = await this.services.api.getReturns();

            if (response.success) {
                this.returns = response.data || [];
                this.filteredReturns = [...this.returns];
                this.updateStats();
                this.updateReturnsTable();
            } else {
                UIUtils.showEmptyState('#returnsTable tbody', 'لا توجد مرتجعات', 'undo');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerReturns.loadReturns');
        }
    }

    /**
     * Load couriers for assignment dropdown
     */
    async loadCouriers() {
        try {
            const response = await this.services.api.getCouriers();
            if (response.success) {
                this.couriers = response.data || [];
                this.populateCourierDropdown();
            }
        } catch (error) {
            log.error('Error loading couriers:', error);
        }
    }

    /**
     * Populate courier dropdown in assign modal
     */
    populateCourierDropdown() {
        const select = document.getElementById('courierId');
        if (!select) return;

        select.innerHTML = '<option value="">اختر المندوب</option>';
        this.couriers.forEach(courier => {
            select.innerHTML += `<option value="${courier.id}">${escapeHtml(courier.name || courier.phone)}</option>`;
        });
    }

    /**
     * Update stats
     */
    updateStats() {
        const totalEl = document.getElementById('totalReturns');
        if (totalEl) totalEl.textContent = this.returns.length;

        const pendingEl = document.getElementById('pendingReturns');
        if (pendingEl) pendingEl.textContent = this.returns.filter(r => r.status === 'REQUESTED').length;

        const completedEl = document.getElementById('completedReturns');
        if (completedEl) completedEl.textContent = this.returns.filter(r =>
            r.status === 'RETURNED_TO_MERCHANT' || r.status === 'RETURNED_TO_HUB'
        ).length;

        const rejectedEl = document.getElementById('rejectedReturns');
        if (rejectedEl) rejectedEl.textContent = this.returns.filter(r => r.status === 'REJECTED').length;
    }

    /**
     * Update returns table
     */
    updateReturnsTable() {
        const tbody = document.querySelector('#returnsTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!this.filteredReturns || this.filteredReturns.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">لا توجد مرتجعات</td></tr>';
            return;
        }

        this.filteredReturns.forEach((ret, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${index + 1}</td>
                <td><span class="fw-bold">${escapeHtml(ret.trackingNumber || ret.shipmentId || '-')}</span></td>
                <td>${escapeHtml(ret.merchantName || '-')}</td>
                <td>${escapeHtml(ret.reason || '-')}</td>
                <td><span class="badge bg-${this.getStatusColor(ret.status)}">${this.getStatusLabel(ret.status)}</span></td>
                <td>${escapeHtml(ret.courierName || 'غير معين')}</td>
                <td>${this.formatDate(ret.createdAt)}</td>
                <td>
                    <div class="btn-group btn-group-sm">
                        ${ret.status === 'REQUESTED' ? `
                            <button class="btn btn-outline-success approve-return" data-id="${ret.id}" title="موافقة">
                                <i class="fas fa-check"></i>
                            </button>
                            <button class="btn btn-outline-danger reject-return" data-id="${ret.id}" title="رفض">
                                <i class="fas fa-times"></i>
                            </button>
                        ` : ''}
                        ${ret.status === 'APPROVED' ? `
                            <button class="btn btn-outline-primary assign-courier" data-id="${ret.id}" title="تعيين مندوب"
                                    data-bs-toggle="modal" data-bs-target="#assignCourierModal">
                                <i class="fas fa-user-plus"></i>
                            </button>
                        ` : ''}
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Get status badge color
     */
    getStatusColor(status) {
        const colors = {
            'REQUESTED': 'warning',
            'APPROVED': 'info',
            'PICKED_UP': 'primary',
            'RETURNED_TO_HUB': 'secondary',
            'RETURNED_TO_MERCHANT': 'success',
            'REJECTED': 'danger',
            'CANCELLED': 'dark'
        };
        return colors[status] || 'secondary';
    }

    /**
     * Get status label in Arabic
     */
    getStatusLabel(status) {
        const labels = {
            'REQUESTED': 'مطلوب',
            'APPROVED': 'موافق عليه',
            'PICKED_UP': 'تم الاستلام',
            'RETURNED_TO_HUB': 'في المخزن',
            'RETURNED_TO_MERCHANT': 'تم الإرجاع',
            'REJECTED': 'مرفوض',
            'CANCELLED': 'ملغي'
        };
        return labels[status] || status || 'غير محدد';
    }

    /**
     * Approve a return
     */
    async approveReturn(id) {
        try {
            UIUtils.showLoading();
            const response = await this.services.api.updateReturnStatus(id, 'APPROVED');
            if (response.success) {
                this.services.notification.success('تمت الموافقة على المرتجع');
                await this.loadReturns();
            } else {
                ErrorHandler.handle(response, 'OwnerReturns.approve');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerReturns.approve');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Reject a return
     */
    async rejectReturn(id) {
        try {
            const result = await Swal.fire({
                title: 'تأكيد الرفض',
                text: 'هل أنت متأكد من رفض هذا المرتجع؟',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#ef4444',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'نعم، ارفض',
                cancelButtonText: 'إلغاء'
            });

            if (result.isConfirmed) {
                UIUtils.showLoading();
                const response = await this.services.api.updateReturnStatus(id, 'REJECTED');
                if (response.success) {
                    this.services.notification.success('تم رفض المرتجع');
                    await this.loadReturns();
                } else {
                    ErrorHandler.handle(response, 'OwnerReturns.reject');
                }
            }
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerReturns.reject');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Assign courier to return
     */
    async assignCourier() {
        try {
            const returnId = document.getElementById('assignReturnId')?.value;
            const courierId = document.getElementById('courierId')?.value;

            if (!returnId || !courierId) {
                this.services.notification.warning('يرجى اختيار المندوب');
                return;
            }

            const btn = document.getElementById('confirmAssignBtn');
            UIUtils.showButtonLoading(btn, 'جاري التعيين...');

            const response = await this.services.api.assignReturnCourier(parseInt(returnId), parseInt(courierId));

            if (response.success) {
                this.services.notification.success('تم تعيين المندوب بنجاح');
                const modal = bootstrap.Modal.getInstance(document.getElementById('assignCourierModal'));
                if (modal) modal.hide();
                await this.loadReturns();
            } else {
                ErrorHandler.handle(response, 'OwnerReturns.assignCourier');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerReturns.assignCourier');
        } finally {
            const btn = document.getElementById('confirmAssignBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Status filter
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', () => {
                const status = statusFilter.value;
                this.filteredReturns = status === 'all'
                    ? [...this.returns]
                    : this.returns.filter(r => r.status === status);
                this.updateReturnsTable();
            });
        }

        // Search
        const search = document.getElementById('returnSearch');
        if (search) {
            search.addEventListener('input', () => {
                const query = search.value.toLowerCase();
                this.filteredReturns = this.returns.filter(r =>
                    (r.trackingNumber || '').toLowerCase().includes(query) ||
                    (r.merchantName || '').toLowerCase().includes(query) ||
                    (r.reason || '').toLowerCase().includes(query)
                );
                this.updateReturnsTable();
            });
        }

        // Table action buttons (event delegation)
        const table = document.getElementById('returnsTable');
        if (table) {
            table.addEventListener('click', (e) => {
                const approveBtn = e.target.closest('.approve-return');
                if (approveBtn) {
                    this.approveReturn(parseInt(approveBtn.dataset.id));
                    return;
                }
                const rejectBtn = e.target.closest('.reject-return');
                if (rejectBtn) {
                    this.rejectReturn(parseInt(rejectBtn.dataset.id));
                    return;
                }
                const assignBtn = e.target.closest('.assign-courier');
                if (assignBtn) {
                    document.getElementById('assignReturnId').value = assignBtn.dataset.id;
                }
            });
        }

        // Confirm assign
        const confirmAssignBtn = document.getElementById('confirmAssignBtn');
        if (confirmAssignBtn) {
            confirmAssignBtn.addEventListener('click', () => this.assignCourier());
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
window.ownerReturnsHandler = new OwnerReturnsHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/owner/returns.html')) {
        setTimeout(() => {
            window.ownerReturnsHandler.init();
        }, 200);
    }
});
