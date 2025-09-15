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

    @Column(name = "experience_coefficient_perMonth")
    private float experience_coefficient_perMonth = 1;

    @Column(name = "experience_coefficient_allTime")
    private float experience_coefficient_allTime = 1;

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
    }

    public void updateCoefficientByCompletedOrder(int id){
        Order completedOrder = findOrderById(id);
        if ((completedOrder.getCompletedAt().getMinute() - completedOrder.getCreatedAt().getMinute()) >= completedOrder.getDurationTime()){
            experience_coefficient_perMonth = experience_coefficient_perMonth + 0.01f;
        }
        else {
            experience_coefficient_perMonth = experience_coefficient_perMonth - 0.05f;
        }
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

    public float getExperience_coefficient_perMonth() {
        return experience_coefficient_perMonth;
    }

    public void setExperience_coefficient_perMonth(float experience_coefficient_perMonth) {
        this.experience_coefficient_perMonth = experience_coefficient_perMonth;
    }

    public float getExperience_coefficient_allTime() {
        return experience_coefficient_allTime;
    }

    public void setExperience_coefficient_allTime(float experience_coefficient_allTime) {
        this.experience_coefficient_allTime = experience_coefficient_allTime;
    }
}