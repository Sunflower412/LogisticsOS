export type OrderStatus =
  | "CREATED"
  | "ASSIGNED"
  | "IN_PROGRESS"
  | "DELIVERED"
  | "FAILED"
  | "COMPLETED_BY_DISPATCHER"
  | "CANCELLED";

export interface Client {
  id?: number;
  companyName?: string;
  contactPerson?: string;
  deliveryTimeWindow?: string;
  phone?: string;
  fromAddress?: string;
  toAddress?: string;
  priority?: number;
}

export interface Driver {
  id?: number;
  // backend может содержать rating, phone, name и т.д. — добавь по необходимости
  ratingAllTime?: number;
  ratingMonthly?: number;
  completedOrdersAllTime?: number;
  completedOrdersMonthly?: number;
  failedOrdersAllTime?: number;
  failedOrdersMonthly?: number;
  // optional location payloads:
  position?: [number, number];
  latitude?: number;
  longitude?: number;
  name?: string;
}

export interface Order {
  id?: number;
  description?: string;
  complexity?: number;
  urgency?: number;
  routeLength?: number;
  delayPenalty?: number;
  fromAddress?: string;
  toAddress?: string;
  weightKg?: number;
  volumeM3?: number;
  client?: Client;
  driver?: Driver;
  durationTime?: number;
  plannedDeliveryTime?: string | null; // ISO
  actualDeliveryTime?: string | null;
  completedAt?: string | null;
  completedSuccessfully?: boolean;
  status?: OrderStatus;
  // optional coords if backend provides:
  position?: [number, number];
  latitude?: number;
  longitude?: number;
}
