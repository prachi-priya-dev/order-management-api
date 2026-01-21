package com.prachi.order_management_api.service;

import com.prachi.order_management_api.domain.*;
import com.prachi.order_management_api.dto.request.CreateOrderRequest;
import com.prachi.order_management_api.dto.response.OrderItemResponse;
import com.prachi.order_management_api.dto.response.OrderResponse;
import com.prachi.order_management_api.exception.ConflictException;
import com.prachi.order_management_api.exception.NotFoundException;
import com.prachi.order_management_api.repository.AppUserRepository;
import com.prachi.order_management_api.repository.InventoryRepository;
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
        private final InventoryRepository inventoryRepository;

        public OrderService(OrderRepository orderRepository,
                        AppUserRepository userRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository) {
                this.orderRepository = orderRepository;
                this.userRepository = userRepository;
                this.productRepository = productRepository;
                this.inventoryRepository = inventoryRepository;
        }

        // Phase 3: Create order + reserve inventory
        @Transactional
        public OrderResponse create(CreateOrderRequest req) {
                OffsetDateTime now = OffsetDateTime.now();

                // Phase 2/3 simplification: create minimal user if not exists (Phase 5 will
                // replace with JWT)
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
                        Long productId = itemReq.productId();
                        int qty = itemReq.quantity();

                        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

                        Inventory inv = inventoryRepository.findByProductIdForUpdate(productId)
                                        .orElseThrow(() -> new NotFoundException(
                                                        "Inventory not found for product: " + productId));

                        if (inv.getAvailableQty() < qty) {
                                throw new ConflictException(
                                                "INSUFFICIENT_STOCK for productId=" + productId +
                                                                " available=" + inv.getAvailableQty() +
                                                                " requested=" + qty);
                        }

                        // Reserve stock: available decreases, reserved increases
                        inv.setAvailableQty(inv.getAvailableQty() - qty);
                        inv.setReservedQty(inv.getReservedQty() + qty);
                        inv.setUpdatedAt(now);

                        BigDecimal unitPrice = product.getPrice();
                        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                        total = total.add(lineTotal);

                        OrderItem item = OrderItem.builder()
                                        .product(product)
                                        .quantity(qty)
                                        .unitPrice(unitPrice)
                                        .lineTotal(lineTotal)
                                        .build();

                        order.addItem(item);
                }

                order.setTotalAmount(total);

                Order saved = orderRepository.save(order);
                return toResponse(saved);
        }

        // Phase 3: Cancel order + release inventory (only if CREATED)
        @Transactional
        public OrderResponse cancel(Long orderId) {
                OffsetDateTime now = OffsetDateTime.now();

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

                if (order.getStatus() == OrderStatus.CANCELLED) {
                        return toResponse(order);
                }

                if (order.getStatus() != OrderStatus.CREATED) {
                        throw new ConflictException(
                                        "Only CREATED orders can be cancelled. Current status=" + order.getStatus());
                }

                // Release stock: reserved decreases, available increases
                for (OrderItem item : order.getItems()) {
                        Long productId = item.getProduct().getId();

                        Inventory inv = inventoryRepository.findByProductIdForUpdate(productId)
                                        .orElseThrow(() -> new NotFoundException(
                                                        "Inventory not found for product: " + productId));

                        int qty = item.getQuantity();

                        if (inv.getReservedQty() < qty) {
                                throw new ConflictException(
                                                "CANNOT_RELEASE_STOCK for productId=" + productId +
                                                                " reserved=" + inv.getReservedQty() +
                                                                " requestedRelease=" + qty);
                        }

                        inv.setReservedQty(inv.getReservedQty() - qty);
                        inv.setAvailableQty(inv.getAvailableQty() + qty);
                        inv.setUpdatedAt(now);
                }

                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(now);

                return toResponse(order);
        }

        @Transactional(readOnly = true)
        public OrderResponse getById(Long id) {
                Order order = orderRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
                return toResponse(order);
        }

        @Transactional(readOnly = true)
        public List<OrderResponse> getAll(OrderStatus status) {
                List<Order> orders = (status == null)
                                ? orderRepository.findAll()
                                : orderRepository.findByStatus(status);
                return orders.stream().map(this::toResponse).toList();
        }

        // ✅ This method was missing in your file
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
        
        @Transactional
        public OrderResponse pay(Long orderId) {
                OffsetDateTime now = OffsetDateTime.now();

                Order order = orderRepository.findByIdWithItems(orderId)
                                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

                if (order.getStatus() != OrderStatus.CREATED) {
                        throw new ConflictException(
                                        "Only CREATED orders can be paid. Current status=" + order.getStatus());
                }

                for (OrderItem item : order.getItems()) {
                        Long productId = item.getProduct().getId();
                        Inventory inv = inventoryRepository.findByProductIdForUpdate(productId)
                                        .orElseThrow(() -> new NotFoundException(
                                                        "Inventory not found for product: " + productId));

                        int qty = item.getQuantity();

                        if (inv.getReservedQty() < qty) {
                                throw new ConflictException(
                                                "RESERVED_STOCK_MISMATCH for productId=" + productId +
                                                                " reserved=" + inv.getReservedQty() + " required="
                                                                + qty);
                        }

                        inv.setReservedQty(inv.getReservedQty() - qty);
                        inv.setUpdatedAt(now);
                }

                order.setStatus(OrderStatus.PAID);
                order.setUpdatedAt(now);

                return toResponse(order);
        }

        @Transactional
        public OrderResponse ship(Long orderId) {
                OffsetDateTime now = OffsetDateTime.now();

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

                if (order.getStatus() != OrderStatus.PAID) {
                        throw new ConflictException(
                                        "Only PAID orders can be shipped. Current status=" + order.getStatus());
                }

                order.setStatus(OrderStatus.SHIPPED);
                order.setUpdatedAt(now);

                return toResponse(order);
        }

}
