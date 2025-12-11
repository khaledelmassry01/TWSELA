import { BasePage } from '../base-page';
import { ShipmentService } from '../../services/shipment';
import { Shipment, ShipmentStatus } from '../../types';

export default class MerchantDashboardPage extends BasePage {
  private shipmentService: ShipmentService;
  private dashboardStats: HTMLElement;
  private recentShipments: HTMLElement;

  constructor() {
    super();
    this.shipmentService = new ShipmentService();
    this.dashboardStats = document.getElementById('dashboard-stats')!;
    this.recentShipments = document.getElementById('recent-shipments')!;

    if (!this.dashboardStats || !this.recentShipments) {
      throw new Error('Required page elements not found');
    }
  }

  protected async initializePage(): Promise<void> {
    // Any initialization beyond constructor
    return Promise.resolve();
  }

  protected async loadData(): Promise<void> {
    try {
      this.setLoading(true);
      
      const [stats, recentShipments] = await Promise.all([
        this.shipmentService.getStats(),
        this.shipmentService.getShipments({ limit: 5 })
      ]);

      this.renderStats(stats);
      this.renderRecentShipments(recentShipments);
    } catch (error) {
      this.showError('Failed to load dashboard data');
      console.error('Error loading dashboard:', error);
    } finally {
      this.setLoading(false);
    }
  }

  protected setupEventListeners(): void {
    // Bind refresh button
    const refreshBtn = document.getElementById('refresh-stats');
    if (refreshBtn) {
      refreshBtn.addEventListener('click', () => this.loadData());
    }
  }

  private renderStats(stats: {
    totalShipments: number;
    pendingShipments: number;
    inTransitShipments: number;
    deliveredShipments: number;
    failedShipments: number;
  }): void {
    const statsHtml = `
      <div class="grid grid-cols-4 gap-4">
        <div class="stat-card">
          <h3>Total Shipments</h3>
          <p>${stats.totalShipments}</p>
        </div>
        <div class="stat-card">
          <h3>Pending</h3>
          <p>${stats.pendingShipments}</p>
        </div>
        <div class="stat-card">
          <h3>In Transit</h3>
          <p>${stats.inTransitShipments}</p>
        </div>
        <div class="stat-card">
          <h3>Delivered</h3>
          <p>${stats.deliveredShipments}</p>
        </div>
      </div>
    `;
    this.dashboardStats.innerHTML = statsHtml;
  }

  private renderRecentShipments(shipments: Shipment[]): void {
    if (!shipments.length) {
      this.recentShipments.innerHTML = '<p class="text-center">No recent shipments</p>';
      return;
    }

    const shipmentsHtml = shipments.map(shipment => `
      <tr>
        <td>${shipment.trackingNumber}</td>
        <td>${shipment.recipientName || 'N/A'}</td>
        <td>${shipment.recipientPhone || 'N/A'}</td>
        <td>${shipment.deliveryAddress ? `${shipment.deliveryAddress.street}, ${shipment.deliveryAddress.city}` : 'N/A'}</td>
        <td><span class="status-badge status-${shipment.status.toLowerCase()}">${shipment.status}</span></td>
        <td>
          <a href="/merchant/shipment-details.html?id=${shipment.id}" class="btn-link">View Details</a>
        </td>
      </tr>
    `).join('');

    this.recentShipments.innerHTML = `
      <table class="table-auto w-full">
        <thead>
          <tr>
            <th>Tracking #</th>
            <th>Recipient</th>
            <th>Phone</th>
            <th>Address</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${shipmentsHtml}
        </tbody>
      </table>
    `;
  }
}