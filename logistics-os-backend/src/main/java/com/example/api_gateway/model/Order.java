package com.example.api_gateway.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // Параметры для расчета рейтинга
    private Integer complexity;      // d: 1-простая, 3-средняя, 6-сложная
    private Integer urgency;         // u: 1-не срочная, 3-средняя, 6-срочная
    private Integer routeLength;     // l: 1-короткий, 2-средний, 3-длинный
    private Integer delayPenalty;    // f: 0, 100, 500

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column(name = "to_address", nullable = false)
    private String toAddress;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "volume_m3")
    private Double volumeM3;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"orders", "hibernateLazyInitializer"})
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    @JsonBackReference
    private Driver driver;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "duration_time")
    private Integer durationTime;

    @Column(name = "planned_delivery_time")
    private LocalDateTime plannedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_successfully")
    private Boolean completedSuccessfully;

    // Конструкторы
    public Order() {
    }

    // Удали сложный конструктор или добавь @JsonCreator если нужен

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getComplexity() { return complexity; }
    public void setComplexity(Integer complexity) { this.complexity = complexity; }

    public Integer getUrgency() { return urgency; }
    public void setUrgency(Integer urgency) { this.urgency = urgency; }

    public Integer getRouteLength() { return routeLength; }
    public void setRouteLength(Integer routeLength) { this.routeLength = routeLength; }

    public Integer getDelayPenalty() { return delayPenalty; }
    public void setDelayPenalty(Integer delayPenalty) { this.delayPenalty = delayPenalty; }

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Double getVolumeM3() { return volumeM3; }
    public void setVolumeM3(Double volumeM3) { this.volumeM3 = volumeM3; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getDurationTime() { return durationTime; }
    public void setDurationTime(Integer durationTime) { this.durationTime = durationTime; }

    public LocalDateTime getPlannedDeliveryTime() { return plannedDeliveryTime; }
    public void setPlannedDeliveryTime(LocalDateTime plannedDeliveryTime) { this.plannedDeliveryTime = plannedDeliveryTime; }

    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    // Исправленный геттер для Boolean
    public Boolean getCompletedSuccessfully() {
        return completedSuccessfully;
    }

    // Старый геттер для обратной совместимости (можно удалить если не используется)
    public boolean isCompletedSuccessfully() {
        return Boolean.TRUE.equals(completedSuccessfully);
    }

    public void setCompletedSuccessfully(Boolean completedSuccessfully) {
        this.completedSuccessfully = completedSuccessfully;
    }
}