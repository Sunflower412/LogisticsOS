// src/pages/OrderDetails.tsx
import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { fetchOrderById } from "../api/orders";
import type { Order } from "../api/types";

export default function OrderDetails() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        if (id) {
          const data = await fetchOrderById(Number(id));
          setOrder(data);
        }
      } catch (err) {
        console.error("Ошибка загрузки заказа:", err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  if (loading) return <div className="p-4">Загрузка...</div>;
  if (!order) return <div className="p-4">Заказ не найден</div>;

  return (
    <div className="p-6">
      <button
        onClick={() => navigate(-1)}
        className="mb-4 px-3 py-1 bg-gray-200 rounded hover:bg-gray-300"
      >
        ← Назад
      </button>

      <h2 className="text-2xl font-bold mb-4">Детали заказа #{order.id}</h2>

      <div className="space-y-2 text-gray-700">
        <p><strong>Описание:</strong> {order.description}</p>
        <p><strong>Статус:</strong> {order.status}</p>
        <p><strong>Адрес откуда:</strong> {order.fromAddress}</p>
        <p><strong>Адрес куда:</strong> {order.toAddress}</p>
        <p><strong>Вес:</strong> {order.weightKg} кг</p>
        <p><strong>Объём:</strong> {order.volumeM3} м³</p>
        {order.driver ? (
          <p>
            <strong>Водитель:</strong> #{order.driver.id} — {order.driver.firstname} {order.driver.lastName}
          </p>
        ) : (
          <p><strong>Водитель:</strong> не назначен</p>
        )}
      </div>
    </div>
  );
}
