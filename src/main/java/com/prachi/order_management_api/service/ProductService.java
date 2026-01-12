package com.prachi.order_management_api.service;


import com.prachi.order_management_api.domain.*;
import com.prachi.order_management_api.dto.request.CreateProductRequest;
import com.prachi.order_management_api.dto.response.ProductResponse;
import com.prachi.order_management_api.exception.NotFoundException;
import com.prachi.order_management_api.repository.InventoryRepository;
import com.prachi.order_management_api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final InventoryRepository inventoryRepository;

  public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
    this.productRepository = productRepository;
    this.inventoryRepository = inventoryRepository;
  }

  @Transactional
  public ProductResponse create(CreateProductRequest req) {
    OffsetDateTime now = OffsetDateTime.now();

    Product product = Product.builder()
        .sku(req.sku())
        .name(req.name())
        .price(req.price())
        .active(req.active() == null || req.active())
        .createdAt(now)
        .build();

    Product saved = productRepository.save(product);

    Inventory inv = Inventory.builder()
        .product(saved)
        .availableQty(req.initialStock())
        .reservedQty(0)
        .updatedAt(now)
        .build();

    Inventory savedInv = inventoryRepository.save(inv);

    return toResponse(saved, savedInv);
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> getAll() {
    return productRepository.findAll().stream()
        .map(p -> {
          Inventory inv = inventoryRepository.findById(p.getId()).orElse(null);
          return toResponse(p, inv);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  public ProductResponse getById(Long id) {
    Product p = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    Inventory inv = inventoryRepository.findById(id).orElse(null);
    return toResponse(p, inv);
  }

  private ProductResponse toResponse(Product p, Inventory inv) {
    int available = inv == null ? 0 : inv.getAvailableQty();
    int reserved = inv == null ? 0 : inv.getReservedQty();
    return new ProductResponse(
        p.getId(), p.getSku(), p.getName(), p.getPrice(), p.isActive(),
        available, reserved, p.getCreatedAt()
    );
  }
}
