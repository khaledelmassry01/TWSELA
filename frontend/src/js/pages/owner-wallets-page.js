import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('owner-wallets-page');

/**
 * Twsela CMS - Owner Wallets Admin Page Handler
 * Displays all wallets for admin management
 */

class OwnerWalletsHandler extends BasePageHandler {
    constructor() {
        super('Owner Wallets');
        this.wallets = [];
        this.filteredWallets = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await this.loadWallets();
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerWallets');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load all wallets
     */
    async loadWallets() {
        try {
            UIUtils.showTableLoading('#walletsTable');
            const response = await this.services.api.getAdminWallets();

            if (response.success) {
                this.wallets = response.data || [];
                this.filteredWallets = [...this.wallets];
                this.updateStats();
                this.updateWalletsTable();
            } else {
                UIUtils.showEmptyState('#walletsTable tbody', 'لا توجد محافظ', 'wallet');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerWallets.loadWallets');
        }
    }

    /**
     * Update summary stats
     */
    updateStats() {
        const totalEl = document.getElementById('totalWallets');
        if (totalEl) totalEl.textContent = this.wallets.length;

        const totalBalanceEl = document.getElementById('totalBalance');
        if (totalBalanceEl) {
            const sum = this.wallets.reduce((acc, w) => acc + parseFloat(w.balance || 0), 0);
            totalBalanceEl.textContent = `${this.formatCurrency(sum)} جنيه`;
        }

        const pendingEl = document.getElementById('pendingWithdrawals');
        if (pendingEl) {
            const pending = this.wallets.reduce((acc, w) => acc + (w.pendingCount || 0), 0);
            pendingEl.textContent = pending;
        }
    }

    /**
     * Update wallets table
     */
    updateWalletsTable() {
        const tbody = document.querySelector('#walletsTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!this.filteredWallets || this.filteredWallets.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">لا توجد محافظ</td></tr>';
            return;
        }

        this.filteredWallets.forEach((wallet, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>
                    <div class="fw-bold">${escapeHtml(wallet.userName || 'غير محدد')}</div>
                    <small class="text-muted">${escapeHtml(wallet.userPhone || '')}</small>
                </td>
                <td><span class="badge bg-info">${this.getRoleLabel(wallet.userRole)}</span></td>
                <td class="fw-bold">${this.formatCurrency(wallet.balance)} جنيه</td>
                <td class="text-success">${this.formatCurrency(wallet.totalCredits || 0)} جنيه</td>
                <td class="text-danger">${this.formatCurrency(wallet.totalDebits || 0)} جنيه</td>
                <td>${this.formatDate(wallet.lastTransactionAt)}</td>
                <td>
                    <a href="/wallet.html?userId=${wallet.userId}" class="btn btn-sm btn-outline-primary" title="عرض التفاصيل">
                        <i class="fas fa-eye"></i>
                    </a>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Get role label in Arabic
     */
    getRoleLabel(role) {
        const labels = {
            'OWNER': 'مالك',
            'ADMIN': 'مدير',
            'MERCHANT': 'تاجر',
            'COURIER': 'سائق',
            'WAREHOUSE_MANAGER': 'مدير مستودع'
        };
        return labels[role] || role || 'غير محدد';
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Role filter
        const roleFilter = document.getElementById('roleFilter');
        if (roleFilter) {
            roleFilter.addEventListener('change', () => {
                const role = roleFilter.value;
                this.filteredWallets = role === 'all'
                    ? [...this.wallets]
                    : this.wallets.filter(w => w.userRole === role);
                this.updateWalletsTable();
            });
        }

        // Search
        const search = document.getElementById('walletSearch');
        if (search) {
            search.addEventListener('input', () => {
                const query = search.value.toLowerCase();
                this.filteredWallets = this.wallets.filter(w =>
                    (w.userName || '').toLowerCase().includes(query) ||
                    (w.userPhone || '').includes(query)
                );
                this.updateWalletsTable();
            });
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
                year: 'numeric', month: 'short', day: 'numeric'
            });
        } catch {
            return 'غير محدد';
        }
    }
}

// Create global instance
window.ownerWalletsHandler = new OwnerWalletsHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/owner/wallets.html')) {
        setTimeout(() => {
            window.ownerWalletsHandler.init();
        }, 200);
    }
});
