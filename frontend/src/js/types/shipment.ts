// Shipment Status
export enum ShipmentStatus {
  CREATED = 'CREATED',
  PICKED_UP = 'PICKED_UP',
  IN_TRANSIT = 'IN_TRANSIT',
  DELIVERED = 'DELIVERED',
  RETURNED = 'RETURNED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

// Shipment Note
export interface ShipmentNote {
  id: number;
  note: string;
  type: 'STATUS' | 'MANUAL';
  userId: number;
  createdAt: string;
}

// Base Shipment Item
export interface ShipmentItem {
  id: number;
  name: string;
  quantity: number;
  price: number;
}

// Base Shipment Details
export interface Shipment {
  id: number;
  trackingNumber: string;
  merchantId: number;
  courierId?: number;
  zoneId: number;
  recipientName: string;
  recipientPhone: string;
  deliveryAddress: {
    street: string;
    city: string;
    state: string;
    postalCode?: string;
  };
  status: ShipmentStatus;
  items: ShipmentItem[];
  price: number;
  codAmount: number;
  deliveryFee: number;
  totalAmount: number;
  notes: ShipmentNote[];
  createdAt: string;
  updatedAt: string;
}