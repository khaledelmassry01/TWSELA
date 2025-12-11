import { ApiService } from './api';

export interface DateRange {
  startDate: string;
  endDate: string;
}

export interface ShipmentsStats {
  totalShipments: number;
  completedShipments: number;
  inTransitShipments: number;
  failedShipments: number;
  completionRate: number;
  failureRate: number;
  averageDeliveryTime: number;
}

export interface CouriersStats {
  activeCouriers: number;
  onlineCouriers: number;
  averageDeliveriesPerCourier: number;
  averageRating: number;
  topPerformers: {
    id: string;
    name: string;
    deliveries: number;
    successRate: number;
    rating: number;
  }[];
}

export interface MerchantsStats {
  activeMerchants: number;
  newMerchants: number;
  totalShipmentValue: number;
  averageShipmentValue: number;
  topMerchants: {
    id: string;
    name: string;
    shipments: number;
    totalValue: number;
    successRate: number;
  }[];
}

export interface RevenueStats {
  totalRevenue: number;
  revenueGrowth: number;
  deliveryFees: number;
  courierPayouts: number;
  netProfit: number;
  profitGrowth: number;
  revenueByZone: {
    id: string;
    name: string;
    shipments: number;
    revenue: number;
    percentage: number;
  }[];
}

export class ReportService {
  private api: ApiService;

  constructor() {
    this.api = new ApiService();
  }

  async getShipmentsStats(dateRange: DateRange): Promise<ShipmentsStats> {
    return this.api.get<ShipmentsStats>('/reports/shipments', {
      params: dateRange
    });
  }

  async getCouriersStats(dateRange: DateRange): Promise<CouriersStats> {
    return this.api.get<CouriersStats>('/reports/couriers', {
      params: dateRange
    });
  }

  async getMerchantsStats(dateRange: DateRange): Promise<MerchantsStats> {
    return this.api.get<MerchantsStats>('/reports/merchants', {
      params: dateRange
    });
  }

  async getRevenueStats(dateRange: DateRange): Promise<RevenueStats> {
    return this.api.get<RevenueStats>('/reports/revenue', {
      params: dateRange
    });
  }

  async generateShipmentsReport(dateRange: DateRange): Promise<Blob> {
    return this.api.get('/reports/shipments/export', {
      params: dateRange,
      responseType: 'blob'
    });
  }

  async generateCouriersReport(dateRange: DateRange): Promise<Blob> {
    return this.api.get('/reports/couriers/export', {
      params: dateRange,
      responseType: 'blob'
    });
  }

  async generateMerchantsReport(dateRange: DateRange): Promise<Blob> {
    return this.api.get('/reports/merchants/export', {
      params: dateRange,
      responseType: 'blob'
    });
  }

  async generateFinancialReport(dateRange: DateRange): Promise<Blob> {
    return this.api.get('/reports/financial/export', {
      params: dateRange,
      responseType: 'blob'
    });
  }
}