import type { User } from '../types';
import store from '../store';
import authService from '../services/auth';
import apiService from '../services/api';

export abstract class BasePage {
    protected user: User | null = null;
    protected isLoading: boolean = false;
    protected initialized: boolean = false;

    constructor() {
        this.initialize();
    }

    private async initialize(): Promise<void> {
        if (this.initialized) return;

        try {
            // Wait for DOM to be ready
            if (document.readyState === 'loading') {
                await new Promise(resolve => {
                    document.addEventListener('DOMContentLoaded', resolve);
                });
            }

            // Check authentication
            if (!this.isPublicPage()) {
                const isAuthenticated = await this.checkAuth();
                if (!isAuthenticated) return;
            }

            // Initialize page
            await this.initializePage();
            
            // Setup event listeners
            this.setupEventListeners();
            
            // Load initial data
            await this.loadData();
            
            this.initialized = true;

        } catch (error) {
            console.error('Page initialization error:', error);
            store.addNotification({
                type: 'error',
                message: 'خطأ في تحميل الصفحة',
                read: false
            });
        }
    }

    protected abstract initializePage(): Promise<void>;
    protected abstract loadData(): Promise<void>;
    protected abstract setupEventListeners(): void;

    protected isPublicPage(): boolean {
        return false;
    }

    protected getElement<T extends HTMLElement>(selector: string): T {
        const element = document.querySelector<T>(selector);
        if (!element) throw new Error(`Element not found: ${selector}`);
        return element;
    }

    protected showLoading(element: HTMLElement): void {
        element.classList.add('loading');
        this.setLoading(true);
    }

    protected hideLoading(element: HTMLElement): void {
        element.classList.remove('loading');
        this.setLoading(false);
    }

    protected handleApiError(error: Error): void {
        console.error('API Error:', error);
        this.showError(error.message || 'An unexpected error occurred');
    }

    protected validateRequired(fields: Record<string, any>): boolean {
        const emptyFields = Object.entries(fields)
            .filter(([_, value]) => !value || value.toString().trim() === '')
            .map(([key]) => key);

        if (emptyFields.length > 0) {
            this.showError(`Please fill in the following fields: ${emptyFields.join(', ')}`);
            return false;
        }

        return true;
    }

    protected get notifications() {
        return {
            success: (message: string) => this.showSuccess(message),
            error: (message: string) => this.showError(message),
            warning: (message: string) => this.showWarning(message),
            info: (message: string) => this.showInfo(message)
        };
    }

    protected async checkAuth(): Promise<boolean> {
        const isAuthenticated = authService.isAuthenticated();
        
        if (!isAuthenticated) {
            window.location.href = '/login.html';
            return false;
        }

        this.user = authService.getCurrentUser();
        return true;
    }

    protected setLoading(loading: boolean): void {
        this.isLoading = loading;
        store.setLoading(loading);
        
        const loadingElement = document.getElementById('loading');
        if (loadingElement) {
            loadingElement.style.display = loading ? 'flex' : 'none';
        }
    }

    protected showError(message: string): void {
        store.addNotification({
            type: 'error',
            message,
            read: false
        });
    }

    protected showSuccess(message: string): void {
        store.addNotification({
            type: 'success',
            message,
            read: false
        });
    }

    protected showWarning(message: string): void {
        store.addNotification({
            type: 'warning',
            message,
            read: false
        });
    }

    protected showInfo(message: string): void {
        store.addNotification({
            type: 'info',
            message,
            read: false
        });
    }

    protected async fetchWithLoader<T>(promise: Promise<T>): Promise<T> {
        this.setLoading(true);
        try {
            return await promise;
        } finally {
            this.setLoading(false);
        }
    }

    protected debounce<T extends (...args: any[]) => any>(
        func: T,
        wait: number
    ): (...args: Parameters<T>) => void {
        let timeout: NodeJS.Timeout;
        
        return (...args: Parameters<T>): void => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func(...args), wait);
        };
    }

    protected throttle<T extends (...args: any[]) => any>(
        func: T,
        limit: number
    ): (...args: Parameters<T>) => void {
        let inThrottle: boolean;
        
        return (...args: Parameters<T>): void => {
            if (!inThrottle) {
                func(...args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    protected formatDate(date: string | Date): string {
        return new Date(date).toLocaleDateString('ar-EG', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    protected formatCurrency(amount: number): string {
        return amount.toLocaleString('ar-EG', {
            style: 'currency',
            currency: 'EGP'
        });
    }

    protected getQueryParam(param: string): string | null {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(param);
    }

    protected setQueryParam(param: string, value: string): void {
        const url = new URL(window.location.href);
        url.searchParams.set(param, value);
        window.history.pushState({}, '', url);
    }

    protected removeQueryParam(param: string): void {
        const url = new URL(window.location.href);
        url.searchParams.delete(param);
        window.history.pushState({}, '', url);
    }

    protected async exportData(type: string, filters: Record<string, any> = {}): Promise<void> {
        try {
            const response = await apiService.get(`/api/export/${type}`, filters);
            
            if (response.success && response.data) {
                const blob = new Blob([response.data as BlobPart], { type: 'text/csv' });
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `${type}-${new Date().toISOString()}.csv`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
            }
        } catch (error) {
            this.showError('فشل تصدير البيانات');
        }
    }

    protected scrollToTop(): void {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    protected confirmAction(message: string): Promise<string | null> {
        return new Promise(resolve => {
            const result = window.prompt(message);
            resolve(result);
        });
    }
}