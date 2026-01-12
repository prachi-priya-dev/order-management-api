package com.prachi.order_management_api.repository;

import com.prachi.order_management_api.domain.Order;
import com.prachi.order_management_api.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}
