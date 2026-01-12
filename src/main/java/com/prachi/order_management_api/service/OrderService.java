package com.prachi.order_management_api.service;

import com.prachi.order_management_api.domain.*;
import com.prachi.order_management_api.dto.request.CreateOrderRequest;
import com.prachi.order_management_api.dto.response.*;
import com.prachi.order_management_api.exception.NotFoundException;
import com.prachi.order_management_api.repository.AppUserRepository;
import com.prachi.order_management_api.repository.OrderRepository;
import com.prachi.order_management_api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, AppUserRepository userRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        OffsetDateTime now = OffsetDateTime.now();

        // Phase 2: if user doesn't exist, create minimal user (later replaced by JWT
        // auth)
        AppUser user = userRepository.findByEmail(req.userEmail())
                .orElseGet(() -> userRepository.save(AppUser.builder()
                        .email(req.userEmail())
                        .passwordHash("N/A")
                        .role(UserRole.ROLE_USER)
                        .createdAt(now)
                        .build()));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : req.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + itemReq.productId()));

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build();

            order.addItem(item);
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAll(OrderStatus status) {
        List<Order> orders = (status == null) ? orderRepository.findAll() : orderRepository.findByStatus(status);
        return orders.stream().map(this::toResponse).toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
