package com.prachi.order_management_api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        boolean active,
        int availableQty,
        int reservedQty,
        OffsetDateTime createdAt) {
}