// OrderDTO.java
package com.example.api_gateway.dto;

import java.time.LocalDateTime;

public class OrderDTO {
    private Long id;
    private String description;
    private String fromAddress;
    private String toAddress;
    private String status;

    // минимальная инфа о водителе
    private Long driverId;
    private String driverFirstname;
    private String driverLastname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderDTO(Long id, String description, String fromAddress, String toAddress, String status,
                    Long driverId, String driverFirstname, String driverLastname,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.description = description;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.status = status;
        this.driverId = driverId;
        this.driverFirstname = driverFirstname;
        this.driverLastname = driverLastname;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public String getFromAddress() { return fromAddress; }
    public String getToAddress() { return toAddress; }
    public String getStatus() { return status; }
    public Long getDriverId() { return driverId; }
    public String getDriverFirstname() { return driverFirstname; }
    public String getDriverLastname() { return driverLastname; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
