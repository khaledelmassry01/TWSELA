// User Types
export interface User {
    id: number;
    username: string;
    name: string;
    email: string;
    role: UserRole;
    avatar?: string;
    permissions: string[];
    settings: UserSettings;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export enum UserRole {
    OWNER = 'OWNER',
    ADMIN = 'ADMIN',
    MERCHANT = 'MERCHANT',
    COURIER = 'COURIER',
    WAREHOUSE = 'WAREHOUSE'
}

export interface UserSettings {
    language: 'ar' | 'en';
    theme: 'light' | 'dark';
    notifications: boolean;
    emailNotifications: boolean;
}

// Shipment Types
export interface Address {
    street: string;
    city: string;
    state?: string;
    postalCode?: string;
    instructions?: string;
}

export interface ShipmentFilter {
    status?: ShipmentStatus | ShipmentStatus[];
    merchantId?: number;
    courierId?: number;
    fromZoneId?: number;
    toZoneId?: number;
    startDate?: string;
    endDate?: string;
    search?: string;
    limit?: number;
    offset?: number;
}

export interface Shipment {
    id: number;
    trackingNumber: string;
    merchantId: number;
    courierId?: number;
    fromZoneId: number;
    toZoneId: number;
    status: ShipmentStatus;
    items: ShipmentItem[];
    price: number;
    codAmount: number;
    deliveryFee: number;
    totalAmount: number;
    notes?: string;
    recipientName: string;
    recipientPhone: string;
    deliveryAddress: Address;
    createdAt: string;
    updatedAt: string;
}

export enum ShipmentStatus {
    CREATED = 'CREATED',
    PICKED_UP = 'PICKED_UP',
    IN_TRANSIT = 'IN_TRANSIT',
    DELIVERED = 'DELIVERED',
    RETURNED = 'RETURNED',
    CANCELLED = 'CANCELLED'
}

export interface ShipmentItem {
    id: number;
    name: string;
    quantity: number;
    price: number;
}

// Zone Types
export interface Zone {
    id: number;
    name: string;
    code: string;
    city: string;
    isActive: boolean;
    deliveryFee: number;
    basePrice: number;
    coordinates: Coordinates[];
}

export interface Coordinates {
    lat: number;
    lng: number;
}

// API Response Types
export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data?: T;
    errors?: ApiError[];
}

export interface ApiError {
    code: string;
    message: string;
    field?: string;
}

// State Management Types
export interface AppState {
    auth: AuthState;
    ui: UIState;
    data: DataState;
}

export interface AuthState {
    isAuthenticated: boolean;
    user: User | null;
    token: string | null;
    loading: boolean;
    error: string | null;
}

export interface UIState {
    theme: 'light' | 'dark';
    language: 'ar' | 'en';
    sidebarCollapsed: boolean;
    notifications: Notification[];
}

export interface DataState {
    shipments: Shipment[];
    zones: Zone[];
    loading: boolean;
    error: string | null;
}

export interface Notification {
    id: number;
    type: 'success' | 'error' | 'warning' | 'info';
    message: string;
    read: boolean;
    createdAt: string;
}

// Service Types
export interface AuthService {
    login(credentials: LoginCredentials): Promise<ApiResponse<AuthData>>;
    logout(): Promise<void>;
    checkAuthStatus(): Promise<boolean>;
    getCurrentUser(): User | null;
    updateUser(data: Partial<User>): Promise<ApiResponse<User>>;
}

export interface LoginCredentials {
    username: string;
    password: string;
    remember?: boolean;
}

export interface AuthData {
    token: string;
    user: User;
}

// Utility Types
export type DeepPartial<T> = {
    [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
}

export type QueryParams = Record<string, string | number | boolean | undefined>;

export interface PaginatedResponse<T> {
    items: T[];
    total: number;
    page: number;
    limit: number;
    hasMore: boolean;
}