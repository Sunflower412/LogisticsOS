import { Driver } from "./types";

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080/api";

export async function fetchDrivers(): Promise<Driver[]> {
  const res = await fetch(`${API_BASE}/drivers`);
  if (!res.ok) throw new Error("Не удалось загрузить водителей");
  return res.json();
}

export async function fetchNotifications(): Promise<string[]> {
  const res = await fetch(`${API_BASE}/notifications`);
  if (!res.ok) throw new Error("Не удалось загрузить уведомления");
  return res.json();
}
