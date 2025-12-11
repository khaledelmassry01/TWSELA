import type { AuthService as IAuthService, LoginCredentials, ApiResponse, AuthData, User } from '../types';
import apiService from './api';
import store from '../store';

class AuthService implements IAuthService {
    private readonly TOKEN_KEY = 'twsela_token';
    private readonly USER_KEY = 'twsela_user';

    constructor() {
        this.initializeFromStorage();
    }

    private initializeFromStorage() {
        const token = localStorage.getItem(this.TOKEN_KEY);
        const userJson = localStorage.getItem(this.USER_KEY);

        if (token) {
            store.setAuth({
                token,
                isAuthenticated: true,
                user: userJson ? JSON.parse(userJson) : null
            });
        }
    }

    public async login(credentials: LoginCredentials): Promise<ApiResponse<AuthData>> {
        store.setAuth({ loading: true, error: null });

        try {
            const response = await apiService.post<AuthData>('/api/auth/login', credentials);

            if (response.success && response.data) {
                const { token, user } = response.data;
                
                // Update store
                store.setAuth({
                    isAuthenticated: true,
                    token,
                    user,
                    loading: false
                });

                // Persist authentication
                localStorage.setItem(this.TOKEN_KEY, token);
                localStorage.setItem(this.USER_KEY, JSON.stringify(user));

                // Show success notification
                store.addNotification({
                    type: 'success',
                    message: 'تم تسجيل الدخول بنجاح',
                    read: false
                });
            }

            return response;
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'فشل تسجيل الدخول';
            
            store.setAuth({ 
                loading: false, 
                error: errorMessage,
                isAuthenticated: false,
                token: null,
                user: null
            });

            store.addNotification({
                type: 'error',
                message: errorMessage,
                read: false
            });

            throw error;
        }
    }

    public async logout(): Promise<void> {
        try {
            await apiService.post('/api/auth/logout');
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // Clear authentication regardless of API call success
            this.clearAuth();
            
            store.addNotification({
                type: 'info',
                message: 'تم تسجيل الخروج',
                read: false
            });
        }
    }

    public async checkAuthStatus(): Promise<boolean> {
        try {
            const response = await apiService.get('/api/auth/me');
            return response.success;
        } catch (error) {
            this.clearAuth();
            return false;
        }
    }

    public async updateUser(data: Partial<User>): Promise<ApiResponse<User>> {
        try {
            const response = await apiService.put<User>('/api/auth/profile', data);
            
            if (response.success && response.data) {
                // Update stored user data
                const updatedUser = { ...this.getCurrentUser(), ...response.data };
                localStorage.setItem(this.USER_KEY, JSON.stringify(updatedUser));
                store.setUser(updatedUser);

                store.addNotification({
                    type: 'success',
                    message: 'تم تحديث البيانات بنجاح',
                    read: false
                });
            }

            return response;
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'فشل تحديث البيانات';
            
            store.addNotification({
                type: 'error',
                message: errorMessage,
                read: false
            });

            throw error;
        }
    }

    public getCurrentUser(): User | null {
        return store.getState().auth.user;
    }

    private clearAuth(): void {
        // Clear local storage
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);

        // Update store
        store.setAuth({
            isAuthenticated: false,
            token: null,
            user: null,
            loading: false,
            error: null
        });

        // Redirect to login
        window.location.href = '/login.html';
    }

    public isAuthenticated(): boolean {
        return store.getState().auth.isAuthenticated;
    }

    public getToken(): string | null {
        return store.getState().auth.token;
    }
}

// Create singleton instance
const authService = new AuthService();

export default authService;