package com.example.api_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test") // Говорит, что метод обрабатывает GET-запросы на адрес /test
    public String testEndpoint() {
        return "Hello from LogisticsOS API! Server is working!";
    }
}
