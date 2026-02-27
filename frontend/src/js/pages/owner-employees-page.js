import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('owner-employees-page');

/**
 * Twsela CMS - Owner Employees Page Handler
 * Handles employee management for owner
 */

// owner-employees-page.js loaded - console.log removed for cleaner console

class OwnerEmployeesHandler extends BasePageHandler {
    constructor() {
        super('Owner Employees');
        this.employees = [];
        this.currentPage = 1;
        this.pageSize = 10;
        // OwnerEmployeesHandler constructor - console.log removed for cleaner console
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
        
            // Load employees data
            await this.loadEmployees();
            
            // Setup pagination
            this.setupPagination();
            
            // Setup modal event listeners
            this.setupModalEventListeners();
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerEmployees');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load employees data
     */
    async loadEmployees() {
        
        try {
            UIUtils.showTableLoading('#employeesTable');
            const response = await this.services.api.getEmployees({
                page: this.currentPage,
                size: this.pageSize
            });
            
            if (response.success) {
                this.employees = response.data || [];
                this.updateEmployeesTable();
                this.updatePaginationInfo(response.totalElements || 0);
            } else {
                UIUtils.showEmptyState('#employeesTable tbody', 'لا توجد موظفين', 'users');
            }
            
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerEmployees.loadEmployees');
        }
    }

    /**
     * Update employees table
     */
    updateEmployeesTable() {
        const tbody = document.querySelector('#employeesTable tbody');
        if (!tbody) return;

        // Clear existing rows
        tbody.innerHTML = '';

        if (!this.employees || this.employees.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Ù„Ø§ ØªÙˆØ¬Ø¯ Ù…ÙˆØ¸ÙÙŠÙ†</td></tr>';
            return;
        }

        // Add employee rows
        this.employees.forEach(employee => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${escapeHtml(employee.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                <td>${escapeHtml(employee.phone || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                <td><span class="badge bg-${this.getStatusColor(employee.status)}">${escapeHtml(employee.status?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</span></td>
                <td>${escapeHtml(employee.role?.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
                <td>${this.formatDate(employee.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="ownerEmployeesHandler.editEmployee(${employee.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="ownerEmployeesHandler.deleteEmployee(${employee.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * Setup pagination
     */
    setupPagination() {
        // Setting up pagination - console.log removed for cleaner console
        
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        
        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                if (this.currentPage > 1) {
                    this.currentPage--;
                    this.loadEmployees();
                }
            });
        }
        
        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                this.currentPage++;
                this.loadEmployees();
            });
        }
    }

    /**
     * Update pagination info
     */
    updatePaginationInfo(totalElements) {
        const paginationInfo = document.getElementById('paginationInfo');
        if (paginationInfo) {
            const startItem = (this.currentPage - 1) * this.pageSize + 1;
            const endItem = Math.min(this.currentPage * this.pageSize, totalElements);
            paginationInfo.textContent = `Ø¹Ø±Ø¶ ${startItem}-${endItem} Ù…Ù† ${totalElements}`;
        }
    }

    /**
     * Edit employee
     */
    async editEmployee(employeeId) {
        try {
            // TODO: Implement edit functionality
            this.services.notification.info('ØªØ¹Ø¯ÙŠÙ„ Ø§Ù„Ù…ÙˆØ¸Ù Ù‚ÙŠØ¯ Ø§Ù„ØªØ·ÙˆÙŠØ±');
        } catch (error) {
            log.error('Error editing employee:', error);
        }
    }

    /**
     * Delete employee
     */
    async deleteEmployee(employeeId) {
        try {
            if (confirm('هل أنت متأكد من حذف هذا الموظف؟')) {
                UIUtils.showLoading();
                const response = await this.services.api.deleteEmployee(employeeId);
                
                if (response.success) {
                    this.services.notification.success('تم حذف الموظف بنجاح');
                    this.loadEmployees(); // Reload data
                } else {
                    ErrorHandler.handle(response, 'OwnerEmployees.deleteEmployee');
                }
            }
        } catch (error) {
            ErrorHandler.handle(error, 'OwnerEmployees.deleteEmployee');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        log.debug('ðŸ”§ Setting up event listeners...');
        
        // Add employee button - Try multiple ways to find it
        let addEmployeeBtn = document.getElementById('addEmployeeBtn');
        // Add employee button found - console.log removed for cleaner console
        
        if (!addEmployeeBtn) {
            // Try to find it after a delay
            setTimeout(() => {
                addEmployeeBtn = document.getElementById('addEmployeeBtn');
                // Add employee button found (delayed) - console.log removed for cleaner console
                if (addEmployeeBtn) {
                    this.bindAddEmployeeButton(addEmployeeBtn);
                }
            }, 1000);
        } else {
            this.bindAddEmployeeButton(addEmployeeBtn);
        }

        // Form submission
        const addEmployeeForm = document.getElementById('addEmployeeForm');
        // Add employee form found - console.log removed for cleaner console
        
        if (addEmployeeForm) {
            addEmployeeForm.addEventListener('submit', (e) => {
                // Form submitted - console.log removed for cleaner console
                e.preventDefault();
                this.handleAddEmployee();
            });
            // Add employee form event listener added - console.log removed for cleaner console
        } else {
            log.error('âŒ Add employee form not found!');
        }

        // Logout button
        const logoutBtn = document.querySelector('.logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                this.handleLogout();
            });
        }
    }

    /**
     * Setup modal event listeners
     */
    setupModalEventListeners() {
        // Setting up modal event listeners - console.log removed for cleaner console
        
        // Wait for modal to be available
        setTimeout(() => {
            const addEmployeeBtn = document.getElementById('addEmployeeBtn');
            if (addEmployeeBtn) {
                // Modal button found, binding event listener - console.log removed for cleaner console
                this.bindAddEmployeeButton(addEmployeeBtn);
            } else {
                log.error('ðŸŽ­ Modal button not found!');
            }
        }, 500);
        
        // Also listen for modal events
        const modal = document.getElementById('addEmployeeModal');
        if (modal) {
            modal.addEventListener('shown.bs.modal', () => {
                // Modal shown, ensuring button is bound - console.log removed for cleaner console
                const addEmployeeBtn = document.getElementById('addEmployeeBtn');
                if (addEmployeeBtn) {
                    this.bindAddEmployeeButton(addEmployeeBtn);
                }
            });
        }
    }
    bindAddEmployeeButton(button) {
        if (!button) return;
        
        // Remove any existing listeners
        button.removeEventListener('click', this.handleAddEmployee);
        
        // Add new listener
        button.addEventListener('click', (e) => {
            // Add employee button clicked - console.log removed for cleaner console
            e.preventDefault();
            this.handleAddEmployee();
        });
        
        // Add employee button event listener added - console.log removed for cleaner console
    }

    /**
     * Get status color class
     */
    getStatusColor(status) {
        if (!status) return 'secondary';
        
        const statusName = status.name?.toLowerCase();
        switch (statusName) {
            case 'active':
            case 'Ù†Ø´Ø·':
                return 'success';
            case 'inactive':
            case 'ØºÙŠØ± Ù†Ø´Ø·':
                return 'secondary';
            case 'pending':
            case 'Ù…Ø¹Ù„Ù‚':
                return 'warning';
            case 'suspended':
            case 'Ù…Ø¹Ù„Ù‚':
                return 'danger';
            default:
                return 'secondary';
        }
    }

    /**
     * Format date
     */
    formatDate(dateString) {
        if (!dateString) return 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('ar-SA', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch (error) {
            return 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
        }
    }
    async handleAddEmployee() {
        // handleAddEmployee called - console.log removed for cleaner console
        
        try {
            // Get form data
            const form = document.getElementById('addEmployeeForm');
            if (!form) {
                log.error('Add employee form not found');
                return;
            }

            const formData = new FormData(form);
            const employeeData = {
                name: formData.get('fullName'),
                phone: formData.get('phone'),
                role: formData.get('role'),
                password: formData.get('password'),
                active: document.getElementById('activeStatus').checked
            };

            // Employee data - console.log removed for cleaner console

            // Validate required fields
            if (!employeeData.name || !employeeData.phone || !employeeData.role || !employeeData.password) {
                log.error('Missing required fields');
                UIUtils.showWarning('يرجى ملء جميع الحقول المطلوبة');
                return;
            }

            // Show loading state
            const addBtn = document.getElementById('addEmployeeBtn');
            UIUtils.showButtonLoading(addBtn, 'جاري الإضافة...');

            // Calling API - console.log removed for cleaner console
            // Call API
            const response = await this.services.api.createEmployee(employeeData);
            // API response - console.log removed for cleaner console

            if (response.success) {
                // Employee added successfully - console.log removed for cleaner console
                
                // Close modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('addEmployeeModal'));
                if (modal) {
                    modal.hide();
                }

                // Reset form
                form.reset();

                // Reload employees list
                await this.loadEmployees();

                // Show success message
                this.services.notification.success('ØªÙ… Ø¥Ø¶Ø§ÙØ© Ø§Ù„Ù…ÙˆØ¸Ù Ø¨Ù†Ø¬Ø§Ø­');
            } else {
                ErrorHandler.handle(response, 'OwnerEmployees.addEmployee');
            }

        } catch (error) {
            ErrorHandler.handle(error, 'OwnerEmployees.addEmployee');
        } finally {
            // Reset button state
            const addBtn = document.getElementById('addEmployeeBtn');
            UIUtils.hideButtonLoading(addBtn);
        }
    }
}

// Create global instance
// Creating global OwnerEmployeesHandler instance - console.log removed for cleaner console
window.ownerEmployeesHandler = new OwnerEmployeesHandler();
// Global instance created - console.log removed for cleaner console

// Make handleAddEmployee globally accessible
window.handleAddEmployee = function() {
    if (window.ownerEmployeesHandler) {
        return window.ownerEmployeesHandler.handleAddEmployee();
    } else {
        log.error('OwnerEmployeesHandler not available');
    }
};

// Global handleAddEmployee function created - console.log removed for cleaner console

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // DOMContentLoaded fired - console.log removed for cleaner console
    // Current pathname - console.log removed for cleaner console
    
    // Only initialize if this is the owner employees page
    if (window.location.pathname.includes('/owner/employees.html')) {
        // Owner employees page detected - console.log removed for cleaner console
        setTimeout(() => {
            log.debug('ðŸŽ¯ Starting Owner Employees initialization...');
            window.ownerEmployeesHandler.init();
        }, 200);
    } else {
        log.debug('âŒ Not owner employees page, skipping initialization');
    }
});
