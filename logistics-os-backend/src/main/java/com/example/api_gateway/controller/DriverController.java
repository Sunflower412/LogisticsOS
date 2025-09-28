package com.example.api_gateway.controller;

import com.example.api_gateway.dto.DriverDTO;
import com.example.api_gateway.model.Driver;
import com.example.api_gateway.repository.DriverRepository;
import com.example.api_gateway.util.MapperUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/drivers", produces = "application/json")
public class DriverController {

    private final DriverRepository driverRepository;


    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @GetMapping
    public List<DriverDTO> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(MapperUtil::toDriverDTO)
                .toList();
    }




    @PostMapping
    public Driver createDriver(@RequestBody Driver driver){
        return driverRepository.save(driver);
    }
}