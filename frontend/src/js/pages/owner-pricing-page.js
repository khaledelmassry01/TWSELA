/**
 * Twsela CMS - Owner Pricing Page
 * Handles pricing plans CRUD, global rules, and filtering
 */

// Initialize pricing page
document.addEventListener('DOMContentLoaded', function() {
    initializePricingPage();
});

function initializePricingPage() {
    loadPricingData();
    setupEventListeners();
}

async function loadPricingData() {
    try {
        UIUtils.showLoading();
        await Promise.all([
            loadGlobalPricingRules(),
            loadCustomPricingPlans()
        ]);
    } catch (error) {
        ErrorHandler.handle(error, 'OwnerPricing');
    } finally {
        UIUtils.hideLoading();
    }
}

async function loadGlobalPricingRules() {
    try {
        const response = await window.apiService.request('/api/pricing/global');
        if (response?.success && response.data) {
            const form = document.getElementById('globalPricingForm');
            if (form) {
                const data = response.data;
                const setVal = (name, val) => { const el = form.querySelector(`[name="${name}"]`); if (el) el.value = val ?? ''; };
                setVal('baseDeliveryFee', data.baseDeliveryFee);
                setVal('additionalWeightFee', data.additionalWeightFee);
                setVal('additionalWeightFeePerKg', data.additionalWeightFeePerKg);
                setVal('serviceFee', data.serviceFee);
                setVal('deliveryTime', data.deliveryTime);
            }
        }
    } catch (error) {
        ErrorHandler.handle(error, 'OwnerPricing.globalRules');
    }
}

async function loadCustomPricingPlans() {
    try {
        const response = await window.apiService.request('/api/pricing/plans');
        if (response?.success && response.data) {
            renderPricingPlansTable(response.data);
        } else {
            UIUtils.showEmptyState('#pricingPlansTable tbody', 'لا توجد خطط تسعير', 'tags');
        }
    } catch (error) {
        ErrorHandler.handle(error, 'OwnerPricing.plans');
    }
}

function renderPricingPlansTable(plans) {
    const tbody = document.querySelector('#pricingPlansTable tbody');
    if (!tbody) return;
    if (!plans || plans.length === 0) {
        UIUtils.showEmptyState('#pricingPlansTable tbody', 'لا توجد خطط تسعير', 'tags');
        return;
    }
    tbody.innerHTML = plans.map(plan => `
        <tr>
            <td>${plan.name || 'غير محدد'}</td>
            <td>${plan.type || 'غير محدد'}</td>
            <td>${plan.baseFee ?? 0}</td>
            <td>${plan.weightFee ?? 0}</td>
            <td><span class="badge bg-${plan.status === 'ACTIVE' ? 'success' : 'secondary'}">${plan.status || 'غير محدد'}</span></td>
            <td>
                <button class="action-btn edit" data-id="${plan.id}"><i class="fas fa-edit"></i></button>
                <button class="action-btn delete" data-id="${plan.id}"><i class="fas fa-trash"></i></button>
            </td>
        </tr>
    `).join('');
}

function setupEventListeners() {
    // Action buttons
    document.addEventListener('click', function(e) {
        if (e.target.closest('.action-btn.edit')) {
            const id = e.target.closest('.action-btn').getAttribute('data-id');
            editPricingPlan(id);
        } else if (e.target.closest('.action-btn.delete')) {
            const id = e.target.closest('.action-btn').getAttribute('data-id');
            deletePricingPlan(id);
        }
    });

    // Search functionality
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value;
            filterPricingPlans(searchTerm);
        });
    }
}

function editPricingPlan(id) {
    loadPlanForEdit(id);
    const modal = new bootstrap.Modal(document.getElementById('editPricingPlanModal'));
    modal.show();
}

async function loadPlanForEdit(id) {
    try {
        UIUtils.showLoading();
        const response = await window.apiService.request(`/api/pricing/plans/${id}`);
        if (response?.success && response.data) {
            const form = document.getElementById('editPricingPlanForm');
            if (form) {
                const data = response.data;
                form.querySelector('[name="planName"]').value = data.name || '';
                form.querySelector('[name="planType"]').value = data.type || '';
                form.querySelector('[name="zoneId"]').value = data.zoneId || '';
                form.querySelector('[name="baseFee"]').value = data.baseFee ?? '';
                form.querySelector('[name="weightFee"]').value = data.weightFee ?? '';
                form.querySelector('[name="status"]').value = data.status || '';
                form.querySelector('[name="description"]').value = data.description || '';
                form.dataset.planId = id;
            }
        }
    } catch (error) {
        ErrorHandler.handle(error, 'OwnerPricing.loadPlan');
    } finally {
        UIUtils.hideLoading();
    }
}

function deletePricingPlan(id) {
    Swal.fire({
        title: 'تأكيد الحذف',
        text: 'هل أنت متأكد من حذف خطة التسعير هذه؟ لا يمكن التراجع عن هذا الإجراء.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#6b7280',
        confirmButtonText: 'نعم، احذف',
        cancelButtonText: 'إلغاء'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                UIUtils.showLoading();
                const response = await window.apiService.request(`/api/pricing/plans/${id}`, { method: 'DELETE' });
                if (response?.success) {
                    UIUtils.showSuccess('تم حذف خطة التسعير بنجاح');
                    loadPricingData();
                } else {
                    ErrorHandler.handle(response, 'OwnerPricing.deletePlan');
                }
            } catch (error) {
                ErrorHandler.handle(error, 'OwnerPricing.deletePlan');
            } finally {
                UIUtils.hideLoading();
            }
        }
    });
}

function filterPricingPlans(searchTerm) {
    const rows = document.querySelectorAll('#pricingPlansTable tbody tr');
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        const matches = text.includes(searchTerm.toLowerCase());
        row.style.display = matches ? '' : 'none';
    });
}

async function saveGlobalRules() {
    const form = document.getElementById('globalPricingForm');
    const formData = new FormData(form);
    
    const globalRules = {
        baseDeliveryFee: parseFloat(formData.get('baseDeliveryFee')),
        additionalWeightFee: parseFloat(formData.get('additionalWeightFee')),
        additionalWeightFeePerKg: parseFloat(formData.get('additionalWeightFeePerKg')),
        serviceFee: parseFloat(formData.get('serviceFee')),
        deliveryTime: parseFloat(formData.get('deliveryTime'))
    };

    try {
        UIUtils.showLoading();
        const response = await window.apiService.request('/api/pricing/global', {
            method: 'PUT',
            body: JSON.stringify(globalRules)
        });
        if (response?.success) {
            UIUtils.showSuccess('تم حفظ القواعد العامة بنجاح');
        } else {
            ErrorHandler.handle(response, 'OwnerPricing.saveGlobalRules');
        }
    } catch (error) {
        ErrorHandler.handle(error, 'OwnerPricing.saveGlobalRules');
    } finally {
        UIUtils.hideLoading();
    }
}

async function addPricingPlan() {
    const form = document.getElementById('addPricingPlanForm');
    const formData = new FormData(form);
    
    const planData = {
        name: formData.get('planName'),
        type: formData.get('planType'),
        zoneId: formData.get('zoneId'),
        baseFee: parseFloat(formData.get('baseFee')),
        weightFee: parseFloat(formData.get('weightFee')) || 0,
        status: formData.get('status'),
        description: formData.get('description')
    };

    try {
        UIUtils.showLoading();
        const response = await window.apiService.request('/api/pricing/plans', {
            method: 'POST',
            body: JSON.stringify(planData)
        });
        if (response?.success) {
            UIUtils.showSuccess('تم إضافة خطة التسعير بنجاح');
            const modal = bootstrap.Modal.getInstance(document.getElementById('addPricingPlanModal'));
            if (modal) modal.hide();
            form.reset();
            loadPricingData();
        } else {
            ErrorHandler.handle(response, 'OwnerPricing.addPlan');
        }
    } catch (error) {
        ErrorHandler.handle(error, 'OwnerPricing.addPlan');
    } finally {
        UIUtils.hideLoading();
    }
}

async function updatePricingPlan() {
    const form = document.getElementById('editPricingPlanForm');
    const formData = new FormData(form);
    
    const planData = {
        name: formData.get('planName'),
        type: formData.get('planType'),
        zoneId: formData.get('zoneId'),
        baseFee: parseFloat(formData.get('baseFee')),
        weightFee: parseFloat(formData.get('weightFee')) || 0,
        status: formData.get('status'),
        description: formData.get('description')
    };

    try {
        UIUtils.showLoading();
        const form = document.getElementById('editPricingPlanForm');
        const planId = form?.dataset?.planId;
        if (!planId) { UIUtils.showError('لم يتم تحديد معرف الخطة'); return; }
        const response = await window.apiService.request(`/api/pricing/plans/${planId}`, {
            method: 'PUT',
            body: JSON.stringify(planData)
        });
        if (response?.success) {
            UIUtils.showSuccess('تم تحديث خطة التسعير بنجاح');
            const modal = bootstrap.Modal.getInstance(document.getElementById('editPricingPlanModal'));
            if (modal) modal.hide();
            loadPricingData();
        } else {
            ErrorHandler.handle(response, 'OwnerPricing.updatePlan');
        }
    } catch (error) {
        ErrorHandler.handle(error, 'OwnerPricing.updatePlan');
    } finally {
        UIUtils.hideLoading();
    }
}

function showNotification(message, type) {
    if (typeof NotificationService !== 'undefined') {
        NotificationService.show(message, type);
    }
}
