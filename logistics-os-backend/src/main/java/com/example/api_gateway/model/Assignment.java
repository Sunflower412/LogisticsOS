package com.example.api_gateway.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "estimated_finish_at")
    private LocalDateTime estimatedFinishAt;

    @Column(name = "actual_finish_at")
    private LocalDateTime actualFinishAt;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status = AssignmentStatus.OFFERED;

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEstimatedFinishAt() { return estimatedFinishAt; }
    public void setEstimatedFinishAt(LocalDateTime estimatedFinishAt) { this.estimatedFinishAt = estimatedFinishAt; }

    public LocalDateTime getActualFinishAt() { return actualFinishAt; }
    public void setActualFinishAt(LocalDateTime actualFinishAt) { this.actualFinishAt = actualFinishAt; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
}
