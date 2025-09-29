package com.example.api_gateway.repository;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {



    List<Driver> findByRatingAllTimeGreaterThanEqual(BigDecimal minRating);

    List<Driver> findByRatingMonthlyGreaterThanEqual(BigDecimal minRating);

    @Query("SELECT d FROM Driver d WHERE d.ratingAllTime < :minRating OR d.ratingMonthly < :minRating")
    List<Driver> findLowRatedDrivers(@Param("minRating") BigDecimal minRating);

    @Query("SELECT d FROM Driver d ORDER BY d.completedOrdersAllTime DESC")
    List<Driver> findTopDrivers();

    List<Driver> findByActiveTrue();

    // Находим активных водителей с рейтингом выше указанного
    List<Driver> findByActiveTrueAndRatingMonthlyGreaterThanEqual(BigDecimal minRating);

    List<Driver> findByActiveTrueOrderByRatingMonthlyDesc();
    // Находим активных водителей с рейтингом за все время выше указанного
    List<Driver> findByActiveTrueAndRatingAllTimeGreaterThanEqual(BigDecimal minRating);

    // Подсчет активных водителей с хорошим рейтингом
    @Query("SELECT COUNT(d) FROM Driver d WHERE d.active = true AND d.ratingMonthly >= :minRating")
    long countByActiveTrueAndRatingMonthlyGreaterThanEqual(@Param("minRating") BigDecimal minRating);

    // Находим водителей ближайших к локации (заглушка для будущей реализации)
    @Query("SELECT d FROM Driver d WHERE d.active = true ORDER BY d.ratingMonthly DESC")
    List<Driver> findTopDriversByRating();
}
