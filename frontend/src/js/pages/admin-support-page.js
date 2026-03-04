/**
 * Twsela CMS - Admin Support Page Handler
 * Admin-facing support ticket management and assignment
 */

class AdminSupportPageHandler extends BasePageHandler {
    constructor() {
        super('AdminSupport');
        this.tickets = [];
        this.currentTicket = null;
        this.stats = {};
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            await Promise.all([
                this.loadStats(),
                this.loadTickets()
            ]);
        } catch (error) {
            ErrorHandler.handle(error, 'AdminSupport');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Filters
        ['filterStatus', 'filterPriority', 'filterCategory'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.addEventListener('change', () => this.loadTickets());
        });

        const searchInput = document.getElementById('filterSearch');
        if (searchInput) {
            let timeout;
            searchInput.addEventListener('input', () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => this.loadTickets(), 500);
            });
        }

        // Back to list
        const backBtn = document.getElementById('backToListBtn');
        if (backBtn) {
            backBtn.addEventListener('click', () => this.showTicketList());
        }

        // Assign to me
        const assignToMeBtn = document.getElementById('assignToMeBtn');
        if (assignToMeBtn) {
            assignToMeBtn.addEventListener('click', () => this.handleAssignToMe());
        }

        // Resolve ticket
        const resolveBtn = document.getElementById('resolveTicketBtn');
        if (resolveBtn) {
            resolveBtn.addEventListener('click', () => this.handleResolveTicket());
        }

        // Close ticket
        const closeBtn = document.getElementById('closeTicketBtn');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.handleCloseTicket());
        }

        // Admin reply
        const replyForm = document.getElementById('adminReplyForm');
        if (replyForm) {
            replyForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleSendReply();
            });
        }

        // Confirm assign
        const confirmAssign = document.getElementById('confirmAssignBtn');
        if (confirmAssign) {
            confirmAssign.addEventListener('click', () => this.handleConfirmAssign());
        }
    }

    /**
     * Load support statistics
     */
    async loadStats() {
        try {
            const response = await this.services.api.getSupportStats();
            if (response?.success) {
                this.stats = response.data || {};
                this.renderStats();
            }
        } catch (error) {
            console.error('Failed to load support stats:', error);
        }
    }

    /**
     * Render statistics
     */
    renderStats() {
        const el = (id, val) => {
            const e = document.getElementById(id);
            if (e) e.textContent = val;
        };

        el('openCount', this.stats.openCount || 0);
        el('inProgressCount', this.stats.inProgressCount || 0);
        el('resolvedCount', this.stats.resolvedCount || 0);
        el('avgResponseTime', this.stats.avgResponseTime ? `${this.stats.avgResponseTime} د` : '-');
    }

    /**
     * Load all tickets
     */
    async loadTickets() {
        try {
            const params = {};
            const status = document.getElementById('filterStatus')?.value;
            const priority = document.getElementById('filterPriority')?.value;
            const category = document.getElementById('filterCategory')?.value;
            const search = document.getElementById('filterSearch')?.value;

            if (status) params.status = status;
            if (priority) params.priority = priority;
            if (category) params.category = category;
            if (search) params.search = search;

            const response = await this.services.api.getAdminTickets(params);
            if (response?.success) {
                this.tickets = response.data?.content || response.data || [];
            } else {
                this.tickets = [];
            }

            this.renderTicketsTable();
        } catch (error) {
            ErrorHandler.handle(error, 'LoadTickets');
            this.tickets = [];
            this.renderTicketsTable();
        }
    }

    /**
     * Render tickets table
     */
    renderTicketsTable() {
        const tbody = document.getElementById('ticketsTableBody');
        if (!tbody) return;

        document.getElementById('totalTicketsCount').textContent = this.tickets.length;

        if (this.tickets.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center py-4 text-muted">
                        <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                        لا توجد تذاكر
                    </td>
                </tr>`;
            return;
        }

        const statusLabels = {
            'OPEN': { text: 'مفتوحة', class: 'bg-warning' },
            'IN_PROGRESS': { text: 'قيد المعالجة', class: 'bg-info' },
            'WAITING_CUSTOMER': { text: 'بانتظار العميل', class: 'bg-primary' },
            'WAITING_AGENT': { text: 'بانتظار الموظف', class: 'bg-secondary' },
            'RESOLVED': { text: 'تم الحل', class: 'bg-success' },
            'CLOSED': { text: 'مغلقة', class: 'bg-dark' },
            'REOPENED': { text: 'أعيد فتحها', class: 'bg-warning' }
        };

        const priorityLabels = {
            'LOW': { text: 'منخفضة', class: 'text-secondary' },
            'MEDIUM': { text: 'متوسطة', class: 'text-warning' },
            'HIGH': { text: 'عالية', class: 'text-danger' },
            'URGENT': { text: 'عاجلة', class: 'text-danger fw-bold' }
        };

        const categoryLabels = {
            'SHIPMENT_ISSUE': 'مشكلة شحنة',
            'PAYMENT_ISSUE': 'مشكلة دفع',
            'ACCOUNT_ISSUE': 'مشكلة حساب',
            'TECHNICAL_ISSUE': 'مشكلة تقنية',
            'PICKUP_ISSUE': 'مشكلة استلام',
            'DELIVERY_ISSUE': 'مشكلة توصيل',
            'BILLING_ISSUE': 'مشكلة فواتير',
            'OTHER': 'أخرى'
        };

        tbody.innerHTML = this.tickets.map(ticket => {
            const status = statusLabels[ticket.status] || { text: ticket.status, class: 'bg-secondary' };
            const priority = priorityLabels[ticket.priority] || { text: '', class: '' };
            const category = categoryLabels[ticket.category] || ticket.category || '-';

            return `
                <tr>
                    <td>${ticket.id || '-'}</td>
                    <td>
                        <a href="#" class="text-decoration-none" onclick="window.adminSupportHandler.viewTicket(${ticket.id}); return false;">
                            ${escapeHtml(ticket.subject || '')}
                        </a>
                    </td>
                    <td>${escapeHtml(ticket.userName || ticket.userEmail || '-')}</td>
                    <td><span class="badge bg-light text-dark">${escapeHtml(category)}</span></td>
                    <td><span class="${priority.class}">${priority.text}</span></td>
                    <td><span class="badge ${status.class}">${status.text}</span></td>
                    <td>${escapeHtml(ticket.agentName || 'غير معين')}</td>
                    <td><small>${ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString('ar-EG') : '-'}</small></td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-primary" onclick="window.adminSupportHandler.viewTicket(${ticket.id})" title="عرض">
                                <i class="fas fa-eye"></i>
                            </button>
                            ${!ticket.agentId ? `
                            <button class="btn btn-outline-success" onclick="window.adminSupportHandler.handleAssignTicket(${ticket.id})" title="تعيين">
                                <i class="fas fa-user-check"></i>
                            </button>` : ''}
                        </div>
                    </td>
                </tr>`;
        }).join('');
    }

    /**
     * View ticket detail
     */
    async viewTicket(ticketId) {
        try {
            const response = await this.services.api.getTicket(ticketId);
            if (response?.success) {
                this.currentTicket = response.data;
                this.renderTicketDetail();
            }
        } catch (error) {
            ErrorHandler.handle(error, 'ViewTicket');
        }
    }

    /**
     * Render ticket detail
     */
    renderTicketDetail() {
        const ticket = this.currentTicket;
        if (!ticket) return;

        // Toggle sections
        document.getElementById('ticketsListSection')?.classList.add('d-none');
        document.querySelector('.content-card.mb-4:first-of-type')?.classList.add('d-none');
        document.getElementById('ticketDetailSection')?.classList.remove('d-none');

        // Header
        document.getElementById('detailSubject').textContent = ticket.subject || '';
        const statusBadge = document.getElementById('detailStatusBadge');
        const statusMap = {
            'OPEN': 'bg-warning', 'IN_PROGRESS': 'bg-info', 'RESOLVED': 'bg-success',
            'CLOSED': 'bg-dark', 'WAITING_CUSTOMER': 'bg-primary', 'WAITING_AGENT': 'bg-secondary'
        };
        if (statusBadge) {
            statusBadge.className = `badge ${statusMap[ticket.status] || 'bg-secondary'}`;
            statusBadge.textContent = ticket.status;
        }

        // Info
        document.getElementById('detailUser').textContent = ticket.userName || ticket.userEmail || '-';
        document.getElementById('detailCategory').textContent = ticket.category || '-';
        document.getElementById('detailPriority').textContent = ticket.priority || '-';
        document.getElementById('detailDate').textContent = ticket.createdAt ? new Date(ticket.createdAt).toLocaleString('ar-EG') : '-';
        document.getElementById('detailAgent').textContent = ticket.agentName || 'غير معين';

        // Messages
        const messagesContainer = document.getElementById('detailMessages');
        if (messagesContainer) {
            const messages = ticket.messages || [];

            let html = `
                <div class="p-3 bg-light rounded mb-2">
                    <div class="d-flex justify-content-between mb-1">
                        <strong>${escapeHtml(ticket.userName || 'المستخدم')}</strong>
                        <small class="text-muted">${ticket.createdAt ? new Date(ticket.createdAt).toLocaleString('ar-EG') : ''}</small>
                    </div>
                    <p class="mb-0">${escapeHtml(ticket.description || '')}</p>
                </div>`;

            html += messages.map(msg => `
                <div class="p-3 ${msg.isStaff ? 'bg-info bg-opacity-10' : 'bg-light'} rounded mb-2">
                    <div class="d-flex justify-content-between mb-1">
                        <strong>${msg.isStaff ? '<i class="fas fa-headset me-1"></i>' + escapeHtml(msg.senderName || 'الدعم') : escapeHtml(msg.senderName || 'المستخدم')}</strong>
                        <small class="text-muted">${msg.createdAt ? new Date(msg.createdAt).toLocaleString('ar-EG') : ''}</small>
                    </div>
                    <p class="mb-0">${escapeHtml(msg.message || '')}</p>
                </div>`).join('');

            messagesContainer.innerHTML = html;
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }

        // Hide reply and actions if closed
        const isClosed = ['CLOSED', 'RESOLVED'].includes(ticket.status);
        document.getElementById('adminReplySection')?.classList.toggle('d-none', isClosed);
        document.getElementById('resolveTicketBtn')?.classList.toggle('d-none', isClosed);
        document.getElementById('closeTicketBtn')?.classList.toggle('d-none', ticket.status === 'CLOSED');
    }

    /**
     * Show ticket list (back from detail)
     */
    showTicketList() {
        document.getElementById('ticketDetailSection')?.classList.add('d-none');
        document.getElementById('ticketsListSection')?.classList.remove('d-none');
        document.querySelectorAll('.content-card.d-none').forEach(el => {
            if (el.id !== 'ticketDetailSection') el.classList.remove('d-none');
        });
        this.currentTicket = null;
    }

    /**
     * Handle assign to me
     */
    async handleAssignToMe() {
        if (!this.currentTicket) return;
        try {
            const response = await this.services.api.assignTicket(this.currentTicket.id);
            if (response?.success) {
                this.services.notification.success('تم تعيين التذكرة لك');
                await this.viewTicket(this.currentTicket.id);
                await this.loadStats();
            } else {
                this.services.notification.error(response?.message || 'فشل في تعيين التذكرة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'AssignToMe');
        }
    }

    /**
     * Handle assign ticket to agent (from table)
     */
    handleAssignTicket(ticketId) {
        this.pendingAssignTicketId = ticketId;
        const modal = new bootstrap.Modal(document.getElementById('assignModal'));
        modal.show();
    }

    /**
     * Handle confirm assign from modal
     */
    async handleConfirmAssign() {
        try {
            const agentId = document.getElementById('assignAgent')?.value;
            if (!agentId) {
                this.services.notification.warning('اختر موظف أولاً');
                return;
            }
            const response = await this.services.api.assignTicket(this.pendingAssignTicketId, agentId);
            if (response?.success) {
                this.services.notification.success('تم تعيين التذكرة');
                bootstrap.Modal.getInstance(document.getElementById('assignModal'))?.hide();
                await this.loadTickets();
                await this.loadStats();
            } else {
                this.services.notification.error(response?.message || 'فشل في تعيين التذكرة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'AssignTicket');
        }
    }

    /**
     * Handle resolve ticket
     */
    async handleResolveTicket() {
        if (!this.currentTicket) return;
        if (!confirm('هل أنت متأكد من تحويل حالة التذكرة إلى "تم الحل"؟')) return;

        try {
            const response = await this.services.api.resolveTicket(this.currentTicket.id);
            if (response?.success) {
                this.services.notification.success('تم حل التذكرة');
                await this.viewTicket(this.currentTicket.id);
                await this.loadStats();
            } else {
                this.services.notification.error(response?.message || 'فشل في حل التذكرة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'ResolveTicket');
        }
    }

    /**
     * Handle close ticket
     */
    async handleCloseTicket() {
        if (!this.currentTicket) return;
        if (!confirm('هل أنت متأكد من إغلاق هذه التذكرة؟')) return;

        try {
            const response = await this.services.api.closeSupportTicket(this.currentTicket.id);
            if (response?.success) {
                this.services.notification.success('تم إغلاق التذكرة');
                await this.viewTicket(this.currentTicket.id);
                await this.loadStats();
            } else {
                this.services.notification.error(response?.message || 'فشل في إغلاق التذكرة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CloseTicket');
        }
    }

    /**
     * Handle send reply
     */
    async handleSendReply() {
        try {
            if (!this.currentTicket) return;
            const message = document.getElementById('adminReplyMessage')?.value;
            if (!message?.trim()) return;

            const response = await this.services.api.sendTicketMessage(this.currentTicket.id, message);
            if (response?.success) {
                document.getElementById('adminReplyMessage').value = '';
                this.services.notification.success('تم إرسال الرد');
                await this.viewTicket(this.currentTicket.id);
            } else {
                this.services.notification.error(response?.message || 'فشل في إرسال الرد');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'SendReply');
        }
    }
}

// Create global instance
window.adminSupportHandler = new AdminSupportPageHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/admin/support.html')) {
        setTimeout(() => {
            window.adminSupportHandler.init();
        }, 200);
    }
});
