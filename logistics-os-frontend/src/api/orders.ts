import { Order } from "./types";

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080/api";

export async function fetchOrders(): Promise<Order[]> {
  const res = await fetch(`${API_BASE}/orders`);
  if (!res.ok) throw new Error("Не удалось загрузить заказы");
  return res.json();
}

export async function createOrder(order: Partial<Order>): Promise<Order> {
  const res = await fetch(`${API_BASE}/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
  if (!res.ok) throw new Error("Не удалось создать заказ");
  return res.json();
}
export async function fetchOrderById(orderId: number): Promise<Order> {
  const res = await fetch(`${API_BASE}/orders/${orderId}`);
  if (!res.ok) throw new Error(`Failed to fetch order with id ${orderId}`);
  return res.json();
}
export async function assignDriver(orderId: number, driverId: number): Promise<void> {
  const res = await fetch(
    `${API_BASE}/orders/${orderId}/assign-driver?driverId=${driverId}`,
    { method: "POST" }
  );
  if (!res.ok) throw new Error("Ошибка при назначении водителя");
}
