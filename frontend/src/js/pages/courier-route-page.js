import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('courier-route-page');

/**
 * Twsela CMS - Courier Route Page Handler
 * Handles route display and optimization for couriers
 */

class CourierRouteHandler extends BasePageHandler {
    constructor() {
        super('Courier Route');
        this.route = null;
        this.courierId = null;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            const userData = this.services.auth.getUserData();
            this.courierId = userData?.id || userData?.userId;
            await this.loadRoute();
        } catch (error) {
            ErrorHandler.handle(error, 'CourierRoute');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Load current route
     */
    async loadRoute() {
        try {
            if (!this.courierId) {
                this.showNoRoute();
                return;
            }

            const response = await this.services.api.getCourierRoute(this.courierId);

            if (response.success && response.data) {
                this.route = response.data;
                this.updateRouteDisplay();
            } else {
                this.showNoRoute();
            }
        } catch (error) {
            if (error.status === 404) {
                this.showNoRoute();
            } else {
                ErrorHandler.handle(error, 'CourierRoute.loadRoute');
            }
        }
    }

    /**
     * Show no route state
     */
    showNoRoute() {
        const noRoute = document.getElementById('noRouteState');
        const list = document.getElementById('waypointsList');
        if (noRoute) noRoute.classList.remove('d-none');
        if (list) list.innerHTML = '';
    }

    /**
     * Update route display
     */
    updateRouteDisplay() {
        if (!this.route) return;

        const noRoute = document.getElementById('noRouteState');
        if (noRoute) noRoute.classList.add('d-none');

        // Update stats
        const waypoints = this.route.waypoints || [];
        const totalStopsEl = document.getElementById('totalStops');
        if (totalStopsEl) totalStopsEl.textContent = waypoints.length;

        const distanceEl = document.getElementById('totalDistance');
        if (distanceEl) distanceEl.textContent = (this.route.totalDistanceKm || 0).toFixed(1);

        const timeEl = document.getElementById('estimatedTime');
        if (timeEl) timeEl.textContent = this.route.estimatedDurationMinutes || 0;

        const completedEl = document.getElementById('completedStops');
        if (completedEl) completedEl.textContent = waypoints.filter(w => w.completed || w.status === 'COMPLETED').length;

        // Render waypoints
        this.renderWaypoints(waypoints);
    }

    /**
     * Render waypoints list
     */
    renderWaypoints(waypoints) {
        const list = document.getElementById('waypointsList');
        if (!list) return;

        list.innerHTML = '';

        if (!waypoints || waypoints.length === 0) {
            this.showNoRoute();
            return;
        }

        waypoints.forEach((wp, index) => {
            const isCompleted = wp.completed || wp.status === 'COMPLETED';
            const item = document.createElement('div');
            item.className = `d-flex align-items-start p-3 mb-2 border rounded ${isCompleted ? 'bg-success bg-opacity-10' : ''}`;
            item.innerHTML = `
                <div class="me-3">
                    <div class="rounded-circle d-flex align-items-center justify-content-center ${isCompleted ? 'bg-success' : 'bg-primary'} text-white" style="width:36px;height:36px;font-weight:bold;">
                        ${index + 1}
                    </div>
                </div>
                <div class="flex-grow-1">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h6 class="mb-1">${escapeHtml(wp.recipientName || wp.address || 'نقطة ' + (index + 1))}</h6>
                            <p class="text-muted small mb-0">${escapeHtml(wp.address || wp.addressLine || '-')}</p>
                            ${wp.trackingNumber ? `<small class="text-muted"><i class="fas fa-hashtag me-1"></i>${escapeHtml(wp.trackingNumber)}</small>` : ''}
                        </div>
                        <div>
                            ${isCompleted
                                ? '<span class="badge bg-success"><i class="fas fa-check me-1"></i>تم</span>'
                                : '<span class="badge bg-warning">قيد التسليم</span>'
                            }
                        </div>
                    </div>
                    ${wp.estimatedArrival ? `<small class="text-muted mt-1 d-block"><i class="fas fa-clock me-1"></i>الوصول المتوقع: ${wp.estimatedArrival}</small>` : ''}
                </div>
            `;
            list.appendChild(item);
        });
    }

    /**
     * Optimize route
     */
    async optimizeRoute() {
        try {
            if (!this.courierId) {
                this.services.notification.warning('لم يتم تحديد المندوب');
                return;
            }

            if (!this.route || !this.route.waypoints || this.route.waypoints.length === 0) {
                this.services.notification.warning('لا توجد نقاط توقف لتحسين المسار');
                return;
            }

            const btn = document.getElementById('optimizeRouteBtn');
            UIUtils.showButtonLoading(btn, 'جاري التحسين...');

            const shipmentIds = this.route.waypoints
                .filter(w => w.shipmentId)
                .map(w => w.shipmentId);

            if (shipmentIds.length === 0) {
                this.services.notification.warning('لا توجد شحنات لتحسين المسار');
                return;
            }

            const response = await this.services.api.optimizeRoute(this.courierId, shipmentIds);

            if (response.success && response.data) {
                this.route = response.data;
                this.updateRouteDisplay();
                this.services.notification.success('تم تحسين المسار بنجاح');
            } else {
                ErrorHandler.handle(response, 'CourierRoute.optimizeRoute');
            }
        } catch (error) {
            ErrorHandler.handle(error, 'CourierRoute.optimizeRoute');
        } finally {
            const btn = document.getElementById('optimizeRouteBtn');
            UIUtils.hideButtonLoading(btn);
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const optimizeBtn = document.getElementById('optimizeRouteBtn');
        if (optimizeBtn) {
            optimizeBtn.addEventListener('click', () => this.optimizeRoute());
        }
    }
}

// Create global instance
window.courierRouteHandler = new CourierRouteHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/courier/route.html')) {
        setTimeout(() => {
            window.courierRouteHandler.init();
        }, 200);
    }
});
