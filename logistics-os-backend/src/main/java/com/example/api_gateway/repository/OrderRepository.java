package com.example.api_gateway.repository;


import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByDriverId(Long driverId);

    List<Order> findByClientId(Long clientId);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    List<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.driver.id = :driverId AND o.status = 'DELIVERED'")
    List<Order> findCompletedOrdersByDriver(@Param("driverId") Long driverId);

    // Новый метод для поиска заказов без водителя
    @Query("SELECT o FROM Order o WHERE o.driver IS NULL AND o.status = 'CREATED'")
    List<Order> findUnassignedOrders();
}