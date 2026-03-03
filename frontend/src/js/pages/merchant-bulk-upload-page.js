import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-bulk-upload-page');

/**
 * Twsela CMS - Merchant Bulk Upload Page Handler
 * Handles bulk shipment upload via Excel files
 */

class MerchantBulkUploadHandler extends BasePageHandler {
    constructor() {
        super('Merchant Bulk Upload');
        this.selectedFile = null;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            this.setupDropZone();
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantBulkUpload');
        }
    }

    /**
     * Setup drag-and-drop zone
     */
    setupDropZone() {
        const dropZone = document.getElementById('dropZone');
        if (!dropZone) return;

        ['dragenter', 'dragover'].forEach(evt => {
            dropZone.addEventListener(evt, (e) => {
                e.preventDefault();
                dropZone.classList.add('border-primary', 'bg-light');
            });
        });

        ['dragleave', 'drop'].forEach(evt => {
            dropZone.addEventListener(evt, (e) => {
                e.preventDefault();
                dropZone.classList.remove('border-primary', 'bg-light');
            });
        });

        dropZone.addEventListener('drop', (e) => {
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                this.handleFileSelect(files[0]);
            }
        });
    }

    /**
     * Handle file selection
     */
    handleFileSelect(file) {
        const validTypes = [
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            'application/vnd.ms-excel'
        ];

        if (!validTypes.includes(file.type) && !file.name.match(/\.(xlsx|xls)$/i)) {
            this.services.notification.error('يرجى اختيار ملف Excel (.xlsx أو .xls)');
            return;
        }

        if (file.size > 10 * 1024 * 1024) {
            this.services.notification.error('حجم الملف يجب أن لا يتجاوز 10 ميغابايت');
            return;
        }

        this.selectedFile = file;

        const fileInfo = document.getElementById('fileInfo');
        const fileName = document.getElementById('fileName');
        const fileSize = document.getElementById('fileSize');

        if (fileInfo) fileInfo.classList.remove('d-none');
        if (fileName) fileName.textContent = file.name;
        if (fileSize) fileSize.textContent = this.formatFileSize(file.size);
    }

    /**
     * Remove selected file
     */
    removeFile() {
        this.selectedFile = null;
        const fileInput = document.getElementById('fileInput');
        if (fileInput) fileInput.value = '';

        const fileInfo = document.getElementById('fileInfo');
        if (fileInfo) fileInfo.classList.add('d-none');

        const resultsCard = document.getElementById('resultsCard');
        if (resultsCard) resultsCard.classList.add('d-none');
    }

    /**
     * Upload file and process
     */
    async uploadFile() {
        if (!this.selectedFile) {
            this.services.notification.warning('يرجى اختيار ملف أولاً');
            return;
        }

        try {
            const uploadBtn = document.getElementById('uploadBtn');
            const progressDiv = document.getElementById('uploadProgress');
            const progressBar = document.getElementById('progressBar');
            const progressText = document.getElementById('progressText');

            if (uploadBtn) UIUtils.showButtonLoading(uploadBtn, 'جاري الرفع...');
            if (progressDiv) progressDiv.classList.remove('d-none');

            // Simulate progress
            let progress = 0;
            const progressInterval = setInterval(() => {
                progress = Math.min(progress + 10, 90);
                if (progressBar) {
                    progressBar.style.width = progress + '%';
                    progressBar.textContent = progress + '%';
                }
            }, 300);

            const response = await this.services.api.bulkUploadShipments(this.selectedFile);

            clearInterval(progressInterval);

            if (progressBar) {
                progressBar.style.width = '100%';
                progressBar.textContent = '100%';
            }
            if (progressText) progressText.textContent = 'اكتملت المعالجة';

            if (response.success) {
                this.services.notification.success('تم رفع ومعالجة الملف بنجاح');
                this.displayResults(response.data);
            } else {
                ErrorHandler.handle(response, 'MerchantBulkUpload.upload');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantBulkUpload.upload');
        } finally {
            const uploadBtn = document.getElementById('uploadBtn');
            if (uploadBtn) UIUtils.hideButtonLoading(uploadBtn);
        }
    }

    /**
     * Download Excel template
     */
    async downloadTemplate() {
        try {
            const btn = document.getElementById('downloadTemplateBtn');
            if (btn) UIUtils.showButtonLoading(btn, 'جاري التحميل...');

            const response = await this.services.api.downloadBulkTemplate();

            if (response instanceof Blob) {
                const url = URL.createObjectURL(response);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'shipments_template.xlsx';
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                URL.revokeObjectURL(url);
                this.services.notification.success('تم تحميل القالب');
            } else if (response.success === false) {
                ErrorHandler.handle(response, 'MerchantBulkUpload.downloadTemplate');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantBulkUpload.downloadTemplate');
        } finally {
            const btn = document.getElementById('downloadTemplateBtn');
            if (btn) UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Display upload results
     */
    displayResults(data) {
        const resultsCard = document.getElementById('resultsCard');
        if (resultsCard) resultsCard.classList.remove('d-none');

        const total = data.totalProcessed || data.total || 0;
        const success = data.successCount || data.successful || 0;
        const failed = data.failedCount || data.failed || 0;
        const skipped = data.skippedCount || data.skipped || 0;

        const totalEl = document.getElementById('totalProcessed');
        if (totalEl) totalEl.textContent = total;
        const successEl = document.getElementById('successCount');
        if (successEl) successEl.textContent = success;
        const failedEl = document.getElementById('failedCount');
        if (failedEl) failedEl.textContent = failed;
        const skippedEl = document.getElementById('skippedCount');
        if (skippedEl) skippedEl.textContent = skipped;

        // Show errors if any
        const errors = data.errors || [];
        const errorDetails = document.getElementById('errorDetails');
        if (errors.length > 0 && errorDetails) {
            errorDetails.classList.remove('d-none');
            const tbody = document.querySelector('#errorsTable tbody');
            if (tbody) {
                tbody.innerHTML = '';
                errors.forEach(err => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${escapeHtml(String(err.row || err.line || '-'))}</td>
                        <td class="text-danger">${escapeHtml(err.message || err.error || '-')}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
        }
    }

    /**
     * Format file size
     */
    formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const selectFileBtn = document.getElementById('selectFileBtn');
        const fileInput = document.getElementById('fileInput');
        if (selectFileBtn && fileInput) {
            selectFileBtn.addEventListener('click', () => fileInput.click());
            fileInput.addEventListener('change', (e) => {
                if (e.target.files.length > 0) {
                    this.handleFileSelect(e.target.files[0]);
                }
            });
        }

        const removeFileBtn = document.getElementById('removeFileBtn');
        if (removeFileBtn) {
            removeFileBtn.addEventListener('click', () => this.removeFile());
        }

        const uploadBtn = document.getElementById('uploadBtn');
        if (uploadBtn) {
            uploadBtn.addEventListener('click', () => this.uploadFile());
        }

        const downloadBtn = document.getElementById('downloadTemplateBtn');
        if (downloadBtn) {
            downloadBtn.addEventListener('click', () => this.downloadTemplate());
        }
    }
}

// Create global instance
window.merchantBulkUploadHandler = new MerchantBulkUploadHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/merchant/bulk-upload.html')) {
        setTimeout(() => {
            window.merchantBulkUploadHandler.init();
        }, 200);
    }
});
