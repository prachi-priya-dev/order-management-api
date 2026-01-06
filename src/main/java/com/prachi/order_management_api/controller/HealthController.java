package com.prachi.order_management_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Order Management API is running ✅";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
