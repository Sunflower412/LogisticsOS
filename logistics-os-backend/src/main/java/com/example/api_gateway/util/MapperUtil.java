// MapperUtil.java
package com.example.api_gateway.util;

import com.example.api_gateway.dto.DriverDTO;
import com.example.api_gateway.dto.OrderDTO;
import com.example.api_gateway.model.Driver;
import com.example.api_gateway.model.Order;

import java.util.List;
import java.util.stream.Collectors;

public class MapperUtil {

    public static DriverDTO toDriverDTO(Driver driver) {
        List<Long> orderIds = driver.getOrders().stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        return new DriverDTO(
                driver.getId(),
                driver.getFirstname(),
                driver.getLastName(),
                driver.getActive(),
                orderIds
        );
    }

    public static OrderDTO toOrderDTO(Order order) {
        return new OrderDTO(
                order.getId(),
                order.getDescription(),
                order.getFromAddress(),
                order.getToAddress(),
                order.getStatus().toString(),
                order.getDriver() != null ? order.getDriver().getId() : null,
                order.getDriver() != null ? order.getDriver().getFirstname() : null,
                order.getDriver() != null ? order.getDriver().getLastName() : null,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
