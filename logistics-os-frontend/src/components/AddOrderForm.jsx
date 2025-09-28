import { useState } from "react";

export default function AddOrderForm({ onOrderCreated, onClose }) {
  const [form, setForm] = useState({
    fromAddress: "",
    toAddress: "",
    weightKg: "",
    volumeM3: ""
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await fetch("http://localhost:8080/api/orders", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(form)
    });
    const created = await res.json();
    onOrderCreated(created);
    if (onClose) onClose(); // закрыть модалку, если передана
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <h2 className="text-xl font-bold">Добавить заказ</h2>

      <input
        name="fromAddress"
        value={form.fromAddress}
        onChange={(e) => setForm({ ...form, fromAddress: e.target.value })}
        placeholder="Откуда"
        className="border p-2 rounded"
        required
      />
      <input
        name="toAddress"
        value={form.toAddress}
        onChange={(e) => setForm({ ...form, toAddress: e.target.value })}
        placeholder="Куда"
        className="border p-2 rounded"
        required
      />
      <input
        name="weightKg"
        type="number"
        value={form.weightKg}
        onChange={(e) => setForm({ ...form, weightKg: e.target.value })}
        placeholder="Вес (кг)"
        className="border p-2 rounded"
      />
      <input
        name="volumeM3"
        type="number"
        value={form.volumeM3}
        onChange={(e) => setForm({ ...form, volumeM3: e.target.value })}
        placeholder="Объем (м³)"
        className="border p-2 rounded"
      />

      <div className="flex gap-2 justify-end">
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 bg-gray-400 text-white rounded"
          >
            Отмена
          </button>
        )}
        <button
          type="submit"
          className="px-4 py-2 bg-green-600 text-white rounded"
        >
          Сохранить
        </button>
      </div>
    </form>
  );
}
