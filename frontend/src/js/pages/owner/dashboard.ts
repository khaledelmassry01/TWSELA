import { BasePage } from '../base-page';
import { ShipmentService } from '../../services/shipment';
import { CourierService } from '../../services/courier';
import { MerchantService } from '../../services/merchant';
import { ReportService } from '../../services/report';

export default class OwnerDashboardPage extends BasePage {
  private shipmentService: ShipmentService;
  private courierService: CourierService;
  private merchantService: MerchantService;
  private reportService: ReportService;
  private shipmentsStatsContainer: HTMLElement;
  private couriersStatsContainer: HTMLElement;
  private merchantsStatsContainer: HTMLElement;
  private revenueStatsContainer: HTMLElement;
  private dateFilter: HTMLSelectElement;

  constructor() {
    super();
    this.shipmentService = new ShipmentService();
    this.courierService = new CourierService();
    this.merchantService = new MerchantService();
    this.reportService = new ReportService();
    
    this.shipmentsStatsContainer = this.getElement('#shipments-stats');
    this.couriersStatsContainer = this.getElement('#couriers-stats');
    this.merchantsStatsContainer = this.getElement('#merchants-stats');
    this.revenueStatsContainer = this.getElement('#revenue-stats');
    this.dateFilter = this.getElement<HTMLSelectElement>('#date-filter');
  }

  protected async init(): Promise<void> {
    try {
      await Promise.all([
        this.loadShipmentsStats(),
        this.loadCouriersStats(),
        this.loadMerchantsStats(),
        this.loadRevenueStats()
      ]);
    } catch (error) {
      await this.handleApiError(error as Error);
    }
  }

  protected bindEvents(): void {
    this.dateFilter.addEventListener('change', () => {
      this.init();
    });

    this.getElement('#refresh-stats').addEventListener('click', () => {
      this.init();
    });
  }

  private async loadShipmentsStats(): Promise<void> {
    try {
      this.showLoading(this.shipmentsStatsContainer);
      const dateRange = this.getDateRange();
      const stats = await this.reportService.getShipmentsStats(dateRange);
      
      const html = `
        <div class="grid grid-cols-4 gap-4">
          <div class="stat-card">
            <h3>Total Shipments</h3>
            <p>${stats.totalShipments}</p>
          </div>
          <div class="stat-card">
            <h3>Completed</h3>
            <p>${stats.completedShipments}</p>
            <small>${(stats.completionRate * 100).toFixed(1)}%</small>
          </div>
          <div class="stat-card">
            <h3>In Transit</h3>
            <p>${stats.inTransitShipments}</p>
          </div>
          <div class="stat-card">
            <h3>Issues</h3>
            <p>${stats.failedShipments}</p>
            <small>${(stats.failureRate * 100).toFixed(1)}%</small>
          </div>
        </div>
      `;

      this.shipmentsStatsContainer.innerHTML = html;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.shipmentsStatsContainer);
    }
  }

  private async loadCouriersStats(): Promise<void> {
    try {
      this.showLoading(this.couriersStatsContainer);
      const dateRange = this.getDateRange();
      const stats = await this.reportService.getCouriersStats(dateRange);
      
      const html = `
        <div class="grid grid-cols-4 gap-4">
          <div class="stat-card">
            <h3>Active Couriers</h3>
            <p>${stats.activeCouriers}</p>
          </div>
          <div class="stat-card">
            <h3>Online Now</h3>
            <p>${stats.onlineCouriers}</p>
          </div>
          <div class="stat-card">
            <h3>Avg Deliveries</h3>
            <p>${stats.averageDeliveriesPerCourier}</p>
          </div>
          <div class="stat-card">
            <h3>Avg Rating</h3>
            <p>${stats.averageRating.toFixed(1)}</p>
          </div>
        </div>
        <div class="mt-4">
          <h4>Top Performers</h4>
          <div class="overflow-x-auto">
            <table class="table-auto w-full">
              <thead>
                <tr>
                  <th>Courier</th>
                  <th>Deliveries</th>
                  <th>Success Rate</th>
                  <th>Rating</th>
                </tr>
              </thead>
              <tbody>
                ${stats.topPerformers.map(courier => `
                  <tr>
                    <td>${courier.name}</td>
                    <td>${courier.deliveries}</td>
                    <td>${(courier.successRate * 100).toFixed(1)}%</td>
                    <td>${courier.rating.toFixed(1)}</td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `;

      this.couriersStatsContainer.innerHTML = html;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.couriersStatsContainer);
    }
  }

  private async loadMerchantsStats(): Promise<void> {
    try {
      this.showLoading(this.merchantsStatsContainer);
      const dateRange = this.getDateRange();
      const stats = await this.reportService.getMerchantsStats(dateRange);
      
      const html = `
        <div class="grid grid-cols-4 gap-4">
          <div class="stat-card">
            <h3>Active Merchants</h3>
            <p>${stats.activeMerchants}</p>
          </div>
          <div class="stat-card">
            <h3>New Merchants</h3>
            <p>${stats.newMerchants}</p>
          </div>
          <div class="stat-card">
            <h3>Total Value</h3>
            <p>${stats.totalShipmentValue} EGP</p>
          </div>
          <div class="stat-card">
            <h3>Avg Per Merchant</h3>
            <p>${stats.averageShipmentValue} EGP</p>
          </div>
        </div>
        <div class="mt-4">
          <h4>Top Merchants</h4>
          <div class="overflow-x-auto">
            <table class="table-auto w-full">
              <thead>
                <tr>
                  <th>Merchant</th>
                  <th>Shipments</th>
                  <th>Total Value</th>
                  <th>Success Rate</th>
                </tr>
              </thead>
              <tbody>
                ${stats.topMerchants.map(merchant => `
                  <tr>
                    <td>${merchant.name}</td>
                    <td>${merchant.shipments}</td>
                    <td>${merchant.totalValue} EGP</td>
                    <td>${(merchant.successRate * 100).toFixed(1)}%</td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `;

      this.merchantsStatsContainer.innerHTML = html;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.merchantsStatsContainer);
    }
  }

  private async loadRevenueStats(): Promise<void> {
    try {
      this.showLoading(this.revenueStatsContainer);
      const dateRange = this.getDateRange();
      const stats = await this.reportService.getRevenueStats(dateRange);
      
      const html = `
        <div class="grid grid-cols-4 gap-4">
          <div class="stat-card">
            <h3>Total Revenue</h3>
            <p>${stats.totalRevenue} EGP</p>
            <small class="${stats.revenueGrowth >= 0 ? 'text-green-500' : 'text-red-500'}">
              ${stats.revenueGrowth >= 0 ? '↑' : '↓'} 
              ${Math.abs(stats.revenueGrowth * 100).toFixed(1)}%
            </small>
          </div>
          <div class="stat-card">
            <h3>Delivery Fees</h3>
            <p>${stats.deliveryFees} EGP</p>
          </div>
          <div class="stat-card">
            <h3>Courier Payouts</h3>
            <p>${stats.courierPayouts} EGP</p>
          </div>
          <div class="stat-card">
            <h3>Net Profit</h3>
            <p>${stats.netProfit} EGP</p>
            <small class="${stats.profitGrowth >= 0 ? 'text-green-500' : 'text-red-500'}">
              ${stats.profitGrowth >= 0 ? '↑' : '↓'} 
              ${Math.abs(stats.profitGrowth * 100).toFixed(1)}%
            </small>
          </div>
        </div>
        <div class="mt-4">
          <h4>Revenue by Zone</h4>
          <div class="overflow-x-auto">
            <table class="table-auto w-full">
              <thead>
                <tr>
                  <th>Zone</th>
                  <th>Shipments</th>
                  <th>Revenue</th>
                  <th>% of Total</th>
                </tr>
              </thead>
              <tbody>
                ${stats.revenueByZone.map(zone => `
                  <tr>
                    <td>${zone.name}</td>
                    <td>${zone.shipments}</td>
                    <td>${zone.revenue} EGP</td>
                    <td>${(zone.percentage * 100).toFixed(1)}%</td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `;

      this.revenueStatsContainer.innerHTML = html;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.revenueStatsContainer);
    }
  }

  private getDateRange(): { startDate: string; endDate: string } {
    const now = new Date();
    const startDate = new Date();
    
    switch (this.dateFilter.value) {
      case 'today':
        startDate.setHours(0, 0, 0, 0);
        break;
      case 'week':
        startDate.setDate(now.getDate() - 7);
        break;
      case 'month':
        startDate.setMonth(now.getMonth() - 1);
        break;
      case 'year':
        startDate.setFullYear(now.getFullYear() - 1);
        break;
      default:
        startDate.setDate(now.getDate() - 30); // Default to last 30 days
    }

    return {
      startDate: startDate.toISOString(),
      endDate: now.toISOString()
    };
  }
}