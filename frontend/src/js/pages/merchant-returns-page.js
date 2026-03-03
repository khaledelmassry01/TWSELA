import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-returns-page');

/**
 * Twsela CMS - Merchant Returns Page Handler
 * Handles return requests for merchants
 */

class MerchantReturnsHandler extends BasePageHandler {
    constructor() {
        super('Merchant Returns');
        this.returns = [];
        this.filteredReturns = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await this.loadReturns();
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantReturns');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load merchant returns
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
            ErrorHandler.handle(error, 'MerchantReturns.loadReturns');
        }
    }

    /**
     * Update stats cards
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
                <td>${this.getReasonLabel(ret.reason)}</td>
                <td>${escapeHtml(ret.notes || '-')}</td>
                <td><span class="badge bg-${this.getStatusColor(ret.status)}">${this.getStatusLabel(ret.status)}</span></td>
                <td>${escapeHtml(ret.courierName || 'غير معين')}</td>
                <td>${this.formatDate(ret.createdAt)}</td>
                <td>
                    ${ret.status === 'REQUESTED' ? `
                        <button class="btn btn-sm btn-outline-danger cancel-return" data-id="${ret.id}" title="إلغاء">
                            <i class="fas fa-times"></i> إلغاء
                        </button>
                    ` : ''}
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Create return request
     */
    async createReturn() {
        try {
            const shipmentId = document.getElementById('shipmentId')?.value;
            const reason = document.getElementById('returnReason')?.value;
            const notes = document.getElementById('returnNotes')?.value;

            if (!shipmentId || !reason) {
                this.services.notification.warning('يرجى ملء جميع الحقول المطلوبة');
                return;
            }

            const btn = document.getElementById('submitReturnBtn');
            UIUtils.showButtonLoading(btn, 'جاري الإرسال...');

            const response = await this.services.api.createReturn({
                shipmentId: parseInt(shipmentId),
                reason: reason,
                notes: notes || ''
            });

            if (response.success) {
                this.services.notification.success('تم إرسال طلب الإرجاع بنجاح');
                const modal = bootstrap.Modal.getInstance(document.getElementById('createReturnModal'));
                if (modal) modal.hide();
                document.getElementById('createReturnForm')?.reset();
                await this.loadReturns();
            } else {
                ErrorHandler.handle(response, 'MerchantReturns.createReturn');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantReturns.createReturn');
        } finally {
            const btn = document.getElementById('submitReturnBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Cancel a return request
     */
    async cancelReturn(id) {
        try {
            const result = await Swal.fire({
                title: 'تأكيد الإلغاء',
                text: 'هل أنت متأكد من إلغاء طلب الإرجاع؟',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#ef4444',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'نعم، ألغِ الطلب',
                cancelButtonText: 'تراجع'
            });

            if (result.isConfirmed) {
                UIUtils.showLoading();
                const response = await this.services.api.updateReturnStatus(id, 'CANCELLED');
                if (response.success) {
                    this.services.notification.success('تم إلغاء طلب الإرجاع');
                    await this.loadReturns();
                } else {
                    ErrorHandler.handle(response, 'MerchantReturns.cancelReturn');
                }
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantReturns.cancelReturn');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Get reason label in Arabic
     */
    getReasonLabel(reason) {
        const labels = {
            'DAMAGED': 'منتج تالف',
            'WRONG_ITEM': 'منتج خاطئ',
            'NOT_AS_DESCRIBED': 'لا يطابق الوصف',
            'CUSTOMER_CHANGED_MIND': 'تغيير رأي العميل',
            'DELIVERY_REFUSED': 'رفض الاستلام',
            'OTHER': 'سبب آخر'
        };
        return labels[reason] || escapeHtml(reason || '-');
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
                    (r.reason || '').toLowerCase().includes(query) ||
                    (r.notes || '').toLowerCase().includes(query)
                );
                this.updateReturnsTable();
            });
        }

        // Submit return
        const submitBtn = document.getElementById('submitReturnBtn');
        if (submitBtn) {
            submitBtn.addEventListener('click', () => this.createReturn());
        }

        // Cancel return (event delegation)
        const table = document.getElementById('returnsTable');
        if (table) {
            table.addEventListener('click', (e) => {
                const cancelBtn = e.target.closest('.cancel-return');
                if (cancelBtn) {
                    this.cancelReturn(parseInt(cancelBtn.dataset.id));
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
window.merchantReturnsHandler = new MerchantReturnsHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/merchant/returns.html')) {
        setTimeout(() => {
            window.merchantReturnsHandler.init();
        }, 200);
    }
});
