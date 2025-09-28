import axios from "./client";

export async function assignDriver(orderId: number, driverId: number) {
  const response = await axios.post(`/api/orders/${orderId}/assign/${driverId}`);
  return response.data;
}
