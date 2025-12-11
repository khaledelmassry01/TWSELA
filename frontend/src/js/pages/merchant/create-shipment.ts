import { BasePage } from '../base-page';
import { ShipmentService } from '../../services/shipment';
import { ZoneService } from '../../services/zone';
import { Shipment, ShipmentItem, Zone, Address } from '../../types';

export default class MerchantCreateShipmentPage extends BasePage {
  private shipmentService: ShipmentService;
  private zoneService: ZoneService;
  private form: HTMLFormElement;
  private itemsContainer: HTMLElement;
  private addItemBtn: HTMLButtonElement;
  private submitBtn: HTMLButtonElement;
  private selectedZone: Zone | null = null;

  constructor() {
    super();
    this.shipmentService = new ShipmentService();
    this.zoneService = new ZoneService();
    this.form = this.getElement<HTMLFormElement>('#create-shipment-form');
    this.itemsContainer = this.getElement('#shipment-items');
    this.addItemBtn = this.getElement<HTMLButtonElement>('#add-item-btn');
    this.submitBtn = this.getElement<HTMLButtonElement>('#submit-btn');
  }

  protected async initializePage(): Promise<void> {
    this.addInitialItem();
  }

  protected async loadData(): Promise<void> {
    try {
      this.setLoading(true);
      const zones = await this.zoneService.getAllZones();
      this.renderZoneSelect(zones);
    } catch (error) {
      this.showError('Failed to load zones');
      console.error('Error loading zones:', error);
    } finally {
      this.setLoading(false);
    }
  }

  protected setupEventListeners(): void {
    this.addItemBtn.addEventListener('click', (e) => {
      e.preventDefault();
      this.addItemRow();
    });

    this.form.addEventListener('submit', async (e) => {
      e.preventDefault();
      await this.handleSubmit();
    });

    this.itemsContainer.addEventListener('click', (e) => {
      const target = e.target as HTMLElement;
      if (target.classList.contains('remove-item')) {
        const row = target.closest('.item-row');
        if (row && this.itemsContainer.children.length > 1) {
          row.remove();
          this.updateTotalAmount();
        }
      }
    });

    this.itemsContainer.addEventListener('input', (e) => {
      const target = e.target as HTMLInputElement;
      if (target.classList.contains('item-quantity') || target.classList.contains('item-price')) {
        this.updateTotalAmount();
      }
    });
  }

  private renderZoneSelect(zones: Zone[]): void {
    const select = this.getElement<HTMLSelectElement>('#zone-select');
    select.innerHTML = `
      <option value="">Select Delivery Zone</option>
      ${zones.map(zone => `
        <option value="${zone.id}">${zone.name} - Base Price: ${zone.basePrice} EGP</option>
      `).join('')}
    `;

    select.addEventListener('change', () => {
      const zoneId = select.value;
      this.selectedZone = zones.find(z => z.id === parseInt(zoneId)) || null;
      this.updateTotalAmount();
    });
  }

  private addInitialItem(): void {
    this.addItemRow();
  }

  private addItemRow(): void {
    const row = document.createElement('div');
    row.className = 'item-row grid grid-cols-4 gap-4 mb-4';
    row.innerHTML = `
      <div>
        <input type="text" class="form-input item-name" placeholder="Item Name" required>
      </div>
      <div>
        <input type="number" class="form-input item-quantity" placeholder="Quantity" min="1" value="1" required>
      </div>
      <div>
        <input type="number" class="form-input item-price" placeholder="Price (EGP)" step="0.01" min="0" required>
      </div>
      <div>
        <button type="button" class="btn-danger remove-item">Remove</button>
      </div>
    `;
    this.itemsContainer.appendChild(row);
  }

  private updateTotalAmount(): void {
    let total = 0;
    const items = this.itemsContainer.querySelectorAll('.item-row');
    
    items.forEach(row => {
      const quantity = parseInt((row.querySelector('.item-quantity') as HTMLInputElement).value) || 0;
      const price = parseFloat((row.querySelector('.item-price') as HTMLInputElement).value) || 0;
      total += quantity * price;
    });

    const deliveryFee = this.selectedZone?.basePrice || 0;
    total += deliveryFee;

    const totalElement = this.getElement('#total-amount');
    totalElement.textContent = `${total.toFixed(2)} EGP`;
  }

  private getFormData(): Partial<Shipment> {
    const formData = new FormData(this.form);
    
    const itemRows = this.itemsContainer.querySelectorAll('.item-row');
    const itemsData = Array.from(itemRows).map(row => ({
      name: (row.querySelector('.item-name') as HTMLInputElement).value,
      quantity: parseInt((row.querySelector('.item-quantity') as HTMLInputElement).value),
      price: parseFloat((row.querySelector('.item-price') as HTMLInputElement).value)
    }));

    // Create address data without country field which is not in our type definition
    const address = {
      street: formData.get('street') as string,
      city: formData.get('city') as string,
      state: formData.get('state') as string || undefined,
      postalCode: formData.get('postal-code') as string || undefined,
      instructions: formData.get('address-notes') as string || undefined
    };

    // Create new shipment with mandatory shipment items and without unnecessary fields
    const items = itemsData.map((item, index) => ({
      id: -1 * (index + 1), // Temporary negative IDs for new items
      ...item
    }));

    return {
      recipientName: formData.get('recipient-name') as string,
      recipientPhone: formData.get('recipient-phone') as string,
      deliveryAddress: address,
      items,
      toZoneId: this.selectedZone?.id ?? 0
    };
  }

  private async handleSubmit(): Promise<void> {
    try {
      this.submitBtn.disabled = true;
      this.showLoading(this.form);

      const shipmentData = this.getFormData();
      
      // Validate required fields
      if (!this.validateRequired({
        'Recipient Name': shipmentData.recipientName || '',
        'Recipient Phone': shipmentData.recipientPhone || '',
        'Delivery Zone': this.selectedZone ? this.selectedZone.id : '',
        'Street Address': shipmentData.deliveryAddress?.street || ''
      })) {
        return;
      }

      const shipment = await this.shipmentService.createShipment(shipmentData);
      
      this.notifications.success('Shipment created successfully');
      window.location.href = `/merchant/shipment-details.html?id=${shipment.id}`;
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.submitBtn.disabled = false;
      this.hideLoading(this.form);
    }
  }
}