from fastapi import FastAPI, HTTPException
import httpx
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI()

# Модель для запроса маршрута
class RouteRequest(BaseModel):
    start_lat: float
    start_lon: float
    end_lat: float
    end_lon: float
    transport_type: str = "car"  # car, foot, bike
    waypoints: Optional[List[List[float]]] = []  # [[lat, lon], ...]

# URL GraphHopper API (локальный или внешний)
GRAPHHOPPER_URL = "http://localhost:8989/route"

@app.post("/route")
async def calculate_route(request: RouteRequest):
    # Формируем запрос к GraphHopper
    points = [[request.start_lon, request.start_lat]] + request.waypoints + [[request.end_lon, request.end_lat]]
    params = {
        "profile": request.transport_type,
        "points": points,
        "points_encoded": False,  # Raw координаты вместо encoded polyline
        "instructions": True,     # Пошаговые инструкции
        "locale": "ru"            # Русский для инструкций
    }

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(GRAPHHOPPER_URL, params={"point": [f"{p[1]},{p[0]}" for p in points], **params})
            response.raise_for_status()
            data = response.json()

            # Извлекаем ключевые данные
            route = data["paths"][0]
            return {
                "status": "success",
                "distance": route["distance"],  # в метрах
                "time": route["time"] / 1000,   # в секундах
                "polyline": route["points"]["coordinates"],  # [[lon, lat], ...]
                "instructions": [
                    {
                        "text": instr["text"],
                        "distance": instr["distance"],
                        "time": instr["time"] / 1000,
                        "type": instr["sign"]  # Код поворота (0=straight, 2=left, ...)
                    } for instr in route["instructions"]
                ]
            }
        except httpx.HTTPError as e:
            raise HTTPException(status_code=500, detail=f"GraphHopper error: {str(e)}")

@app.post("/reroute")
async def reroute(request: RouteRequest):
    # То же, что /route, но для пересчёта при отклонении
    return await calculate_route(request)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)