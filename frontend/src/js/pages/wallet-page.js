import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('wallet-page');

/**
 * Twsela CMS - Wallet Page Handler
 * Handles wallet balance, transactions, and withdrawal requests
 * Shared page for all authenticated roles
 */

class WalletPageHandler extends BasePageHandler {
    constructor() {
        super('Wallet');
        this.wallet = null;
        this.transactions = [];
        this.currentPage = 0;
        this.pageSize = 20;
        this.totalElements = 0;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();

            // Build sidebar based on role
            this.buildSidebar();

            // Load wallet data
            await this.loadWalletData();

            // Load transactions
            await this.loadTransactions();

            // Setup filter
            this.setupFilter();

        } catch (error) {
            ErrorHandler.handle(error, 'WalletPage');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Build sidebar navigation based on user role
     */
    buildSidebar() {
        const nav = document.getElementById('sidebarNav');
        if (!nav) return;

        const user = this.getCurrentUser();
        const role = user?.role?.name || sessionStorage.getItem('userRole') || '';

        let links = '';
        if (role === 'OWNER') {
            links = `
                <a href="/owner/dashboard.html" class="nav-link"><i class="fas fa-tachometer-alt"></i><span>لوحة التحكم</span></a>
                <a href="/owner/shipments.html" class="nav-link"><i class="fas fa-shipping-fast"></i><span>الشحنات</span></a>
                <a href="/owner/merchants.html" class="nav-link"><i class="fas fa-store"></i><span>التجار</span></a>
                <a href="/wallet.html" class="nav-link active"><i class="fas fa-wallet"></i><span>المحفظة</span></a>
                <a href="/owner/returns.html" class="nav-link"><i class="fas fa-undo"></i><span>المرتجعات</span></a>
                <a href="/notifications.html" class="nav-link"><i class="fas fa-bell"></i><span>الإشعارات</span></a>
            `;
        } else if (role === 'MERCHANT') {
            links = `
                <a href="/merchant/dashboard.html" class="nav-link"><i class="fas fa-tachometer-alt"></i><span>لوحة التحكم</span></a>
                <a href="/merchant/shipments.html" class="nav-link"><i class="fas fa-shipping-fast"></i><span>الشحنات</span></a>
                <a href="/merchant/create-shipment.html" class="nav-link"><i class="fas fa-plus-circle"></i><span>إنشاء شحنة</span></a>
                <a href="/wallet.html" class="nav-link active"><i class="fas fa-wallet"></i><span>المحفظة</span></a>
                <a href="/merchant/returns.html" class="nav-link"><i class="fas fa-undo"></i><span>المرتجعات</span></a>
                <a href="/notifications.html" class="nav-link"><i class="fas fa-bell"></i><span>الإشعارات</span></a>
            `;
        } else if (role === 'COURIER') {
            links = `
                <a href="/courier/dashboard.html" class="nav-link"><i class="fas fa-tachometer-alt"></i><span>لوحة التحكم</span></a>
                <a href="/courier/manifest.html" class="nav-link"><i class="fas fa-clipboard-list"></i><span>المانيفست</span></a>
                <a href="/wallet.html" class="nav-link active"><i class="fas fa-wallet"></i><span>المحفظة</span></a>
                <a href="/notifications.html" class="nav-link"><i class="fas fa-bell"></i><span>الإشعارات</span></a>
            `;
        } else {
            links = `
                <a href="/wallet.html" class="nav-link active"><i class="fas fa-wallet"></i><span>المحفظة</span></a>
                <a href="/notifications.html" class="nav-link"><i class="fas fa-bell"></i><span>الإشعارات</span></a>
            `;
        }

        links += `
            <div class="nav-divider"></div>
            <a href="/profile.html" class="nav-link"><i class="fas fa-user"></i><span>الملف الشخصي</span></a>
            <a href="/settings.html" class="nav-link"><i class="fas fa-cog"></i><span>الإعدادات</span></a>
            <a href="#" class="nav-link logout-link" data-action="logout"><i class="fas fa-sign-out-alt"></i><span>تسجيل الخروج</span></a>
        `;

        nav.innerHTML = links;
    }

    /**
     * Load wallet data
     */
    async loadWalletData() {
        try {
            const response = await this.services.api.getMyWallet();

            if (response.success && response.data) {
                this.wallet = response.data;
                this.updateWalletDisplay();
            }
        } catch (error) {
            ErrorHandler.handle(error, 'WalletPage.loadWalletData');
        }
    }

    /**
     * Update wallet display
     */
    updateWalletDisplay() {
        if (!this.wallet) return;

        const balanceEl = document.getElementById('walletBalance');
        if (balanceEl) {
            balanceEl.textContent = `${this.formatCurrency(this.wallet.balance)} جنيه`;
        }

        const totalCreditsEl = document.getElementById('totalCredits');
        if (totalCreditsEl) {
            totalCreditsEl.textContent = `${this.formatCurrency(this.wallet.totalCredits || 0)} جنيه`;
        }

        const totalDebitsEl = document.getElementById('totalDebits');
        if (totalDebitsEl) {
            totalDebitsEl.textContent = `${this.formatCurrency(this.wallet.totalDebits || 0)} جنيه`;
        }

        const pendingEl = document.getElementById('pendingTransactions');
        if (pendingEl) {
            pendingEl.textContent = this.wallet.pendingCount || '0';
        }

        const availableEl = document.getElementById('availableBalance');
        if (availableEl) {
            availableEl.textContent = this.formatCurrency(this.wallet.balance || 0);
        }
    }

    /**
     * Load transactions
     */
    async loadTransactions() {
        try {
            UIUtils.showTableLoading('#transactionsTable');
            const response = await this.services.api.getWalletTransactions(this.currentPage, this.pageSize);

            if (response.success) {
                this.transactions = response.data || [];
                this.totalElements = response.totalElements || this.transactions.length;
                this.updateTransactionsTable();
                this.updatePaginationInfo();
            } else {
                UIUtils.showEmptyState('#transactionsTable tbody', 'لا توجد معاملات', 'receipt');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'WalletPage.loadTransactions');
        }
    }

    /**
     * Update transactions table
     */
    updateTransactionsTable() {
        const tbody = document.querySelector('#transactionsTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!this.transactions || this.transactions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">لا توجد معاملات</td></tr>';
            return;
        }

        this.transactions.forEach((tx, index) => {
            const row = document.createElement('tr');
            const isCredit = tx.type === 'CREDIT';
            const amountClass = isCredit ? 'text-success' : 'text-danger';
            const amountPrefix = isCredit ? '+' : '-';

            row.innerHTML = `
                <td>${(this.currentPage * this.pageSize) + index + 1}</td>
                <td><span class="badge bg-${isCredit ? 'success' : 'warning'}">${isCredit ? 'إيداع' : 'سحب'}</span></td>
                <td class="${amountClass} fw-bold">${amountPrefix}${this.formatCurrency(tx.amount)} جنيه</td>
                <td>${this.getTransactionReasonLabel(tx.reason)}</td>
                <td>${escapeHtml(tx.description || '-')}</td>
                <td>${this.formatDate(tx.createdAt)}</td>
                <td>${this.formatCurrency(tx.balanceAfter || 0)} جنيه</td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Get human-readable transaction reason label
     */
    getTransactionReasonLabel(reason) {
        const labels = {
            'SHIPMENT_COD': 'تحصيل شحنة',
            'SHIPMENT_FEE': 'رسوم شحنة',
            'WITHDRAWAL': 'طلب سحب',
            'REFUND': 'استرداد',
            'COMMISSION': 'عمولة',
            'PAYOUT': 'صرف مستحقات',
            'ADJUSTMENT': 'تعديل يدوي',
            'BONUS': 'مكافأة'
        };
        return labels[reason] || reason || '-';
    }

    /**
     * Setup filter
     */
    setupFilter() {
        const filter = document.getElementById('transactionFilter');
        if (filter) {
            filter.addEventListener('change', () => {
                this.currentPage = 0;
                this.loadTransactions();
            });
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Withdraw button
        const confirmBtn = document.getElementById('confirmWithdrawBtn');
        if (confirmBtn) {
            confirmBtn.addEventListener('click', () => this.handleWithdraw());
        }

        // Pagination
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');

        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                if (this.currentPage > 0) {
                    this.currentPage--;
                    this.loadTransactions();
                }
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                if ((this.currentPage + 1) * this.pageSize < this.totalElements) {
                    this.currentPage++;
                    this.loadTransactions();
                }
            });
        }

        // Logout
        const logoutBtn = document.querySelector('.logout-link');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.handleLogout();
            });
        }
    }

    /**
     * Handle withdrawal request
     */
    async handleWithdraw() {
        try {
            const amountInput = document.getElementById('withdrawAmount');
            const amount = parseFloat(amountInput?.value);

            if (!amount || amount <= 0) {
                this.services.notification.warning('يرجى إدخال مبلغ صحيح');
                return;
            }

            if (this.wallet && amount > this.wallet.balance) {
                this.services.notification.warning('المبلغ المطلوب أكبر من الرصيد المتاح');
                return;
            }

            const confirmBtn = document.getElementById('confirmWithdrawBtn');
            UIUtils.showButtonLoading(confirmBtn, 'جاري السحب...');

            const response = await this.services.api.requestWithdrawal(amount);

            if (response.success) {
                this.services.notification.success('تم طلب السحب بنجاح');

                // Close modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('withdrawModal'));
                if (modal) modal.hide();

                // Reset form
                amountInput.value = '';

                // Reload data
                await this.loadWalletData();
                await this.loadTransactions();
            } else {
                ErrorHandler.handle(response, 'WalletPage.withdraw');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'WalletPage.withdraw');
        } finally {
            const confirmBtn = document.getElementById('confirmWithdrawBtn');
            UIUtils.hideButtonLoading(confirmBtn);
        }
    }

    /**
     * Update pagination info
     */
    updatePaginationInfo() {
        const info = document.getElementById('paginationInfo');
        if (info) {
            const start = this.currentPage * this.pageSize + 1;
            const end = Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
            info.textContent = this.totalElements > 0
                ? `عرض ${start}-${end} من ${this.totalElements}`
                : 'عرض 0 من 0';
        }
    }

    /**
     * Format currency
     */
    formatCurrency(value) {
        return parseFloat(value || 0).toLocaleString('ar-EG', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    /**
     * Format date
     */
    formatDate(dateString) {
        if (!dateString) return 'غير محدد';
        try {
            return new Date(dateString).toLocaleDateString('ar-SA', {
                year: 'numeric', month: 'short', day: 'numeric',
                hour: '2-digit', minute: '2-digit'
            });
        } catch {
            return 'غير محدد';
        }
    }
}

// Create global instance
window.walletPageHandler = new WalletPageHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/wallet.html')) {
        setTimeout(() => {
            window.walletPageHandler.init();
        }, 200);
    }
});
