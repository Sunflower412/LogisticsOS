# 1. Билдим React (Vite)
FROM node:20 AS build
WORKDIR /app

# Копируем только манифесты
COPY package*.json ./

# Устанавливаем зависимости
RUN npm install

# Копируем всё остальное (включая index.html, src/, vite.config.*)
COPY . .

# Собираем проект
RUN npm run build

# 2. Отдаём собранный фронт через Nginx
FROM nginx:1.25-alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
