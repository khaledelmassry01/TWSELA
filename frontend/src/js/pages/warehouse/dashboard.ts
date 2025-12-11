import { BasePage } from '../base-page';
import { ShipmentService } from '../../services/shipment';
import { CourierService } from '../../services/courier';
import { ZoneService } from '../../services/zone';
import { Shipment, Courier, Zone, ShipmentStatus } from '../../types';

export default class WarehouseDashboardPage extends BasePage {
  private shipmentService: ShipmentService;
  private courierService: CourierService;
  private zoneService: ZoneService;
  
  private pendingShipmentsContainer: HTMLElement;
  private assignedShipmentsContainer: HTMLElement;
  private couriersContainer: HTMLElement;
  private statsContainer: HTMLElement;

  constructor() {
    super();
    this.shipmentService = new ShipmentService();
    this.courierService = new CourierService();
    this.zoneService = new ZoneService();

    this.pendingShipmentsContainer = this.getElement('#pending-shipments');
    this.assignedShipmentsContainer = this.getElement('#assigned-shipments');
    this.couriersContainer = this.getElement('#available-couriers');
    this.statsContainer = this.getElement('#warehouse-stats');
  }

  protected async init(): Promise<void> {
    try {
      await Promise.all([
        this.loadPendingShipments(),
        this.loadAssignedShipments(),
        this.loadAvailableCouriers(),
        this.loadStats()
      ]);
    } catch (error) {
      await this.handleApiError(error as Error);
    }
  }

  protected bindEvents(): void {
    // Bind refresh buttons
    this.getElements('.refresh-btn').forEach(btn => {
      btn.addEventListener('click', () => this.init());
    });

    // Bind courier assignment
    this.pendingShipmentsContainer.addEventListener('change', async (e) => {
      const target = e.target as HTMLSelectElement;
      if (target.classList.contains('courier-select')) {
        const shipmentId = target.closest('[data-shipment-id]')?.getAttribute('data-shipment-id');
        const courierId = target.value;
        
        if (shipmentId && courierId) {
          await this.assignCourier(shipmentId, courierId);
        }
      }
    });

    // Bind shipment actions
    this.assignedShipmentsContainer.addEventListener('click', async (e) => {
      const target = e.target as HTMLElement;
      const shipmentId = target.closest('[data-shipment-id]')?.getAttribute('data-shipment-id');
      
      if (!shipmentId) return;

      if (target.classList.contains('btn-unassign')) {
        await this.unassignCourier(shipmentId);
      }
    });
  }

  private async loadPendingShipments(): Promise<void> {
    try {
      this.showLoading(this.pendingShipmentsContainer);

      const shipments = await this.shipmentService.getShipments({
        status: ShipmentStatus.CREATED
      });

      if (!shipments.length) {
        this.pendingShipmentsContainer.innerHTML = `
          <div class="text-center py-8">
            <p class="text-gray-500">No pending shipments</p>
          </div>
        `;
        return;
      }

      const couriers = await this.courierService.getActiveCouriers();
      const courierOptions = couriers.map(courier => `
        <option value="${courier.id}">${courier.username} - Zone: ${courier.currentZone?.name || 'Not Assigned'}</option>
      `).join('');

      const shipmentsHtml = shipments.map(shipment => `
        <div class="shipment-card" data-shipment-id="${shipment.id}">
          <div class="shipment-header">
            <h3>Tracking #${shipment.trackingNumber}</h3>
            <span class="status-badge status-created">CREATED</span>
          </div>
          <div class="shipment-details">
            <p><strong>Zone:</strong> ${shipment.zone.name}</p>
            <p><strong>Recipient:</strong> ${shipment.recipientName}</p>
            <p><strong>Address:</strong> ${this.formatAddress(shipment.deliveryAddress)}</p>
          </div>
          <div class="shipment-actions">
            <select class="form-select courier-select">
              <option value="">Assign Courier</option>
              ${courierOptions}
            </select>
          </div>
        </div>
      `).join('');

      this.pendingShipmentsContainer.innerHTML = shipmentsHtml;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.pendingShipmentsContainer);
    }
  }

  private async loadAssignedShipments(): Promise<void> {
    try {
      this.showLoading(this.assignedShipmentsContainer);

      const shipments = await this.shipmentService.getShipments({
        status: [ShipmentStatus.CREATED, ShipmentStatus.PICKED_UP],
        hasAssignedCourier: true
      });

      if (!shipments.length) {
        this.assignedShipmentsContainer.innerHTML = `
          <div class="text-center py-8">
            <p class="text-gray-500">No assigned shipments</p>
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
          <div class="shipment-details">
            <p><strong>Courier:</strong> ${shipment.courier?.username || 'N/A'}</p>
            <p><strong>Recipient:</strong> ${shipment.recipientName}</p>
            <p><strong>Address:</strong> ${this.formatAddress(shipment.deliveryAddress)}</p>
          </div>
          <div class="shipment-actions">
            <button class="btn-danger btn-unassign">Unassign Courier</button>
          </div>
        </div>
      `).join('');

      this.assignedShipmentsContainer.innerHTML = shipmentsHtml;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.assignedShipmentsContainer);
    }
  }

  private async loadAvailableCouriers(): Promise<void> {
    try {
      this.showLoading(this.couriersContainer);

      const couriers = await this.courierService.getActiveCouriers();
      
      if (!couriers.length) {
        this.couriersContainer.innerHTML = `
          <div class="text-center py-8">
            <p class="text-gray-500">No available couriers</p>
          </div>
        `;
        return;
      }

      const couriersHtml = couriers.map(courier => `
        <div class="courier-card">
          <div class="courier-header">
            <h3>${courier.username}</h3>
            <span class="status-badge ${courier.isOnline ? 'status-online' : 'status-offline'}">
              ${courier.isOnline ? 'Online' : 'Offline'}
            </span>
          </div>
          <div class="courier-details">
            <p><strong>Zone:</strong> ${courier.currentZone?.name || 'Not Assigned'}</p>
            <p><strong>Rating:</strong> ${courier.rating.toFixed(1)} / 5.0</p>
            <p><strong>Success Rate:</strong> ${(courier.successRate * 100).toFixed(1)}%</p>
          </div>
        </div>
      `).join('');

      this.couriersContainer.innerHTML = couriersHtml;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.couriersContainer);
    }
  }

  private async loadStats(): Promise<void> {
    try {
      this.showLoading(this.statsContainer);

      const stats = await this.api.get('/warehouse/stats');
      
      const html = `
        <div class="grid grid-cols-4 gap-4">
          <div class="stat-card">
            <h3>Pending Assignment</h3>
            <p>${stats.pendingAssignment}</p>
          </div>
          <div class="stat-card">
            <h3>Active Deliveries</h3>
            <p>${stats.activeDeliveries}</p>
          </div>
          <div class="stat-card">
            <h3>Available Couriers</h3>
            <p>${stats.availableCouriers}</p>
          </div>
          <div class="stat-card">
            <h3>Avg Assignment Time</h3>
            <p>${stats.avgAssignmentTime} min</p>
          </div>
        </div>
      `;

      this.statsContainer.innerHTML = html;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.statsContainer);
    }
  }

  private async assignCourier(shipmentId: string, courierId: string): Promise<void> {
    try {
      await this.shipmentService.assignCourier(shipmentId, courierId);
      this.notifications.success('Courier assigned successfully');
      await this.init();
    } catch (error) {
      await this.handleApiError(error as Error);
    }
  }

  private async unassignCourier(shipmentId: string): Promise<void> {
    if (!confirm('Are you sure you want to unassign the courier from this shipment?')) {
      return;
    }

    try {
      await this.shipmentService.unassignCourier(shipmentId);
      this.notifications.success('Courier unassigned successfully');
      await this.init();
    } catch (error) {
      await this.handleApiError(error as Error);
    }
  }

  private formatAddress(address: any): string {
    if (!address) return 'N/A';
    return `${address.street}, ${address.city}`;
  }
}