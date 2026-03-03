import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('notifications-page');

/**
 * Twsela CMS - Notifications Center Page Handler
 * Displays notifications list with filtering and preference management
 * Shared page for all authenticated roles
 */

class NotificationsPageHandler extends BasePageHandler {
    constructor() {
        super('Notifications');
        this.notifications = [];
        this.currentPage = 0;
        this.pageSize = 20;
        this.totalElements = 0;
        this.unreadCount = 0;
        this.activeFilter = 'all';
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();

            // Build sidebar
            this.buildSidebar();

            // Load unread count
            await this.loadUnreadCount();

            // Load notifications
            await this.loadNotifications();

            // Load preferences into modal
            await this.loadPreferences();

        } catch (error) {
            ErrorHandler.handle(error, 'NotificationsPage');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Build sidebar navigation based on user role
     */
    buildSidebar() {
        const nav = document.getElementById('sidebarNav');
        if (!nav) return;

        const user = this.getCurrentUser();
        const role = user?.role?.name || sessionStorage.getItem('userRole') || '';

        let links = '';
        if (role === 'OWNER') {
            links = `
                <a href="/owner/dashboard.html" class="nav-link"><i class="fas fa-tachometer-alt"></i><span>لوحة التحكم</span></a>
                <a href="/owner/shipments.html" class="nav-link"><i class="fas fa-shipping-fast"></i><span>الشحنات</span></a>
                <a href="/wallet.html" class="nav-link"><i class="fas fa-wallet"></i><span>المحفظة</span></a>
                <a href="/notifications.html" class="nav-link active"><i class="fas fa-bell"></i><span>الإشعارات</span></a>
            `;
        } else if (role === 'MERCHANT') {
            links = `
                <a href="/merchant/dashboard.html" class="nav-link"><i class="fas fa-tachometer-alt"></i><span>لوحة التحكم</span></a>
                <a href="/merchant/shipments.html" class="nav-link"><i class="fas fa-shipping-fast"></i><span>الشحنات</span></a>
                <a href="/wallet.html" class="nav-link"><i class="fas fa-wallet"></i><span>المحفظة</span></a>
                <a href="/notifications.html" class="nav-link active"><i class="fas fa-bell"></i><span>الإشعارات</span></a>
            `;
        } else if (role === 'COURIER') {
            links = `
                <a href="/courier/dashboard.html" class="nav-link"><i class="fas fa-tachometer-alt"></i><span>لوحة التحكم</span></a>
                <a href="/courier/manifest.html" class="nav-link"><i class="fas fa-clipboard-list"></i><span>المانيفست</span></a>
                <a href="/wallet.html" class="nav-link"><i class="fas fa-wallet"></i><span>المحفظة</span></a>
                <a href="/notifications.html" class="nav-link active"><i class="fas fa-bell"></i><span>الإشعارات</span></a>
            `;
        } else {
            links = `
                <a href="/notifications.html" class="nav-link active"><i class="fas fa-bell"></i><span>الإشعارات</span></a>
            `;
        }

        links += `
            <div class="nav-divider"></div>
            <a href="/profile.html" class="nav-link"><i class="fas fa-user"></i><span>الملف الشخصي</span></a>
            <a href="/settings.html" class="nav-link"><i class="fas fa-cog"></i><span>الإعدادات</span></a>
            <a href="#" class="nav-link logout-link" data-action="logout"><i class="fas fa-sign-out-alt"></i><span>تسجيل الخروج</span></a>
        `;

        nav.innerHTML = links;
    }

    /**
     * Load unread notifications count
     */
    async loadUnreadCount() {
        try {
            const response = await this.services.api.getUnreadNotifications();
            if (response.success && response.data) {
                this.unreadCount = response.data.count || 0;
                const el = document.getElementById('unreadCount');
                if (el) el.textContent = this.unreadCount;
            }
        } catch (error) {
            log.error('Error loading unread count:', error);
        }
    }

    /**
     * Load notifications
     */
    async loadNotifications() {
        try {
            const response = await this.services.api.getNotifications({
                page: this.currentPage,
                size: this.pageSize
            });

            if (response.success) {
                const data = response.data;
                // Handle paginated response
                const items = data?.content || data || [];
                this.notifications = this.currentPage === 0 ? items : [...this.notifications, ...items];
                this.totalElements = data?.totalElements || this.notifications.length;

                const totalEl = document.getElementById('totalNotifications');
                if (totalEl) totalEl.textContent = this.totalElements;

                this.renderNotifications();

                // Show/hide load more button
                const loadMoreBtn = document.getElementById('loadMoreBtn');
                if (loadMoreBtn) {
                    loadMoreBtn.style.display =
                        this.notifications.length < this.totalElements ? 'inline-block' : 'none';
                }
            }
        } catch (error) {
            ErrorHandler.handle(error, 'NotificationsPage.loadNotifications');
        }
    }

    /**
     * Render notifications list
     */
    renderNotifications() {
        const container = document.getElementById('notificationsList');
        if (!container) return;

        // Apply filter
        let filtered = [...this.notifications];
        if (this.activeFilter === 'unread') {
            filtered = filtered.filter(n => !n.read);
        } else if (this.activeFilter !== 'all') {
            filtered = filtered.filter(n => (n.type || '').includes(this.activeFilter));
        }

        if (filtered.length === 0) {
            container.innerHTML = `
                <div class="text-center p-5 text-muted">
                    <i class="fas fa-bell-slash fa-3x mb-3"></i>
                    <p>لا توجد إشعارات</p>
                </div>
            `;
            return;
        }

        container.innerHTML = filtered.map(notification => `
            <div class="list-group-item list-group-item-action ${notification.read ? '' : 'bg-light border-start border-primary border-3'}"
                 data-notification-id="${notification.id}" style="cursor: pointer;">
                <div class="d-flex w-100 justify-content-between align-items-start">
                    <div class="d-flex align-items-start">
                        <div class="me-3 mt-1">
                            <i class="fas ${this.getNotificationIcon(notification.type)} fa-lg ${notification.read ? 'text-muted' : 'text-primary'}"></i>
                        </div>
                        <div>
                            <h6 class="mb-1 ${notification.read ? 'text-muted' : 'fw-bold'}">${escapeHtml(notification.title || 'إشعار')}</h6>
                            <p class="mb-1 small ${notification.read ? 'text-muted' : ''}">${escapeHtml(notification.message || notification.body || '')}</p>
                        </div>
                    </div>
                    <small class="text-muted text-nowrap ms-3">${this.formatTimeAgo(notification.createdAt)}</small>
                </div>
            </div>
        `).join('');
    }

    /**
     * Get notification icon based on type
     */
    getNotificationIcon(type) {
        const icons = {
            'SHIPMENT': 'fa-shipping-fast',
            'WALLET': 'fa-wallet',
            'RETURN': 'fa-undo',
            'PAYOUT': 'fa-money-bill-wave',
            'SYSTEM': 'fa-cog',
            'ALERT': 'fa-exclamation-triangle'
        };
        // Check if type contains any of the keys
        for (const [key, icon] of Object.entries(icons)) {
            if (type && type.includes(key)) return icon;
        }
        return 'fa-bell';
    }

    /**
     * Format time ago
     */
    formatTimeAgo(dateString) {
        if (!dateString) return '';
        const now = new Date();
        const date = new Date(dateString);
        const diff = Math.floor((now - date) / 1000);

        if (diff < 60) return 'الآن';
        if (diff < 3600) return `منذ ${Math.floor(diff / 60)} دقيقة`;
        if (diff < 86400) return `منذ ${Math.floor(diff / 3600)} ساعة`;
        if (diff < 604800) return `منذ ${Math.floor(diff / 86400)} يوم`;
        return new Date(dateString).toLocaleDateString('ar-SA', {
            year: 'numeric', month: 'short', day: 'numeric'
        });
    }

    /**
     * Load notification preferences
     */
    async loadPreferences() {
        try {
            const response = await this.services.api.getNotificationPreferences();
            if (response.success && response.data) {
                const pref = response.data;
                const channels = pref.enabledChannels || {};

                const pushEl = document.getElementById('channelPush');
                if (pushEl) pushEl.checked = channels.PUSH !== false;

                const smsEl = document.getElementById('channelSms');
                if (smsEl) smsEl.checked = channels.SMS === true;

                const emailEl = document.getElementById('channelEmail');
                if (emailEl) emailEl.checked = channels.EMAIL === true;

                const quietStart = document.getElementById('quietStart');
                if (quietStart && pref.quietHoursStart) quietStart.value = pref.quietHoursStart;

                const quietEnd = document.getElementById('quietEnd');
                if (quietEnd && pref.quietHoursEnd) quietEnd.value = pref.quietHoursEnd;

                const digestMode = document.getElementById('digestMode');
                if (digestMode && pref.digestMode) digestMode.value = pref.digestMode;
            }
        } catch (error) {
            log.error('Error loading preferences:', error);
        }
    }

    /**
     * Save notification preferences
     */
    async savePreferences() {
        try {
            const data = {
                enabledChannels: {
                    PUSH: document.getElementById('channelPush')?.checked || false,
                    SMS: document.getElementById('channelSms')?.checked || false,
                    EMAIL: document.getElementById('channelEmail')?.checked || false
                },
                quietHoursStart: document.getElementById('quietStart')?.value || null,
                quietHoursEnd: document.getElementById('quietEnd')?.value || null,
                digestMode: document.getElementById('digestMode')?.value || 'NONE'
            };

            const btn = document.getElementById('savePreferencesBtn');
            UIUtils.showButtonLoading(btn, 'جاري الحفظ...');

            const response = await this.services.api.updateNotificationPreferences(data);

            if (response.success) {
                this.services.notification.success('تم حفظ التفضيلات بنجاح');
                const modal = bootstrap.Modal.getInstance(document.getElementById('preferencesModal'));
                if (modal) modal.hide();
            } else {
                ErrorHandler.handle(response, 'NotificationsPage.savePreferences');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'NotificationsPage.savePreferences');
        } finally {
            const btn = document.getElementById('savePreferencesBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Mark single notification as read
     */
    async markAsRead(notificationId) {
        try {
            const response = await this.services.api.markNotificationRead(notificationId);
            if (response.success) {
                // Update local state
                const notification = this.notifications.find(n => n.id === notificationId);
                if (notification) notification.read = true;
                this.unreadCount = Math.max(0, this.unreadCount - 1);

                const el = document.getElementById('unreadCount');
                if (el) el.textContent = this.unreadCount;

                this.renderNotifications();
            }
        } catch (error) {
            log.error('Error marking notification as read:', error);
        }
    }

    /**
     * Mark all notifications as read
     */
    async markAllAsRead() {
        try {
            const response = await this.services.api.markAllNotificationsRead();
            if (response.success) {
                this.notifications.forEach(n => n.read = true);
                this.unreadCount = 0;

                const el = document.getElementById('unreadCount');
                if (el) el.textContent = '0';

                this.renderNotifications();
                this.services.notification.success('تم تحديد الكل كمقروء');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'NotificationsPage.markAllAsRead');
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Mark all read button
        const markAllBtn = document.getElementById('markAllReadBtn');
        if (markAllBtn) {
            markAllBtn.addEventListener('click', () => this.markAllAsRead());
        }

        // Save preferences
        const savePrefsBtn = document.getElementById('savePreferencesBtn');
        if (savePrefsBtn) {
            savePrefsBtn.addEventListener('click', () => this.savePreferences());
        }

        // Filter buttons
        document.querySelectorAll('[data-filter]').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('[data-filter]').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.activeFilter = btn.dataset.filter;
                this.renderNotifications();
            });
        });

        // Click on notification to mark as read
        const list = document.getElementById('notificationsList');
        if (list) {
            list.addEventListener('click', (e) => {
                const item = e.target.closest('[data-notification-id]');
                if (item) {
                    const id = parseInt(item.dataset.notificationId);
                    if (id) this.markAsRead(id);
                }
            });
        }

        // Load more
        const loadMoreBtn = document.getElementById('loadMoreBtn');
        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', () => {
                this.currentPage++;
                this.loadNotifications();
            });
        }
    }
}

// Create global instance
window.notificationsPageHandler = new NotificationsPageHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/notifications.html')) {
        setTimeout(() => {
            window.notificationsPageHandler.init();
        }, 200);
    }
});
