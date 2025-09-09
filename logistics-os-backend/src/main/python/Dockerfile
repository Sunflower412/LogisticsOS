# Используем официальный образ Python в качестве базового
FROM python:3.11-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы зависимостей
COPY python/requirements.txt ./

# Устанавливаем зависимости из requirements.txt
RUN pip install --no-cache-dir -r requirements.txt \
    && rm -rf /root/.cache/pip

# Копируем весь проект в контейнер
COPY python/ .

# Указываем переменные окружения (опционально, можно настроить через .env)
ENV PYTHONUNBUFFERED=1

# Команда для запуска приложения
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080", "--reload"]

# Экспозиция порта
EXPOSE 8080