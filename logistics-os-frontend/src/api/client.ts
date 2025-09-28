import type { Order, Driver } from "./types";

const API_BASE = "/api";

// --- заказы ---
export async function fetchOrders(): Promise<Order[]> {
  const res = await fetch(`${API_BASE}/orders`);
  if (!res.ok) throw new Error("Failed to fetch orders");
  return res.json();
}

export async function createOrder(order: Partial<Order>): Promise<Order> {
  const res = await fetch(`${API_BASE}/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
  if (!res.ok) throw new Error("Failed to create order");
  return res.json();
}

// --- водители ---
export async function fetchDrivers(): Promise<Driver[]> {
  const res = await fetch(`${API_BASE}/drivers`);
  if (!res.ok) throw new Error("Failed to fetch drivers");
  return res.json();
}

// --- уведомления ---
export async function fetchNotifications(): Promise<string[]> {
  const res = await fetch(`${API_BASE}/notifications`);
  if (!res.ok) throw new Error("Failed to fetch notifications");
  return res.json();
}

// --- назначение водителя ---
export async function assignDriver(orderId: number, driverId: number): Promise<Order> {
  const res = await fetch(`${API_BASE}/orders/${orderId}/assign-driver?driverId=${driverId}`, {
    method: "POST",
  });
  if (!res.ok) throw new Error("Failed to assign driver");
  return res.json();
}
