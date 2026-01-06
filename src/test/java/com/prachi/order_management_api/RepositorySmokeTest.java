package com.prachi.order_management_api;

import com.prachi.order_management_api.domain.*;
import com.prachi.order_management_api.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RepositorySmokeTest {

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void canPersistAndReadCoreEntities() {
        OffsetDateTime now = OffsetDateTime.now();

        AppUser user = appUserRepository.save(AppUser.builder()
                .email("user1@test.com")
                .passwordHash("dummy-hash")
                .role(UserRole.ROLE_USER)
                .createdAt(now)
                .build());

        Product product = productRepository.save(Product.builder()
                .sku("SKU-001")
                .name("iPhone Case")
                .price(new BigDecimal("499.00"))
                .active(true)
                .createdAt(now)
                .build());

        Inventory inv = inventoryRepository.save(Inventory.builder()
                .product(product)
                .availableQty(10)
                .reservedQty(0)
                .updatedAt(now)
                .build());

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(2)
                .unitPrice(product.getPrice())
                .lineTotal(product.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();

        order.addItem(item);
        order.setTotalAmount(item.getLineTotal());

        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(inv.getProductId()).isEqualTo(product.getId());
        assertThat(savedOrder.getItems()).hasSize(1);
    }
}
