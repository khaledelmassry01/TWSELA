/**
 * Twsela CMS - Support Page Handler
 * User-facing support ticket management (shared across all roles)
 */

class SupportPageHandler extends BasePageHandler {
    constructor() {
        super('Support');
        this.tickets = [];
        this.currentTicket = null;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            this.setupDashboardLink();
            await this.loadTickets();
        } catch (error) {
            ErrorHandler.handle(error, 'Support');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup dynamic dashboard link based on user role
     */
    setupDashboardLink() {
        const link = document.getElementById('backToDashboard');
        if (link && this.services.auth) {
            const user = this.services.auth.getUser?.() || {};
            const role = (user.role || '').toLowerCase();
            const roleMap = {
                'owner': '/owner/dashboard.html',
                'admin': '/admin/dashboard.html',
                'merchant': '/merchant/dashboard.html',
                'courier': '/courier/dashboard.html',
                'warehouse': '/warehouse/dashboard.html'
            };
            link.href = roleMap[role] || '/login.html';
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const createBtn = document.getElementById('createTicketBtn');
        if (createBtn) {
            createBtn.addEventListener('click', () => this.showCreateModal());
        }

        const createForm = document.getElementById('createTicketForm');
        if (createForm) {
            createForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleCreateTicket();
            });
        }

        const replyForm = document.getElementById('replyForm');
        if (replyForm) {
            replyForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleSendReply();
            });
        }

        const backBtn = document.getElementById('backToListBtn');
        if (backBtn) {
            backBtn.addEventListener('click', () => this.showTicketList());
        }

        const closeBtn = document.getElementById('closeTicketBtn');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.handleCloseTicket());
        }

        const statusFilter = document.getElementById('ticketStatusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', () => this.loadTickets());
        }
    }

    /**
     * Load user's tickets
     */
    async loadTickets() {
        try {
            const params = {};
            const status = document.getElementById('ticketStatusFilter')?.value;
            if (status) params.status = status;

            const response = await this.services.api.getMyTickets(params);
            if (response?.success) {
                this.tickets = response.data || [];
            } else {
                this.tickets = [];
            }
            this.renderTicketsList();
            this.updateStats();
        } catch (error) {
            ErrorHandler.handle(error, 'LoadTickets');
            this.tickets = [];
            this.renderTicketsList();
        }
    }

    /**
     * Update quick stats
     */
    updateStats() {
        const open = this.tickets.filter(t => t.status === 'OPEN').length;
        const inProgress = this.tickets.filter(t => ['IN_PROGRESS', 'WAITING_CUSTOMER', 'WAITING_AGENT'].includes(t.status)).length;
        const resolved = this.tickets.filter(t => ['RESOLVED', 'CLOSED'].includes(t.status)).length;

        const el = (id, val) => {
            const e = document.getElementById(id);
            if (e) e.textContent = val;
        };

        el('openTickets', open);
        el('inProgressTickets', inProgress);
        el('resolvedTickets', resolved);
    }

    /**
     * Render tickets list
     */
    renderTicketsList() {
        const container = document.getElementById('ticketsList');
        if (!container) return;

        if (this.tickets.length === 0) {
            container.innerHTML = `
                <div class="text-center text-muted py-4">
                    <i class="fas fa-ticket-alt fa-3x mb-3 d-block"></i>
                    <p>لا توجد تذاكر دعم</p>
                    <button class="btn btn-primary btn-sm" onclick="window.supportPageHandler.showCreateModal()">
                        <i class="fas fa-plus me-1"></i>إنشاء تذكرة
                    </button>
                </div>`;
            return;
        }

        const statusLabels = {
            'OPEN': { text: 'مفتوحة', class: 'bg-warning' },
            'IN_PROGRESS': { text: 'قيد المعالجة', class: 'bg-info' },
            'WAITING_CUSTOMER': { text: 'بانتظار ردك', class: 'bg-primary' },
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

        container.innerHTML = this.tickets.map(ticket => {
            const status = statusLabels[ticket.status] || { text: ticket.status, class: 'bg-secondary' };
            const priority = priorityLabels[ticket.priority] || { text: '', class: '' };
            const category = categoryLabels[ticket.category] || ticket.category;

            return `
                <div class="d-flex align-items-center p-3 border-bottom cursor-pointer ticket-item" 
                     onclick="window.supportPageHandler.viewTicket(${ticket.id})" style="cursor:pointer;">
                    <div class="flex-grow-1">
                        <h6 class="mb-1">${escapeHtml(ticket.subject || '')}</h6>
                        <small class="text-muted">
                            <span class="badge bg-light text-dark">${escapeHtml(category)}</span>
                            <span class="${priority.class} ms-2">${priority.text}</span>
                            <span class="ms-2">${ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString('ar-EG') : ''}</span>
                        </small>
                    </div>
                    <span class="badge ${status.class}">${status.text}</span>
                </div>`;
        }).join('');
    }

    /**
     * Show create ticket modal
     */
    showCreateModal() {
        const form = document.getElementById('createTicketForm');
        if (form) form.reset();
        const modal = new bootstrap.Modal(document.getElementById('createTicketModal'));
        modal.show();
    }

    /**
     * Handle create ticket
     */
    async handleCreateTicket() {
        try {
            const ticketData = {
                category: document.getElementById('ticketCategory')?.value,
                priority: document.getElementById('ticketPriority')?.value,
                subject: document.getElementById('ticketSubjectInput')?.value,
                description: document.getElementById('ticketDescription')?.value
            };

            const response = await this.services.api.createSupportTicket(ticketData);
            if (response?.success) {
                this.services.notification.success('تم إرسال تذكرة الدعم بنجاح');
                bootstrap.Modal.getInstance(document.getElementById('createTicketModal'))?.hide();
                await this.loadTickets();
            } else {
                this.services.notification.error(response?.message || 'فشل في إرسال التذكرة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CreateTicket');
        }
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
     * Render ticket detail view
     */
    renderTicketDetail() {
        const ticket = this.currentTicket;
        if (!ticket) return;

        // Hide list, show detail
        document.querySelector('.content-card:not(#ticketDetailSection)')?.closest('.content-card')?.classList.add('d-none');
        document.getElementById('ticketDetailSection')?.classList.remove('d-none');

        // Populate header
        document.getElementById('ticketSubject').textContent = ticket.subject || '';

        const statusBadge = document.getElementById('ticketStatusBadge');
        const statusMap = {
            'OPEN': 'bg-warning', 'IN_PROGRESS': 'bg-info', 'RESOLVED': 'bg-success',
            'CLOSED': 'bg-dark', 'WAITING_CUSTOMER': 'bg-primary', 'WAITING_AGENT': 'bg-secondary'
        };
        if (statusBadge) {
            statusBadge.className = `badge ${statusMap[ticket.status] || 'bg-secondary'}`;
            statusBadge.textContent = ticket.status;
        }

        const catBadge = document.getElementById('ticketCategoryBadge');
        if (catBadge) catBadge.textContent = ticket.category || '';

        // Render messages
        const messagesContainer = document.getElementById('ticketMessages');
        if (messagesContainer) {
            const messages = ticket.messages || [];
            if (messages.length === 0) {
                messagesContainer.innerHTML = `
                    <div class="p-3 bg-light rounded mb-2">
                        <p class="mb-0">${escapeHtml(ticket.description || '')}</p>
                        <small class="text-muted">${ticket.createdAt ? new Date(ticket.createdAt).toLocaleString('ar-EG') : ''}</small>
                    </div>`;
            } else {
                messagesContainer.innerHTML = messages.map(msg => `
                    <div class="p-3 ${msg.isStaff ? 'bg-info bg-opacity-10' : 'bg-light'} rounded mb-2">
                        <div class="d-flex justify-content-between mb-1">
                            <strong>${msg.isStaff ? '<i class="fas fa-headset me-1"></i>الدعم' : 'أنت'}</strong>
                            <small class="text-muted">${msg.createdAt ? new Date(msg.createdAt).toLocaleString('ar-EG') : ''}</small>
                        </div>
                        <p class="mb-0">${escapeHtml(msg.message || '')}</p>
                    </div>`).join('');
            }
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }

        // Hide reply section if ticket is closed/resolved
        const replySection = document.getElementById('replySection');
        if (replySection) {
            replySection.classList.toggle('d-none', ['CLOSED', 'RESOLVED'].includes(ticket.status));
        }
    }

    /**
     * Show ticket list (go back from detail)
     */
    showTicketList() {
        document.getElementById('ticketDetailSection')?.classList.add('d-none');
        document.querySelectorAll('.content-card.d-none').forEach(el => {
            if (el.id !== 'ticketDetailSection') el.classList.remove('d-none');
        });
        this.currentTicket = null;
    }

    /**
     * Handle send reply
     */
    async handleSendReply() {
        try {
            if (!this.currentTicket) return;
            const message = document.getElementById('replyMessage')?.value;
            if (!message?.trim()) return;

            const response = await this.services.api.sendTicketMessage(this.currentTicket.id, message);
            if (response?.success) {
                document.getElementById('replyMessage').value = '';
                this.services.notification.success('تم إرسال الرد');
                await this.viewTicket(this.currentTicket.id);
            } else {
                this.services.notification.error(response?.message || 'فشل في إرسال الرد');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'SendReply');
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
                this.showTicketList();
                await this.loadTickets();
            } else {
                this.services.notification.error(response?.message || 'فشل في إغلاق التذكرة');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CloseTicket');
        }
    }
}

// Create global instance
window.supportPageHandler = new SupportPageHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/support.html') && !window.location.pathname.includes('/admin/')) {
        setTimeout(() => {
            window.supportPageHandler.init();
        }, 200);
    }
});
