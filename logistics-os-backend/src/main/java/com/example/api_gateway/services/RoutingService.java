package com.example.api_gateway.services;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;
import java.util.Optional;

@Service
public class RoutingService {

    private final DriverRepository driverRepository;

    public RoutingService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver findBestDriverForOrder(Order order) {
        List<Driver> availableDrivers = driverRepository.findAll();

        // Используем Optional для безопасного получения результата
        Optional<Driver> bestDriver = availableDrivers.stream()
                .filter(Driver::getActive) // Фильтруем только активных водителей
                .filter(driver -> driver.getRatingMonthly().compareTo(java.math.BigDecimal.valueOf(2.5)) >= 0)
                .max(Comparator.comparing(Driver::getRatingMonthly)); // Сортируем по рейтингу

        return bestDriver.orElse(null); // Возвращаем null если водитель не найден
    }

    // Альтернативный метод с более сложной логикой выбора
    public Driver findOptimalDriverForOrder(Order order) {
        List<Driver> availableDrivers = driverRepository.findByActiveTrue();

        return availableDrivers.stream()
                .filter(driver -> driver.getRatingMonthly().compareTo(java.math.BigDecimal.valueOf(2.5)) >= 0)
                .min(Comparator.comparing(driver -> calculateDistance(driver, order))) // Выбираем ближайшего
                .orElse(null);
    }

    // Заглушка для расчета расстояния (будет интегрировано с GIS сервисом)
    private double calculateDistance(Driver driver, Order order) {
        // TODO: Интеграция с картографическим сервисом
        return Math.random() * 10; // Временная заглушка
    }

    // Метод для предложения водителя (используется в контроллере)
    public Long suggestDriverForOrder(Long orderId) {
        // TODO: Реализовать логику подбора водителя на основе заказа
        // Пока возвращаем первого активного водителя с хорошим рейтингом
        List<Driver> suitableDrivers = driverRepository.findByActiveTrueAndRatingMonthlyGreaterThanEqual(
                java.math.BigDecimal.valueOf(2.5));

        if (!suitableDrivers.isEmpty()) {
            return suitableDrivers.get(0).getId();
        }
        return null;
    }

    // Метод для проверки доступности водителей
    public boolean hasAvailableDrivers() {
        return driverRepository.countByActiveTrueAndRatingMonthlyGreaterThanEqual(
                java.math.BigDecimal.valueOf(2.5)) > 0;
    }
}