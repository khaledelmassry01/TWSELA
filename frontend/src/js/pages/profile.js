import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('profile');

/**
 * Twsela CMS - Profile Page Handler
 * Handles profile page functionality including user data display, password change, and profile updates
 */

class ProfilePageHandler {
    constructor() {
        this.isEditing = false;
        this.init();
    }

    /**
     * Initialize profile page
     */
    init() {
        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setup());
        } else {
            this.setup();
        }
    }

    /**
     * Setup profile page
     */
    setup() {
        this.setupEventListeners();
        this.loadUserData();
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Edit profile button
        const editBtn = document.getElementById('editProfileBtn');
        if (editBtn) {
            editBtn.addEventListener('click', () => this.toggleEditMode());
        }

        // Save profile button
        const saveBtn = document.getElementById('saveProfileBtn');
        if (saveBtn) {
            saveBtn.addEventListener('click', () => this.saveProfile());
        }

        // Change password buttons
        const changePasswordBtn = document.getElementById('changePasswordBtn');
        const changePasswordBtn2 = document.getElementById('changePasswordBtn2');
        if (changePasswordBtn) {
            changePasswordBtn.addEventListener('click', () => this.showChangePasswordModal());
        }
        if (changePasswordBtn2) {
            changePasswordBtn2.addEventListener('click', () => this.showChangePasswordModal());
        }

        // Update password button
        const updatePasswordBtn = document.getElementById('updatePasswordBtn');
        if (updatePasswordBtn) {
            updatePasswordBtn.addEventListener('click', () => this.updatePassword());
        }

        // Logout button
        const logoutBtn = document.getElementById('logoutLink');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.logout();
            });
        }

        // Dashboard link
        const dashboardLink = document.getElementById('dashboardLink');
        if (dashboardLink) {
            dashboardLink.addEventListener('click', (e) => {
                e.preventDefault();
                this.redirectToDashboard();
            });
        }
    }

    /**
     * Load user data
     */
    async loadUserData() {
        try {
            if (window.authService) {
                const userData = await window.authService.getCurrentUser();
                if (userData) {
                    this.displayUserData(userData);
                }
            }
        } catch (error) {
            log.error('Error loading user data:', error);
        }
    }

    /**
     * Display user data
     */
    displayUserData(userData) {
        // Update name field
        const nameField = document.getElementById('name');
        if (nameField) {
            nameField.value = userData.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        }

        // Update phone field
        const phoneField = document.getElementById('phone');
        if (phoneField) {
            phoneField.value = userData.phone || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        }

        // Update role field
        const roleField = document.getElementById('role');
        if (roleField) {
            const roleNames = {
                'OWNER': 'Ø§Ù„Ù…Ø§Ù„Ùƒ',
                'ADMIN': 'Ø§Ù„Ù…Ø¯ÙŠØ±',
                'MERCHANT': 'Ø§Ù„ØªØ§Ø¬Ø±',
                'COURIER': 'Ø§Ù„Ø³Ø§Ø¦Ù‚',
                'WAREHOUSE_MANAGER': 'Ù…Ø¯ÙŠØ± Ø§Ù„Ù…Ø³ØªÙˆØ¯Ø¹'
            };
            roleField.value = roleNames[userData.role?.name] || userData.role?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        }

        // Update status field
        const statusField = document.getElementById('status');
        if (statusField) {
            const statusNames = {
                'ACTIVE': 'Ù†Ø´Ø·',
                'INACTIVE': 'ØºÙŠØ± Ù†Ø´Ø·',
                'SUSPENDED': 'Ù…Ø¹Ù„Ù‚',
                'PENDING': 'ÙÙŠ Ø§Ù„Ø§Ù†ØªØ¸Ø§Ø±'
            };
            statusField.value = statusNames[userData.status?.name] || userData.status?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        }
    }

    /**
     * Toggle edit mode
     */
    toggleEditMode() {
        this.isEditing = !this.isEditing;
        
        // Update UI based on edit mode
        const editBtn = document.getElementById('editProfileBtn');
        const saveBtn = document.getElementById('saveProfileBtn');
        
        if (this.isEditing) {
            if (editBtn) editBtn.style.display = 'none';
            if (saveBtn) saveBtn.style.display = 'inline-block';
        } else {
            if (editBtn) editBtn.style.display = 'inline-block';
            if (saveBtn) saveBtn.style.display = 'none';
        }
    }

    /**
     * Save profile
     */
    async saveProfile() {
        try {
            // Here you would implement profile saving logic
            log.debug('Saving profile...');
            this.toggleEditMode();
        } catch (error) {
            log.error('Error saving profile:', error);
        }
    }

    /**
     * Show change password modal
     */
    showChangePasswordModal() {
        const modal = new bootstrap.Modal(document.getElementById('changePasswordModal'));
        modal.show();
    }

    /**
     * Update password
     */
    async updatePassword() {
        try {
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (!currentPassword || !newPassword || !confirmPassword) {
                alert('ÙŠØ±Ø¬Ù‰ Ù…Ù„Ø¡ Ø¬Ù…ÙŠØ¹ Ø§Ù„Ø­Ù‚ÙˆÙ„');
                return;
            }

            if (newPassword !== confirmPassword) {
                alert('ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ± Ø§Ù„Ø¬Ø¯ÙŠØ¯Ø© ÙˆØªØ£ÙƒÙŠØ¯Ù‡Ø§ ØºÙŠØ± Ù…ØªØ·Ø§Ø¨Ù‚ØªÙŠÙ†');
                return;
            }

            if (window.authService) {
                await window.authService.changePassword(currentPassword, newPassword);
                alert('ØªÙ… ØªØ­Ø¯ÙŠØ« ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ± Ø¨Ù†Ø¬Ø§Ø­');
                
                // Close modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('changePasswordModal'));
                if (modal) modal.hide();
                
                // Clear form
                document.getElementById('changePasswordForm').reset();
            }
        } catch (error) {
            log.error('Error updating password:', error);
            alert('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ ØªØ­Ø¯ÙŠØ« ÙƒÙ„Ù…Ø© Ø§Ù„Ù…Ø±ÙˆØ±');
        }
    }

    /**
     * Logout user
     */
    async logout() {
        try {
            if (window.authService) {
                await window.authService.logout();
                window.location.href = '/login.html';
            }
        } catch (error) {
            log.error('Error logging out:', error);
        }
    }

    /**
     * Redirect to dashboard
     */
    redirectToDashboard() {
        // Get user role and redirect to appropriate dashboard
        if (window.authService && window.authService.getCurrentUserData()) {
            const userData = window.authService.getCurrentUserData();
            const role = userData.role?.name;
            
            switch (role) {
                case 'OWNER':
                    window.location.href = '/owner/dashboard.html';
                    break;
                case 'MERCHANT':
                    window.location.href = '/merchant/dashboard.html';
                    break;
                case 'COURIER':
                    window.location.href = '/courier/dashboard.html';
                    break;
                case 'WAREHOUSE_MANAGER':
                    window.location.href = '/warehouse/dashboard.html';
                    break;
                default:
                    window.location.href = '/index.html';
            }
        } else {
            window.location.href = '/index.html';
        }
    }
}

// Initialize profile page when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new ProfilePageHandler();
});
