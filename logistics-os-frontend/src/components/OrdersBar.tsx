import React from "react";
import type { Order } from "../api/types";

export default function OrdersBar({ orders, onAdd }: { orders: Order[]; onAdd: () => void; }) {
  return (
    <div className="flex gap-2 p-2 bg-gray-100 shadow">
      {orders.map(o => (
        <div key={o.id}
             className={`px-3 py-1 rounded ${o.status === "IN_PROGRESS" ? "bg-blue-200" :
               o.status === "DELIVERED" ? "bg-green-200" :
               "bg-yellow-200"}`}>
          №{o.id}: {o.status}
        </div>
      ))}
      <button onClick={onAdd} className="ml-auto px-3 py-1 rounded bg-blue-600 text-white">
        Добавить заказ
      </button>
    </div>
  );
}
