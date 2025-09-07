package com.example.api_gateway.repository;

import com.example.api_gateway.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface DriverRepository extends JpaRepository<Driver, Long> {}