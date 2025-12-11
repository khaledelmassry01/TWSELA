import { ApiService } from './api';
import { User, UserRole } from '../types';

export interface Merchant extends User {
  companyName: string;
  businessType: string;
  taxId?: string;
  website?: string;
  totalShipments: number;
  activeShipments: number;
  successRate: number;
  balance: number;
}

export class MerchantService {
  private api: ApiService;

  constructor() {
    this.api = new ApiService();
  }

  async getAllMerchants(): Promise<Merchant[]> {
    return this.api.get<Merchant[]>('/merchants');
  }

  async getActiveMerchants(): Promise<Merchant[]> {
    return this.api.get<Merchant[]>('/merchants/active');
  }

  async getMerchantById(id: string): Promise<Merchant> {
    return this.api.get<Merchant>(`/merchants/${id}`);
  }

  async createMerchant(merchantData: Partial<Merchant>): Promise<Merchant> {
    const data = {
      ...merchantData,
      role: UserRole.MERCHANT
    };
    return this.api.post<Merchant>('/merchants', data);
  }

  async updateMerchant(id: string, merchantData: Partial<Merchant>): Promise<Merchant> {
    return this.api.put<Merchant>(`/merchants/${id}`, merchantData);
  }

  async updateMerchantStatus(id: string, isActive: boolean): Promise<Merchant> {
    return this.api.patch<Merchant>(`/merchants/${id}/status`, { isActive });
  }

  async deleteMerchant(id: string): Promise<void> {
    return this.api.delete(`/merchants/${id}`);
  }

  async getMerchantBalance(id: string): Promise<{
    currentBalance: number;
    pendingBalance: number;
    totalEarnings: number;
    lastPayout: {
      amount: number;
      date: string;
    };
  }> {
    return this.api.get(`/merchants/${id}/balance`);
  }

  async getStats(id: string): Promise<{
    totalShipments: number;
    activeShipments: number;
    completedShipments: number;
    failedShipments: number;
    successRate: number;
    totalValue: number;
    averageValue: number;
  }> {
    return this.api.get(`/merchants/${id}/stats`);
  }

  async requestPayout(id: string, amount: number): Promise<{
    requestId: string;
    status: 'PENDING' | 'APPROVED' | 'REJECTED';
    amount: number;
    requestDate: string;
  }> {
    return this.api.post(`/merchants/${id}/payouts`, { amount });
  }

  async getPayoutHistory(id: string): Promise<{
    id: string;
    amount: number;
    status: 'PENDING' | 'APPROVED' | 'REJECTED';
    requestDate: string;
    processedDate?: string;
    note?: string;
  }[]> {
    return this.api.get(`/merchants/${id}/payouts`);
  }
}