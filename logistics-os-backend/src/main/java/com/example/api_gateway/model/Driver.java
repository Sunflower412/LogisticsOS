package com.example.api_gateway.model;

import jakarta.persistence.*;
import org.aspectj.weaver.ast.Or;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    String firstname;

    @Column(name = "last_name", nullable = false)
    String lastName;

    @Column(name = "experience_level")
    int experienceLevel;

    @Column(name = "activity")
    private Boolean active = true;

    @Column(name = "rating_all_time", precision = 3, scale = 2)
    private BigDecimal ratingAllTime = new BigDecimal("4.00");

    @Column(name = "rating_monthly", precision = 3, scale = 2)
    private BigDecimal ratingMonthly = new BigDecimal("4.00");

    private Integer completedOrdersAllTime = 0;
    private Integer completedOrdersMonthly = 0;
    private Integer failedOrdersAllTime = 0;
    private Integer failedOrdersMonthly = 0;

    @OneToMany
    @Column(name = "orders")
    private List<Order> orders;

    public Order findOrderById(int id){
        for (Order order : getOrders()){
            if (order.getId() == id){
                return order;
            }
        }
        throw new IllegalArgumentException("Заказа с указанным Id не существует.");
    }


    public Driver() {}

    public Driver(Long id, String firstname, String lastName, int experienceLevel) {
        this.id = id;
        this.firstname = firstname;
        this.lastName = lastName;
        this.experienceLevel = experienceLevel;
        this.ratingMonthly = ratingMonthly;
        this.ratingAllTime = ratingAllTime;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(int experienceLevel) {
        this.experienceLevel = experienceLevel;
    }



    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }


//    public BigDecimal getExperience_coefficient_perMonth() {
//        return experienceCoefficientPerMonth;
//    }
//
//    public void setExperience_coefficient_perMonth(BigDecimal experience_coefficient_perMonth) {
//        this.experienceCoefficientPerMonth = experience_coefficient_perMonth;
//    }
//
//    public BigDecimal getExperience_coefficient_allTime() {
//        return experienceCoefficientAllTime;
//    }
//
//    public void setExperience_coefficient_allTime(BigDecimal experience_coefficient_allTime) {
//        this.experienceCoefficientAllTime = experience_coefficient_allTime;
//    }
//    public void applyRatingChange(BigDecimal change) {
//        this.experienceCoefficientPerMonth = this.experienceCoefficientPerMonth.add(change);
//        this.experienceCoefficientAllTime = this.experienceCoefficientAllTime.add(change);
//        applyRatingLimits();
//    }
//
//    private void applyRatingLimits() {
//        // Ограничения снизу
//        if (experienceCoefficientPerMonth.compareTo(new BigDecimal("2.5")) < 0) {
//            experienceCoefficientPerMonth = new BigDecimal("2.5");
//        }
//        if (experienceCoefficientAllTime.compareTo(new BigDecimal("2.5")) < 0) {
//            experienceCoefficientAllTime = new BigDecimal("2.5");
//        }
//
//        // Ограничения сверху
//        if (experienceCoefficientPerMonth.compareTo(new BigDecimal("5.0")) > 0) {
//            experienceCoefficientPerMonth = new BigDecimal("5.0");
//        }
//        if (experienceCoefficientAllTime.compareTo(new BigDecimal("5.0")) > 0) {
//            experienceCoefficientAllTime = new BigDecimal("5.0");
//        }
//    }
//
//
//    @Override
//    public String toString() {
//        return "Driver{" +
//                "id=" + id +
//                ", firstName='" + firstname + '\'' +
//                ", lastName='" + lastName + '\'' +
//                ", experienceLevel=" + experienceLevel +
//                ", experienceCoefficientPerMonth=" + experienceCoefficientPerMonth +
//                ", experienceCoefficientAllTime=" + experienceCoefficientAllTime +
//                '}';
//    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigDecimal getRatingAllTime() {
        return ratingAllTime;
    }

    public void setRatingAllTime(BigDecimal ratingAllTime) {
        this.ratingAllTime = ratingAllTime;
    }

    public BigDecimal getRatingMonthly() {
        return ratingMonthly;
    }

    public void setRatingMonthly(BigDecimal ratingMonthly) {
        this.ratingMonthly = ratingMonthly;
    }

    public Integer getCompletedOrdersAllTime() {
        return completedOrdersAllTime;
    }

    public void setCompletedOrdersAllTime(Integer completedOrdersAllTime) {
        this.completedOrdersAllTime = completedOrdersAllTime;
    }

    public Integer getCompletedOrdersMonthly() {
        return completedOrdersMonthly;
    }

    public void setCompletedOrdersMonthly(Integer completedOrdersMonthly) {
        this.completedOrdersMonthly = completedOrdersMonthly;
    }

    public Integer getFailedOrdersAllTime() {
        return failedOrdersAllTime;
    }

    public void setFailedOrdersAllTime(Integer failedOrdersAllTime) {
        this.failedOrdersAllTime = failedOrdersAllTime;
    }

    public Integer getFailedOrdersMonthly() {
        return failedOrdersMonthly;
    }

    public void setFailedOrdersMonthly(Integer failedOrdersMonthly) {
        this.failedOrdersMonthly = failedOrdersMonthly;
    }
}