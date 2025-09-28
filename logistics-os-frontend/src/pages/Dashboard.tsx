import React, { useEffect, useState } from "react";
import MapView from "../components/MapView";
import OrdersBar from "../components/OrdersBar";
import EventsPanel from "../components/EventsPanel";
import AddOrderForm from "../components/AddOrderForm";
import type { Order, Driver } from "../api/types";
import {
  fetchOrders,
  fetchDrivers,
  createOrder,
  fetchNotifications,
  assignDriver,
} from "../api/client";

export default function Dashboard() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [events, setEvents] = useState<any[]>([]);

  const loadAll = async () => {
    try {
      const [os, ds, evs] = await Promise.all([
        fetchOrders(),
        fetchDrivers(),
        fetchNotifications(),
      ]);
      setOrders(os);
      setDrivers(ds);
      setEvents(evs?.map((t: string, i: number) => ({ id: i, text: t })) ?? []);
    } catch (err) {
      console.error("load error", err);
    }
  };

  useEffect(() => {
    loadAll();
    const interval = setInterval(loadAll, 30000);
    return () => clearInterval(interval);
  }, []);

  // ✅ автоназначение водителя после создания заказа
  const handleOrderCreated = async (order: Order) => {
    setOrders((prev) => [...prev, order]);

    if (drivers.length > 0) {
      try {
        const driverId = drivers[0].id; // пока берём первого
        await assignDriver(order.id, driverId);
        await loadAll(); // обновим всё после назначения
      } catch (err) {
        console.error("Ошибка автоназначения:", err);
      }
    }
  };

  const handleEventAction = (action: string) => {
    if (action === "call_driver") alert("Звоню водителю...");
    else if (action === "notify_client") alert("Уведомляем клиента...");
    else alert("Action: " + action);
  };

  return (
    <div className="flex flex-col h-screen">
      {/* панель заказов */}
      <OrdersBar orders={orders} onAdd={() => {}} />

      {/* форма добавления заказа */}
      <div className="p-4 bg-gray-100 border-b">
        <h2 className="text-lg font-semibold mb-2">Добавить заказ</h2>
        <AddOrderForm onOrderCreated={handleOrderCreated} />
      </div>

      <div className="flex flex-1">
        <div className="flex-1">
          <MapView orders={orders} drivers={drivers} routes={[]} />
        </div>
        <EventsPanel events={events} onAction={handleEventAction} />
      </div>
    </div>
  );
}
