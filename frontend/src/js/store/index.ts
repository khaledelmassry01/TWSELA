import type { AppState, AuthState, UIState, DataState, User, Notification, Shipment, Zone } from '../types';

class Store {
    private state: AppState = {
        auth: {
            isAuthenticated: false,
            user: null,
            token: null,
            loading: false,
            error: null
        },
        ui: {
            theme: 'light',
            language: 'ar',
            sidebarCollapsed: false,
            notifications: []
        },
        data: {
            shipments: [],
            zones: [],
            loading: false,
            error: null
        }
    };

    private listeners: Array<(state: AppState) => void> = [];

    // Get State
    getState(): AppState {
        return this.state;
    }

    // Subscribe to state changes
    subscribe(listener: (state: AppState) => void): () => void {
        this.listeners.push(listener);
        return () => {
            this.listeners = this.listeners.filter(l => l !== listener);
        };
    }

    // Auth Actions
    setAuth(authState: Partial<AuthState>) {
        this.updateState({
            auth: { ...this.state.auth, ...authState }
        });
    }

    setUser(user: User | null) {
        this.updateState({
            auth: { ...this.state.auth, user }
        });
    }

    // UI Actions
    setTheme(theme: 'light' | 'dark') {
        this.updateState({
            ui: { ...this.state.ui, theme }
        });
        document.documentElement.setAttribute('data-theme', theme);
    }

    setLanguage(language: 'ar' | 'en') {
        this.updateState({
            ui: { ...this.state.ui, language }
        });
        document.documentElement.setAttribute('dir', language === 'ar' ? 'rtl' : 'ltr');
    }

    toggleSidebar() {
        this.updateState({
            ui: { ...this.state.ui, sidebarCollapsed: !this.state.ui.sidebarCollapsed }
        });
    }

    addNotification(notification: Omit<Notification, 'id' | 'createdAt'>) {
        const newNotification: Notification = {
            ...notification,
            id: Date.now(),
            createdAt: new Date().toISOString(),
            read: false
        };

        this.updateState({
            ui: {
                ...this.state.ui,
                notifications: [newNotification, ...this.state.ui.notifications]
            }
        });

        // Auto remove after 5 seconds
        setTimeout(() => {
            this.removeNotification(newNotification.id);
        }, 5000);
    }

    removeNotification(id: number) {
        this.updateState({
            ui: {
                ...this.state.ui,
                notifications: this.state.ui.notifications.filter(n => n.id !== id)
            }
        });
    }

    // Data Actions
    setShipments(shipments: Shipment[]) {
        this.updateState({
            data: { ...this.state.data, shipments }
        });
    }

    addShipment(shipment: Shipment) {
        this.updateState({
            data: {
                ...this.state.data,
                shipments: [shipment, ...this.state.data.shipments]
            }
        });
    }

    updateShipment(id: number, updates: Partial<Shipment>) {
        this.updateState({
            data: {
                ...this.state.data,
                shipments: this.state.data.shipments.map(s => 
                    s.id === id ? { ...s, ...updates } : s
                )
            }
        });
    }

    setZones(zones: Zone[]) {
        this.updateState({
            data: { ...this.state.data, zones }
        });
    }

    setLoading(loading: boolean) {
        this.updateState({
            data: { ...this.state.data, loading }
        });
    }

    setError(error: string | null) {
        this.updateState({
            data: { ...this.state.data, error }
        });
    }

    // Private Methods
    private updateState(updates: Partial<AppState>) {
        this.state = {
            ...this.state,
            ...updates
        };
        this.notifyListeners();
    }

    private notifyListeners() {
        this.listeners.forEach(listener => listener(this.state));
    }

    // Persistence
    persistState() {
        try {
            localStorage.setItem('twsela_state', JSON.stringify({
                auth: {
                    token: this.state.auth.token,
                    user: this.state.auth.user
                },
                ui: {
                    theme: this.state.ui.theme,
                    language: this.state.ui.language,
                    sidebarCollapsed: this.state.ui.sidebarCollapsed
                }
            }));
        } catch (error) {
            console.error('Failed to persist state:', error);
        }
    }

    loadPersistedState() {
        try {
            const persistedState = localStorage.getItem('twsela_state');
            if (persistedState) {
                const { auth, ui } = JSON.parse(persistedState);
                this.updateState({
                    auth: { ...this.state.auth, ...auth },
                    ui: { ...this.state.ui, ...ui }
                });
                
                // Apply theme and language
                if (ui.theme) this.setTheme(ui.theme);
                if (ui.language) this.setLanguage(ui.language);
            }
        } catch (error) {
            console.error('Failed to load persisted state:', error);
        }
    }

    // Initialize
    init() {
        this.loadPersistedState();
        
        // Subscribe to state changes to persist them
        this.subscribe(() => {
            this.persistState();
        });
    }
}

// Create singleton instance
const store = new Store();
store.init();

export default store;