import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-shipment-details');

/**
 * Twsela CMS - Merchant Shipment Details Page Handler
 * Handles shipment details view, status timeline, and actions
 */

class MerchantShipmentDetailsHandler {
    constructor() {
        this.shipmentId = null;
        this.shipment = null;
        this.init();
    }

    /**
     * Initialize the shipment details page
     */
    async init() {
        try {
            this.shipmentId = this.getShipmentIdFromUrl();
            if (!this.shipmentId) {
                this.showError('Ù„Ù… ÙŠØªÙ… ØªØ­Ø¯ÙŠØ¯ Ø±Ù‚Ù… Ø§Ù„Ø´Ø­Ù†Ø©');
                return;
            }

            this.setupEventListeners();
            await this.loadShipmentDetails();
        } catch (error) {
            log.error('Shipment details initialization failed:', error);
            this.showError('ÙØ´Ù„ ÙÙŠ ØªØ­Ù…ÙŠÙ„ Ø§Ù„ØªÙØ§ØµÙŠÙ„');
        }
    }

    /**
     * Get shipment ID from URL
     * @returns {string|null}
     */
    getShipmentIdFromUrl() {
        const params = new URLSearchParams(window.location.search);
        return params.get('id') || params.get('shipmentId');
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Print button
        const printBtn = document.getElementById('printBtn');
        if (printBtn) {
            printBtn.addEventListener('click', () => window.print());
        }

        // Share button
        const shareBtn = document.getElementById('shareBtn');
        if (shareBtn) {
            shareBtn.addEventListener('click', () => this.shareShipment());
        }

        // Save status button
        const saveStatusBtn = document.getElementById('saveStatusBtn');
        if (saveStatusBtn) {
            saveStatusBtn.addEventListener('click', () => this.updateStatus());
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

        // Call button
        const callBtn = document.getElementById('callBtn');
        if (callBtn) {
            callBtn.addEventListener('click', () => {
                const phone = document.getElementById('recipientPhone')?.textContent;
                if (phone && phone !== '-') window.open(`tel:${phone}`);
            });
        }

        // WhatsApp button
        const whatsappBtn = document.getElementById('whatsappBtn');
        if (whatsappBtn) {
            whatsappBtn.addEventListener('click', () => {
                const phone = document.getElementById('recipientPhone')?.textContent;
                if (phone && phone !== '-') {
                    const cleanPhone = phone.replace(/[^0-9+]/g, '');
                    window.open(`https://wa.me/${cleanPhone}`);
                }
            });
        }
    }

    /**
     * Load shipment details from API
     */
    async loadShipmentDetails() {
        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const token = sessionStorage.getItem('authToken');
            if (!token) {
                window.location.href = '/login.html';
                return;
            }

            const response = await fetch(`${apiBaseUrl}/api/shipments/${this.shipmentId}`, {
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

            if (response.status === 404) {
                this.showError('Ø§Ù„Ø´Ø­Ù†Ø© ØºÙŠØ± Ù…ÙˆØ¬ÙˆØ¯Ø©');
                return;
            }

            const result = await response.json();
            this.shipment = result.data || result;

            this.renderShipmentDetails();
            await this.loadStatusHistory();
        } catch (error) {
            log.error('Failed to load shipment details:', error);
            this.showError('ÙØ´Ù„ ÙÙŠ ØªØ­Ù…ÙŠÙ„ ØªÙØ§ØµÙŠÙ„ Ø§Ù„Ø´Ø­Ù†Ø©');
        }
    }

    /**
     * Render shipment details into the page
     */
    renderShipmentDetails() {
        const s = this.shipment;
        if (!s) return;

        // Page title
        this.setText('pageTitle', `ØªÙØ§ØµÙŠÙ„ Ø§Ù„Ø´Ø­Ù†Ø© #${s.trackingNumber || s.id}`);
        this.setText('pageSubtitle', `Ø§Ù„Ø­Ø§Ù„Ø©: ${this.getStatusLabel(s.status)}`);

        // Tracking number and status
        this.setText('trackingNumber', s.trackingNumber || '-');
        this.setStatusBadge('currentStatus', s.status);

        // Recipient info
        this.setText('recipientName', s.recipientDetails?.name || s.recipientName || '-');
        this.setText('recipientPhone', s.recipientDetails?.phone || s.recipientPhone || '-');
        this.setText('recipientAddress', s.recipientDetails?.address || s.recipientAddress || '-');

        // Package info
        this.setText('packageDescription', s.packageDescription || s.recipientNotes || '-');
        this.setText('packageWeight', s.packageWeight ? `${s.packageWeight} ÙƒØ¬Ù…` : '-');

        // Financial info
        this.setText('itemValue', s.itemValue != null ? `${s.itemValue} Ø¬.Ù…` : '-');
        this.setText('codAmount', s.codAmount != null ? `${s.codAmount} Ø¬.Ù…` : '-');
        this.setText('deliveryFee', s.deliveryFee != null ? `${s.deliveryFee} Ø¬.Ù…` : '-');
        this.setText('shippingFeePaidBy', s.shippingFeePaidBy === 'MERCHANT' ? 'Ø§Ù„ØªØ§Ø¬Ø±' : 'Ø§Ù„Ù…Ø³ØªÙ„Ù…');

        // Zone info
        this.setText('zoneName', s.zone?.name || '-');

        // Dates
        this.setText('createdAt', this.formatDate(s.createdAt));
        this.setText('updatedAt', this.formatDate(s.updatedAt));
    }

    /**
     * Load status history timeline
     */
    async loadStatusHistory() {
        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const token = sessionStorage.getItem('authToken');

            const response = await fetch(`${apiBaseUrl}/api/shipments/${this.shipmentId}/history`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) return;

            const result = await response.json();
            const history = result.data || result || [];
            this.renderTimeline(history);
        } catch (error) {
            // Timeline is optional, don't break the page
            log.warn('Failed to load status history:', error);
        }
    }

    /**
     * Render status timeline
     * @param {Array} history
     */
    renderTimeline(history) {
        const timelineEl = document.getElementById('statusTimeline');
        if (!timelineEl || !Array.isArray(history) || history.length === 0) return;

        timelineEl.innerHTML = history.map((entry, index) => `
            <div class="timeline-item ${index === 0 ? 'active' : ''}">
                <div class="timeline-marker">
                    <i class="fas ${this.getStatusIcon(entry.status)}"></i>
                </div>
                <div class="timeline-content">
                    <h6>${this.getStatusLabel(entry.status)}</h6>
                    <p class="text-muted mb-0">${entry.notes || ''}</p>
                    <small class="text-muted">${this.formatDateTime(entry.createdAt || entry.timestamp)}</small>
                </div>
            </div>
        `).join('');
    }

    /**
     * Update shipment status
     */
    async updateStatus() {
        const newStatus = document.getElementById('newStatus')?.value;
        const notes = document.getElementById('statusNotes')?.value?.trim() || '';

        if (!newStatus) {
            alert('ÙŠØ±Ø¬Ù‰ Ø§Ø®ØªÙŠØ§Ø± Ø§Ù„Ø­Ø§Ù„Ø© Ø§Ù„Ø¬Ø¯ÙŠØ¯Ø©');
            return;
        }

        try {
            const apiBaseUrl = this.getApiBaseUrl();
            const token = sessionStorage.getItem('authToken');

            const response = await fetch(`${apiBaseUrl}/api/shipments/${this.shipmentId}/status`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: newStatus, notes })
            });

            const result = await response.json();

            if (response.ok && result.success) {
                // Close modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('updateStatusModal'));
                if (modal) modal.hide();

                // Reload details
                await this.loadShipmentDetails();
                this.showNotification('ØªÙ… ØªØ­Ø¯ÙŠØ« Ø§Ù„Ø­Ø§Ù„Ø© Ø¨Ù†Ø¬Ø§Ø­', 'success');
            } else {
                this.showNotification(result.message || 'ÙØ´Ù„ ØªØ­Ø¯ÙŠØ« Ø§Ù„Ø­Ø§Ù„Ø©', 'danger');
            }
        } catch (error) {
            log.error('Status update failed:', error);
            this.showNotification('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ ØªØ­Ø¯ÙŠØ« Ø§Ù„Ø­Ø§Ù„Ø©', 'danger');
        }
    }

    /**
     * Share shipment tracking link
     */
    shareShipment() {
        const trackingNumber = this.shipment?.trackingNumber;
        if (!trackingNumber) return;

        const trackUrl = `${window.location.origin}/index.html?track=${trackingNumber}`;

        if (navigator.clipboard) {
            navigator.clipboard.writeText(trackUrl).then(() => {
                this.showNotification('ØªÙ… Ù†Ø³Ø® Ø±Ø§Ø¨Ø· Ø§Ù„ØªØªØ¨Ø¹', 'success');
            }).catch(() => {
                prompt('Ø§Ù†Ø³Ø® Ø±Ø§Ø¨Ø· Ø§Ù„ØªØªØ¨Ø¹:', trackUrl);
            });
        } else {
            prompt('Ø§Ù†Ø³Ø® Ø±Ø§Ø¨Ø· Ø§Ù„ØªØªØ¨Ø¹:', trackUrl);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Set text content of element by ID
     */
    setText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text || '-';
    }

    /**
     * Set status badge
     */
    setStatusBadge(id, status) {
        const el = document.getElementById(id);
        if (!el) return;
        const statusName = typeof status === 'string' ? status : (status?.name || 'UNKNOWN');
        const label = this.getStatusLabel(status);
        const color = this.getStatusColor(statusName);
        el.innerHTML = `<span class="badge bg-${color}">${label}</span>`;
    }

    /**
     * Get human-readable status label
     */
    getStatusLabel(status) {
        const name = typeof status === 'string' ? status : (status?.name || 'UNKNOWN');
        const labels = {
            'PENDING': 'ÙÙŠ Ø§Ù„Ø§Ù†ØªØ¸Ø§Ø±',
            'PICKED_UP': 'ØªÙ… Ø§Ù„Ø§Ø³ØªÙ„Ø§Ù…',
            'IN_TRANSIT': 'ÙÙŠ Ø§Ù„Ø·Ø±ÙŠÙ‚',
            'OUT_FOR_DELIVERY': 'Ø®Ø§Ø±Ø¬ Ù„Ù„ØªÙˆØµÙŠÙ„',
            'DELIVERED': 'ØªÙ… Ø§Ù„ØªØ³Ù„ÙŠÙ…',
            'CANCELLED': 'Ù…Ù„ØºØ§Ø©',
            'RETURNED_TO_ORIGIN': 'Ù…Ø±ØªØ¬Ø¹'
        };
        return labels[name] || status?.nameAr || name;
    }

    /**
     * Get Bootstrap color for status
     */
    getStatusColor(statusName) {
        const colors = {
            'PENDING': 'warning', 'PICKED_UP': 'info', 'IN_TRANSIT': 'primary',
            'OUT_FOR_DELIVERY': 'info', 'DELIVERED': 'success',
            'CANCELLED': 'danger', 'RETURNED_TO_ORIGIN': 'secondary'
        };
        return colors[statusName] || 'secondary';
    }

    /**
     * Get icon for status
     */
    getStatusIcon(status) {
        const name = typeof status === 'string' ? status : (status?.name || '');
        const icons = {
            'PENDING': 'fa-clock', 'PICKED_UP': 'fa-hand-holding-box',
            'IN_TRANSIT': 'fa-truck', 'OUT_FOR_DELIVERY': 'fa-shipping-fast',
            'DELIVERED': 'fa-check-circle', 'CANCELLED': 'fa-times-circle',
            'RETURNED_TO_ORIGIN': 'fa-undo'
        };
        return icons[name] || 'fa-info-circle';
    }

    /**
     * Format date
     */
    formatDate(dateStr) {
        if (!dateStr) return '-';
        try {
            return new Date(dateStr).toLocaleDateString('ar-EG', {
                year: 'numeric', month: 'long', day: 'numeric'
            });
        } catch { return dateStr; }
    }

    /**
     * Format date and time
     */
    formatDateTime(dateStr) {
        if (!dateStr) return '-';
        try {
            return new Date(dateStr).toLocaleString('ar-EG', {
                year: 'numeric', month: 'short', day: 'numeric',
                hour: '2-digit', minute: '2-digit'
            });
        } catch { return dateStr; }
    }

    /**
     * Show error state on page
     */
    showError(message) {
        const mainContent = document.querySelector('.main-content');
        if (mainContent) {
            mainContent.innerHTML = `
                <div class="text-center py-5">
                    <i class="fas fa-exclamation-triangle fa-3x text-danger mb-3"></i>
                    <h4>${message}</h4>
                    <a href="shipments.html" class="btn btn-primary mt-3">Ø§Ù„Ø¹ÙˆØ¯Ø© Ù„Ù‚Ø§Ø¦Ù…Ø© Ø§Ù„Ø´Ø­Ù†Ø§Øª</a>
                </div>
            `;
        }
    }

    /**
     * Show notification
     */
    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show`;
        notification.style.cssText = 'position: fixed; top: 20px; left: 50%; transform: translateX(-50%); z-index: 9999; min-width: 300px;';
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        document.body.appendChild(notification);
        setTimeout(() => { if (notification.parentNode) notification.remove(); }, 4000);
    }

    /**
     * Get API base URL
     */
    getApiBaseUrl() {
        return window.getApiBaseUrl();
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new MerchantShipmentDetailsHandler();
});
