import React from "react";
import { assignDriver } from "../api/client";

export default function OrdersTable({ orders, drivers, reload }) {
  const handleAssign = async (orderId, driverId) => {
    try {
      await assignDriver(orderId, driverId);
      await reload(); // обновляем список заказов
    } catch (err) {
      console.error("Ошибка при назначении водителя:", err);
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
            <td className="p-2">
              {order.driver
                ? `${order.driver.firstname} ${order.driver.lastName}`
                : "—"}
            </td>
            <td className="p-2">
              {!order.driver && (
                <select
                  onChange={(e) =>
                    handleAssign(order.id, Number(e.target.value))
                  }
                  defaultValue=""
                >
                  <option value="" disabled>
                    Назначить водителя
                  </option>
                  {drivers.map((driver) => (
                    <option key={driver.id} value={driver.id}>
                      {driver.firstname} {driver.lastName}
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
