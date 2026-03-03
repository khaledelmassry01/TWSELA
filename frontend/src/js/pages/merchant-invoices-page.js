import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-invoices-page');

/**
 * Twsela CMS - Merchant Invoices Page Handler
 * Handles invoice viewing and payment for merchants
 */

class MerchantInvoicesHandler extends BasePageHandler {
    constructor() {
        super('Merchant Invoices');
        this.invoices = [];
        this.filteredInvoices = [];
        this.selectedInvoiceId = null;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await this.loadInvoices();
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantInvoices');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load merchant invoices
     */
    async loadInvoices() {
        try {
            UIUtils.showTableLoading('#invoicesTable');
            const response = await this.services.api.getInvoices();

            if (response.success) {
                this.invoices = response.data?.content || response.data || [];
                this.filteredInvoices = [...this.invoices];
                this.updateStats();
                this.updateInvoicesTable();
            } else {
                UIUtils.showEmptyState('#invoicesTable tbody', 'لا توجد فواتير', 'file-invoice');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantInvoices.loadInvoices');
        }
    }

    /**
     * Update stats cards
     */
    updateStats() {
        const totalEl = document.getElementById('totalInvoices');
        if (totalEl) totalEl.textContent = this.invoices.length;

        const pendingEl = document.getElementById('pendingInvoices');
        if (pendingEl) pendingEl.textContent = this.invoices.filter(i => i.status === 'PENDING').length;

        const paidEl = document.getElementById('paidInvoices');
        if (paidEl) paidEl.textContent = this.invoices.filter(i => i.status === 'PAID').length;

        const overdueEl = document.getElementById('overdueInvoices');
        if (overdueEl) overdueEl.textContent = this.invoices.filter(i => i.status === 'OVERDUE').length;
    }

    /**
     * Update invoices table
     */
    updateInvoicesTable() {
        const tbody = document.querySelector('#invoicesTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!this.filteredInvoices || this.filteredInvoices.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">لا توجد فواتير</td></tr>';
            return;
        }

        this.filteredInvoices.forEach((invoice, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${index + 1}</td>
                <td><span class="fw-bold">${escapeHtml(invoice.invoiceNumber || '-')}</span></td>
                <td>${this.formatCurrency(invoice.amount)}</td>
                <td>${this.formatCurrency(invoice.tax)}</td>
                <td class="fw-bold">${this.formatCurrency(invoice.totalAmount)}</td>
                <td><span class="badge bg-${this.getStatusColor(invoice.status)}">${this.getStatusLabel(invoice.status)}</span></td>
                <td>${this.formatDate(invoice.dueDate)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary view-invoice me-1" data-id="${invoice.id}" title="عرض">
                        <i class="fas fa-eye"></i>
                    </button>
                    ${invoice.status === 'PENDING' || invoice.status === 'OVERDUE' ? `
                        <button class="btn btn-sm btn-success pay-invoice" data-id="${invoice.id}" 
                            data-number="${escapeHtml(invoice.invoiceNumber || '')}" 
                            data-amount="${invoice.totalAmount || 0}" title="دفع">
                            <i class="fas fa-credit-card"></i>
                        </button>
                    ` : ''}
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * View invoice details
     */
    async viewInvoice(id) {
        try {
            UIUtils.showLoading();
            const response = await this.services.api.getInvoice(id);

            if (response.success) {
                const inv = response.data;
                const body = document.getElementById('invoiceDetailsBody');
                if (body) {
                    body.innerHTML = `
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <p><strong>رقم الفاتورة:</strong> ${escapeHtml(inv.invoiceNumber || '-')}</p>
                                <p><strong>الحالة:</strong> <span class="badge bg-${this.getStatusColor(inv.status)}">${this.getStatusLabel(inv.status)}</span></p>
                            </div>
                            <div class="col-md-6">
                                <p><strong>تاريخ الاستحقاق:</strong> ${this.formatDate(inv.dueDate)}</p>
                                <p><strong>تاريخ الدفع:</strong> ${inv.paidAt ? this.formatDate(inv.paidAt) : 'لم يتم الدفع'}</p>
                            </div>
                        </div>
                        <hr>
                        <div class="row mb-3">
                            <div class="col-md-4"><strong>المبلغ:</strong> ${this.formatCurrency(inv.amount)}</div>
                            <div class="col-md-4"><strong>الضريبة:</strong> ${this.formatCurrency(inv.tax)}</div>
                            <div class="col-md-4"><strong>الإجمالي:</strong> <span class="fw-bold text-primary">${this.formatCurrency(inv.totalAmount)}</span></div>
                        </div>
                        ${inv.items && inv.items.length > 0 ? `
                            <hr>
                            <h6>بنود الفاتورة</h6>
                            <div class="table-responsive">
                                <table class="table table-sm">
                                    <thead><tr><th>الوصف</th><th>الكمية</th><th>السعر</th><th>الإجمالي</th></tr></thead>
                                    <tbody>
                                        ${inv.items.map(item => `
                                            <tr>
                                                <td>${escapeHtml(item.description || '-')}</td>
                                                <td>${item.quantity || 1}</td>
                                                <td>${this.formatCurrency(item.unitPrice)}</td>
                                                <td>${this.formatCurrency(item.totalPrice)}</td>
                                            </tr>
                                        `).join('')}
                                    </tbody>
                                </table>
                            </div>
                        ` : ''}
                    `;
                }
                const modal = new bootstrap.Modal(document.getElementById('invoiceDetailsModal'));
                modal.show();
            } else {
                ErrorHandler.handle(response, 'MerchantInvoices.viewInvoice');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantInvoices.viewInvoice');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Open pay invoice modal
     */
    openPayModal(id, number, amount) {
        this.selectedInvoiceId = id;
        const numEl = document.getElementById('payInvoiceNumber');
        if (numEl) numEl.textContent = number;
        const amtEl = document.getElementById('payInvoiceAmount');
        if (amtEl) amtEl.textContent = this.formatCurrency(amount);

        document.getElementById('paymentGateway').value = '';
        const modal = new bootstrap.Modal(document.getElementById('payInvoiceModal'));
        modal.show();
    }

    /**
     * Confirm payment
     */
    async confirmPayment() {
        try {
            const gateway = document.getElementById('paymentGateway')?.value;
            if (!gateway) {
                this.services.notification.warning('يرجى اختيار طريقة الدفع');
                return;
            }

            const btn = document.getElementById('confirmPayBtn');
            UIUtils.showButtonLoading(btn, 'جاري الدفع...');

            const response = await this.services.api.payInvoice(this.selectedInvoiceId, {
                gateway,
                currency: 'EGP'
            });

            if (response.success) {
                this.services.notification.success('تم دفع الفاتورة بنجاح');
                const modal = bootstrap.Modal.getInstance(document.getElementById('payInvoiceModal'));
                if (modal) modal.hide();
                await this.loadInvoices();
            } else {
                ErrorHandler.handle(response, 'MerchantInvoices.confirmPayment');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantInvoices.confirmPayment');
        } finally {
            const btn = document.getElementById('confirmPayBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Get status badge color
     */
    getStatusColor(status) {
        const colors = {
            'DRAFT': 'secondary',
            'PENDING': 'warning',
            'PAID': 'success',
            'OVERDUE': 'danger',
            'CANCELLED': 'dark',
            'REFUNDED': 'info'
        };
        return colors[status] || 'secondary';
    }

    /**
     * Get status label in Arabic
     */
    getStatusLabel(status) {
        const labels = {
            'DRAFT': 'مسودة',
            'PENDING': 'في انتظار الدفع',
            'PAID': 'مدفوعة',
            'OVERDUE': 'متأخرة',
            'CANCELLED': 'ملغاة',
            'REFUNDED': 'مستردة'
        };
        return labels[status] || status || 'غير محدد';
    }

    /**
     * Format currency
     */
    formatCurrency(amount) {
        if (amount === null || amount === undefined) return '0.00 ج.م';
        return parseFloat(amount).toFixed(2) + ' ج.م';
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

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const statusFilter = document.getElementById('invoiceStatusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', () => {
                const status = statusFilter.value;
                this.filteredInvoices = status === 'all'
                    ? [...this.invoices]
                    : this.invoices.filter(i => i.status === status);
                this.updateInvoicesTable();
            });
        }

        const search = document.getElementById('invoiceSearch');
        if (search) {
            search.addEventListener('input', () => {
                const query = search.value.toLowerCase();
                this.filteredInvoices = this.invoices.filter(i =>
                    (i.invoiceNumber || '').toLowerCase().includes(query)
                );
                this.updateInvoicesTable();
            });
        }

        const confirmPayBtn = document.getElementById('confirmPayBtn');
        if (confirmPayBtn) {
            confirmPayBtn.addEventListener('click', () => this.confirmPayment());
        }

        const table = document.getElementById('invoicesTable');
        if (table) {
            table.addEventListener('click', (e) => {
                const viewBtn = e.target.closest('.view-invoice');
                if (viewBtn) {
                    this.viewInvoice(parseInt(viewBtn.dataset.id));
                }
                const payBtn = e.target.closest('.pay-invoice');
                if (payBtn) {
                    this.openPayModal(
                        parseInt(payBtn.dataset.id),
                        payBtn.dataset.number,
                        payBtn.dataset.amount
                    );
                }
            });
        }
    }
}

// Create global instance
window.merchantInvoicesHandler = new MerchantInvoicesHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/merchant/invoices.html')) {
        setTimeout(() => {
            window.merchantInvoicesHandler.init();
        }, 200);
    }
});
