package com.example.api_gateway.repository;

import com.example.api_gateway.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByPriority(int priority);
    List<Client> findByCompanyName(String companyName);
}
