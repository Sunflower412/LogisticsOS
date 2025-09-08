package com.example.api_gateway.controller;


import com.example.api_gateway.model.Client;
import com.example.api_gateway.repository.ClientRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public List<Client> getAllClients(){
        return clientRepository.findAll();
    }

    @PostMapping
    public Client createClient(@RequestBody Client client){
        return clientRepository.save(client);
    }

    @GetMapping("/priority/{priority}")
    public List<Client> getClientById(@PathVariable int priority){
        return clientRepository.findByPriority(priority);
    }

    @GetMapping("/company_name/{company_name}")
    public List<Client> getClientByCompanyName(@PathVariable String company_name){
        return clientRepository.findByCompanyName(company_name);
    }
}
