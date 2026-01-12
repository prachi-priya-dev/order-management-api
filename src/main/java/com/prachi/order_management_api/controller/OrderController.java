package com.prachi.order_management_api.controller;

import com.prachi.order_management_api.domain.OrderStatus;
import com.prachi.order_management_api.dto.request.CreateOrderRequest;
import com.prachi.order_management_api.dto.response.OrderResponse;
import com.prachi.order_management_api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest req) {
        return orderService.create(req);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @GetMapping
    public List<OrderResponse> getAll(@RequestParam(required = false) OrderStatus status) {
        return orderService.getAll(status);
    }
}
