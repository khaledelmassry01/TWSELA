import { BasePage } from '../base-page';
import { ShipmentService } from '../../services/shipment';
import { Shipment, ShipmentStatus } from '../../types';

interface ManifestStats {
  totalStops: number;
  totalValue: number;
  pickups: number;
  deliveries: number;
}

interface PageState {
  isLoading: boolean;
  error: string | null;
}

export default class CourierManifestPage extends BasePage {
  private shipmentService: ShipmentService;
  private manifestContainer: HTMLElement | null = null;
  private printBtn: HTMLButtonElement | null = null;
  private shipments: Shipment[] = [];
  private pageState: PageState = {
    isLoading: false,
    error: null
  };
  private stats: ManifestStats = {
    totalStops: 0,
    totalValue: 0,
    pickups: 0,
    deliveries: 0
  };

  constructor() {
    super();
    this.shipmentService = new ShipmentService();
  }

  protected async initializePage(): Promise<void> {
    this.manifestContainer = document.getElementById('manifest-container');
    this.printBtn = document.getElementById('print-manifest') as HTMLButtonElement;

    if (!this.manifestContainer || !this.printBtn) {
      this.showError('Required page elements not found');
      return;
    }
  }

  protected async loadData(): Promise<void> {
    try {
      this.pageState.isLoading = true;
      const response = await this.shipmentService.getShipments({
        status: [ShipmentStatus.CREATED, ShipmentStatus.PICKED_UP]
      });

      this.shipments = response;
      this.calculateStats();
      this.optimizeRoute();
      this.renderManifest();
    } catch (error) {
      this.pageState.error = 'Failed to load manifest data';
      console.error('Error loading manifest:', error);
      this.showError('Failed to load manifest data');
    } finally {
      this.pageState.isLoading = false;
      this.renderManifest();
    }
  }

  private calculateStats(): void {
    this.stats = this.shipments.reduce((acc, shipment) => ({
      totalStops: acc.totalStops + 1,
      totalValue: acc.totalValue + shipment.totalAmount,
      pickups: acc.pickups + (shipment.status === ShipmentStatus.CREATED ? 1 : 0),
      deliveries: acc.deliveries + (shipment.status === ShipmentStatus.PICKED_UP ? 1 : 0)
    }), {
      totalStops: 0,
      totalValue: 0,
      pickups: 0,
      deliveries: 0
    });
  }

  protected setupEventListeners(): void {
    if (!this.printBtn || !this.manifestContainer) return;

    this.printBtn.addEventListener('click', () => {
      window.print();
    });

    this.manifestContainer.addEventListener('click', async (e) => {
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
            await this.updateStatus(shipmentId, ShipmentStatus.PICKED_UP);
            break;

          case 'deliver':
            await this.updateStatus(shipmentId, ShipmentStatus.DELIVERED);
            break;

          case 'return':
            const reason = window.prompt('Please enter the return reason:');
            if (reason) {
              await this.updateStatus(shipmentId, ShipmentStatus.RETURNED, reason);
            }
            break;
        }
      } catch (error) {
        this.showError('Failed to update shipment status');
        console.error('Error updating status:', error);
      }
    });
  }

  private renderManifest(): void {
    if (!this.manifestContainer || !this.printBtn) return;

    if (this.pageState.isLoading) {
      this.manifestContainer.innerHTML = `
        <div class="loading-spinner">
          <div class="spinner"></div>
          <p>Loading manifest...</p>
        </div>
      `;
      return;
    }

    if (this.pageState.error) {
      this.manifestContainer.innerHTML = `
        <div class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          <p>${this.pageState.error}</p>
        </div>
      `;
      return;
    }

    if (!this.shipments.length) {
      this.manifestContainer.innerHTML = `
        <div class="text-center py-8">
          <p class="text-gray-500">No shipments in your manifest</p>
        </div>
      `;
      this.printBtn.disabled = true;
      return;
    }

    this.printBtn.disabled = false;

    this.manifestContainer.innerHTML = `
      <div class="manifest-header">
        <h2>Delivery Manifest - ${new Date().toLocaleDateString('ar-EG')}</h2>
        <div class="manifest-stats grid grid-cols-4 gap-4">
          <div class="stat-card">
            <h3>Total Stops</h3>
            <p>${this.stats.totalStops}</p>
          </div>
          <div class="stat-card">
            <h3>Pickups</h3>
            <p>${this.stats.pickups}</p>
          </div>
          <div class="stat-card">
            <h3>Deliveries</h3>
            <p>${this.stats.deliveries}</p>
          </div>
          <div class="stat-card">
            <h3>Total Value</h3>
            <p>${this.formatCurrency(this.stats.totalValue)}</p>
          </div>
        </div>
      </div>
      <div class="manifest-list">
        ${this.shipments.map((shipment, index) => this.renderShipmentCard(shipment, index)).join('')}
      </div>
    `;
  }

  private renderShipmentCard(shipment: Shipment, index: number): string {
    return `
      <div class="manifest-item" data-shipment-id="${shipment.id}">
        <div class="manifest-header">
          <span class="stop-number">${index + 1}</span>
          <h3>Tracking #${shipment.trackingNumber}</h3>
          <span class="status-badge status-${shipment.status.toLowerCase()}">${shipment.status}</span>
        </div>
        <div class="manifest-details">
          <div class="shipment-info">
            <p><strong>Zone:</strong> ${shipment.toZoneId}</p>
            <p><strong>Items:</strong> ${this.formatItems(shipment.items)}</p>
            <p><strong>Total Amount:</strong> ${this.formatCurrency(shipment.totalAmount)}</p>
            ${this.formatNotes(shipment.notes)}
          </div>
        </div>
        <div class="manifest-actions">
          ${this.renderActionButtons(shipment)}
        </div>
      </div>
    `;
  }

  private formatNotes(notes?: string): string {
    if (!notes) return '';
    return `
      <p><strong>Notes:</strong> 
        <span class="text-sm text-gray-600">${notes}</span>
      </p>
    `;
  }

  private renderActionButtons(shipment: Shipment): string {
    switch (shipment.status) {
      case ShipmentStatus.CREATED:
        return `
          <button class="btn-primary" data-action="pickup">
            <i class="fas fa-box"></i> Pick Up
          </button>
        `;
      
      case ShipmentStatus.PICKED_UP:
        return `
          <button class="btn-success" data-action="deliver">
            <i class="fas fa-check"></i> Deliver
          </button>
          <button class="btn-warning" data-action="return">
            <i class="fas fa-undo"></i> Return
          </button>
        `;
      
      default:
        return '';
    }
  }

  private async updateStatus(
    shipmentId: string,
    status: ShipmentStatus,
    note?: string
  ): Promise<void> {
    try {
      await this.shipmentService.updateShipmentStatus(shipmentId, status);
      
      if (note) {
        await this.shipmentService.addNote(shipmentId, note);
      }

      // Update local data
      this.shipments = this.shipments.filter(s => String(s.id) !== shipmentId);
      this.calculateStats();
      this.renderManifest();
      
      this.showSuccess('Shipment status updated successfully');
    } catch (error) {
      this.showError('Failed to update shipment status');
      console.error('Error updating status:', error);
    }
  }

  private optimizeRoute(): void {
    // Group shipments by zone for more efficient routing
    const zoneGroups = this.shipments.reduce((groups, shipment) => {
      if (!groups[shipment.toZoneId]) {
        groups[shipment.toZoneId] = [];
      }
      groups[shipment.toZoneId].push(shipment);
      return groups;
    }, {} as Record<number, Shipment[]>);

    // Sort shipments within each zone by status
    Object.values(zoneGroups).forEach(zoneShipments => {
      zoneShipments.sort((a, b) => {
        // Prioritize pickups over deliveries
        if (a.status !== b.status) {
          return a.status === ShipmentStatus.CREATED ? -1 : 1;
        }
        return 0;
      });
    });

    // Flatten the groups back into a single array
    this.shipments = Object.values(zoneGroups).flat();
  }

  private formatItems(items: Shipment['items']): string {
    if (!items?.length) return 'No items';
    return items.map(item => `${item.quantity}x ${item.name}`).join(', ');
  }

  protected formatCurrency(amount: number): string {
    return new Intl.NumberFormat('ar-EG', {
      style: 'currency',
      currency: 'EGP'
    }).format(amount);
  }
}