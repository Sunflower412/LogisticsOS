import uvicorn
from fastapi import FastAPI

app = FastAPI(
    title="Logistics OS",
)

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8080)