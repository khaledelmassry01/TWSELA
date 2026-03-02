/**
 * Twsela CMS - Notification Bell Component
 * Injects a notification bell into the page header and manages real-time notifications
 * via WebSocket. Auto-initializes when imported.
 */
class NotificationBell {
    constructor() {
        this.bellEl = null;
        this.badgeEl = null;
        this.dropdownEl = null;
        this.unreadCount = 0;
        this.notifications = [];
        this.isOpen = false;
        this.wsSubId = null;
    }

    /**
     * Initialize the notification bell - call after DOM is ready
     */
    async init() {
        const headerRight = document.querySelector('.header-right');
        if (!headerRight) {
            log.debug('No .header-right element found, skipping notification bell');
            return;
        }

        this._injectBellHTML(headerRight);
        this._bindEvents();
        await this._loadInitialData();
        this._connectWebSocket();
    }

    /**
     * Inject bell icon HTML into header
     */
    _injectBellHTML(headerRight) {
        const bellWrapper = document.createElement('div');
        bellWrapper.className = 'notification-bell-wrapper';
        bellWrapper.innerHTML = `
            <button class="notification-bell-btn" id="notificationBellBtn" aria-label="الإشعارات" title="الإشعارات">
                <i class="fas fa-bell"></i>
                <span class="notification-badge" id="notificationBadge" style="display:none">0</span>
            </button>
            <div class="notification-dropdown" id="notificationDropdown" style="display:none">
                <div class="notification-dropdown-header">
                    <h6>الإشعارات</h6>
                    <button class="mark-all-read-btn" id="markAllReadBtn" title="تحديد الكل كمقروء">
                        <i class="fas fa-check-double"></i>
                    </button>
                </div>
                <div class="notification-dropdown-body" id="notificationList">
                    <div class="notification-empty">لا توجد إشعارات</div>
                </div>
                <div class="notification-dropdown-footer">
                    <a href="#" id="viewAllNotifications">عرض جميع الإشعارات</a>
                </div>
            </div>
        `;

        // Insert before the profile dropdown
        const profileDropdown = headerRight.querySelector('.profile-dropdown');
        if (profileDropdown) {
            headerRight.insertBefore(bellWrapper, profileDropdown);
        } else {
            headerRight.prepend(bellWrapper);
        }

        this.bellEl = document.getElementById('notificationBellBtn');
        this.badgeEl = document.getElementById('notificationBadge');
        this.dropdownEl = document.getElementById('notificationDropdown');

        // Inject minimal styles if not already present
        this._injectStyles();
    }

    /**
     * Bind click events
     */
    _bindEvents() {
        // Toggle dropdown
        this.bellEl?.addEventListener('click', (e) => {
            e.stopPropagation();
            this.isOpen = !this.isOpen;
            this.dropdownEl.style.display = this.isOpen ? 'block' : 'none';
        });

        // Mark all as read
        document.getElementById('markAllReadBtn')?.addEventListener('click', async () => {
            await this._markAllAsRead();
        });

        // Close dropdown on outside click
        document.addEventListener('click', (e) => {
            if (this.isOpen && !e.target.closest('.notification-bell-wrapper')) {
                this.isOpen = false;
                this.dropdownEl.style.display = 'none';
            }
        });
    }

    /**
     * Load initial unread notifications from API
     */
    async _loadInitialData() {
        try {
            const token = sessionStorage.getItem('authToken');
            if (!token) return;

            const baseUrl = window.getApiBaseUrl ? window.getApiBaseUrl() : '';
            const response = await fetch(`${baseUrl}/api/notifications/unread`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) return;

            const body = await response.json();
            const data = body.data || body;
            this.unreadCount = data.count || 0;
            this.notifications = data.notifications || [];

            this._updateBadge();
            this._renderNotifications();
        } catch (error) {
            log.warn('Failed to load notifications:', error);
        }
    }

    /**
     * Connect to WebSocket for real-time notifications
     */
    _connectWebSocket() {
        try {
            const userStr = sessionStorage.getItem('userData') || sessionStorage.getItem('currentUser');
            if (!userStr) return;

            let user;
            try { user = JSON.parse(userStr); } catch { return; }

            const userId = user.id || user.userId;
            if (!userId) return;

            window.websocketService.connect();
            this.wsSubId = window.websocketService.subscribeNotifications(userId, (notification) => {
                this._onNewNotification(notification);
            });
        } catch (error) {
            log.warn('Failed to connect WebSocket for notifications:', error);
        }
    }

    /**
     * Handle incoming real-time notification
     */
    _onNewNotification(notification) {
        this.unreadCount++;
        this.notifications.unshift(notification);

        // Keep only latest 10 in dropdown
        if (this.notifications.length > 10) {
            this.notifications = this.notifications.slice(0, 10);
        }

        this._updateBadge();
        this._renderNotifications();

        // Play subtle sound or show toast if available
        if (window.NotificationService) {
            window.NotificationService.info(notification.title || 'إشعار جديد');
        }
    }

    /**
     * Update badge count
     */
    _updateBadge() {
        if (this.unreadCount > 0) {
            this.badgeEl.textContent = this.unreadCount > 99 ? '99+' : this.unreadCount;
            this.badgeEl.style.display = 'flex';
        } else {
            this.badgeEl.style.display = 'none';
        }
    }

    /**
     * Render notification items in dropdown
     */
    _renderNotifications() {
        const listEl = document.getElementById('notificationList');
        if (!listEl) return;

        if (this.notifications.length === 0) {
            listEl.innerHTML = '<div class="notification-empty">لا توجد إشعارات</div>';
            return;
        }

        listEl.innerHTML = this.notifications.map(n => `
            <div class="notification-item ${n.read ? '' : 'unread'}" data-id="${n.id}">
                <div class="notification-icon">
                    <i class="fas ${this._getIconForType(n.type)}"></i>
                </div>
                <div class="notification-content">
                    <div class="notification-title">${this._escapeHtml(n.title || '')}</div>
                    <div class="notification-message">${this._escapeHtml(n.message || '')}</div>
                    <div class="notification-time">${this._formatTime(n.createdAt)}</div>
                </div>
            </div>
        `).join('');

        // Click to mark as read
        listEl.querySelectorAll('.notification-item.unread').forEach(el => {
            el.addEventListener('click', () => this._markAsRead(el.dataset.id));
        });
    }

    /**
     * Mark single notification as read
     */
    async _markAsRead(id) {
        try {
            const token = sessionStorage.getItem('authToken');
            const baseUrl = window.getApiBaseUrl ? window.getApiBaseUrl() : '';
            await fetch(`${baseUrl}/api/notifications/${id}/read`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            const item = this.notifications.find(n => String(n.id) === String(id));
            if (item) item.read = true;
            this.unreadCount = Math.max(0, this.unreadCount - 1);

            this._updateBadge();
            this._renderNotifications();
        } catch (error) {
            log.warn('Failed to mark notification as read:', error);
        }
    }

    /**
     * Mark all notifications as read
     */
    async _markAllAsRead() {
        try {
            const token = sessionStorage.getItem('authToken');
            const baseUrl = window.getApiBaseUrl ? window.getApiBaseUrl() : '';
            await fetch(`${baseUrl}/api/notifications/read-all`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            this.notifications.forEach(n => n.read = true);
            this.unreadCount = 0;

            this._updateBadge();
            this._renderNotifications();
        } catch (error) {
            log.warn('Failed to mark all as read:', error);
        }
    }

    /**
     * Get FontAwesome icon class for notification type
     */
    _getIconForType(type) {
        const icons = {
            'SHIPMENT_STATUS': 'fa-shipping-fast',
            'SHIPMENT_ASSIGNED': 'fa-user-check',
            'PAYOUT_PROCESSED': 'fa-money-bill-wave',
            'SYSTEM_ALERT': 'fa-exclamation-triangle',
            'WELCOME': 'fa-hand-wave',
            'PASSWORD_RESET': 'fa-key'
        };
        return icons[type] || 'fa-bell';
    }

    /**
     * Format timestamp for display
     */
    _formatTime(timestamp) {
        if (!timestamp) return '';
        try {
            const date = new Date(timestamp);
            const now = new Date();
            const diffMs = now - date;
            const diffMin = Math.floor(diffMs / 60000);
            const diffHr = Math.floor(diffMs / 3600000);
            const diffDay = Math.floor(diffMs / 86400000);

            if (diffMin < 1) return 'الآن';
            if (diffMin < 60) return `منذ ${diffMin} دقيقة`;
            if (diffHr < 24) return `منذ ${diffHr} ساعة`;
            if (diffDay < 7) return `منذ ${diffDay} يوم`;
            return date.toLocaleDateString('ar-EG');
        } catch {
            return '';
        }
    }

    /**
     * Basic HTML escaping
     */
    _escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Inject minimal CSS for the notification bell
     */
    _injectStyles() {
        if (document.getElementById('notification-bell-styles')) return;

        const style = document.createElement('style');
        style.id = 'notification-bell-styles';
        style.textContent = `
            .notification-bell-wrapper {
                position: relative;
                margin-left: 12px;
                margin-right: 12px;
            }
            .notification-bell-btn {
                background: none;
                border: none;
                font-size: 1.2rem;
                color: var(--text-color, #333);
                cursor: pointer;
                position: relative;
                padding: 8px;
            }
            .notification-bell-btn:hover {
                color: var(--primary-color, #0d6efd);
            }
            .notification-badge {
                position: absolute;
                top: 2px;
                right: 0;
                background: #dc3545;
                color: #fff;
                border-radius: 50%;
                font-size: 0.65rem;
                min-width: 18px;
                height: 18px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-weight: bold;
            }
            .notification-dropdown {
                position: absolute;
                top: 100%;
                left: 0;
                width: 320px;
                background: #fff;
                border-radius: 8px;
                box-shadow: 0 4px 20px rgba(0,0,0,0.15);
                z-index: 1050;
                max-height: 400px;
                overflow: hidden;
                direction: rtl;
            }
            .notification-dropdown-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 12px 16px;
                border-bottom: 1px solid #eee;
            }
            .notification-dropdown-header h6 {
                margin: 0;
                font-weight: 600;
            }
            .mark-all-read-btn {
                background: none;
                border: none;
                color: #6c757d;
                cursor: pointer;
                font-size: 0.9rem;
            }
            .mark-all-read-btn:hover { color: #0d6efd; }
            .notification-dropdown-body {
                max-height: 300px;
                overflow-y: auto;
            }
            .notification-item {
                display: flex;
                gap: 10px;
                padding: 10px 16px;
                border-bottom: 1px solid #f0f0f0;
                cursor: pointer;
                transition: background 0.15s;
            }
            .notification-item:hover { background: #f8f9fa; }
            .notification-item.unread { background: #f0f7ff; }
            .notification-icon {
                flex-shrink: 0;
                width: 32px;
                height: 32px;
                border-radius: 50%;
                background: #e9ecef;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 0.85rem;
                color: #495057;
            }
            .notification-content { flex: 1; min-width: 0; }
            .notification-title {
                font-weight: 600;
                font-size: 0.85rem;
                margin-bottom: 2px;
            }
            .notification-message {
                font-size: 0.8rem;
                color: #6c757d;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .notification-time {
                font-size: 0.7rem;
                color: #adb5bd;
                margin-top: 2px;
            }
            .notification-empty {
                padding: 30px 16px;
                text-align: center;
                color: #adb5bd;
            }
            .notification-dropdown-footer {
                padding: 10px 16px;
                text-align: center;
                border-top: 1px solid #eee;
            }
            .notification-dropdown-footer a {
                color: var(--primary-color, #0d6efd);
                text-decoration: none;
                font-size: 0.85rem;
            }
        `;
        document.head.appendChild(style);
    }

    /**
     * Clean up subscriptions
     */
    destroy() {
        if (this.wsSubId && window.websocketService) {
            window.websocketService.unsubscribe(this.wsSubId);
        }
    }
}

// Auto-initialize singleton when DOM is ready
const notificationBell = new NotificationBell();

function initBell() {
    notificationBell.init().catch(e => log.warn('NotificationBell init failed:', e));
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initBell);
} else {
    initBell();
}

window.NotificationBell = notificationBell;
