package com.example.api_gateway.services;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.repository.DriverRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DriverRatingService {

    private final DriverRepository driverRepository;

    public DriverRatingService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public void updateDriverRating(Order order) {
        if (order.getDriver() == null || order.getPlannedDeliveryTime() == null) {
            return;
        }

        Driver driver = order.getDriver();
        LocalDateTime plannedTime = order.getPlannedDeliveryTime();
        LocalDateTime actualTime = order.getActualDeliveryTime();

        // Проверяем опоздание более чем на 2 часа
        if (actualTime != null) {
            Duration delay = Duration.between(plannedTime, actualTime);
            long delayMinutes = delay.toMinutes();

            if (delayMinutes > 120) { // Более 2 часов опоздания
                handleFailedOrder(driver);
                return;
            }
        }

        // Расчет рейтинга по формуле
        BigDecimal ratingPoints = calculateRatingPoints(order);
        updateDriverStats(driver, ratingPoints, true);

        driverRepository.save(driver);
    }

    private BigDecimal calculateRatingPoints(Order order) {
        int d = order.getComplexity(); // Сложность (1, 3, 6)
        int u = order.getUrgency();    // Срочность (1, 3, 6)
        int l = order.getRouteLength(); // Длина маршрута (1, 2, 3)
        int f = calculateDelayPenalty(order); // Штраф за опоздание

        // Формула: ((d² + u²) * l) / (f + 1000)
        double dSquared = Math.pow(d, 2);
        double uSquared = Math.pow(u, 2);
        double numerator = (dSquared + uSquared) * l;
        double denominator = f + 1000;
        double result = numerator / denominator;

        // Преобразуем в 5-балльную систему (масштабируем)
        double scaledResult = result * 0.5; // Эмпирический коэффициент для 5-балльной системы

        return BigDecimal.valueOf(scaledResult)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private int calculateDelayPenalty(Order order) {
        if (order.getActualDeliveryTime() == null) {
            return 1000; // Максимальный штраф если время не указано
        }

        Duration delay = Duration.between(
                order.getPlannedDeliveryTime(),
                order.getActualDeliveryTime()
        );
        long delayMinutes = delay.toMinutes();

        if (delayMinutes <= 30) return 0;
        if (delayMinutes <= 60) return 100;
        if (delayMinutes <= 120) return 500;
        return 1000; // На случай если больше 2 часов (должно обрабатываться выше)
    }

    private void handleFailedOrder(Driver driver) {
        // Понижаем рейтинг на 0.1 за проваленный заказ
        BigDecimal newRatingAllTime = driver.getRatingAllTime()
                .subtract(new BigDecimal("0.10"))
                .max(new BigDecimal("0.00"));

        BigDecimal newRatingMonthly = driver.getRatingMonthly()
                .subtract(new BigDecimal("0.10"))
                .max(new BigDecimal("0.00"));

        driver.setRatingAllTime(newRatingAllTime);
        driver.setRatingMonthly(newRatingMonthly);
        driver.setFailedOrdersAllTime(driver.getFailedOrdersAllTime() + 1);
        driver.setFailedOrdersMonthly(driver.getFailedOrdersMonthly() + 1);

        // Проверяем на увольнение (рейтинг ниже 2.5)
        if (driver.getRatingAllTime().compareTo(new BigDecimal("2.50")) < 0 ||
                driver.getRatingMonthly().compareTo(new BigDecimal("2.50")) < 0) {
            driver.setActive(false);
        }
    }

    private void updateDriverStats(Driver driver, BigDecimal ratingPoints, boolean success) {
        if (success) {
            // Обновляем рейтинг как среднее арифметическое
            int totalOrdersAllTime = driver.getCompletedOrdersAllTime() + driver.getFailedOrdersAllTime();
            int totalOrdersMonthly = driver.getCompletedOrdersMonthly() + driver.getFailedOrdersMonthly();

            BigDecimal currentTotalAllTime = driver.getRatingAllTime()
                    .multiply(BigDecimal.valueOf(totalOrdersAllTime));
            BigDecimal currentTotalMonthly = driver.getRatingMonthly()
                    .multiply(BigDecimal.valueOf(totalOrdersMonthly));

            BigDecimal newTotalAllTime = currentTotalAllTime.add(ratingPoints);
            BigDecimal newTotalMonthly = currentTotalMonthly.add(ratingPoints);

            driver.setRatingAllTime(newTotalAllTime
                    .divide(BigDecimal.valueOf(totalOrdersAllTime + 1), 2, RoundingMode.HALF_UP));
            driver.setRatingMonthly(newTotalMonthly
                    .divide(BigDecimal.valueOf(totalOrdersMonthly + 1), 2, RoundingMode.HALF_UP));

            driver.setCompletedOrdersAllTime(driver.getCompletedOrdersAllTime() + 1);
            driver.setCompletedOrdersMonthly(driver.getCompletedOrdersMonthly() + 1);
        }
    }

    // Ежемесячный сброс monthly статистики
    @Scheduled(cron = "0 0 0 1 * ?") // Первое число каждого месяца в 00:00
    @Transactional
    public void resetMonthlyStats() {
        List<Driver> drivers = driverRepository.findAll();
        for (Driver driver : drivers) {
            driver.setRatingMonthly(new BigDecimal("5.00"));
            driver.setCompletedOrdersMonthly(0);
            driver.setFailedOrdersMonthly(0);

            // Реактивируем водителей если их общий рейтинг выше 2.5
            if (driver.getRatingAllTime().compareTo(new BigDecimal("2.50")) >= 0) {
                driver.setActive(true);
            }
        }
        driverRepository.saveAll(drivers);
    }

    // Метод для получения подходящего водителя для заказа
    public Driver findSuitableDriver(Order order) {
        List<Driver> activeDrivers = driverRepository.findByActiveTrue();

        return activeDrivers.stream()
                .max((d1, d2) -> {
                    // Приоритет для водителей с хорошим рейтингом, но не перегруженных
                    BigDecimal score1 = calculateDriverScore(d1, order);
                    BigDecimal score2 = calculateDriverScore(d2, order);
                    return score1.compareTo(score2);
                })
                .orElse(null);
    }

    private BigDecimal calculateDriverScore(Driver driver, Order order) {
        // Базовый рейтинг
        BigDecimal score = driver.getRatingAllTime();

        // Бонус новичкам (меньше 10 выполненных заказов)
        if (driver.getCompletedOrdersAllTime() < 10) {
            score = score.add(new BigDecimal("0.5"));
        }

        // Штраф за перегруженность (много текущих заказов)
        // Здесь можно добавить логику проверки текущей загрузки

        return score;
    }
}