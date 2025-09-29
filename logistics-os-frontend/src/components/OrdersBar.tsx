import React from "react";
import type { Order } from "../api/types";

type Props = {
  orders?: Order[]; // сделал необязательным
  onAdd?: () => void;
};

export default function OrdersBar({ orders = [], onAdd }: Props) {
  // последние 15 заказов, сортировка по id убыванию
  const lastOrders = [...orders]
    .filter((o) => o && o.id) // защита от мусора
    .sort((a, b) => b.id - a.id)
    .slice(0, 15);

  // Цвета по статусам
  const statusColors: Record<string, string> = {
    CREATED: "bg-gray-400 text-white",
    ASSIGNED: "bg-blue-500 text-white",
    DELIVERED: "bg-green-600 text-white",
    FAILED: "bg-red-500 text-white",
  };

  return (
    <div className="flex items-center gap-2 p-4 bg-gray-100 border-b overflow-x-auto">
      <h2 className="font-bold text-lg mr-4">Заказы:</h2>

      {lastOrders.length === 0 ? (
        <span className="text-gray-500">Нет заказов</span>
      ) : (
        lastOrders.map((order) => (
          <span
            key={order.id}
            className={`px-3 py-1 text-sm rounded-full shadow-sm ${
              statusColors[order.status] || "bg-gray-200 text-black"
            }`}
          >
            №{order.id}: {order.status}
          </span>
        ))
      )}

      {onAdd && (
        <button
          onClick={onAdd}
          className="ml-auto px-4 py-1 bg-yellow-500 text-white rounded hover:bg-yellow-600"
        >
          ➕ Добавить заказ
        </button>
      )}
    </div>
  );
}
