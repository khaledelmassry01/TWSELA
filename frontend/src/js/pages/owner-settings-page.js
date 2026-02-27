import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('owner-settings-page');

/**
 * Twsela CMS - Owner Settings Page Handler
 * Manages system settings for the Owner role
 */

class OwnerSettingsPageHandler {
    constructor() {
        this.apiService = window.apiService;
        this.init();
    }

    async init() {
        try {
            if (document.readyState === 'loading') {
                await new Promise(resolve => document.addEventListener('DOMContentLoaded', resolve));
            }
            UIUtils.showLoading();
            await this.loadSettings();
            this.setupEventListeners();
            log.debug('✅ Owner Settings page initialized');
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerSettings');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load current system settings from API
     */
    async loadSettings() {
        try {
            if (!this.apiService) {
                log.warn('ApiService not available yet');
                return;
            }
            const response = await this.apiService.getSettings();
            if (response && response.success && response.data) {
                this.populateSettingsForm(response.data);
            }
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerSettings.loadSettings');
        }
    }

    /**
     * Populate the settings form with data from the API
     */
    populateSettingsForm(data) {
        const fields = document.querySelectorAll('[data-setting]');
        fields.forEach(field => {
            const key = field.getAttribute('data-setting');
            if (data[key] !== undefined) {
                if (field.type === 'checkbox') {
                    field.checked = data[key];
                } else {
                    field.value = data[key];
                }
            }
        });
    }

    /**
     * Setup event listeners for form submissions
     */
    setupEventListeners() {
        // Save settings button
        const saveBtn = document.getElementById('saveSettingsBtn');
        if (saveBtn) {
            saveBtn.addEventListener('click', () => this.saveSettings());
        }

        // Form submit handlers
        const forms = document.querySelectorAll('form[data-settings-form]');
        forms.forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.saveSettings();
            });
        });
    }

    /**
     * Save settings to the API
     */
    async saveSettings() {
        try {
            const saveBtn = document.getElementById('saveSettingsBtn');
            if (saveBtn) UIUtils.showButtonLoading(saveBtn, 'جاري الحفظ...');

            const fields = document.querySelectorAll('[data-setting]');
            const settings = {};
            fields.forEach(field => {
                const key = field.getAttribute('data-setting');
                settings[key] = field.type === 'checkbox' ? field.checked : field.value;
            });

            const response = await this.apiService.updateSettings(settings);

            if (response && response.success) {
                UIUtils.showSuccess('تم حفظ الإعدادات بنجاح');
            } else {
                UIUtils.showError('فشل في حفظ الإعدادات');
            }

            if (saveBtn) UIUtils.hideButtonLoading(saveBtn);
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerSettings.saveSettings');
            const saveBtn = document.getElementById('saveSettingsBtn');
            if (saveBtn) UIUtils.hideButtonLoading(saveBtn);
        }
    }

    /**
     * Show a success/error message
     */
    showMessage(text, type = 'success') {
        const container = document.getElementById('messageContainer');
        if (!container) return;

        const alertClass = type === 'success'
            ? 'bg-green-100 border-green-400 text-green-700'
            : 'bg-red-100 border-red-400 text-red-700';

        const msg = document.createElement('div');
        msg.className = `border px-4 py-3 rounded relative mb-2 ${alertClass}`;
        msg.textContent = text;
        container.appendChild(msg);

        setTimeout(() => msg.remove(), 4000);
    }
}

// Initialize when DOM is ready
window.ownerSettingsHandler = new OwnerSettingsPageHandler();
