import { BasePage } from '../base-page';
import { ShipmentService } from '../../services/shipment';
import { Shipment, ShipmentStatus } from '../../types';

interface CourierStats {
  todayDeliveries: number;
  successRate: number;
  totalEarnings: number;
  rating: number;
}

export default class CourierDashboardPage extends BasePage {
  private shipmentService: ShipmentService;
  private activeShipmentsContainer: HTMLElement;
  private completedShipmentsContainer: HTMLElement;
  private statsContainer: HTMLElement;
  private locationUpdateInterval: NodeJS.Timeout | null = null;

  constructor() {
    super();
    this.shipmentService = new ShipmentService();
    this.activeShipmentsContainer = document.getElementById('active-shipments')!;
    this.completedShipmentsContainer = document.getElementById('completed-shipments')!;
    this.statsContainer = document.getElementById('courier-stats')!;

    if (!this.activeShipmentsContainer || !this.completedShipmentsContainer || !this.statsContainer) {
      throw new Error('Required page elements not found');
    }
  }

  protected async initializePage(): Promise<void> {
    this.startLocationUpdates();
  }

  protected async loadData(): Promise<void> {
    try {
      await Promise.all([
        this.loadActiveShipments(),
        this.loadCompletedShipments(),
        this.loadStats()
      ]);
    } catch (error) {
      this.showError('Failed to load dashboard data');
      console.error('Error loading dashboard:', error);
    }
  }

  protected setupEventListeners(): void {
    this.activeShipmentsContainer.addEventListener('click', async (e) => {
      const target = e.target as HTMLElement;
      if (!target.matches('button[data-action]')) return;
      
      const shipmentElement = target.closest<HTMLElement>('[data-shipment-id]');
      if (!shipmentElement) return;

      const shipmentId = shipmentElement.dataset.shipmentId;
      if (!shipmentId) return;

      const action = target.dataset.action;
      try {
        switch (action) {
          case 'pickup':
            await this.updateShipmentStatus(shipmentId, ShipmentStatus.PICKED_UP);
            break;

          case 'deliver':
            await this.updateShipmentStatus(shipmentId, ShipmentStatus.DELIVERED);
            break;

          case 'return':
            const reason = window.prompt('Please enter the return reason:');
            if (reason) {
              await this.updateShipmentStatus(shipmentId, ShipmentStatus.RETURNED, reason);
            }
            break;
        }
      } catch (error) {
        this.showError('Failed to update shipment status');
        console.error('Error updating status:', error);
      }
    });
  }

  private async loadActiveShipments(): Promise<void> {
    try {
      this.setLoading(true);

      const shipments = await this.shipmentService.getShipments({
        status: [ShipmentStatus.CREATED, ShipmentStatus.PICKED_UP, ShipmentStatus.IN_TRANSIT]
      });

      this.renderActiveShipments(shipments);
    } catch (error) {
      this.showError('Failed to load active shipments');
      console.error('Error loading active shipments:', error);
    } finally {
      this.setLoading(false);
    }
  }

  private async loadCompletedShipments(): Promise<void> {
    try {
      this.setLoading(true);

      const shipments = await this.shipmentService.getShipments({
        status: [ShipmentStatus.DELIVERED, ShipmentStatus.RETURNED],
        limit: 5
      });

      this.renderCompletedShipments(shipments);
    } catch (error) {
      this.showError('Failed to load completed shipments');
      console.error('Error loading completed shipments:', error);
    } finally {
      this.setLoading(false);
    }
  }

  private async loadStats(): Promise<void> {
    try {
      this.setLoading(true);

      const response = await this.shipmentService.getShipments({
        status: [ShipmentStatus.DELIVERED]
      });

      const stats: CourierStats = {
        todayDeliveries: response.filter(s => {
          const today = new Date();
          const updatedAt = new Date(s.updatedAt);
          return updatedAt.toDateString() === today.toDateString();
        }).length,
        successRate: response.length ? (response.filter(s => s.status === ShipmentStatus.DELIVERED).length / response.length) : 0,
        totalEarnings: response.reduce((sum, s) => sum + s.deliveryFee, 0),
        rating: 4.5 // This should come from a proper rating service
      };

      this.renderStats(stats);
    } catch (error) {
      this.showError('Failed to load stats');
      console.error('Error loading stats:', error);
    } finally {
      this.setLoading(false);
    }
  }

  private renderActiveShipments(shipments: Shipment[]): void {
    if (!shipments.length) {
      this.activeShipmentsContainer.innerHTML = `
        <div class="text-center py-8">
          <p class="text-gray-500">No active shipments</p>
        </div>
      `;
      return;
    }

    const shipmentsHtml = shipments.map(shipment => `
      <div class="shipment-card" data-shipment-id="${shipment.id}">
        <div class="shipment-header">
          <h3>Tracking #${shipment.trackingNumber}</h3>
          <span class="status-badge status-${shipment.status.toLowerCase()}">${shipment.status}</span>
        </div>
        <div class="shipment-body">
          <p><strong>Items:</strong> ${this.formatItems(shipment.items)}</p>
          <p><strong>Zone:</strong> ${shipment.toZoneId}</p>
          <p><strong>Total Amount:</strong> ${this.formatCurrency(shipment.totalAmount)}</p>
          ${shipment.notes ? `<p><strong>Notes:</strong> ${shipment.notes}</p>` : ''}
        </div>
        <div class="shipment-actions">
          ${this.renderShipmentActions(shipment)}
        </div>
      </div>
    `).join('');

    this.activeShipmentsContainer.innerHTML = shipmentsHtml;
  }

  private renderCompletedShipments(shipments: Shipment[]): void {
    if (!shipments.length) {
      this.completedShipmentsContainer.innerHTML = `
        <div class="text-center py-8">
          <p class="text-gray-500">No completed shipments</p>
        </div>
      `;
      return;
    }

    const shipmentsHtml = shipments.map(shipment => `
      <div class="shipment-card">
        <div class="shipment-header">
          <h3>Tracking #${shipment.trackingNumber}</h3>
          <span class="status-badge status-${shipment.status.toLowerCase()}">${shipment.status}</span>
        </div>
        <div class="shipment-body">
          <p><strong>Zone:</strong> ${shipment.toZoneId}</p>
          <p><strong>Completed:</strong> ${new Date(shipment.updatedAt).toLocaleString()}</p>
          ${shipment.notes ? `<p><strong>Notes:</strong> ${shipment.notes}</p>` : ''}
        </div>
      </div>
    `).join('');

    this.completedShipmentsContainer.innerHTML = shipmentsHtml;
  }

  private renderStats(stats: CourierStats): void {
    const statsHtml = `
      <div class="grid grid-cols-4 gap-4">
        <div class="stat-card">
          <h3>Today's Deliveries</h3>
          <p>${stats.todayDeliveries}</p>
        </div>
        <div class="stat-card">
          <h3>Success Rate</h3>
          <p>${(stats.successRate * 100).toFixed(1)}%</p>
        </div>
        <div class="stat-card">
          <h3>Total Earnings</h3>
          <p>${this.formatCurrency(stats.totalEarnings)}</p>
        </div>
        <div class="stat-card">
          <h3>Rating</h3>
          <p>${stats.rating.toFixed(1)} / 5.0</p>
        </div>
      </div>
    `;
    this.statsContainer.innerHTML = statsHtml;
  }

  private renderShipmentActions(shipment: Shipment): string {
    switch (shipment.status) {
      case ShipmentStatus.CREATED:
        return `<button class="btn-primary" data-action="pickup">Pick Up</button>`;
      
      case ShipmentStatus.PICKED_UP:
      case ShipmentStatus.IN_TRANSIT:
        return `
          <button class="btn-success" data-action="deliver">Mark Delivered</button>
          <button class="btn-warning" data-action="return">Return</button>
        `;
      
      default:
        return '';
    }
  }

  private async updateShipmentStatus(
    shipmentId: string,
    status: ShipmentStatus,
    note?: string
  ): Promise<void> {
    try {
      await this.shipmentService.updateShipmentStatus(shipmentId, status);
      
      if (note) {
        await this.shipmentService.addNote(shipmentId, note);
      }

      this.showSuccess('Shipment status updated successfully');
      await this.loadData();
    } catch (error) {
      this.showError('Failed to update shipment status');
      console.error('Error updating status:', error);
    }
  }

  private startLocationUpdates(): void {
    if (!navigator.geolocation) {
      this.showWarning('Geolocation is not supported by your browser');
      return;
    }

    this.locationUpdateInterval = setInterval(() => {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          try {
            await this.shipmentService.updateLocation({
              latitude: position.coords.latitude,
              longitude: position.coords.longitude
            });
          } catch (error) {
            console.error('Failed to update location:', error);
          }
        },
        (error) => {
          console.error('Geolocation error:', error);
        },
        {
          enableHighAccuracy: true,
          timeout: 5000,
          maximumAge: 0
        }
      );
    }, 60000); // Update every minute
  }

  private stopLocationUpdates(): void {
    if (this.locationUpdateInterval) {
      clearInterval(this.locationUpdateInterval);
      this.locationUpdateInterval = null;
    }
  }

  private formatItems(items: Shipment['items']): string {
    if (!items?.length) return 'No items';
    return items.map(item => `${item.quantity}x ${item.name}`).join(', ');
  }

  public destroy(): void {
    this.stopLocationUpdates();
  }
}