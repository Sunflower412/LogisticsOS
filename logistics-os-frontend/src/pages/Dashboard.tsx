import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import MapView from "../components/MapView";
import OrdersBar from "../components/OrdersBar";
import EventsPanel from "../components/EventsPanel";
import { fetchDrivers, fetchNotifications } from "../api/client";
import type { Order, Driver } from "../api/types";

export default function Dashboard({
  orders,
  setOrders,
}: {
  orders: Order[];
  setOrders: React.Dispatch<React.SetStateAction<Order[]>>;
}) {
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [events, setEvents] = useState<any[]>([]);
  const [seenOrders, setSeenOrders] = useState<number[]>([]);
  const navigate = useNavigate();

  const loadDriversAndEvents = async () => {
    try {
      const [ds, evs] = await Promise.all([fetchDrivers(), fetchNotifications()]);

      setDrivers(ds);

      const systemEvents =
        evs?.map((t: string, i: number) => ({ id: `n-${i}`, text: t })) ?? [];

      const orderEvents = orders
        .filter(
          (o) =>
            (o.status === "DELIVERED" || o.status === "FAILED") &&
            !seenOrders.includes(o.id)
        )
        .map((o) => ({
          id: `o-${o.id}`,
          text:
            o.status === "DELIVERED"
              ? `✅ Заказ #${o.id} выполнен`
              : `⚠️ Заказ #${o.id} задержан или не выполнен`,
        }));

      if (orderEvents.length > 0) {
        setSeenOrders((prev) => [
          ...prev,
          ...orderEvents.map((e) => Number(e.id.split("-")[1])),
        ]);
      }

      let newEvents = [...systemEvents, ...events, ...orderEvents];
      if (newEvents.length > 20) newEvents = newEvents.slice(-20);

      setEvents(newEvents);
    } catch (err) {
      console.error("Ошибка при загрузке:", err);
    }
  };

  useEffect(() => {
    loadDriversAndEvents();
    const interval = setInterval(loadDriversAndEvents, 30000);
    return () => clearInterval(interval);
  }, [orders]);

  return (
    <div className="flex flex-col h-screen">
      {/* верхняя панель заказов (с кнопкой "Добавить заказ" справа) */}
      <OrdersBar orders={orders.slice(-15)} onAdd={() => navigate("/add-order")} />

      {/* нижняя панель с кнопкой Все заказы */}
      <div className="p-4 bg-gray-100 border-b flex gap-4">
        <button
          onClick={() => navigate("/orders")}
          className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
        >
          📋 Все заказы
        </button>
      </div>

      {/* карта + события */}
      <div className="flex flex-1">
        <div className="flex-1">
          <MapView orders={orders} drivers={drivers} routes={[]} />
        </div>
        <EventsPanel events={events} />
      </div>
    </div>
  );
}
