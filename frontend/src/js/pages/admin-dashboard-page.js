/**
 * Twsela CMS - Admin Dashboard Page Handler
 * Handles admin panel: user management, system stats, audit logs, quick actions
 */

class AdminDashboardHandler extends BasePageHandler {
    constructor() {
        super('Admin Dashboard');
        this.users = [];
        this.activities = [];
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();

            // Setup event listeners
            this.setupQuickActions();
            this.setupUserForm();

            // Load dashboard data
            await this.loadDashboardData();
        } catch (error) {
            ErrorHandler.handle(error, 'AdminDashboard');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load all dashboard data
     */
    async loadDashboardData() {
        try {
            const [usersRes, merchantsRes, couriersRes] = await Promise.allSettled([
                window.apiService.getUsers ? window.apiService.getUsers() : Promise.reject('Not available'),
                window.apiService.getMerchants({ limit: 100 }),
                window.apiService.getCouriers({ limit: 100 })
            ]);

            // Process users
            if (usersRes.status === 'fulfilled' && usersRes.value?.success) {
                this.users = usersRes.value.data || [];
            } else {
                // Fallback: combine merchants and couriers as user entries
                const merchants = (merchantsRes.status === 'fulfilled' && merchantsRes.value?.success) 
                    ? (merchantsRes.value.data || []).map(m => ({ ...m, role: 'MERCHANT' })) : [];
                const couriers = (couriersRes.status === 'fulfilled' && couriersRes.value?.success) 
                    ? (couriersRes.value.data || []).map(c => ({ ...c, role: 'COURIER' })) : [];
                this.users = [...merchants, ...couriers];
            }

            // Update stats
            this.updateStatistics();

            // Render tables
            this.renderUsersTable();
            this.renderRecentActivity();

        } catch (error) {
            ErrorHandler.handle(error, 'AdminDashboard.loadData');
        }
    }

    /**
     * Update statistics cards
     */
    updateStatistics() {
        const totalUsers = this.users.length;
        const activeUsers = this.users.filter(u => u.status === 'ACTIVE' || u.active).length;
        const merchants = this.users.filter(u => u.role === 'MERCHANT').length;
        const couriers = this.users.filter(u => u.role === 'COURIER').length;

        const statValues = document.querySelectorAll('.stat-value');
        if (statValues.length >= 4) {
            statValues[0].textContent = totalUsers;
            statValues[1].textContent = activeUsers;
            statValues[2].textContent = merchants;
            statValues[3].textContent = couriers;
        }
    }

    /**
     * Render users table
     */
    renderUsersTable() {
        const tbody = document.querySelector('#usersTable tbody');
        if (!tbody) return;

        if (this.users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">\u0644\u0627 \u062a\u0648\u062c\u062f \u0628\u064a\u0627\u0646\u0627\u062a</td></tr>';
            return;
        }

        tbody.innerHTML = this.users.slice(0, 50).map(user => `
            <tr>
                <td>${escapeHtml(user.name || '')}</td>
                <td>${escapeHtml(user.phone || '')}</td>
                <td><span class="badge bg-info">${escapeHtml(this.getRoleText(user.role))}</span></td>
                <td><span class="badge bg-${user.status === 'ACTIVE' || user.active ? 'success' : 'secondary'}">${user.status === 'ACTIVE' || user.active ? '\u0646\u0634\u0637' : '\u063a\u064a\u0631 \u0646\u0634\u0637'}</span></td>
                <td>${SharedDataUtils.formatDate(user.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary edit-user-btn" data-user-id="${user.id}" title="\u062a\u0639\u062f\u064a\u0644">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger toggle-user-btn" data-user-id="${user.id}" data-active="${user.status === 'ACTIVE' || user.active}" title="\u062a\u063a\u064a\u064a\u0631 \u0627\u0644\u062d\u0627\u0644\u0629">
                        <i class="fas fa-${user.status === 'ACTIVE' || user.active ? 'ban' : 'check'}"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    /**
     * Get Arabic text for role
     */
    getRoleText(role) {
        const roles = {
            'OWNER': '\u0645\u0627\u0644\u0643',
            'ADMIN': '\u0645\u062f\u064a\u0631',
            'MERCHANT': '\u062a\u0627\u062c\u0631',
            'COURIER': '\u0633\u0627\u0626\u0642',
            'WAREHOUSE': '\u0645\u0633\u062a\u0648\u062f\u0639'
        };
        return roles[role] || role || '\u063a\u064a\u0631 \u0645\u062d\u062f\u062f';
    }

    /**
     * Render recent activity
     */
    renderRecentActivity() {
        const container = document.getElementById('recentActivity');
        if (!container) return;

        if (this.activities.length === 0) {
            container.innerHTML = '<p class="text-center text-muted">\u0644\u0627 \u062a\u0648\u062c\u062f \u0623\u0646\u0634\u0637\u0629 \u062d\u062f\u064a\u062b\u0629</p>';
            return;
        }

        container.innerHTML = this.activities.map(activity => `
            <div class="activity-item">
                <div class="activity-icon">
                    <i class="fas fa-info-circle"></i>
                </div>
                <div class="activity-content">
                    <p class="activity-text">${escapeHtml(activity.description || '')}</p>
                    <small class="activity-time">${SharedDataUtils.formatDate(activity.timestamp)}</small>
                </div>
            </div>
        `).join('');
    }

    /**
     * Setup quick action buttons
     */
    setupQuickActions() {
        document.querySelectorAll('.quick-action-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const action = btn.dataset.action;
                this.handleQuickAction(action);
            });
        });

        // Refresh button
        const refreshBtn = document.getElementById('refreshDataBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', async () => {
                await this.loadDashboardData();
                NotificationService.success('\u062a\u0645 \u062a\u062d\u062f\u064a\u062b \u0627\u0644\u0628\u064a\u0627\u0646\u0627\u062a');
            });
        }

        // Event delegation for table actions
        document.addEventListener('click', (e) => {
            const editBtn = e.target.closest('.edit-user-btn');
            if (editBtn) {
                this.editUser(editBtn.dataset.userId);
                return;
            }

            const toggleBtn = e.target.closest('.toggle-user-btn');
            if (toggleBtn) {
                this.toggleUserStatus(toggleBtn.dataset.userId, toggleBtn.dataset.active === 'true');
                return;
            }
        });
    }

    /**
     * Handle quick action buttons
     */
    handleQuickAction(action) {
        switch (action) {
            case 'addUser':
                this.showAddUserModal();
                break;
            case 'createZone':
                window.location.href = '/owner/zones.html';
                break;
            case 'viewReports':
                window.location.href = '/owner/reports.html';
                break;
            case 'systemSettings':
                window.location.href = '/settings.html';
                break;
        }
    }

    /**
     * Show add user modal
     */
    showAddUserModal() {
        const modal = document.getElementById('addUserModal');
        if (modal) {
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
        }
    }

    /**
     * Setup user form submission
     */
    setupUserForm() {
        const form = document.getElementById('addUserForm');
        if (!form) return;

        // Show/hide merchant fields based on role selection
        const roleSelect = document.getElementById('role');
        if (roleSelect) {
            roleSelect.addEventListener('change', () => {
                const isMerchant = roleSelect.value === 'MERCHANT';
                document.querySelectorAll('[id^="merchantFields"]').forEach(el => {
                    el.classList.toggle('d-none', !isMerchant);
                });
            });
        }

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.createUser(new FormData(form));
        });
    }

    /**
     * Create a new user
     */
    async createUser(formData) {
        try {
            UIUtils.showLoading();
            const userData = Object.fromEntries(formData.entries());

            let response;
            if (userData.role === 'MERCHANT') {
                response = await window.apiService.createMerchant(userData);
            } else if (userData.role === 'COURIER') {
                response = await window.apiService.createCourier(userData);
            } else {
                response = await window.apiService.createUser(userData);
            }

            if (response?.success) {
                NotificationService.success('\u062a\u0645 \u0625\u0646\u0634\u0627\u0621 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645 \u0628\u0646\u062c\u0627\u062d');
                // Close modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('addUserModal'));
                if (modal) modal.hide();
                // Reload data
                await this.loadDashboardData();
            }
        } catch (error) {
            ErrorHandler.handle(error, 'AdminDashboard.createUser');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Edit user (redirect to detailed view)
     */
    editUser(userId) {
        // In a full implementation, this would open an edit modal
        NotificationService.info('\u062c\u0627\u0631\u064a \u0641\u062a\u062d \u0628\u064a\u0627\u0646\u0627\u062a \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645...');
    }

    /**
     * Toggle user active/inactive status
     */
    async toggleUserStatus(userId, isCurrentlyActive) {
        const result = await Swal.fire({
            title: isCurrentlyActive ? '\u062a\u0639\u0637\u064a\u0644 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645' : '\u062a\u0641\u0639\u064a\u0644 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645',
            text: isCurrentlyActive 
                ? '\u0647\u0644 \u0623\u0646\u062a \u0645\u062a\u0623\u0643\u062f \u0645\u0646 \u062a\u0639\u0637\u064a\u0644 \u0647\u0630\u0627 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645\u061f' 
                : '\u0647\u0644 \u0623\u0646\u062a \u0645\u062a\u0623\u0643\u062f \u0645\u0646 \u062a\u0641\u0639\u064a\u0644 \u0647\u0630\u0627 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645\u061f',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: isCurrentlyActive ? '#ef4444' : '#22c55e',
            cancelButtonColor: '#6b7280',
            confirmButtonText: isCurrentlyActive ? '\u0646\u0639\u0645\u060c \u062a\u0639\u0637\u064a\u0644' : '\u0646\u0639\u0645\u060c \u062a\u0641\u0639\u064a\u0644',
            cancelButtonText: '\u0625\u0644\u063a\u0627\u0621'
        });

        if (!result.isConfirmed) return;

        try {
            UIUtils.showLoading();
            const newStatus = isCurrentlyActive ? 'INACTIVE' : 'ACTIVE';
            await window.apiService.updateUserStatus(userId, newStatus);
            NotificationService.success('\u062a\u0645 \u062a\u062d\u062f\u064a\u062b \u062d\u0627\u0644\u0629 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645');
            await this.loadDashboardData();
        } catch (error) {
            ErrorHandler.handle(error, 'AdminDashboard.toggleStatus');
        } finally {
            UIUtils.hideLoading();
        }
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (typeof BasePageHandler !== 'undefined') {
        window.adminDashboardHandler = new AdminDashboardHandler();
    }
});
