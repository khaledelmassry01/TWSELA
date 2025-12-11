import ApiService from './api';
import { Shipment, ShipmentStatus, ShipmentFilter } from '../types';

export class ShipmentService {
  private api = ApiService;

  constructor() {}

  async getShipments(filter?: ShipmentFilter): Promise<Shipment[]> {
    const params = new URLSearchParams();
    if (filter?.status) {
      if (Array.isArray(filter.status)) {
        filter.status.forEach(status => params.append('status', status));
      } else {
        params.append('status', filter.status);
      }
    }
    if (filter?.limit) {
      params.append('limit', filter.limit.toString());
    }
    if (filter?.offset) {
      params.append('offset', filter.offset.toString());
    }
    
    const response = await this.api.get<{ data: Shipment[] }>(`/shipments?${params.toString()}`);
    return response.data?.data || [];
  }

  async getShipmentById(id: string): Promise<Shipment> {
    const response = await this.api.get<{ data: Shipment }>(`/shipments/${id}`);
    if (!response.data?.data) throw new Error('Shipment not found');
    return response.data.data;
  }

  async createShipment(shipmentData: Partial<Shipment>): Promise<Shipment> {
    const response = await this.api.post<{ data: Shipment }>('/shipments', shipmentData);
    if (!response.data?.data) throw new Error('Failed to create shipment');
    return response.data.data;
  }

  async updateShipmentStatus(id: string, status: ShipmentStatus): Promise<Shipment> {
    const response = await this.api.post<{ data: Shipment }>(`/shipments/${id}/status`, { status });
    if (!response.data?.data) throw new Error('Failed to update shipment status');
    return response.data.data;
  }

  async getStats(): Promise<{
    totalShipments: number;
    pendingShipments: number;
    inTransitShipments: number;
    deliveredShipments: number;
    failedShipments: number;
  }> {
    const response = await this.api.get<{
      data: {
        totalShipments: number;
        pendingShipments: number;
        inTransitShipments: number;
        deliveredShipments: number;
        failedShipments: number;
      }
    }>('/shipments/stats');
    if (!response.data?.data) throw new Error('Failed to get stats');
    return response.data.data;
  }

  async updateLocation(coords: { latitude: number; longitude: number }): Promise<void> {
    await this.api.post('/courier/location', coords);
  }

  async assignCourier(shipmentId: string, courierId: string): Promise<Shipment> {
    const response = await this.api.post<{ data: Shipment }>(`/shipments/${shipmentId}/assign`, { courierId });
    if (!response.data?.data) throw new Error('Failed to assign courier');
    return response.data.data;
  }

  async unassignCourier(shipmentId: string): Promise<Shipment> {
    const response = await this.api.delete<{ data: Shipment }>(`/shipments/${shipmentId}/assign`);
    if (!response.data?.data) throw new Error('Failed to unassign courier');
    return response.data.data;
  }

  async addNote(shipmentId: string, note: string): Promise<Shipment> {
    const response = await this.api.post<{ data: Shipment }>(`/shipments/${shipmentId}/notes`, { note });
    if (!response.data?.data) throw new Error('Failed to add note');
    return response.data.data;
  }
}