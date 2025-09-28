package com.example.api_gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drivers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstname;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "experience_level")
    private int experienceLevel;

    @Column(name = "activity")
    private Boolean active = true;

    @Column(name = "rating_all_time", precision = 3, scale = 2)
    private BigDecimal ratingAllTime = BigDecimal.valueOf(4.00);

    @Column(name = "rating_monthly", precision = 3, scale = 2)
    private BigDecimal ratingMonthly = BigDecimal.valueOf(4.00);

    private Integer completedOrdersAllTime = 0;
    private Integer completedOrdersMonthly = 0;
    private Integer failedOrdersAllTime = 0;
    private Integer failedOrdersMonthly = 0;


    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Order> orders = new ArrayList<>();

    public Driver() {}

    public Driver(Long id, String firstname, String lastName, int experienceLevel) {
        this.id = id;
        this.firstname = firstname;
        this.lastName = lastName;
        this.experienceLevel = experienceLevel;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public int getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(int experienceLevel) { this.experienceLevel = experienceLevel; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public BigDecimal getRatingAllTime() { return ratingAllTime; }
    public void setRatingAllTime(BigDecimal ratingAllTime) { this.ratingAllTime = ratingAllTime; }

    public BigDecimal getRatingMonthly() { return ratingMonthly; }
    public void setRatingMonthly(BigDecimal ratingMonthly) { this.ratingMonthly = ratingMonthly; }

    public Integer getCompletedOrdersAllTime() { return completedOrdersAllTime; }
    public void setCompletedOrdersAllTime(Integer completedOrdersAllTime) { this.completedOrdersAllTime = completedOrdersAllTime; }

    public Integer getCompletedOrdersMonthly() { return completedOrdersMonthly; }
    public void setCompletedOrdersMonthly(Integer completedOrdersMonthly) { this.completedOrdersMonthly = completedOrdersMonthly; }

    public Integer getFailedOrdersAllTime() { return failedOrdersAllTime; }
    public void setFailedOrdersAllTime(Integer failedOrdersAllTime) { this.failedOrdersAllTime = failedOrdersAllTime; }

    public Integer getFailedOrdersMonthly() { return failedOrdersMonthly; }
    public void setFailedOrdersMonthly(Integer failedOrdersMonthly) { this.failedOrdersMonthly = failedOrdersMonthly; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }

    public void addOrder(Order order) {
        orders.add(order);
        order.setDriver(this);
    }
}
