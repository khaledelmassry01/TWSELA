import { ApiService } from './api';
import { Courier } from '../types';

export class CourierService {
  private api: ApiService;

  constructor() {
    this.api = new ApiService();
  }

  async getAllCouriers(): Promise<Courier[]> {
    return this.api.get<Courier[]>('/couriers');
  }

  async getActiveCouriers(): Promise<Courier[]> {
    return this.api.get<Courier[]>('/couriers/active');
  }

  async getCourierById(id: string): Promise<Courier> {
    return this.api.get<Courier>(`/couriers/${id}`);
  }

  async updateCourierStatus(id: string, isActive: boolean): Promise<Courier> {
    return this.api.patch<Courier>(`/couriers/${id}/status`, { isActive });
  }

  async updateCourierLocation(id: string, location: { latitude: number; longitude: number }): Promise<void> {
    return this.api.post(`/couriers/${id}/location`, location);
  }

  async assignZoneToCourier(courierId: string, zoneId: string): Promise<Courier> {
    return this.api.post<Courier>(`/couriers/${courierId}/zones`, { zoneId });
  }

  async removeCourierFromZone(courierId: string, zoneId: string): Promise<void> {
    return this.api.delete(`/couriers/${courierId}/zones/${zoneId}`);
  }

  async updateCourierProfile(id: string, profileData: Partial<Courier>): Promise<Courier> {
    return this.api.put<Courier>(`/couriers/${id}`, profileData);
  }

  async getStats(id: string): Promise<{
    totalDeliveries: number;
    successRate: number;
    rating: number;
    earnings: number;
  }> {
    return this.api.get(`/couriers/${id}/stats`);
  }
}