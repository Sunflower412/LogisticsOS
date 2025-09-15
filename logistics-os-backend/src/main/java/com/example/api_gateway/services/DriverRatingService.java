//package com.example.api_gateway.services;
//
//import com.example.api_gateway.model.Driver;
//import com.example.api_gateway.model.Order;
//import com.example.api_gateway.repository.DriverRepository;
//import com.example.api_gateway.repository.OrderRepository;
//import jakarta.transaction.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//public class DriverRatingService {
//    private final DriverRepository driverRepository;
//
//    public DriverRatingService(DriverRepository driverRepository) {
//        this.driverRepository = driverRepository;
//    }
//    @Transactional
//    public void updateDriverRating(Long orderId, Long driverId, boolean completedSuccessfully,
//                                   LocalDateTime plannedDeliveryTime, LocalDateTime actualDeliveryTime) {
//
//        Driver driver = driverRepository.findById(driverId)
//                .orElseThrow(() -> new IllegalArgumentException("Водителя с таким Id не существует.");
//
//        BigDecimal ratingChange = calculateRatingChange(completedSuccessfully, plannedDeliveryTime, actualDeliveryTime);
//
//        // Обновляем коэффициенты
//        driver.setExperienceCoefficientPerMonth(driver.getExperienceCoefficientPerMonth().add(ratingChange));
//        driver.setExperienceCoefficientAllTime(
//                driver.getExperienceCoefficientAllTime().add(ratingChange)
//        );
//
//        // Ограничиваем значения (например, от 0.5 до 2.0)
//        applyRatingLimits(driver);
//
//        driverRepository.save(driver);
//    }
//
//    private BigDecimal calculateRatingChange(boolean completedSuccessfully,
//                                             LocalDateTime plannedTime,
//                                             LocalDateTime actualTime) {
//
//        if (!completedSuccessfully) {
//            return new BigDecimal("-0.1"); // Штраф за невыполнение
//        }
//
//        // Расчет опоздания/преждевременности
//        Duration timeDifference = Duration.between(plannedTime, actualTime);
//        long minutesDifference = timeDifference.toMinutes();
//
//        if (minutesDifference <= 0) {
//            return new BigDecimal("0.05"); // Бонус за выполнение вовремя или раньше
//        } else if (minutesDifference <= 15) {
//            return new BigDecimal("0.02"); // Небольшой бонус за небольшое опоздание
//        } else if (minutesDifference <= 30) {
//            return BigDecimal.ZERO; // Ничего не меняем
//        } else {
//            return new BigDecimal("-0.05"); // Штраф за сильное опоздание
//        }
//    }
//
//    private void applyRatingLimits(Driver driver) {
//        // Ограничения для monthly коэффициента
//        if (driver.getExperienceCoefficientPerMonth().compareTo(new BigDecimal("0.5")) < 0) {
//            driver.setExperienceCoefficientPerMonth(new BigDecimal("0.5"));
//        }
//        if (driver.getExperienceCoefficientPerMonth().compareTo(new BigDecimal("2.0")) > 0) {
//            driver.setExperienceCoefficientPerMonth(new BigDecimal("2.0"));
//        }
//
//        // Ограничения для alltime коэффициента
//        if (driver.getExperienceCoefficientAllTime().compareTo(new BigDecimal("0.5")) < 0) {
//            driver.setExperienceCoefficientAllTime(new BigDecimal("0.5"));
//        }
//        if (driver.getExperienceCoefficientAllTime().compareTo(new BigDecimal("2.0")) > 0) {
//            driver.setExperienceCoefficientAllTime(new BigDecimal("2.0"));
//        }
//    }
//
//    // Метод для ежемесячного сброса monthly коэффициента
//    @Transactional
//    public void resetMonthlyRatings() {
//        List<Driver> allDrivers = driverRepository.findAll();
//        for (Driver driver : allDrivers) {
//            driver.setExperienceCoefficientPerMonth(new BigDecimal("1.0"));
//        }
//        driverRepository.saveAll(allDrivers);
//    }
//}
//    }
//}
