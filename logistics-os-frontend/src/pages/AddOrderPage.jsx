import { useNavigate } from "react-router-dom";
import AddOrderForm from "../components/AddOrderForm";

export default function AddOrderPage({ setOrders }) {
  const navigate = useNavigate();

  const handleOrderCreated = (newOrder) => {
    setOrders((prev) => [...prev, newOrder]); // 👉 сразу добавляем в Dashboard
    navigate("/"); // возвращаемся на главную
  };

  return (
    <div className="p-6">
      <h1 className="text-xl font-bold mb-4">Добавить заказ</h1>
      <AddOrderForm onOrderCreated={handleOrderCreated} />
    </div>
  );
}
