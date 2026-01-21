package com.prachi.order_management_api.repository;

import com.prachi.order_management_api.domain.Order;
import com.prachi.order_management_api.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    @Query("""
                select o from Order o
                left join fetch o.items i
                left join fetch i.product
                where o.id = :id
            """)
    Optional<Order> findByIdWithItems(Long id);
}
