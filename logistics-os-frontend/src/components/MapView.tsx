import React, { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Polyline, Popup } from "react-leaflet";
import L from "leaflet";
import type { Order, Driver } from "../api/types";
import "leaflet/dist/leaflet.css";

// небольшой helper для иконки (чтобы не терялись дефолтные маркеры)
const defaultIcon = L.icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41]
});

const truckIcon = L.divIcon({
  html: "🚚",
  className: "text-xl"
});

function getLatLngFrom(obj: any): [number, number] | null {
  if (!obj) return null;
  if (Array.isArray(obj.position) && obj.position.length === 2) {
    return [obj.position[0], obj.position[1]];
  }
  if (obj.latitude != null && obj.longitude != null) {
    return [Number(obj.latitude), Number(obj.longitude)];
  }
  // if backend returns geoPoint: {lat, lon}
  if (obj.lat != null && obj.lon != null) return [Number(obj.lat), Number(obj.lon)];
  return null;
}

export default function MapView({ orders, drivers, routes }: {
  orders: Order[];
  drivers: Driver[];
  routes?: Array<[number, number][]>;
}) {
  const center: [number, number] = [55.751244, 37.618423];

  // derive initial viewport from first available point
  const firstPoint =
    getLatLngFrom(orders[0]) ?? getLatLngFrom(drivers[0]) ?? center;

  return (
    <MapContainer center={firstPoint} zoom={10} className="h-full w-full">
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      {/* Routes */}
      {routes?.map((r, idx) => (
        <Polyline key={idx} positions={r.map(p => [p[0], p[1]]) as any} />
      ))}

      {/* Orders */}
      {orders.map((o) => {
        const pos = getLatLngFrom(o);
        if (!pos) return null;
        return (
          <Marker key={o.id} position={pos as any} icon={defaultIcon}>
            <Popup>
              <div>
                <b>Заказ #{o.id}</b>
                <div>{o.description}</div>
                <div>Статус: {o.status}</div>
                <div>{o.fromAddress} → {o.toAddress}</div>
              </div>
            </Popup>
          </Marker>
        );
      })}

      {/* Drivers as trucks */}
      {drivers.map((d) => {
        const pos = getLatLngFrom(d);
        if (!pos) return null;
        return (
          <Marker key={d.id} position={pos as any} icon={truckIcon as any}>
            <Popup>
              <div>
                <b>Водитель #{d.id}</b>
                <div>{d.name ?? "—"}</div>
              </div>
            </Popup>
          </Marker>
        );
      })}
    </MapContainer>
  );
}
