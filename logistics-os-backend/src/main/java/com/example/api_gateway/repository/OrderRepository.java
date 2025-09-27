package com.example.api_gateway.repository;

import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Находим заказы по статусу
    List<Order> findByStatus(OrderStatus status);

    // Находим заказы по списку статусов
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    // Находим заказы по ID водителя
    List<Order> findByDriverId(Long driverId);

    // УДАЛИТЬ или ЗАКОММЕНТИРОВАТЬ этот метод, т.к. поля customerPhone нет в Order
    // List<Order> findByCustomerPhone(String customerPhone);

    // Находим заказы за период
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Находим активные заказы для водителя
    @Query("SELECT o FROM Order o WHERE o.driver.id = :driverId AND o.status IN :statuses")
    List<Order> findActiveOrdersByDriver(@Param("driverId") Long driverId,
                                         @Param("statuses") List<OrderStatus> statuses);

    // Находим последние заказы
    List<Order> findTop10ByOrderByCreatedAtDesc();

    // Добавим метод для поиска по клиенту (если нужно)
    @Query("SELECT o FROM Order o WHERE o.client.phone = :phone")
    List<Order> findByClientPhone(@Param("phone") String phone);
}