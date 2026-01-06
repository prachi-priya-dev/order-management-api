package com.prachi.order_management_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prachi.order_management_api.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}