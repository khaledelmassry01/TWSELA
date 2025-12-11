/**
 * Twsela CMS - Owner Payouts Handler
 * Handles payout management including approval, rejection, and financial reporting
 * Follows DRY principle with unified financial management logic
 */

class OwnerPayoutsHandler {
    constructor() {
        this.payouts = [];
        this.currentPage = 1;
        this.itemsPerPage = 10;
        this.totalPages = 0;
        this.filters = {
            search: '',
            status: 'ALL',
            dateFrom: '',
            dateTo: '',
            amountFrom: '',
            amountTo: ''
        };
        this.init();
    }

    /**
     * Initialize the handler
     */
    async init() {
        try {
            this.setupEventListeners();
            await this.loadPayouts();
            this.initializeDataTable();
            this.loadSummaryStats();
        } catch (error) {

            
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('payoutSearch');
        if (searchInput) {
            searchInput.addEventListener('input', this.debounce((e) => {
                this.filters.search = e.target.value;
                this.loadPayouts();
            }, 500));
        }

        // Status filter
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', (e) => {
                this.filters.status = e.target.value;
                this.loadPayouts();
            });
        }

        // Date filters
        const dateFromInput = document.getElementById('dateFrom');
        if (dateFromInput) {
            dateFromInput.addEventListener('change', (e) => {
                this.filters.dateFrom = e.target.value;
                this.loadPayouts();
            });
        }

        const dateToInput = document.getElementById('dateTo');
        if (dateToInput) {
            dateToInput.addEventListener('change', (e) => {
                this.filters.dateTo = e.target.value;
                this.loadPayouts();
            });
        }

        // Amount filters
        const amountFromInput = document.getElementById('amountFrom');
        if (amountFromInput) {
            amountFromInput.addEventListener('input', this.debounce((e) => {
                this.filters.amountFrom = e.target.value;
                this.loadPayouts();
            }, 500));
        }

        const amountToInput = document.getElementById('amountTo');
        if (amountToInput) {
            amountToInput.addEventListener('input', this.debounce((e) => {
                this.filters.amountTo = e.target.value;
                this.loadPayouts();
            }, 500));
        }

        // Bulk actions
        const selectAllCheckbox = document.getElementById('selectAllPayouts');
        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', (e) => {
                this.toggleSelectAll(e.target.checked);
            });
        }

        const bulkApproveBtn = document.getElementById('bulkApproveBtn');
        if (bulkApproveBtn) {
            bulkApproveBtn.addEventListener('click', () => {
                this.bulkApprovePayouts();
            });
        }

        const bulkRejectBtn = document.getElementById('bulkRejectBtn');
        if (bulkRejectBtn) {
            bulkRejectBtn.addEventListener('click', () => {
                this.bulkRejectPayouts();
            });
        }

        // Export functionality
        const exportBtn = document.getElementById('exportPayoutsBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => {
                this.exportPayouts();
            });
        }

        // Pagination
        this.setupPaginationListeners();
    }

    /**
     * Setup pagination event listeners
     */
    setupPaginationListeners() {
        const paginationContainer = document.getElementById('payoutsPagination');
        if (!paginationContainer) return;

        paginationContainer.addEventListener('click', (e) => {
            if (e.target.matches('.page-btn')) {
                const page = parseInt(e.target.dataset.page);
                if (page && page !== this.currentPage) {
                    this.goToPage(page);
                }
            }
        });
    }

    /**
     * Load payouts from API
     */
    async loadPayouts() {
        try {
            GlobalUIHandler.showLoading();
            
            const params = {
                page: this.currentPage,
                limit: this.itemsPerPage,
                ...this.filters
            };

            const response = await apiService.getPayouts(params);
            
            if (response.success) {
                this.payouts = response.data || [];
                this.totalPages = response.totalPages || 1;
                this.renderPayoutsTable();
                this.updatePagination();
                this.updateBulkActions();
            } else {
                throw new Error(response.message || 'Failed to load payouts');
            }
        } catch (error) {

            
        } finally {
            GlobalUIHandler.hideLoading();
        }
    }

    /**
     * Load summary statistics
     */
    async loadSummaryStats() {
        try {
            const response = await apiService.getFinancialReports({
                type: 'payouts_summary',
                ...this.filters
            });
            
            if (response.success) {
                this.updateSummaryStats(response.data);
            }
        } catch (error) {

        }
    }

    /**
     * Update summary statistics display
     */
    updateSummaryStats(stats) {
        // Total pending amount
        const totalPendingElement = document.getElementById('totalPendingAmount');
        if (totalPendingElement && stats.totalPending) {
            totalPendingElement.textContent = DataUtils.formatCurrency(stats.totalPending);
        }

        // Total approved amount
        const totalApprovedElement = document.getElementById('totalApprovedAmount');
        if (totalApprovedElement && stats.totalApproved) {
            totalApprovedElement.textContent = DataUtils.formatCurrency(stats.totalApproved);
        }

        // Total rejected amount
        const totalRejectedElement = document.getElementById('totalRejectedAmount');
        if (totalRejectedElement && stats.totalRejected) {
            totalRejectedElement.textContent = DataUtils.formatCurrency(stats.totalRejected);
        }

        // Pending count
        const pendingCountElement = document.getElementById('pendingCount');
        if (pendingCountElement && stats.pendingCount) {
            pendingCountElement.textContent = stats.pendingCount;
        }
    }

    /**
     * Render payouts table
     */
    renderPayoutsTable() {
        const tbody = document.getElementById('payoutsTableBody');
        if (!tbody) return;

        if (this.payouts.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center py-4">
                        <div class="text-muted">
                            <i class="fas fa-inbox fa-2x mb-2"></i>
                            <p>لا توجد مدفوعات</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = this.payouts.map(payout => this.createPayoutRow(payout)).join('');
        
        // Add event listeners for action buttons
        this.setupPayoutEventListeners();
    }

    /**
     * Setup event listeners for payout action buttons
     */
    setupPayoutEventListeners() {
        // View buttons
        const viewButtons = document.querySelectorAll('.action-btn.view[data-payout-id]');
        viewButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const payoutId = parseInt(button.dataset.payoutId);
                this.viewPayout(payoutId);
            });
        });

        // Approve buttons
        const approveButtons = document.querySelectorAll('.action-btn.edit[data-action="approve"]');
        approveButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const payoutId = parseInt(button.dataset.payoutId);
                this.approvePayout(payoutId);
            });
        });

        // Reject buttons
        const rejectButtons = document.querySelectorAll('.action-btn.delete[data-action="reject"]');
        rejectButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const payoutId = parseInt(button.dataset.payoutId);
                this.rejectPayout(payoutId);
            });
        });
    }

    /**
     * Create payout table row - Using GlobalUIHandler
     */
    createPayoutRow(payout) {
        return GlobalUIHandler.createTableRow(payout, 'payout');
    }

    /**
     * Get status badge HTML - Using unified SharedDataUtils
     */
    getStatusBadge(status) {
        return SharedDataUtils.createStatusBadge(status);
    }

    /**
     * Update pagination - Using GlobalUIHandler
     */
    updatePagination() {
        GlobalUIHandler.updatePagination('payoutsPagination', this.currentPage, this.totalPages);
    }

    /**
     * Go to specific page
     */
    goToPage(page) {
        if (page >= 1 && page <= this.totalPages) {
            this.currentPage = page;
            this.loadPayouts();
        }
    }

    /**
     * Toggle select all payouts - Using GlobalUIHandler
     */
    toggleSelectAll(checked) {
        GlobalUIHandler.toggleSelectAll(checked, '.payout-checkbox');
        this.updateBulkActions();
    }

    /**
     * Update bulk actions visibility - Using GlobalUIHandler
     */
    updateBulkActions() {
        GlobalUIHandler.updateBulkActions('bulkActionsContainer', '.payout-checkbox');
    }

    /**
     * View payout details - Using GlobalUIHandler
     */
    async viewPayout(payoutId) {
        try {
            GlobalUIHandler.showLoading();
            
            const response = await apiService.getPayout(payoutId);
            if (response.success) {
                const modalHtml = GlobalUIHandler.createModal(response.data, 'payout', 'viewPayoutModal');
                GlobalUIHandler.showModalWithContent(modalHtml, 'viewPayoutModal');
            } else {
                throw new Error(response.message || 'Failed to load payout');
            }
        } catch (error) {

            
        } finally {
            GlobalUIHandler.hideLoading();
        }
    }

    /**
     * Approve payout
     */
    async approvePayout(payoutId) {
        const payout = this.payouts.find(p => p.id === payoutId);
        if (!payout) return;

        if (confirm(`هل أنت متأكد من الموافقة على المدفوعات بقيمة ${SharedDataUtils.formatCurrency(payout.amount)}؟`)) {
            try {
                GlobalUIHandler.showLoading();
                
                const response = await apiService.approvePayout(payoutId);
                if (response.success) {
                    NotificationService.success('تم الموافقة على المدفوعات بنجاح');
                    this.loadPayouts();
                    this.loadSummaryStats();
                } else {
                    throw new Error(response.message || 'Failed to approve payout');
                }
            } catch (error) {

                
            } finally {
                GlobalUIHandler.hideLoading();
            }
        }
    }

    /**
     * Reject payout
     */
    async rejectPayout(payoutId) {
        const payout = this.payouts.find(p => p.id === payoutId);
        if (!payout) return;

        const reason = prompt('يرجى إدخال سبب الرفض:');
        if (reason && reason.trim()) {
            try {
                GlobalUIHandler.showLoading();
                
                const response = await apiService.rejectPayout(payoutId, reason.trim());
                if (response.success) {
                    NotificationService.success('تم رفض المدفوعات بنجاح');
                    this.loadPayouts();
                    this.loadSummaryStats();
                } else {
                    throw new Error(response.message || 'Failed to reject payout');
                }
            } catch (error) {

                
            } finally {
                GlobalUIHandler.hideLoading();
            }
        }
    }

    /**
     * Bulk approve payouts
     */
    async bulkApprovePayouts() {
        const selectedIds = this.getSelectedPayoutIds();
        if (selectedIds.length === 0) {
            NotificationService.warning('يرجى اختيار المدفوعات المراد الموافقة عليها');
            return;
        }

        if (confirm(`هل أنت متأكد من الموافقة على ${selectedIds.length} مدفوعات؟`)) {
            try {
                GlobalUIHandler.showLoading();
                
                const promises = selectedIds.map(id => apiService.approvePayout(id));
                const results = await Promise.allSettled(promises);
                
                const successful = results.filter(r => r.status === 'fulfilled' && r.value.success).length;
                const failed = results.length - successful;
                
                if (successful > 0) {
                    NotificationService.success(`تم الموافقة على ${successful} مدفوعات بنجاح`);
                }
                if (failed > 0) {
                    NotificationService.warning(`فشل في الموافقة على ${failed} مدفوعات`);
                }
                
                this.loadPayouts();
                this.loadSummaryStats();
            } catch (error) {

                
            } finally {
                GlobalUIHandler.hideLoading();
            }
        }
    }

    /**
     * Bulk reject payouts
     */
    async bulkRejectPayouts() {
        const selectedIds = this.getSelectedPayoutIds();
        if (selectedIds.length === 0) {
            NotificationService.warning('يرجى اختيار المدفوعات المراد رفضها');
            return;
        }

        const reason = prompt('يرجى إدخال سبب الرفض (سيتم تطبيقه على جميع المدفوعات المختارة):');
        if (reason && reason.trim()) {
            try {
                GlobalUIHandler.showLoading();
                
                const promises = selectedIds.map(id => apiService.rejectPayout(id, reason.trim()));
                const results = await Promise.allSettled(promises);
                
                const successful = results.filter(r => r.status === 'fulfilled' && r.value.success).length;
                const failed = results.length - successful;
                
                if (successful > 0) {
                    NotificationService.success(`تم رفض ${successful} مدفوعات بنجاح`);
                }
                if (failed > 0) {
                    NotificationService.warning(`فشل في رفض ${failed} مدفوعات`);
                }
                
                this.loadPayouts();
                this.loadSummaryStats();
            } catch (error) {

                
            } finally {
                GlobalUIHandler.hideLoading();
            }
        }
    }

    /**
     * Get selected payout IDs - Using GlobalUIHandler
     */
    getSelectedPayoutIds() {
        return GlobalUIHandler.getSelectedValues('.payout-checkbox');
    }

    /**
     * Setup event listeners for modal buttons - Using GlobalUIHandler
     */
    setupModalEventListeners(modalId) {
        // Use event delegation for modal action buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.modal-action-btn')) {
                const button = e.target.closest('.modal-action-btn');
                const payoutId = parseInt(button.dataset.payoutId);
                const action = button.dataset.action;
                
                switch (action) {
                    case 'approve':
                        this.approvePayout(payoutId);
                        break;
                    case 'reject':
                        this.rejectPayout(payoutId);
                        break;
                }
            }
        });
    }

    /**
     * Export payouts data
     */
    async exportPayouts() {
        try {
            GlobalUIHandler.showLoading();
            
            const response = await apiService.exportData('payouts', this.filters);
            if (response.success) {
                // Create download link
                const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `payouts_${new Date().toISOString().split('T')[0]}.xlsx`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
                
                NotificationService.success('تم تصدير البيانات بنجاح');
            } else {
                throw new Error(response.message || 'Failed to export data');
            }
        } catch (error) {

            
        } finally {
            GlobalUIHandler.hideLoading();
        }
    }

    /**
     * Initialize data table
     */
    initializeDataTable() {
        // Add event listeners for individual checkboxes
        document.addEventListener('change', (e) => {
            if (e.target.matches('.payout-checkbox')) {
                this.updateBulkActions();
            }
        });
    }

    /**
     * Debounce function
     */
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Only initialize on payouts page
    if (window.location.pathname.includes('payouts')) {
        window.ownerPayoutsHandler = new OwnerPayoutsHandler();
    }
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = OwnerPayoutsHandler;
}
