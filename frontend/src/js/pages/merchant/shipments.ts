import { BasePage } from '../base-page';
import { ShipmentService } from '../../services/shipment';
import { Shipment, ShipmentStatus, PaginatedResponse, Address, ShipmentFilter } from '../../types';

export default class MerchantShipmentsPage extends BasePage {
  private shipmentService: ShipmentService;
  private shipmentsContainer!: HTMLElement;
  private paginationContainer!: HTMLElement;
  private statusFilter!: HTMLSelectElement;
  private searchInput!: HTMLInputElement;
  private currentPage = 1;
  private pageSize = 10;

  constructor() {
    super();
    this.shipmentService = new ShipmentService();
  }

  protected async initializePage(): Promise<void> {
    this.shipmentsContainer = this.getElement('#shipments-list');
    this.paginationContainer = this.getElement('#pagination');
    this.statusFilter = this.getElement<HTMLSelectElement>('#status-filter');
    this.searchInput = this.getElement<HTMLInputElement>('#search-input');
  }

  protected async loadData(): Promise<void> {
    try {
      this.renderStatusFilter();
      await this.loadShipments();
    } catch (error) {
      this.handleApiError(error as Error);
    }
  }

  protected setupEventListeners(): void {
    this.statusFilter.addEventListener('change', () => {
      this.currentPage = 1;
      this.loadShipments();
    });

    this.searchInput.addEventListener('input', this.debounce(() => {
      this.currentPage = 1;
      this.loadShipments();
    }, 300));

    this.paginationContainer.addEventListener('click', (e) => {
      const target = e.target as HTMLElement;
      if (target.classList.contains('page-btn')) {
        e.preventDefault();
        const page = parseInt(target.getAttribute('data-page') || '1');
        if (page !== this.currentPage) {
          this.currentPage = page;
          this.loadShipments();
        }
      }
    });
  }

  private async loadShipments(): Promise<void> {
    try {
      this.showLoading(this.shipmentsContainer);

      const filter: ShipmentFilter = {
        status: this.statusFilter.value ? (this.statusFilter.value as ShipmentStatus) : undefined,
        search: this.searchInput.value || undefined,
        offset: (this.currentPage - 1) * this.pageSize,
        limit: this.pageSize
      };

      const response = await this.shipmentService.getShipments(filter);
      
      const paginatedResponse: PaginatedResponse<Shipment> = {
        items: response,
        total: response.length,
        page: this.currentPage,
        limit: this.pageSize,
        hasMore: response.length === this.pageSize
      };
      this.renderShipments(paginatedResponse);
    } catch (error) {
      await this.handleApiError(error as Error);
    } finally {
      this.hideLoading(this.shipmentsContainer);
    }
  }

  private renderStatusFilter(): void {
    const options = Object.entries(ShipmentStatus).map(([key, value]) => `
      <option value="${value}">${key.replace(/_/g, ' ')}</option>
    `).join('');

    this.statusFilter.innerHTML = `
      <option value="">All Status</option>
      ${options}
    `;
  }

  private renderShipments(response: PaginatedResponse<Shipment>): void {
    if (!response.items.length) {
      this.shipmentsContainer.innerHTML = `
        <div class="text-center py-8">
          <p class="text-gray-500">No shipments found</p>
        </div>
      `;
      this.paginationContainer.innerHTML = '';
      return;
    }

    const shipmentsHtml = response.items.map(shipment => `
      <tr>
        <td>${shipment.trackingNumber}</td>
        <td>${shipment.recipientName}</td>
        <td>${shipment.recipientPhone}</td>
        <td>${this.formatAddress(shipment.deliveryAddress)}</td>
        <td><span class="status-badge status-${shipment.status.toLowerCase()}">${shipment.status}</span></td>
        <td>${shipment.createdAt ? new Date(shipment.createdAt).toLocaleDateString() : '-'}</td>
        <td>
          <a href="/merchant/shipment-details.html?id=${shipment.id}" class="btn-link">View Details</a>
        </td>
      </tr>
    `).join('');

    this.shipmentsContainer.innerHTML = `
      <table class="table-auto w-full">
        <thead>
          <tr>
            <th>Tracking #</th>
            <th>Recipient</th>
            <th>Phone</th>
            <th>Address</th>
            <th>Status</th>
            <th>Created At</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${shipmentsHtml}
        </tbody>
      </table>
    `;

    this.renderPagination(response);
  }

  private renderPagination(response: PaginatedResponse<Shipment>): void {
    const totalPages = Math.ceil(response.total / this.pageSize);
    if (totalPages <= 1) {
      this.paginationContainer.innerHTML = '';
      return;
    }

    let pagesHtml = '';
    const maxVisiblePages = 5;
    let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    if (startPage > 1) {
      pagesHtml += `
        <button class="page-btn" data-page="1">&laquo;</button>
        ${startPage > 2 ? '<span class="px-2">...</span>' : ''}
      `;
    }

    for (let i = startPage; i <= endPage; i++) {
      pagesHtml += `
        <button class="page-btn ${i === this.currentPage ? 'active' : ''}" 
                data-page="${i}">${i}</button>
      `;
    }

    if (endPage < totalPages) {
      pagesHtml += `
        ${endPage < totalPages - 1 ? '<span class="px-2">...</span>' : ''}
        <button class="page-btn" data-page="${totalPages}">&raquo;</button>
      `;
    }

    this.paginationContainer.innerHTML = `
      <div class="flex justify-center items-center space-x-2">
        ${pagesHtml}
      </div>
    `;
  }

  private formatAddress(address: Address): string {
    if (!address) return '-';
    return `${address.street}, ${address.city}`;
  }


}