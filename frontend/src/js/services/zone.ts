import ApiService from './api';
import { Zone } from '../types';

export class ZoneService {
  private api = ApiService;

  constructor() {}

  async getAllZones(): Promise<Zone[]> {
    const response = await this.api.get<{ data: Zone[] }>('/zones');
    return response.data?.data || [];
  }

  async getZoneById(id: string): Promise<Zone> {
    const response = await this.api.get<{ data: Zone }>(`/zones/${id}`);
    if (!response.data?.data) throw new Error('Zone not found');
    return response.data.data;
  }

  async createZone(zoneData: Partial<Zone>): Promise<Zone> {
    const response = await this.api.post<{ data: Zone }>('/zones', zoneData);
    if (!response.data?.data) throw new Error('Failed to create zone');
    return response.data.data;
  }

  async updateZone(id: string, zoneData: Partial<Zone>): Promise<Zone> {
    const response = await this.api.put<{ data: Zone }>(`/zones/${id}`, zoneData);
    if (!response.data?.data) throw new Error('Failed to update zone');
    return response.data.data;
  }

  async deleteZone(id: string): Promise<void> {
    await this.api.delete(`/zones/${id}`);
  }

  async toggleZoneStatus(id: string, isActive: boolean): Promise<Zone> {
    const response = await this.api.post<{ data: Zone }>(`/zones/${id}/status`, { isActive });
    if (!response.data?.data) throw new Error('Failed to toggle zone status');
    return response.data.data;
  }

  async assignCourierToZone(zoneId: string, courierId: string): Promise<Zone> {
    const response = await this.api.post<{ data: Zone }>(`/zones/${zoneId}/couriers`, { courierId });
    if (!response.data?.data) throw new Error('Failed to assign courier to zone');
    return response.data.data;
  }

  async removeCourierFromZone(zoneId: string, courierId: string): Promise<Zone> {
    const response = await this.api.delete<{ data: Zone }>(`/zones/${zoneId}/couriers/${courierId}`);
    if (!response.data?.data) throw new Error('Failed to remove courier from zone');
    return response.data.data;
  }
}