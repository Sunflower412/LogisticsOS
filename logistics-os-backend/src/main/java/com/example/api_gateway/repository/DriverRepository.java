package com.example.api_gateway.repository;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByActivityTrue();

    List<Driver> findByRatingAllTimeGreaterThanEqual(BigDecimal minRating);

    List<Driver> findByRatingMonthlyGreaterThanEqual(BigDecimal minRating);

    @Query("SELECT d FROM Driver d WHERE d.ratingAllTime < :minRating OR d.ratingMonthly < :minRating")
    List<Driver> findLowRatedDrivers(@Param("minRating") BigDecimal minRating);

    @Query("SELECT d FROM Driver d ORDER BY d.completedOrdersAllTime DESC")
    List<Driver> findTopDrivers();
}