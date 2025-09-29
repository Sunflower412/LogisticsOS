import React from "react";
import { assignDriver } from "../api/orders";
import type { Order, Driver } from "../api/types";

type Props = {
  orders: Order[];
  drivers: Driver[];
  reload: () => Promise<void>;
};

export default function OrdersTable({ orders, drivers, reload }: Props) {
  const handleAssign = async (orderId: number, driverId: number) => {
    try {
      await assignDriver(orderId, driverId);
      await reload();
    } catch (err) {
      console.error("❌ Ошибка при назначении водителя:", err);
      alert("Ошибка при назначении водителя");
    }
  };

  return (
    <table className="min-w-full bg-white border border-gray-200 rounded-lg shadow-sm">
      <thead>
        <tr className="bg-gray-100 border-b">
          <th className="p-2 text-left">ID</th>
          <th className="p-2 text-left">Описание</th>
          <th className="p-2 text-left">Статус</th>
          <th className="p-2 text-left">Водитель</th>
          <th className="p-2 text-left">Действия</th>
        </tr>
      </thead>
      <tbody>
        {orders.map((order) => (
          <tr key={order.id} className="border-b hover:bg-gray-50">
            <td className="p-2">{order.id}</td>
            <td className="p-2">{order.description}</td>
            <td className="p-2">{order.status}</td>

            {/* Колонка водитель */}
            <td className="p-2">
              {order.driver ? (
                <>
                  #{order.driver.id} — {order.driver.firstname}{" "}
                  {order.driver.lastname}
                </>
              ) : order.driverId ? (
                `#${order.driverId}`
              ) : (
                "—"
              )}
            </td>

            {/* Колонка действий */}
            <td className="p-2">
              {!order.driver && (
                <select
                  onChange={(e) =>
                    handleAssign(order.id, Number(e.target.value))
                  }
                  defaultValue=""
                  className="border rounded px-2 py-1"
                >
                  <option value="" disabled>
                    Назначить водителя
                  </option>
                  {drivers.map((driver) => (
                    <option key={driver.id} value={driver.id}>
                      #{driver.id} — {driver.firstname} {driver.lastname}
                    </option>
                  ))}
                </select>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
