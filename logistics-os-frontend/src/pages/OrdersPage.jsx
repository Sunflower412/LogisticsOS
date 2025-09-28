import { useEffect, useState } from "react";
import AddOrderForm from "../components/AddOrderForm";
import OrdersTable from "../components/OrdersTable";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    fetch("http://localhost:8080/api/orders")
      .then((res) => res.json())
      .then((data) => setOrders(data))
      .catch((err) => console.error("Ошибка загрузки заказов:", err));
  }, []);

  const handleOrderCreated = (order) => {
    setOrders((prev) => [...prev, order]);
  };

  return (
    <div className="p-6">
      <h1 className="text-xl font-bold mb-4">Управление заказами</h1>

      {/* Форма добавления */}
      <AddOrderForm onOrderCreated={handleOrderCreated} />

      {/* Кнопка открыть таблицу */}
      <div className="mt-4">
        <button
          onClick={() => setShowModal(true)}
          className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
        >
          Показать заказы за сегодня
        </button>
      </div>

      {/* Всплывающее модальное окно */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg w-3/4 max-h-[80vh] overflow-y-auto p-6 relative">
            <h2 className="text-lg font-bold mb-4">Заказы за сегодня</h2>

            {/* Таблица */}
            <OrdersTable orders={orders} />

            {/* Кнопка закрыть */}
            <button
              onClick={() => setShowModal(false)}
              className="absolute top-2 right-2 px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600"
            >
              ✕
            </button>
          </div>
        </div>
      )}

      {/* Просто список всех заказов под кнопкой */}
      <ul className="mt-6 space-y-2">
        {orders.map((order) => (
          <li key={order.id} className="border p-2 rounded">
            <strong>ID:</strong> {order.id} | <strong>Описание:</strong>{" "}
            {order.description} | <strong>Статус:</strong> {order.status}
          </li>
        ))}
      </ul>
    </div>
  );
}
