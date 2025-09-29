import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { useState, useEffect } from "react";
import Dashboard from "./pages/Dashboard";
import OrdersPage from "./pages/OrdersPage";
import AddOrderPage from "./pages/AddOrderPage";
import OrderDetails from "./pages/OrderDetails";
import { fetchOrders } from "./api/orders";
import type { Order } from "./api/types";

function App() {
  const [orders, setOrders] = useState<Order[]>([]);

  // Загружаем заказы один раз при старте
  useEffect(() => {
    fetchOrders().then(setOrders).catch((err) => {
      console.error("Ошибка загрузки заказов:", err);
    });
  }, []);

  return (
    <Router>
      <Routes>
        <Route
          path="/"
          element={<Dashboard orders={orders} setOrders={setOrders} />}
        />
        <Route
          path="/orders"
          element={<OrdersPage orders={orders} setOrders={setOrders} />}
        />
        <Route
          path="/add-order"
          element={<AddOrderPage setOrders={setOrders} />}
        />
        <Route path="/orders/:id" element={<OrderDetails />} />
      </Routes>
    </Router>
  );
}

export default App;
