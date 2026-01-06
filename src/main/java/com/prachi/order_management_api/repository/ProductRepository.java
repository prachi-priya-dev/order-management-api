package com.prachi.order_management_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prachi.order_management_api.domain.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
}