/**
 * Twsela CMS - WebSocket Service
 * Real-time communication via STOMP over SockJS
 * Auto-reconnect with exponential backoff
 */

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.baseReconnectDelay = 1000;
        this.maxReconnectDelay = 30000;
        this.reconnectTimer = null;
        this.listeners = new Map();
    }

    /**
     * Get WebSocket endpoint URL
     */
    getWsUrl() {
        const base = window.getApiBaseUrl ? window.getApiBaseUrl() : '';
        const wsBase = base.replace(/\/api\/?$/, '');
        return `${wsBase}/ws`;
    }

    /**
     * Connect to WebSocket server
     */
    async connect() {
        if (this.connected) return;

        const token = sessionStorage.getItem('authToken');
        if (!token) return;

        try {
            await this._ensureLibraries();

            const wsUrl = this.getWsUrl();
            const socket = new SockJS(wsUrl);
            this.stompClient = Stomp.over(socket);
            this.stompClient.debug = () => {};

            const headers = { Authorization: `Bearer ${token}` };

            this.stompClient.connect(
                headers,
                () => this._onConnected(),
                (error) => this._onError(error)
            );
        } catch (error) {
            this._scheduleReconnect();
        }
    }

    /**
     * Disconnect from WebSocket server
     */
    disconnect() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }

        if (this.stompClient && this.connected) {
            this.subscriptions.forEach((sub) => {
                try { sub.unsubscribe(); } catch (e) { /* ignore */ }
            });
            this.subscriptions.clear();
            try { this.stompClient.disconnect(); } catch (e) { /* ignore */ }
            this.connected = false;
            this._emit('disconnected');
        }
    }

    /**
     * Subscribe to a topic
     * @param {string} destination - STOMP destination
     * @param {Function} callback - Message handler
     * @returns {string} subscription ID
     */
    subscribe(destination, callback) {
        if (!this.stompClient || !this.connected) {
            const subId = `pending_${Date.now()}_${Math.random().toString(36).slice(2)}`;
            this.subscriptions.set(subId, { destination, callback, pending: true });
            return subId;
        }

        const sub = this.stompClient.subscribe(destination, (message) => {
            try {
                const body = JSON.parse(message.body);
                callback(body);
            } catch (e) {
                callback(message.body);
            }
        });

        const subId = sub.id;
        this.subscriptions.set(subId, { destination, callback, subscription: sub });
        return subId;
    }

    /**
     * Unsubscribe from a topic
     */
    unsubscribe(subId) {
        const entry = this.subscriptions.get(subId);
        if (entry) {
            if (entry.subscription) {
                try { entry.subscription.unsubscribe(); } catch (e) { /* ignore */ }
            }
            this.subscriptions.delete(subId);
        }
    }

    /**
     * Send a message to a destination
     */
    send(destination, body) {
        if (!this.stompClient || !this.connected) return false;
        this.stompClient.send(destination, {}, JSON.stringify(body));
        return true;
    }

    /**
     * Listen for service events (connected, disconnected, error, reconnecting)
     */
    on(event, callback) {
        if (!this.listeners.has(event)) this.listeners.set(event, []);
        this.listeners.get(event).push(callback);
    }

    off(event, callback) {
        const cbs = this.listeners.get(event);
        if (cbs) this.listeners.set(event, cbs.filter(cb => cb !== callback));
    }

    /** Subscribe to notifications for user */
    subscribeNotifications(userId, callback) {
        return this.subscribe(`/topic/notifications/${userId}`, callback);
    }

    /** Subscribe to shipment updates */
    subscribeShipmentUpdates(shipmentId, callback) {
        return this.subscribe(`/topic/shipment/${shipmentId}`, callback);
    }

    /** Subscribe to dashboard stats updates */
    subscribeDashboardStats(scope, callback) {
        return this.subscribe(`/topic/dashboard/stats/${scope}`, callback);
    }

    /** Subscribe to courier-specific updates */
    subscribeCourierUpdates(courierId, callback) {
        return this.subscribe(`/topic/courier/${courierId}`, callback);
    }

    // --- Private ---

    _onConnected() {
        this.connected = true;
        this.reconnectAttempts = 0;
        this._emit('connected');

        // Re-subscribe pending subscriptions
        this.subscriptions.forEach((entry, subId) => {
            if (entry.pending) {
                const newSub = this.stompClient.subscribe(entry.destination, (message) => {
                    try { entry.callback(JSON.parse(message.body)); }
                    catch (e) { entry.callback(message.body); }
                });
                this.subscriptions.set(subId, {
                    destination: entry.destination,
                    callback: entry.callback,
                    subscription: newSub
                });
            }
        });
    }

    _onError(error) {
        this.connected = false;
        this._emit('error', error);
        this._scheduleReconnect();
    }

    _scheduleReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            this._emit('maxRetriesReached');
            return;
        }

        const delay = Math.min(
            this.baseReconnectDelay * Math.pow(2, this.reconnectAttempts),
            this.maxReconnectDelay
        );
        this.reconnectAttempts++;
        this._emit('reconnecting', { attempt: this.reconnectAttempts, delay });
        this.reconnectTimer = setTimeout(() => this.connect(), delay);
    }

    _emit(event, data) {
        const cbs = this.listeners.get(event);
        if (cbs) cbs.forEach(cb => { try { cb(data); } catch (e) { /* ignore */ } });
    }

    async _ensureLibraries() {
        if (typeof SockJS === 'undefined') {
            await this._loadScript('https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js');
        }
        if (typeof Stomp === 'undefined') {
            await this._loadScript('https://cdn.jsdelivr.net/npm/@stomp/stompjs@5/bundles/stomp.umd.min.js');
        }
    }

    _loadScript(src) {
        return new Promise((resolve, reject) => {
            if (document.querySelector(`script[src="${src}"]`)) { resolve(); return; }
            const script = document.createElement('script');
            script.src = src;
            script.onload = resolve;
            script.onerror = () => reject(new Error(`Failed to load ${src}`));
            document.head.appendChild(script);
        });
    }
}

// Singleton + global export
window.websocketService = new WebSocketService();

