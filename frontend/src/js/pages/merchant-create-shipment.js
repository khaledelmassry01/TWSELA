/**
 * Twsela CMS - Merchant Create Shipment Handler
 * Handles shipment creation form with zone-based pricing calculation
 * Follows DRY principle with unified shipment management logic
 */

class MerchantCreateShipmentHandler {
    constructor() {
        this.zones = [];
        this.selectedZone = null;
        this.pricingData = {};
        this.form = null;
        this.init();
    }

    /**
     * Initialize the handler
     */
    async init() {
        try {
            this.form = document.getElementById('createShipmentForm');
            if (!this.form) {

                return;
            }

            await this.loadZones();
            this.setupEventListeners();
            this.initializeForm();
        } catch (error) {

            
        }
    }

    /**
     * Load all zones for pricing calculation
     */
    async loadZones() {
        try {
            UIUtils.showLoading();
            const response = await apiService.getZones({ status: 'ACTIVE' });
            
            if (response.success) {
                this.zones = response.data || [];
                this.populateZoneSelect();
            } else {

                this.zones = [];
                this.populateZoneSelect();
            }
        } catch (error) {

            
            this.zones = [];
            this.populateZoneSelect();
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Populate zone select dropdown
     */
    populateZoneSelect() {
        const zoneSelect = document.getElementById('zoneId');
        if (!zoneSelect) return;

        // Clear existing options
        zoneSelect.innerHTML = '<option value="">اختر المنطقة</option>';

        this.zones.forEach(zone => {
            const option = document.createElement('option');
            option.value = zone.id;
            option.textContent = `${zone.name} - ${zone.city}`;
            option.dataset.defaultFee = zone.defaultFee;
            option.dataset.codFee = zone.codFee || 0;
            zoneSelect.appendChild(option);
        });
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Zone selection change
        const zoneSelect = document.getElementById('zoneId');
        if (zoneSelect) {
            zoneSelect.addEventListener('change', (e) => {
                this.handleZoneChange(e.target.value);
            });
        }

        // Weight input change
        const weightInput = document.getElementById('weight');
        if (weightInput) {
            weightInput.addEventListener('input', () => {
                this.calculatePricing();
            });
        }

        // COD amount change
        const codAmountInput = document.getElementById('codAmount');
        if (codAmountInput) {
            codAmountInput.addEventListener('input', () => {
                this.calculatePricing();
            });
        }

        // Form submission
        if (this.form) {
            this.form.addEventListener('submit', (e) => {
                this.handleFormSubmit(e);
            });
        }

        // Real-time validation
        this.setupRealTimeValidation();
    }

    /**
     * Setup real-time form validation
     */
    setupRealTimeValidation() {
        const inputs = this.form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validateField(input);
            });
        });
    }

    /**
     * Handle zone selection change
     */
    handleZoneChange(zoneId) {
        if (!zoneId) {
            this.selectedZone = null;
            this.clearPricing();
            return;
        }

        this.selectedZone = this.zones.find(zone => zone.id == zoneId);
        if (this.selectedZone) {
            this.pricingData = {
                defaultFee: parseFloat(this.selectedZone.defaultFee) || 0,
                codFee: parseFloat(this.selectedZone.codFee) || 0
            };
            this.calculatePricing();
        }
    }

    /**
     * Calculate shipping and COD fees
     */
    calculatePricing() {
        if (!this.selectedZone) {
            this.clearPricing();
            return;
        }

        const weight = parseFloat(document.getElementById('weight')?.value) || 0;
        const codAmount = parseFloat(document.getElementById('codAmount')?.value) || 0;

        // Calculate shipping fee based on weight and zone
        const shippingFee = this.calculateShippingFee(weight);
        
        // Calculate COD fee based on COD amount
        const codFee = this.calculateCodFee(codAmount);

        // Calculate total fee
        const totalFee = shippingFee + codFee;

        // Update UI
        this.updatePricingDisplay({
            shippingFee,
            codFee,
            totalFee,
            weight,
            codAmount
        });
    }

    /**
     * Calculate shipping fee based on weight and zone
     */
    calculateShippingFee(weight) {
        if (!this.selectedZone || weight <= 0) return 0;

        const baseFee = this.pricingData.defaultFee;
        const weightMultiplier = this.getWeightMultiplier(weight);
        
        return baseFee * weightMultiplier;
    }

    /**
     * Get weight multiplier for pricing calculation
     */
    getWeightMultiplier(weight) {
        if (weight <= 1) return 1;
        if (weight <= 5) return 1.2;
        if (weight <= 10) return 1.5;
        if (weight <= 20) return 2;
        return 2.5; // For weights over 20kg
    }

    /**
     * Calculate COD fee based on amount
     */
    calculateCodFee(codAmount) {
        if (!this.selectedZone || codAmount <= 0) return 0;

        const codFeeRate = this.pricingData.codFee;
        return codAmount * (codFeeRate / 100);
    }

    /**
     * Update pricing display in UI
     */
    updatePricingDisplay(pricing) {
        // Update shipping fee
        const shippingFeeElement = document.getElementById('shippingFee');
        if (shippingFeeElement) {
            shippingFeeElement.textContent = DataUtils.formatCurrency(pricing.shippingFee);
        }

        // Update COD fee
        const codFeeElement = document.getElementById('codFee');
        if (codFeeElement) {
            codFeeElement.textContent = DataUtils.formatCurrency(pricing.codFee);
        }

        // Update total fee
        const totalFeeElement = document.getElementById('totalFee');
        if (totalFeeElement) {
            totalFeeElement.textContent = DataUtils.formatCurrency(pricing.totalFee);
        }

        // Update weight display
        const weightDisplayElement = document.getElementById('weightDisplay');
        if (weightDisplayElement) {
            weightDisplayElement.textContent = `${pricing.weight} كيلو`;
        }

        // Update COD amount display
        const codAmountDisplayElement = document.getElementById('codAmountDisplay');
        if (codAmountDisplayElement) {
            codAmountDisplayElement.textContent = DataUtils.formatCurrency(pricing.codAmount);
        }
    }

    /**
     * Clear pricing display
     */
    clearPricing() {
        this.updatePricingDisplay({
            shippingFee: 0,
            codFee: 0,
            totalFee: 0,
            weight: 0,
            codAmount: 0
        });
    }

    /**
     * Initialize form with default values
     */
    initializeForm() {
        // Set default values
        const currentDate = new Date().toISOString().split('T')[0];
        const dateInput = document.getElementById('preferredDeliveryDate');
        if (dateInput) {
            dateInput.value = currentDate;
        }

        // Set default weight
        const weightInput = document.getElementById('weight');
        if (weightInput) {
            weightInput.value = '1';
        }

        // Initialize pricing calculation
        this.calculatePricing();
    }

    /**
     * Handle form submission
     */
    async handleFormSubmit(e) {
        e.preventDefault();

        if (!this.validateForm()) {
            return;
        }

        try {
            UIUtils.showLoading();
            
            const formData = this.collectFormData();
            const response = await apiService.createShipment(formData);

            if (response.success) {
                NotificationService.success('تم إنشاء الشحنة بنجاح');
                this.resetForm();
                
                // Redirect to shipments list or show success details
                setTimeout(() => {
                    window.location.href = '/merchant/shipments.html';
                }, 2000);
            } else {
                
            }
        } catch (error) {

            
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Validate entire form
     */
    validateForm() {
        let isValid = true;

        // Required fields validation
        const requiredFields = [
            'zoneId', 'recipientName', 'recipientPhone', 'address',
            'weight', 'itemDescription'
        ];

        requiredFields.forEach(fieldName => {
            const field = document.getElementById(fieldName);
            if (field && !this.validateField(field)) {
                isValid = false;
            }
        });

        // Phone number validation
        const phoneField = document.getElementById('recipientPhone');
        if (phoneField && !this.validatePhoneNumber(phoneField.value)) {
            this.showFieldError(phoneField, 'رقم الهاتف غير صحيح');
            isValid = false;
        }

        // Weight validation
        const weightField = document.getElementById('weight');
        if (weightField && parseFloat(weightField.value) <= 0) {
            this.showFieldError(weightField, 'يجب أن يكون الوزن أكبر من صفر');
            isValid = false;
        }

        // COD amount validation
        const codAmountField = document.getElementById('codAmount');
        if (codAmountField && codAmountField.value && parseFloat(codAmountField.value) < 0) {
            this.showFieldError(codAmountField, 'مبلغ الدفع عند الاستلام يجب أن يكون أكبر من أو يساوي صفر');
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validate individual field
     */
    validateField(field) {
        const value = field.value.trim();
        const isRequired = field.hasAttribute('required');

        if (isRequired && !value) {
            this.showFieldError(field, 'هذا الحقل مطلوب');
            return false;
        }

        this.clearFieldError(field);
        return true;
    }

    /**
     * Validate phone number
     */
    validatePhoneNumber(phone) {
        const phoneRegex = /^(\+966|0)?[5-9][0-9]{8}$/;
        return phoneRegex.test(phone.replace(/\s/g, ''));
    }

    /**
     * Show field error
     */
    showFieldError(field, message) {
        field.classList.add('is-invalid');
        
        let errorElement = field.parentNode.querySelector('.invalid-feedback');
        if (!errorElement) {
            errorElement = document.createElement('div');
            errorElement.className = 'invalid-feedback';
            field.parentNode.appendChild(errorElement);
        }
        errorElement.textContent = message;
    }

    /**
     * Clear field error
     */
    clearFieldError(field) {
        field.classList.remove('is-invalid');
        const errorElement = field.parentNode.querySelector('.invalid-feedback');
        if (errorElement) {
            errorElement.remove();
        }
    }

    /**
     * Collect form data
     */
    collectFormData() {
        const formData = new FormData(this.form);
        const data = {};

        // Convert FormData to object
        for (let [key, value] of formData.entries()) {
            data[key] = value;
        }

        // Add calculated pricing
        data.shippingFee = this.calculateShippingFee(parseFloat(data.weight) || 0);
        data.codFee = this.calculateCodFee(parseFloat(data.codAmount) || 0);
        data.totalFee = data.shippingFee + data.codFee;

        // Add metadata
        data.createdAt = new Date().toISOString();
        data.status = 'PENDING';

        return data;
    }

    /**
     * Reset form
     */
    resetForm() {
        this.form.reset();
        this.clearPricing();
        this.clearAllFieldErrors();
        this.initializeForm();
    }

    /**
     * Clear all field errors
     */
    clearAllFieldErrors() {
        const invalidFields = this.form.querySelectorAll('.is-invalid');
        invalidFields.forEach(field => {
            this.clearFieldError(field);
        });
    }

    /**
     * Get zone pricing information
     */
    getZonePricingInfo(zoneId) {
        const zone = this.zones.find(z => z.id == zoneId);
        if (!zone) return null;

        return {
            name: zone.name,
            city: zone.city,
            defaultFee: zone.defaultFee,
            codFee: zone.codFee || 0,
            estimatedDeliveryTime: zone.estimatedDeliveryTime || '1-3 أيام'
        };
    }

    /**
     * Show zone information modal
     */
    showZoneInfo(zoneId) {
        const zoneInfo = this.getZonePricingInfo(zoneId);
        if (!zoneInfo) return;

        const modalHtml = `
            <div class="modal fade" id="zoneInfoModal" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">معلومات المنطقة</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-6">
                                    <strong>المنطقة:</strong> ${zoneInfo.name}
                                </div>
                                <div class="col-6">
                                    <strong>المدينة:</strong> ${zoneInfo.city}
                                </div>
                                <div class="col-6">
                                    <strong>رسوم الشحن الأساسية:</strong> ${DataUtils.formatCurrency(zoneInfo.defaultFee)}
                                </div>
                                <div class="col-6">
                                    <strong>رسوم الدفع عند الاستلام:</strong> ${zoneInfo.codFee}%
                                </div>
                                <div class="col-12">
                                    <strong>وقت التوصيل المتوقع:</strong> ${zoneInfo.estimatedDeliveryTime}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Remove existing modal
        const existingModal = document.getElementById('zoneInfoModal');
        if (existingModal) {
            existingModal.remove();
        }

        // Add new modal
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        
        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('zoneInfoModal'));
        modal.show();
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Only initialize on create shipment page
    if (window.location.pathname.includes('create-shipment')) {
        window.merchantCreateShipmentHandler = new MerchantCreateShipmentHandler();
    }
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = MerchantCreateShipmentHandler;
}
