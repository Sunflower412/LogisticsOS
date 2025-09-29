import { useEffect, useState } from "react";
import OrdersTable from "../components/OrdersTable";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [drivers, setDrivers] = useState([]);

  const load = async () => {
    try {
      const resOrders = await fetch("http://localhost:8080/api/orders");
      const resDrivers = await fetch("http://localhost:8080/api/drivers");
      setOrders(await resOrders.json());
      setDrivers(await resDrivers.json());
    } catch (err) {
      console.error("Ошибка загрузки:", err);
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <div className="p-6">
      <h1 className="text-xl font-bold mb-4">Все заказы</h1>
      <OrdersTable orders={orders} drivers={drivers} reload={load} />
    </div>
  );
}
