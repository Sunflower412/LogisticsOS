package com.example.api_gateway.services;

import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;
import com.example.api_gateway.model.OrderStatus;
import com.example.api_gateway.repository.DriverRepository;
import com.example.api_gateway.repository.OrderRepository;
import com.example.api_gateway.services.RoutingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderAssignmentService {
    private static final Logger log = LoggerFactory.getLogger(OrderAssignmentService.class);

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final DriverRatingService driverRatingService; // если нужен
    private final RoutingService routingService; // твой существующий service (используем для оценки маршрута/времени)
    private final RabbitMQService rabbitMQService;

    // минимальный рейтинг для назначения (можно вынести в настройки)
    private final BigDecimal MIN_RATING = new BigDecimal("2.5");

    public OrderAssignmentService(OrderRepository orderRepository,
                                  DriverRepository driverRepository,
                                  DriverRatingService driverRatingService,
                                  RoutingService routingService,
                                  RabbitMQService rabbitMQService) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.driverRatingService = driverRatingService;
        this.routingService = routingService;
        this.rabbitMQService = rabbitMQService;
    }

    /**
     * Асинхронно/периодически назначаем неподназначенные заказы.
     * Можно вызывать вручную или через @Scheduled.
     */

    @Transactional
    public Order assignDriverToOrder(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new RuntimeException("Driver not found"));

        if (!Boolean.TRUE.equals(driver.getActive())) throw new RuntimeException("Driver is not active");
        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);
        order.setUpdatedAt(LocalDateTime.now());
        driver.getOrders().add(order);
        setLastAssignedAt(driver, LocalDateTime.now());

        orderRepository.save(order);
        driverRepository.save(driver);

        rabbitMQService.sendDriverAssignedEvent(orderId, driverId);
        return order;
    }

    private boolean isAvailableNow(Driver d) {
        // если есть поле nextAvailableAt — проверить его, иначе возвращаем true
        try {
            java.lang.reflect.Method m = d.getClass().getMethod("getNextAvailableAt");
            Object val = m.invoke(d);
            if (val instanceof LocalDateTime) {
                LocalDateTime next = (LocalDateTime) val;
                return next.isBefore(LocalDateTime.now()) || next.isEqual(LocalDateTime.now());
            }
        } catch (NoSuchMethodException ignored) {
            // метод отсутствует — считаем доступным
            return true;
        } catch (Exception ex) {
            return true;
        }
        return true;
    }

    private LocalDateTime getLastAssignedAt(Driver d) {
        try {
            java.lang.reflect.Method m = d.getClass().getMethod("getLastAssignedAt");
            Object val = m.invoke(d);
            if (val instanceof LocalDateTime) return (LocalDateTime) val;
        } catch (Exception ignored) {}
        return null;
    }

    private void setLastAssignedAt(Driver d, LocalDateTime when) {
        try {
            java.lang.reflect.Method m = d.getClass().getMethod("setLastAssignedAt", LocalDateTime.class);
            m.invoke(d, when);
        } catch (Exception ignored) {}
    }

    private RoutingInfo tryGetRoutingInfo(Order order) {
        try {
            // Предполагаем что в routingService есть метод getRoutingInfo(from, to)
            // Если нет — реализуй его в RoutingService, возвращающий RoutingInfo.
            return routingService.getRoutingInfo(order.getFromAddress(), order.getToAddress());
        } catch (NoSuchMethodError | NullPointerException ex) {
            log.debug("RoutingService.getRoutingInfo not available: {}", ex.getMessage());
            return null;
        }
    }

    // Можно добавить @Scheduled для периодического вызова
    // @Scheduled(fixedDelayString = "${assignment.poll-ms:30000}")
    public void scheduledAssignPending() {
        assignPendingOrders();
    }
    @Transactional
    public void assignPendingOrders() {
        List<Order> pending = orderRepository.findByStatus(OrderStatus.CREATED);
        if (pending.isEmpty()) return;

        List<Driver> drivers = driverRepository.findByActiveTrueAndRatingMonthlyGreaterThanEqual(MIN_RATING);
        if (drivers.isEmpty()) return;

        Map<Long, Long> assignedCount = new HashMap<>();
        for (Driver d : drivers) {
            long c = orderRepository.countByDriverIdAndStatusIn(d.getId(),
                    Arrays.asList(OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS));
            assignedCount.put(d.getId(), c);
        }

        Comparator<Driver> driverComparator = Comparator
                .comparingLong((Driver d) -> assignedCount.getOrDefault(d.getId(), 0L))
                .thenComparing((Driver d) -> d.getRatingMonthly(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(d -> Optional.ofNullable(getLastAssignedAt(d)).orElse(LocalDateTime.MIN));

        List<Driver> sortedDrivers = new ArrayList<>(drivers);
        sortedDrivers.sort(driverComparator);

        for (Order order : pending) {
            sortedDrivers.sort(driverComparator);

            Optional<Driver> maybeDriver = sortedDrivers.stream()
                    .filter(d -> isAvailableNow(d))
                    .filter(d -> canHandleVehicle(d, order)) // проверка габаритов
                    .findFirst();

            if (maybeDriver.isEmpty()) continue;

            Driver chosen = maybeDriver.get();

            // === Планируемое время ===
            RoutingInfo route = tryGetRoutingInfo(order);
            long loadingTimeMin = 60;
            long complexityFactor = Optional.ofNullable(order.getComplexity()).orElse(1) * 5L;
            long urgencyFactor = Optional.ofNullable(order.getUrgency()).orElse(1) * 3L;
            double driverCoef = Math.max(1.0, 5.0 - chosen.getRatingMonthly().doubleValue()); // хуже рейтинг — медленнее

            long plannedMinutes = (long) Math.ceil(route.getTimeMin() * driverCoef)
                    + loadingTimeMin + complexityFactor + urgencyFactor;

            order.setDriver(chosen);
            order.setStatus(OrderStatus.ASSIGNED);
            order.setUpdatedAt(LocalDateTime.now());
            order.setPlannedDeliveryTime(LocalDateTime.now().plusMinutes(plannedMinutes));

            chosen.getOrders().add(order);
            setLastAssignedAt(chosen, LocalDateTime.now());
            assignedCount.put(chosen.getId(), assignedCount.getOrDefault(chosen.getId(), 0L) + 1);

            orderRepository.save(order);
            driverRepository.save(chosen);

            rabbitMQService.sendDriverAssignedEvent(order.getId(), chosen.getId());
            rabbitMQService.sendNotification(
                    String.format("🚛 Водитель %s %s назначен на заказ #%d. Плановое время: %s",
                            chosen.getFirstname(), chosen.getLastName(),
                            order.getId(), order.getPlannedDeliveryTime())
            );
        }
    }

    private boolean canHandleVehicle(Driver d, Order order) {
        // пока простая проверка по весу/объему
        if (order.getWeightKg() != null && order.getWeightKg() > 10000) return false; // >10т — нет
        if (order.getVolumeM3() != null && order.getVolumeM3() > 51) return false;
        return true;
    }

}
