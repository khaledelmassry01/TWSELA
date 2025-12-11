import type { ApiResponse, QueryParams } from '../types';
import store from '../store';

class ApiService {
    private baseUrl: string;
    private defaultHeaders: HeadersInit;

    constructor() {
        this.baseUrl = this.getBaseUrl();
        this.defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
    }

    private getBaseUrl(): string {
        return process.env.NODE_ENV === 'production'
            ? 'https://api.twsela.com'
            : 'http://localhost:8080';
    }

    private async getHeaders(): Promise<HeadersInit> {
        const token = store.getState().auth.token;
        return {
            ...this.defaultHeaders,
            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        };
    }

    private buildUrl(endpoint: string, params?: QueryParams): string {
        const url = new URL(this.baseUrl + endpoint);
        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined) {
                    url.searchParams.append(key, String(value));
                }
            });
        }
        return url.toString();
    }

    private async handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
        const contentType = response.headers.get('content-type');
        
        // Handle non-JSON responses
        if (!contentType || !contentType.includes('application/json')) {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return {
                success: true,
                message: 'Success',
                data: await response.text() as unknown as T
            };
        }

        const data = await response.json();

        if (!response.ok) {
            // Handle error responses
            store.addNotification({
                type: 'error',
                message: data.message || 'An error occurred',
                read: false
            });

            throw new Error(data.message || 'API Error');
        }

        return data;
    }

    public async request<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
        try {
            const headers = await this.getHeaders();
            const response = await fetch(this.buildUrl(endpoint), {
                ...options,
                headers: {
                    ...headers,
                    ...options.headers
                }
            });

            return this.handleResponse<T>(response);
        } catch (error) {
            console.error('API Request Error:', error);
            store.addNotification({
                type: 'error',
                message: error instanceof Error ? error.message : 'An error occurred',
                read: false
            });
            throw error;
        }
    }

    // Generic GET request
    public async get<T>(endpoint: string, params?: QueryParams): Promise<ApiResponse<T>> {
        return this.request<T>(endpoint, {
            method: 'GET',
            ...params && { params }
        });
    }

    // Generic POST request
    public async post<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
        return this.request<T>(endpoint, {
            method: 'POST',
            body: data ? JSON.stringify(data) : undefined
        });
    }

    // Generic PUT request
    public async put<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
        return this.request<T>(endpoint, {
            method: 'PUT',
            body: data ? JSON.stringify(data) : undefined
        });
    }

    // Generic DELETE request
    public async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
        return this.request<T>(endpoint, {
            method: 'DELETE'
        });
    }

    // File upload
    public async uploadFile(endpoint: string, file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<string>> {
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch(this.buildUrl(endpoint), {
                method: 'POST',
                headers: await this.getHeaders(),
                body: formData
            });

            return this.handleResponse<string>(response);
        } catch (error) {
            console.error('File Upload Error:', error);
            store.addNotification({
                type: 'error',
                message: 'Failed to upload file',
                read: false
            });
            throw error;
        }
    }

    // Batch requests
    public async batch<T>(requests: Array<{ endpoint: string, method: string, data?: any }>): Promise<Array<ApiResponse<T>>> {
        try {
            const promises = requests.map(request => 
                this.request<T>(request.endpoint, {
                    method: request.method,
                    body: request.data ? JSON.stringify(request.data) : undefined
                })
            );

            return Promise.all(promises);
        } catch (error) {
            console.error('Batch Request Error:', error);
            store.addNotification({
                type: 'error',
                message: 'Failed to process batch request',
                read: false
            });
            throw error;
        }
    }

    // Retry mechanism
    public async retryRequest<T>(
        endpoint: string, 
        options: RequestInit = {}, 
        retries: number = 3,
        delay: number = 1000
    ): Promise<ApiResponse<T>> {
        try {
            return await this.request<T>(endpoint, options);
        } catch (error) {
            if (retries === 0) throw error;
            
            await new Promise(resolve => setTimeout(resolve, delay));
            return this.retryRequest<T>(endpoint, options, retries - 1, delay * 2);
        }
    }

    // Cache management
    private cache = new Map<string, { data: any, timestamp: number }>();
    private readonly CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    public async getCached<T>(
        endpoint: string, 
        params?: QueryParams,
        duration: number = this.CACHE_DURATION
    ): Promise<ApiResponse<T>> {
        const cacheKey = this.buildUrl(endpoint, params);
        const cached = this.cache.get(cacheKey);

        if (cached && Date.now() - cached.timestamp < duration) {
            return cached.data;
        }

        const response = await this.get<T>(endpoint, params);
        this.cache.set(cacheKey, {
            data: response,
            timestamp: Date.now()
        });

        return response;
    }

    public clearCache(endpoint?: string): void {
        if (endpoint) {
            const cacheKey = this.buildUrl(endpoint);
            this.cache.delete(cacheKey);
        } else {
            this.cache.clear();
        }
    }
}

// Create singleton instance
const apiService = new ApiService();

export default apiService;