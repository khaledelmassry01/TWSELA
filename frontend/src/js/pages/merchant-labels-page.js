import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-labels-page');

/**
 * Twsela CMS - Merchant Labels Page Handler
 * Handles label generation, barcode/QR download for shipments
 */

class MerchantLabelsHandler extends BasePageHandler {
    constructor() {
        super('Merchant Labels');
        this.recentDownloads = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            this.updateBulkCount();
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantLabels');
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Single shipment buttons
        const downloadLabelBtn = document.getElementById('downloadLabelBtn');
        if (downloadLabelBtn) {
            downloadLabelBtn.addEventListener('click', () => this.downloadLabel());
        }

        const downloadBarcodeBtn = document.getElementById('downloadBarcodeBtn');
        if (downloadBarcodeBtn) {
            downloadBarcodeBtn.addEventListener('click', () => this.downloadBarcode());
        }

        const downloadQRBtn = document.getElementById('downloadQRBtn');
        if (downloadQRBtn) {
            downloadQRBtn.addEventListener('click', () => this.downloadQR());
        }

        // Bulk labels
        const downloadBulkBtn = document.getElementById('downloadBulkLabelsBtn');
        if (downloadBulkBtn) {
            downloadBulkBtn.addEventListener('click', () => this.downloadBulkLabels());
        }

        // Update bulk count on input
        const bulkTextarea = document.getElementById('bulkShipmentIds');
        if (bulkTextarea) {
            bulkTextarea.addEventListener('input', () => this.updateBulkCount());
        }
    }

    /**
     * Get single shipment ID from input
     */
    getSingleShipmentId() {
        const input = document.getElementById('singleShipmentId');
        const value = input ? input.value.trim() : '';
        if (!value) {
            this.services.notification.warning('يرجى إدخال رقم الشحنة');
            return null;
        }
        return value;
    }

    /**
     * Download shipment label (PDF)
     */
    async downloadLabel() {
        const shipmentId = this.getSingleShipmentId();
        if (!shipmentId) return;

        try {
            UIUtils.showLoading();
            const blob = await this.services.api.downloadShipmentLabel(shipmentId);
            this.triggerDownload(blob, `label-${shipmentId}.pdf`, 'application/pdf');
            this.addRecentDownload('ملصق', shipmentId);
            this.services.notification.success('تم تحميل الملصق بنجاح');
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantLabels.downloadLabel');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Download shipment barcode
     */
    async downloadBarcode() {
        const shipmentId = this.getSingleShipmentId();
        if (!shipmentId) return;

        try {
            UIUtils.showLoading();
            const blob = await this.services.api.getShipmentBarcode(shipmentId);
            this.triggerDownload(blob, `barcode-${shipmentId}.png`, 'image/png');
            this.showPreview(blob, 'image');
            this.addRecentDownload('باركود', shipmentId);
            this.services.notification.success('تم تحميل الباركود بنجاح');
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantLabels.downloadBarcode');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Download shipment QR code
     */
    async downloadQR() {
        const shipmentId = this.getSingleShipmentId();
        if (!shipmentId) return;

        try {
            UIUtils.showLoading();
            const blob = await this.services.api.getShipmentQRCode(shipmentId);
            this.triggerDownload(blob, `qr-${shipmentId}.png`, 'image/png');
            this.showPreview(blob, 'image');
            this.addRecentDownload('QR كود', shipmentId);
            this.services.notification.success('تم تحميل QR كود بنجاح');
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantLabels.downloadQR');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Download bulk labels
     */
    async downloadBulkLabels() {
        const ids = this.parseBulkIds();
        if (ids.length === 0) {
            this.services.notification.warning('يرجى إدخال أرقام الشحنات');
            return;
        }

        try {
            UIUtils.showLoading();
            const blob = await this.services.api.downloadBulkLabels(ids);
            this.triggerDownload(blob, `bulk-labels-${Date.now()}.pdf`, 'application/pdf');
            this.addRecentDownload(`ملصقات بالجملة (${ids.length})`, ids.join(', '));
            this.services.notification.success(`تم تحميل ${ids.length} ملصق بنجاح`);
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantLabels.downloadBulkLabels');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Parse bulk shipment IDs from textarea
     */
    parseBulkIds() {
        const textarea = document.getElementById('bulkShipmentIds');
        if (!textarea) return [];
        return textarea.value
            .split('\n')
            .map(line => line.trim())
            .filter(line => line.length > 0);
    }

    /**
     * Update the bulk count display
     */
    updateBulkCount() {
        const count = this.parseBulkIds().length;
        const countEl = document.getElementById('bulkCount');
        if (countEl) {
            countEl.textContent = `عدد الشحنات: ${count}`;
        }
    }

    /**
     * Show image preview
     */
    showPreview(blob, type) {
        const previewArea = document.getElementById('previewArea');
        const previewContent = document.getElementById('previewContent');
        if (!previewArea || !previewContent) return;

        previewArea.classList.remove('d-none');

        if (type === 'image') {
            const url = URL.createObjectURL(blob);
            previewContent.innerHTML = `<img src="${url}" alt="معاينة" class="img-fluid" style="max-width: 300px;">`;
        }
    }

    /**
     * Trigger file download from blob
     */
    triggerDownload(blob, filename, mimeType) {
        const url = URL.createObjectURL(new Blob([blob], { type: mimeType }));
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }

    /**
     * Add to recent downloads list
     */
    addRecentDownload(type, detail) {
        this.recentDownloads.unshift({
            type,
            detail,
            time: new Date().toLocaleTimeString('ar-EG')
        });

        // Keep only last 10
        if (this.recentDownloads.length > 10) {
            this.recentDownloads = this.recentDownloads.slice(0, 10);
        }

        this.renderRecentDownloads();
    }

    /**
     * Render recent downloads list
     */
    renderRecentDownloads() {
        const container = document.getElementById('recentDownloads');
        if (!container) return;

        if (this.recentDownloads.length === 0) {
            container.innerHTML = `
                <div class="text-center text-muted py-4">
                    <i class="fas fa-download fa-2x mb-2 d-block"></i>
                    <p>لا توجد تنزيلات حديثة</p>
                </div>
            `;
            return;
        }

        container.innerHTML = this.recentDownloads.map(d => `
            <div class="d-flex justify-content-between align-items-center py-2 border-bottom">
                <div>
                    <i class="fas fa-check-circle text-success me-2"></i>
                    <strong>${escapeHtml(d.type)}</strong>
                    <small class="text-muted ms-2">${escapeHtml(d.detail)}</small>
                </div>
                <small class="text-muted">${d.time}</small>
            </div>
        `).join('');
    }
}

// Create global instance
window.merchantLabelsHandler = new MerchantLabelsHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/merchant/labels.html')) {
        setTimeout(() => {
            window.merchantLabelsHandler.init();
        }, 200);
    }
});
