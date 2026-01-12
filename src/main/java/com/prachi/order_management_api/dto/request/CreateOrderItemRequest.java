package com.prachi.order_management_api.dto.request;

import jakarta.validation.constraints.*;

public record CreateOrderItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity) {
}