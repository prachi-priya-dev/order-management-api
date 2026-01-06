package com.prachi.order_management_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prachi.order_management_api.domain.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}