// DriverDTO.java
package com.example.api_gateway.dto;

import java.util.List;

public class DriverDTO {
    private Long id;
    private String firstname;
    private String lastName;
    private Boolean active;
    private List<Long> orderIds; // ✅ только id заказов

    public DriverDTO(Long id, String firstname, String lastName, Boolean active, List<Long> orderIds) {
        this.id = id;
        this.firstname = firstname;
        this.lastName = lastName;
        this.active = active;
        this.orderIds = orderIds;
    }

    public Long getId() { return id; }
    public String getFirstname() { return firstname; }
    public String getLastName() { return lastName; }
    public Boolean getActive() { return active; }
    public List<Long> getOrderIds() { return orderIds; }
}
