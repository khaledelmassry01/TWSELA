/**
 * Twsela CMS - Global UI Handler
 * Consolidated UI operations and notifications
 * Follows Single Responsibility Principle - ONLY UI operations
 */

// GlobalUIHandler initialization - console.log removed for cleaner console

class GlobalUIHandler {
    /**
     * Show loading spinner
     */
    static showLoading(container = null) {
        const target = container || document.body;
        const loadingHtml = `
            <div class="loading-spinner" id="global-loading">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">جاري التحميل...</span>
                </div>
            </div>
        `;
        target.insertAdjacentHTML('beforeend', loadingHtml);
    }

    /**
     * Hide loading spinner
     */
    static hideLoading(container = null) {
        const target = container || document.body;
        const loading = target.querySelector('#global-loading');
        if (loading) {
            loading.remove();
        }
    }

    /**
     * Toggle sidebar on mobile
     */
    static toggleSidebar() {
        const sidebar = document.querySelector('.sidebar');
        if (sidebar) {
            sidebar.classList.toggle('show');
        }
    }

    /**
     * Close sidebar on mobile
     */
    static closeSidebar() {
        const sidebar = document.querySelector('.sidebar');
        if (sidebar) {
            sidebar.classList.remove('show');
        }
    }

    /**
     * Show modal
     */
    static showModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
        }
    }

    /**
     * Hide modal
     */
    static hideModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            const bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) {
                bsModal.hide();
            }
        }
    }

    /**
     * Validate form
     */
    static validateForm(form) {
        const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');
        let isValid = true;
        
        inputs.forEach(input => {
            if (!input.value.trim()) {
                input.classList.add('is-invalid');
                isValid = false;
            } else {
                input.classList.remove('is-invalid');
            }
        });
        
        return isValid;
    }

    /**
     * Clear form validation
     */
    static clearFormValidation(form) {
        const inputs = form.querySelectorAll('.is-invalid');
        inputs.forEach(input => {
            input.classList.remove('is-invalid');
        });
    }

    /**
     * Reset form
     */
    static resetForm(form) {
        form.reset();
        this.clearFormValidation(form);
    }

    /**
     * Animate number with smooth counting effect
     */
    static animateNumber(element, start, end, duration = 1000) {
        const startTime = performance.now();
        const range = end - start;
        
        const updateNumber = (currentTime) => {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            const current = Math.floor(start + (range * this.easeOutCubic(progress)));
            element.textContent = current.toLocaleString('ar-SA');
            
            if (progress < 1) {
                requestAnimationFrame(updateNumber);
            }
        };
        
        requestAnimationFrame(updateNumber);
    }

    /**
     * Easing function for smooth animation
     */
    static easeOutCubic(t) {
        return 1 - Math.pow(1 - t, 3);
    }

    /**
     * Create table row HTML - Pure helper function
     * @param {Object} data - Row data
     * @param {string} type - Row type (payout, shipment, user, etc.)
     * @returns {string} HTML string for table row
     */
    static createTableRow(data, type) {
        switch (type) {
            case 'payout':
                return this.createPayoutRow(data);
            case 'shipment':
                return this.createShipmentRow(data);
            case 'user':
                return this.createUserRow(data);
            case 'zone':
                return this.createZoneRow(data);
            default:
                return this.createGenericRow(data);
        }
    }

    /** @private Shorthand for XSS-safe escaping */
    static _e(str) { return SharedDataUtils.escapeHtml(str); }

    /**
     * Create payout row HTML
     */
    static createPayoutRow(payout) {
        const e = this._e;
        const statusBadge = SharedDataUtils.createStatusBadge(payout.status);
        const requestDate = SharedDataUtils.formatDate(payout.requestDate);
        const amount = SharedDataUtils.formatCurrency(payout.amount);
        const merchantName = e(payout.merchant?.name || 'غير محدد');

        return `
            <tr data-payout-id="${payout.id}">
                <td>
                    <div class="form-check">
                        <input class="form-check-input payout-checkbox" type="checkbox" 
                               value="${payout.id}" ${payout.status !== 'PENDING' ? 'disabled' : ''}>
                    </div>
                </td>
                <td>
                    <div class="d-flex align-items-center">
                        <div class="avatar me-2">
                            <i class="fas fa-user"></i>
                        </div>
                        <div>
                            <div class="fw-bold">${merchantName}</div>
                            <small class="text-muted">${e(payout.merchant?.email || '')}</small>
                        </div>
                    </div>
                </td>
                <td>
                    <span class="badge ${statusBadge.class}">${statusBadge.text}</span>
                </td>
                <td>${amount}</td>
                <td>${requestDate}</td>
                <td>${e(payout.paymentMethod || 'غير محدد')}</td>
                <td>${e(payout.bankAccount || 'غير محدد')}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn view" data-payout-id="${payout.id}" title="عرض">
                            <i class="fas fa-eye"></i>
                        </button>
                        ${payout.status === 'PENDING' ? `
                            <button class="action-btn edit" data-payout-id="${payout.id}" data-action="approve" title="موافقة">
                                <i class="fas fa-check"></i>
                            </button>
                            <button class="action-btn delete" data-payout-id="${payout.id}" data-action="reject" title="رفض">
                                <i class="fas fa-times"></i>
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Create shipment row HTML
     */
    static createShipmentRow(shipment) {
        const e = this._e;
        const statusBadge = SharedDataUtils.createStatusBadge(shipment.status);
        const codAmount = SharedDataUtils.formatCurrency(shipment.codAmount || 0);
        const createdDate = SharedDataUtils.formatDate(shipment.createdAt);

        return `
            <tr data-shipment-id="${shipment.id}">
                <td>${e(shipment.trackingNumber || 'غير محدد')}</td>
                <td>${e(shipment.recipientName || 'غير محدد')}</td>
                <td>${e(shipment.recipientPhone || 'غير محدد')}</td>
                <td>${e(shipment.address || 'غير محدد')}</td>
                <td>${codAmount}</td>
                <td>
                    <span class="badge ${statusBadge.class}">${statusBadge.text}</span>
                </td>
                <td>${createdDate}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn view" data-shipment-id="${shipment.id}" title="عرض">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="action-btn update" data-shipment-id="${shipment.id}" title="تحديث الحالة">
                            <i class="fas fa-edit"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Create user row HTML
     */
    static createUserRow(user) {
        const e = this._e;
        const roleBadge = SharedDataUtils.createRoleBadge(user.role);
        const statusBadge = SharedDataUtils.createStatusBadge(user.status);
        const createdDate = SharedDataUtils.formatDate(user.createdAt);

        return `
            <tr data-user-id="${user.id}">
                <td>
                    <div class="d-flex align-items-center">
                        <div class="avatar me-2">
                            <i class="fas fa-user"></i>
                        </div>
                        <div>
                            <div class="fw-bold">${e(user.name || 'غير محدد')}</div>
                            <small class="text-muted">${e(user.email || '')}</small>
                        </div>
                    </div>
                </td>
                <td>
                    <span class="badge ${roleBadge.class}">${roleBadge.text}</span>
                </td>
                <td>
                    <span class="badge ${statusBadge.class}">${statusBadge.text}</span>
                </td>
                <td>${e(user.phone || 'غير محدد')}</td>
                <td>${createdDate}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn view" data-user-id="${user.id}" title="عرض">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="action-btn edit" data-user-id="${user.id}" title="تعديل">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="action-btn delete" data-user-id="${user.id}" title="حذف">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Create zone row HTML
     */
    static createZoneRow(zone) {
        const e = this._e;
        const statusBadge = SharedDataUtils.createStatusBadge(zone.status);
        const createdDate = SharedDataUtils.formatDate(zone.createdAt);
        const defaultFee = SharedDataUtils.formatCurrency(zone.defaultFee || 0);

        return `
            <tr data-zone-id="${zone.id}">
                <td>
                    <div class="d-flex align-items-center">
                        <div class="zone-icon me-2">
                            <i class="fas fa-map-marker-alt"></i>
                        </div>
                        <div>
                            <div class="fw-bold">${e(zone.name || 'غير محدد')}</div>
                            <small class="text-muted">${e(zone.city || 'غير محدد')}</small>
                        </div>
                    </div>
                </td>
                <td>
                    <span class="badge ${statusBadge.class}">${statusBadge.text}</span>
                </td>
                <td>${defaultFee}</td>
                <td>${zone.codFee ? e(String(zone.codFee)) + '%' : 'غير محدد'}</td>
                <td>${e(zone.estimatedDeliveryTime || 'غير محدد')}</td>
                <td>${createdDate}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn edit" data-zone-id="${zone.id}" data-action="edit" title="تعديل">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="action-btn view" data-zone-id="${zone.id}" data-action="view" title="عرض">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="action-btn delete" data-zone-id="${zone.id}" data-action="delete" title="حذف">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Create generic row HTML
     */
    static createGenericRow(data) {
        return `
            <tr>
                <td>${data.id || 'غير محدد'}</td>
                <td>${data.name || 'غير محدد'}</td>
                <td>${data.description || 'غير محدد'}</td>
                <td>${SharedDataUtils.formatDate(data.createdAt)}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn view" data-id="${data.id}" title="عرض">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="action-btn edit" data-id="${data.id}" title="تعديل">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="action-btn delete" data-id="${data.id}" title="حذف">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Create modal HTML - Pure helper function
     * @param {Object} data - Modal data
     * @param {string} type - Modal type (payout, shipment, user, etc.)
     * @param {string} modalId - Modal ID
     * @returns {string} HTML string for modal
     */
    static createModal(data, type, modalId) {
        switch (type) {
            case 'payout':
                return this.createPayoutModal(data, modalId);
            case 'shipment':
                return this.createShipmentModal(data, modalId);
            case 'user':
                return this.createUserModal(data, modalId);
            default:
                return this.createGenericModal(data, modalId);
        }
    }

    /**
     * Create payout modal HTML
     */
    static createPayoutModal(payoutData, modalId) {
        const statusBadge = SharedDataUtils.createStatusBadge(payoutData.status);
        const requestDate = SharedDataUtils.formatDate(payoutData.requestDate);
        const amount = SharedDataUtils.formatCurrency(payoutData.amount);

        return `
            <div class="modal fade" id="${modalId}" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">تفاصيل المدفوعات</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <h6>معلومات التاجر</h6>
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>الاسم:</strong></td>
                                            <td>${this._e(payoutData.merchant?.name || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>البريد الإلكتروني:</strong></td>
                                            <td>${this._e(payoutData.merchant?.email || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>رقم الهاتف:</strong></td>
                                            <td>${this._e(payoutData.merchant?.phone || 'غير محدد')}</td>
                                        </tr>
                                    </table>
                                </div>
                                <div class="col-md-6">
                                    <h6>معلومات المدفوعات</h6>
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>المبلغ:</strong></td>
                                            <td>${amount}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>الحالة:</strong></td>
                                            <td><span class="badge ${statusBadge.class}">${statusBadge.text}</span></td>
                                        </tr>
                                        <tr>
                                            <td><strong>تاريخ الطلب:</strong></td>
                                            <td>${requestDate}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>طريقة الدفع:</strong></td>
                                            <td>${this._e(payoutData.paymentMethod || 'غير محدد')}</td>
                                        </tr>
                                    </table>
                                </div>
                                <div class="col-12">
                                    <h6>معلومات الحساب البنكي</h6>
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>اسم البنك:</strong></td>
                                            <td>${this._e(payoutData.bankName || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>رقم الحساب:</strong></td>
                                            <td>${this._e(payoutData.bankAccount || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>IBAN:</strong></td>
                                            <td>${this._e(payoutData.iban || 'غير محدد')}</td>
                                        </tr>
                                    </table>
                                </div>
                                ${payoutData.rejectionReason ? `
                                    <div class="col-12">
                                        <h6>سبب الرفض</h6>
                                        <p class="text-danger">${this._e(payoutData.rejectionReason)}</p>
                                    </div>
                                ` : ''}
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">إغلاق</button>
                            ${payoutData.status === 'PENDING' ? `
                                <button type="button" class="btn btn-success modal-action-btn" data-payout-id="${payoutData.id}" data-action="approve">
                                    موافقة
                                </button>
                                <button type="button" class="btn btn-danger modal-action-btn" data-payout-id="${payoutData.id}" data-action="reject">
                                    رفض
                                </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Create shipment modal HTML
     */
    static createShipmentModal(shipmentData, modalId) {
        const statusBadge = SharedDataUtils.createStatusBadge(shipmentData.status);
        const createdDate = SharedDataUtils.formatDate(shipmentData.createdAt);
        const codAmount = SharedDataUtils.formatCurrency(shipmentData.codAmount || 0);

        return `
            <div class="modal fade" id="${modalId}" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">تفاصيل الشحنة</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <h6>معلومات الشحنة</h6>
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>رقم التتبع:</strong></td>
                                            <td>${this._e(shipmentData.trackingNumber || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>الحالة:</strong></td>
                                            <td><span class="badge ${statusBadge.class}">${statusBadge.text}</span></td>
                                        </tr>
                                        <tr>
                                            <td><strong>تاريخ الإنشاء:</strong></td>
                                            <td>${createdDate}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>مبلغ الدفع عند الاستلام:</strong></td>
                                            <td>${codAmount}</td>
                                        </tr>
                                    </table>
                                </div>
                                <div class="col-md-6">
                                    <h6>معلومات المستلم</h6>
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>الاسم:</strong></td>
                                            <td>${this._e(shipmentData.recipientName || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>رقم الهاتف:</strong></td>
                                            <td>${this._e(shipmentData.recipientPhone || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>العنوان:</strong></td>
                                            <td>${this._e(shipmentData.address || 'غير محدد')}</td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">إغلاق</button>
                            <button type="button" class="btn btn-primary modal-action-btn" data-shipment-id="${shipmentData.id}" data-action="update">
                                تحديث الحالة
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Create user modal HTML
     */
    static createUserModal(userData, modalId) {
        const roleBadge = SharedDataUtils.createRoleBadge(userData.role);
        const statusBadge = SharedDataUtils.createStatusBadge(userData.status);
        const createdDate = SharedDataUtils.formatDate(userData.createdAt);

        return `
            <div class="modal fade" id="${modalId}" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">تفاصيل المستخدم</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <h6>معلومات المستخدم</h6>
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>الاسم:</strong></td>
                                            <td>${this._e(userData.name || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>البريد الإلكتروني:</strong></td>
                                            <td>${this._e(userData.email || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>رقم الهاتف:</strong></td>
                                            <td>${this._e(userData.phone || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>الدور:</strong></td>
                                            <td><span class="badge ${roleBadge.class}">${roleBadge.text}</span></td>
                                        </tr>
                                        <tr>
                                            <td><strong>الحالة:</strong></td>
                                            <td><span class="badge ${statusBadge.class}">${statusBadge.text}</span></td>
                                        </tr>
                                        <tr>
                                            <td><strong>تاريخ الإنشاء:</strong></td>
                                            <td>${createdDate}</td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">إغلاق</button>
                            <button type="button" class="btn btn-primary modal-action-btn" data-user-id="${userData.id}" data-action="edit">
                                تعديل
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Create generic modal HTML
     */
    static createGenericModal(data, modalId) {
        return `
            <div class="modal fade" id="${modalId}" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">تفاصيل</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-12">
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>المعرف:</strong></td>
                                            <td>${this._e(String(data.id || 'غير محدد'))}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>الاسم:</strong></td>
                                            <td>${this._e(data.name || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>الوصف:</strong></td>
                                            <td>${this._e(data.description || 'غير محدد')}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>تاريخ الإنشاء:</strong></td>
                                            <td>${SharedDataUtils.formatDate(data.createdAt)}</td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">إغلاق</button>
                            <button type="button" class="btn btn-primary modal-action-btn" data-id="${data.id}" data-action="edit">
                                تعديل
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Show modal with HTML content
     */
    static showModalWithContent(modalHtml, modalId) {
        // Remove existing modal
        const existingModal = document.getElementById(modalId);
        if (existingModal) {
            existingModal.remove();
        }

        // Add new modal
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        
        // Show modal
        const modal = new bootstrap.Modal(document.getElementById(modalId));
        modal.show();
    }

    /**
     * Update pagination
     */
    static updatePagination(containerId, currentPage, totalPages) {
        const paginationContainer = document.getElementById(containerId);
        if (!paginationContainer) return;

        if (totalPages <= 1) {
            paginationContainer.innerHTML = '';
            return;
        }

        let paginationHtml = '';
        
        // Previous button
        paginationHtml += `
            <button class="page-btn" data-page="${currentPage - 1}" 
                    ${currentPage === 1 ? 'disabled' : ''}>
                <i class="fas fa-chevron-right"></i>
            </button>
        `;

        // Page numbers
        const startPage = Math.max(1, currentPage - 2);
        const endPage = Math.min(totalPages, currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            paginationHtml += `
                <button class="page-btn ${i === currentPage ? 'active' : ''}" 
                        data-page="${i}">${i}</button>
            `;
        }

        // Next button
        paginationHtml += `
            <button class="page-btn" data-page="${currentPage + 1}" 
                    ${currentPage === totalPages ? 'disabled' : ''}>
                <i class="fas fa-chevron-left"></i>
            </button>
        `;

        paginationContainer.innerHTML = paginationHtml;
    }

    /**
     * Update bulk actions visibility
     */
    static updateBulkActions(containerId, checkboxSelector) {
        const selectedCheckboxes = document.querySelectorAll(`${checkboxSelector}:checked`);
        const bulkActionsContainer = document.getElementById(containerId);
        
        if (bulkActionsContainer) {
            if (selectedCheckboxes.length > 0) {
                bulkActionsContainer.style.display = 'block';
            } else {
                bulkActionsContainer.style.display = 'none';
            }
        }
    }

    /**
     * Toggle select all checkboxes
     */
    static toggleSelectAll(checked, checkboxSelector) {
        const checkboxes = document.querySelectorAll(`${checkboxSelector}:not(:disabled)`);
        checkboxes.forEach(checkbox => {
            checkbox.checked = checked;
        });
    }

    /**
     * Get selected checkbox values
     */
    static getSelectedValues(checkboxSelector) {
        const selectedCheckboxes = document.querySelectorAll(`${checkboxSelector}:checked`);
        return Array.from(selectedCheckboxes).map(cb => parseInt(cb.value));
    }
}

// Create global instance
window.GlobalUIHandler = GlobalUIHandler;

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = GlobalUIHandler;
}

// GlobalUIHandler loaded - console.log removed for cleaner console
