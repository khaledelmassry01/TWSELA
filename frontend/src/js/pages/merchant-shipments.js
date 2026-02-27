import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-shipments');

/**
 * Twsela CMS - Merchant Shipments Page Handler
 * Handles shipment listing, search, filtering, and pagination
 */

class MerchantShipmentsHandler {
    constructor() {
        this.shipments = [];
        this.currentPage = 1;
        this.pageSize = 10;
        this.totalPages = 0;
        this.filters = {
            status: '',
            dateRange: '',
            search: ''
        };
        this.init();
    }

    /**
     * Initialize the shipments page
     */
    async init() {
        try {
            this.setupEventListeners();
            await this.loadShipments();
        } catch (error) {
            log.error('Shipments page initialization failed:', error);
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Filter controls
        const applyFiltersBtn = document.getElementById('applyFiltersBtn');
        if (applyFiltersBtn) {
            applyFiltersBtn.addEventListener('click', () => this.applyFilters());
        }

        const refreshBtn = document.getElementById('refreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => this.loadShipments());
        }

        const exportBtn = document.getElementById('exportBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => this.exportShipments());
        }

        // Search on Enter key
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.applyFilters();
            });
        }

        // Logout link
        const logoutLink = document.getElementById('logoutLink');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                sessionStorage.removeItem('authToken');
                sessionStorage.removeItem('userData');
                window.location.href = '/login.html';
            });
        }
    }

    /**
     * Apply current filters and reload
     */
    applyFilters() {
        this.filters.status = document.getElementById('statusFilter')?.value || '';
        this.filters.dateRange = document.getElementById('dateFilter')?.value || '';
        this.filters.search = document.getElementById('searchInput')?.value?.trim() || '';
        this.currentPage = 1;
        this.loadShipments();
    }

    /**
     * Load shipments from API
     */
    async loadShipments() {
        try {
            this.showTableLoading(true);

            const apiBaseUrl = this.getApiBaseUrl();
            const token = sessionStorage.getItem('authToken');
            if (!token) {
                window.location.href = '/login.html';
                return;
            }

            // Build query parameters
            const params = new URLSearchParams();
            params.append('page', this.currentPage - 1); // Spring uses 0-based
            params.append('size', this.pageSize);
            if (this.filters.status) params.append('status', this.filters.status);
            if (this.filters.search) params.append('search', this.filters.search);

            const response = await fetch(`${apiBaseUrl}/api/shipments?${params.toString()}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });

            if (response.status === 401) {
                sessionStorage.removeItem('authToken');
                window.location.href = '/login.html';
                return;
            }

            const result = await response.json();

            if (result.success && result.data) {
                this.shipments = result.data.content || result.data || [];
                this.totalPages = result.data.totalPages || Math.ceil((result.data.totalElements || this.shipments.length) / this.pageSize);
                this.renderShipments();
                this.renderPagination();
            } else if (Array.isArray(result)) {
                this.shipments = result;
                this.totalPages = 1;
                this.renderShipments();
                this.renderPagination();
            } else {
                this.showEmptyState();
            }
        } catch (error) {
            log.error('Failed to load shipments:', error);
            this.showError('ÙØ´Ù„ ÙÙŠ ØªØ­Ù…ÙŠÙ„ Ø§Ù„Ø´Ø­Ù†Ø§Øª. ÙŠØ±Ø¬Ù‰ Ø§Ù„Ù…Ø­Ø§ÙˆÙ„Ø© Ù…Ø±Ø© Ø£Ø®Ø±Ù‰.');
        } finally {
            this.showTableLoading(false);
        }
    }

    /**
     * Render shipments table
     */
    renderShipments() {
        const tbody = document.querySelector('#shipmentsTable tbody');
        if (!tbody) return;

        if (!this.shipments || this.shipments.length === 0) {
            this.showEmptyState();
            return;
        }

        tbody.innerHTML = this.shipments.map(shipment => `
            <tr>
                <td><a href="shipment-details.html?id=${shipment.id}" class="text-primary text-decoration-none">${this.escapeHtml(shipment.trackingNumber || '-')}</a></td>
                <td>${this.escapeHtml(shipment.recipientDetails?.name || shipment.recipientName || '-')}</td>
                <td>${this.escapeHtml(shipment.recipientDetails?.phone || shipment.recipientPhone || '-')}</td>
                <td>${this.getStatusBadge(shipment.status)}</td>
                <td>${this.escapeHtml(shipment.zone?.name || '-')}</td>
                <td>${shipment.deliveryFee != null ? shipment.deliveryFee + ' Ø¬.Ù…' : '-'}</td>
                <td>${this.formatDate(shipment.createdAt)}</td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <a href="shipment-details.html?id=${shipment.id}" class="btn btn-outline-primary" title="ØªÙØ§ØµÙŠÙ„">
                            <i class="fas fa-eye"></i>
                        </a>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    /**
     * Render pagination controls
     */
    renderPagination() {
        const paginationEl = document.getElementById('pagination');
        if (!paginationEl || this.totalPages <= 1) {
            if (paginationEl) paginationEl.innerHTML = '';
            return;
        }

        let html = '';

        // Previous button
        html += `<li class="page-item ${this.currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${this.currentPage - 1}">Ø§Ù„Ø³Ø§Ø¨Ù‚</a>
        </li>`;

        // Page numbers
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.totalPages, this.currentPage + 2);
        for (let i = startPage; i <= endPage; i++) {
            html += `<li class="page-item ${i === this.currentPage ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
            </li>`;
        }

        // Next button
        html += `<li class="page-item ${this.currentPage === this.totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${this.currentPage + 1}">Ø§Ù„ØªØ§Ù„ÙŠ</a>
        </li>`;

        paginationEl.innerHTML = html;

        // Add click events
        paginationEl.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(link.dataset.page);
                if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
                    this.currentPage = page;
                    this.loadShipments();
                }
            });
        });
    }

    /**
     * Show empty state
     */
    showEmptyState() {
        const tbody = document.querySelector('#shipmentsTable tbody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center py-5">
                        <i class="fas fa-box-open fa-3x text-muted mb-3 d-block"></i>
                        <p class="text-muted">Ù„Ø§ ØªÙˆØ¬Ø¯ Ø´Ø­Ù†Ø§Øª</p>
                        <a href="create-shipment.html" class="btn btn-primary btn-sm">Ø¥Ù†Ø´Ø§Ø¡ Ø´Ø­Ù†Ø© Ø¬Ø¯ÙŠØ¯Ø©</a>
                    </td>
                </tr>
            `;
        }
    }

    /**
     * Export shipments (CSV)
     */
    exportShipments() {
        if (!this.shipments || this.shipments.length === 0) {
            alert('Ù„Ø§ ØªÙˆØ¬Ø¯ Ø¨ÙŠØ§Ù†Ø§Øª Ù„Ù„ØªØµØ¯ÙŠØ±');
            return;
        }

        const headers = ['Ø±Ù‚Ù… Ø§Ù„ØªØªØ¨Ø¹', 'Ø§Ù„Ù…Ø³ØªÙ„Ù…', 'Ø§Ù„Ù‡Ø§ØªÙ', 'Ø§Ù„Ø­Ø§Ù„Ø©', 'Ø§Ù„Ù…Ù†Ø·Ù‚Ø©', 'Ø§Ù„Ø±Ø³ÙˆÙ…', 'Ø§Ù„ØªØ§Ø±ÙŠØ®'];
        const rows = this.shipments.map(s => [
            s.trackingNumber || '',
            s.recipientDetails?.name || '',
            s.recipientDetails?.phone || '',
            s.status?.nameAr || s.status?.name || '',
            s.zone?.name || '',
            s.deliveryFee || '',
            this.formatDate(s.createdAt)
        ]);

        const csvContent = '\uFEFF' + [headers, ...rows].map(r => r.join(',')).join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `shipments_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
    }

    /**
     * Get status badge HTML
     * @param {Object|string} status
     * @returns {string}
     */
    getStatusBadge(status) {
        const statusName = typeof status === 'string' ? status : (status?.name || 'UNKNOWN');
        const statusMap = {
            'PENDING': { label: 'ÙÙŠ Ø§Ù„Ø§Ù†ØªØ¸Ø§Ø±', color: 'warning' },
            'PICKED_UP': { label: 'ØªÙ… Ø§Ù„Ø§Ø³ØªÙ„Ø§Ù…', color: 'info' },
            'IN_TRANSIT': { label: 'ÙÙŠ Ø§Ù„Ø·Ø±ÙŠÙ‚', color: 'primary' },
            'DELIVERED': { label: 'ØªÙ… Ø§Ù„ØªØ³Ù„ÙŠÙ…', color: 'success' },
            'CANCELLED': { label: 'Ù…Ù„ØºØ§Ø©', color: 'danger' },
            'RETURNED_TO_ORIGIN': { label: 'Ù…Ø±ØªØ¬Ø¹', color: 'secondary' }
        };
        const info = statusMap[statusName] || { label: statusName, color: 'secondary' };
        return `<span class="badge bg-${info.color}">${info.label}</span>`;
    }

    /**
     * Format date string
     * @param {string} dateStr
     * @returns {string}
     */
    formatDate(dateStr) {
        if (!dateStr) return '-';
        try {
            return new Date(dateStr).toLocaleDateString('ar-EG', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch {
            return dateStr;
        }
    }

    /**
     * Escape HTML to prevent XSS
     * @param {string} str
     * @returns {string}
     */
    escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    /**
     * Show/hide table loading state
     * @param {boolean} loading
     */
    showTableLoading(loading) {
        const tbody = document.querySelector('#shipmentsTable tbody');
        if (!tbody) return;
        if (loading) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center py-5">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Ø¬Ø§Ø±ÙŠ Ø§Ù„ØªØ­Ù…ÙŠÙ„...</span>
                        </div>
                        <p class="mt-2 text-muted">Ø¬Ø§Ø±ÙŠ ØªØ­Ù…ÙŠÙ„ Ø§Ù„Ø´Ø­Ù†Ø§Øª...</p>
                    </td>
                </tr>
            `;
        }
    }

    /**
     * Show error in table
     * @param {string} message
     */
    showError(message) {
        const tbody = document.querySelector('#shipmentsTable tbody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center py-5 text-danger">
                        <i class="fas fa-exclamation-triangle fa-3x mb-3 d-block"></i>
                        <p>${message}</p>
                        <button class="btn btn-outline-primary btn-sm" onclick="window.merchantShipments.loadShipments()">
                            Ø¥Ø¹Ø§Ø¯Ø© Ø§Ù„Ù…Ø­Ø§ÙˆÙ„Ø©
                        </button>
                    </td>
                </tr>
            `;
        }
    }

    /**
     * Get API base URL
     * @returns {string}
     */
    getApiBaseUrl() {
        return window.getApiBaseUrl();
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.merchantShipments = new MerchantShipmentsHandler();
});
