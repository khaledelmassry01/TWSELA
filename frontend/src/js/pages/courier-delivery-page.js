import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('courier-delivery-page');

/**
 * Twsela CMS - Courier Delivery Proof Page Handler
 * Handles delivery proof submission and attempt recording
 */

class CourierDeliveryHandler extends BasePageHandler {
    constructor() {
        super('Courier Delivery');
        this.currentShipmentId = null;
        this.selectedPhoto = null;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            // Check for shipment ID in URL params
            const params = new URLSearchParams(window.location.search);
            const shipmentId = params.get('shipmentId');
            if (shipmentId) {
                document.getElementById('shipmentIdInput').value = shipmentId;
                await this.lookupShipment();
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CourierDelivery');
        }
    }

    /**
     * Lookup shipment and show forms
     */
    async lookupShipment() {
        try {
            const shipmentId = document.getElementById('shipmentIdInput')?.value?.trim();
            if (!shipmentId) {
                this.services.notification.warning('يرجى إدخال رقم الشحنة');
                return;
            }

            this.currentShipmentId = parseInt(shipmentId);
            UIUtils.showLoading();

            // Show proof and attempt sections
            const proofSection = document.getElementById('proofSection');
            const attemptSection = document.getElementById('attemptSection');
            if (proofSection) proofSection.classList.remove('d-none');
            if (attemptSection) attemptSection.classList.remove('d-none');

            // Load previous attempts
            await this.loadAttempts();
        } catch (error) {
            ErrorHandler.handle(error, 'CourierDelivery.lookupShipment');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load delivery attempts for current shipment
     */
    async loadAttempts() {
        try {
            if (!this.currentShipmentId) return;

            const response = await this.services.api.getDeliveryAttempts(this.currentShipmentId);
            const section = document.getElementById('attemptsHistorySection');
            const list = document.getElementById('attemptsList');

            if (response.success && response.data && response.data.length > 0) {
                if (section) section.classList.remove('d-none');
                if (list) {
                    list.innerHTML = response.data.map((attempt, index) => `
                        <div class="border rounded p-3 mb-2">
                            <div class="d-flex justify-content-between">
                                <div>
                                    <span class="badge bg-warning me-2">محاولة ${index + 1}</span>
                                    <strong>${this.getFailureReasonLabel(attempt.failureReason)}</strong>
                                </div>
                                <small class="text-muted">${this.formatDate(attempt.createdAt || attempt.attemptDate)}</small>
                            </div>
                            ${attempt.notes ? `<p class="text-muted small mt-1 mb-0">${escapeHtml(attempt.notes)}</p>` : ''}
                        </div>
                    `).join('');
                }
            } else {
                if (section) section.classList.add('d-none');
            }
        } catch (error) {
            // Silent — no history is OK
            log.warn('Could not load attempts:', error);
        }
    }

    /**
     * Handle photo selection
     */
    handlePhotoSelect(file) {
        if (!file || !file.type.startsWith('image/')) {
            this.services.notification.error('يرجى اختيار ملف صورة');
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            this.services.notification.error('حجم الصورة يجب أن لا يتجاوز 5 ميغابايت');
            return;
        }

        this.selectedPhoto = file;

        const preview = document.getElementById('photoPreview');
        const previewImg = document.getElementById('previewImg');
        if (preview) preview.classList.remove('d-none');
        if (previewImg) {
            const reader = new FileReader();
            reader.onload = (e) => previewImg.src = e.target.result;
            reader.readAsDataURL(file);
        }
    }

    /**
     * Remove selected photo
     */
    removePhoto() {
        this.selectedPhoto = null;
        const preview = document.getElementById('photoPreview');
        if (preview) preview.classList.add('d-none');
        const input = document.getElementById('proofPhoto');
        if (input) input.value = '';
    }

    /**
     * Submit delivery proof
     */
    async submitProof() {
        try {
            if (!this.currentShipmentId) {
                this.services.notification.warning('يرجى البحث عن شحنة أولاً');
                return;
            }

            const recipientName = document.getElementById('recipientNameInput')?.value?.trim();
            if (!recipientName) {
                this.services.notification.warning('يرجى إدخال اسم المستلم');
                return;
            }

            const btn = document.getElementById('submitProofBtn');
            UIUtils.showButtonLoading(btn, 'جاري التأكيد...');

            const formData = new FormData();
            if (this.selectedPhoto) {
                formData.append('photo', this.selectedPhoto);
            }
            formData.append('recipientName', recipientName);

            const notes = document.getElementById('proofNotes')?.value?.trim();
            if (notes) formData.append('notes', notes);

            // Try to get GPS coordinates
            if (navigator.geolocation) {
                try {
                    const pos = await new Promise((resolve, reject) => {
                        navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 5000 });
                    });
                    formData.append('lat', pos.coords.latitude);
                    formData.append('lng', pos.coords.longitude);
                } catch (geoError) {
                    log.warn('Could not get location:', geoError);
                }
            }

            const response = await this.services.api.submitDeliveryProof(this.currentShipmentId, formData);

            if (response.success) {
                this.services.notification.success('تم تأكيد التسليم بنجاح');
                document.getElementById('proofForm')?.reset();
                this.removePhoto();
            } else {
                ErrorHandler.handle(response, 'CourierDelivery.submitProof');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CourierDelivery.submitProof');
        } finally {
            const btn = document.getElementById('submitProofBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Record failed delivery attempt
     */
    async recordAttempt() {
        try {
            if (!this.currentShipmentId) {
                this.services.notification.warning('يرجى البحث عن شحنة أولاً');
                return;
            }

            const failureReason = document.getElementById('failureReason')?.value;
            if (!failureReason) {
                this.services.notification.warning('يرجى اختيار سبب الفشل');
                return;
            }

            const btn = document.getElementById('submitAttemptBtn');
            UIUtils.showButtonLoading(btn, 'جاري التسجيل...');

            const data = {
                failureReason,
                notes: document.getElementById('attemptNotes')?.value?.trim() || ''
            };

            // Try to get GPS coordinates
            if (navigator.geolocation) {
                try {
                    const pos = await new Promise((resolve, reject) => {
                        navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 5000 });
                    });
                    data.lat = pos.coords.latitude;
                    data.lng = pos.coords.longitude;
                } catch (geoError) {
                    log.warn('Could not get location:', geoError);
                }
            }

            const response = await this.services.api.recordDeliveryAttempt(this.currentShipmentId, data);

            if (response.success) {
                this.services.notification.success('تم تسجيل المحاولة');
                document.getElementById('attemptForm')?.reset();
                await this.loadAttempts();
            } else {
                ErrorHandler.handle(response, 'CourierDelivery.recordAttempt');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CourierDelivery.recordAttempt');
        } finally {
            const btn = document.getElementById('submitAttemptBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Get failure reason label in Arabic
     */
    getFailureReasonLabel(reason) {
        const labels = {
            'NOT_HOME': 'المستلم غير متواجد',
            'WRONG_ADDRESS': 'عنوان خاطئ',
            'REFUSED': 'رفض الاستلام',
            'DAMAGED': 'الشحنة تالفة',
            'RESCHEDULED': 'تم إعادة الجدولة',
            'INACCESSIBLE': 'المنطقة غير قابلة للوصول',
            'INCOMPLETE_ADDRESS': 'عنوان غير مكتمل',
            'PHONE_UNREACHABLE': 'الهاتف مغلق'
        };
        return labels[reason] || escapeHtml(reason || '-');
    }

    /**
     * Format date
     */
    formatDate(dateString) {
        if (!dateString) return 'غير محدد';
        try {
            return new Date(dateString).toLocaleDateString('ar-SA', {
                year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
            });
        } catch {
            return 'غير محدد';
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const lookupBtn = document.getElementById('lookupShipmentBtn');
        if (lookupBtn) {
            lookupBtn.addEventListener('click', () => this.lookupShipment());
        }

        const shipmentInput = document.getElementById('shipmentIdInput');
        if (shipmentInput) {
            shipmentInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.lookupShipment();
            });
        }

        // Photo handling
        const selectPhotoBtn = document.getElementById('selectPhotoBtn');
        const photoInput = document.getElementById('proofPhoto');
        if (selectPhotoBtn && photoInput) {
            selectPhotoBtn.addEventListener('click', () => photoInput.click());
            const photoZone = document.getElementById('photoDropZone');
            if (photoZone) photoZone.addEventListener('click', () => photoInput.click());
            photoInput.addEventListener('change', (e) => {
                if (e.target.files.length > 0) this.handlePhotoSelect(e.target.files[0]);
            });
        }

        const removePhotoBtn = document.getElementById('removePhotoBtn');
        if (removePhotoBtn) {
            removePhotoBtn.addEventListener('click', () => this.removePhoto());
        }

        const submitProofBtn = document.getElementById('submitProofBtn');
        if (submitProofBtn) {
            submitProofBtn.addEventListener('click', () => this.submitProof());
        }

        const submitAttemptBtn = document.getElementById('submitAttemptBtn');
        if (submitAttemptBtn) {
            submitAttemptBtn.addEventListener('click', () => this.recordAttempt());
        }
    }
}

// Create global instance
window.courierDeliveryHandler = new CourierDeliveryHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/courier/delivery.html')) {
        setTimeout(() => {
            window.courierDeliveryHandler.init();
        }, 200);
    }
});
