import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('merchant-recipients-page');

/**
 * Twsela CMS - Merchant Recipients (Address Book) Page Handler
 * Handles recipient CRUD and address management
 */

class MerchantRecipientsHandler extends BasePageHandler {
    constructor() {
        super('Merchant Recipients');
        this.recipients = [];
        this.selectedRecipientId = null;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            // Recipients are searched by phone; show empty state initially
            this.showEmptyState();
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantRecipients');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Show empty state
     */
    showEmptyState() {
        const grid = document.getElementById('recipientsGrid');
        const empty = document.getElementById('emptyState');
        if (grid) grid.innerHTML = '';
        if (empty) empty.classList.remove('d-none');
    }

    /**
     * Search recipient by phone
     */
    async searchByPhone() {
        try {
            const search = document.getElementById('recipientSearch');
            const phone = search?.value?.trim();

            if (!phone) {
                this.services.notification.warning('يرجى إدخال رقم الهاتف');
                return;
            }

            UIUtils.showLoading();
            const response = await this.services.api.getRecipientByPhone(phone);

            if (response.success && response.data) {
                this.recipients = [response.data];
                this.renderRecipients();
            } else {
                this.recipients = [];
                this.showEmptyState();
                this.services.notification.info('لم يتم العثور على مستلم بهذا الرقم');
            }
        } catch (error) {
            if (error.status === 404) {
                this.recipients = [];
                this.showEmptyState();
                this.services.notification.info('لم يتم العثور على مستلم بهذا الرقم');
            } else {
                ErrorHandler.handle(error, 'MerchantRecipients.searchByPhone');
            }
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Render recipients cards
     */
    renderRecipients() {
        const grid = document.getElementById('recipientsGrid');
        const empty = document.getElementById('emptyState');

        if (!this.recipients || this.recipients.length === 0) {
            this.showEmptyState();
            return;
        }

        if (empty) empty.classList.add('d-none');
        if (!grid) return;

        grid.innerHTML = '';

        this.recipients.forEach(recipient => {
            const col = document.createElement('div');
            col.className = 'col-md-6 col-lg-4';
            col.innerHTML = `
                <div class="content-card h-100">
                    <div class="card-content">
                        <div class="d-flex align-items-center mb-3">
                            <div class="avatar bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center" style="width:48px;height:48px;">
                                <i class="fas fa-user fa-lg"></i>
                            </div>
                            <div class="ms-3">
                                <h6 class="mb-0">${escapeHtml(recipient.name || '-')}</h6>
                                <small class="text-muted">${escapeHtml(recipient.phone || '-')}</small>
                            </div>
                        </div>
                        ${recipient.email ? `<p class="small text-muted mb-2"><i class="fas fa-envelope me-1"></i>${escapeHtml(recipient.email)}</p>` : ''}
                        <div class="d-flex gap-2 mt-3">
                            <button class="btn btn-sm btn-outline-primary view-addresses" data-id="${recipient.id}" data-name="${escapeHtml(recipient.name || '')}">
                                <i class="fas fa-map-marker-alt me-1"></i> العناوين
                            </button>
                            <button class="btn btn-sm btn-outline-secondary edit-recipient" data-id="${recipient.id}">
                                <i class="fas fa-edit me-1"></i> تعديل
                            </button>
                        </div>
                    </div>
                </div>
            `;
            grid.appendChild(col);
        });
    }

    /**
     * Create new recipient
     */
    async createRecipient() {
        try {
            const name = document.getElementById('recipientName')?.value?.trim();
            const phone = document.getElementById('recipientPhone')?.value?.trim();
            const email = document.getElementById('recipientEmail')?.value?.trim();
            const addressLabel = document.getElementById('addressLabel')?.value?.trim();
            const addressLine = document.getElementById('addressLine')?.value?.trim();
            const city = document.getElementById('addressCity')?.value?.trim();
            const area = document.getElementById('addressArea')?.value?.trim();

            if (!name || !phone || !addressLine || !city) {
                this.services.notification.warning('يرجى ملء جميع الحقول المطلوبة');
                return;
            }

            const btn = document.getElementById('submitRecipientBtn');
            UIUtils.showButtonLoading(btn, 'جاري الحفظ...');

            // Create recipient profile
            const recipientResponse = await this.services.api.createRecipient({
                name, phone, email: email || null
            });

            if (recipientResponse.success && recipientResponse.data) {
                const profileId = recipientResponse.data.id;

                // Create address
                await this.services.api.createRecipientAddress({
                    recipientProfileId: profileId,
                    label: addressLabel || 'رئيسي',
                    addressLine,
                    city,
                    area: area || null
                });

                this.services.notification.success('تم إضافة المستلم بنجاح');
                const modal = bootstrap.Modal.getInstance(document.getElementById('addRecipientModal'));
                if (modal) modal.hide();
                document.getElementById('addRecipientForm')?.reset();

                // Show the new recipient
                this.recipients = [recipientResponse.data];
                this.renderRecipients();
            } else {
                ErrorHandler.handle(recipientResponse, 'MerchantRecipients.createRecipient');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantRecipients.createRecipient');
        } finally {
            const btn = document.getElementById('submitRecipientBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * View recipient addresses
     */
    async viewAddresses(profileId, name) {
        try {
            this.selectedRecipientId = profileId;
            UIUtils.showLoading();

            const response = await this.services.api.getRecipientAddresses(profileId);
            const body = document.getElementById('addressesBody');
            const title = document.getElementById('addressesModalLabel');

            if (title) title.textContent = `عناوين ${name}`;

            if (response.success && response.data && response.data.length > 0) {
                if (body) {
                    body.innerHTML = response.data.map(addr => `
                        <div class="border rounded p-3 mb-2">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <span class="badge bg-primary mb-1">${escapeHtml(addr.label || 'رئيسي')}</span>
                                    <p class="mb-1">${escapeHtml(addr.addressLine || '-')}</p>
                                    <small class="text-muted">${escapeHtml(addr.city || '')} ${addr.area ? '- ' + escapeHtml(addr.area) : ''}</small>
                                </div>
                            </div>
                        </div>
                    `).join('');
                }
            } else {
                if (body) body.innerHTML = '<p class="text-center text-muted">لا توجد عناوين مسجلة</p>';
            }

            const modal = new bootstrap.Modal(document.getElementById('addressesModal'));
            modal.show();
        } catch (error) {
            ErrorHandler.handle(error, 'MerchantRecipients.viewAddresses');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Add new address to existing recipient
     */
    async addAddress() {
        if (!this.selectedRecipientId) return;

        const { value: formValues } = await Swal.fire({
            title: 'إضافة عنوان جديد',
            html: `
                <input id="swal-label" class="swal2-input" placeholder="تسمية العنوان (المنزل، العمل...)">
                <input id="swal-address" class="swal2-input" placeholder="العنوان بالتفصيل" required>
                <input id="swal-city" class="swal2-input" placeholder="المدينة" required>
                <input id="swal-area" class="swal2-input" placeholder="المنطقة">
            `,
            focusConfirm: false,
            showCancelButton: true,
            confirmButtonText: 'حفظ',
            cancelButtonText: 'إلغاء',
            preConfirm: () => {
                const address = document.getElementById('swal-address').value;
                const city = document.getElementById('swal-city').value;
                if (!address || !city) {
                    Swal.showValidationMessage('يرجى ملء العنوان والمدينة');
                    return false;
                }
                return {
                    label: document.getElementById('swal-label').value || 'إضافي',
                    addressLine: address,
                    city: city,
                    area: document.getElementById('swal-area').value || null
                };
            }
        });

        if (formValues) {
            try {
                UIUtils.showLoading();
                const response = await this.services.api.createRecipientAddress({
                    recipientProfileId: this.selectedRecipientId,
                    ...formValues
                });

                if (response.success) {
                    this.services.notification.success('تم إضافة العنوان بنجاح');
                    // Refresh addresses modal
                    const modal = bootstrap.Modal.getInstance(document.getElementById('addressesModal'));
                    if (modal) modal.hide();
                    // Re-open with updated data
                    const recipient = this.recipients.find(r => r.id === this.selectedRecipientId);
                    if (recipient) {
                        await this.viewAddresses(this.selectedRecipientId, recipient.name);
                    }
                } else {
                    ErrorHandler.handle(response, 'MerchantRecipients.addAddress');
                }
            } catch (error) {
                ErrorHandler.handle(error, 'MerchantRecipients.addAddress');
            } finally {
                UIUtils.hideLoading();
            }
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const searchBtn = document.getElementById('searchByPhoneBtn');
        if (searchBtn) {
            searchBtn.addEventListener('click', () => this.searchByPhone());
        }

        const searchInput = document.getElementById('recipientSearch');
        if (searchInput) {
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.searchByPhone();
            });
        }

        const submitBtn = document.getElementById('submitRecipientBtn');
        if (submitBtn) {
            submitBtn.addEventListener('click', () => this.createRecipient());
        }

        const addAddressBtn = document.getElementById('addAddressBtn');
        if (addAddressBtn) {
            addAddressBtn.addEventListener('click', () => this.addAddress());
        }

        // Event delegation for grid
        const grid = document.getElementById('recipientsGrid');
        if (grid) {
            grid.addEventListener('click', (e) => {
                const viewBtn = e.target.closest('.view-addresses');
                if (viewBtn) {
                    this.viewAddresses(parseInt(viewBtn.dataset.id), viewBtn.dataset.name);
                }
            });
        }
    }
}

// Create global instance
window.merchantRecipientsHandler = new MerchantRecipientsHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/merchant/recipients.html')) {
        setTimeout(() => {
            window.merchantRecipientsHandler.init();
        }, 200);
    }
});
