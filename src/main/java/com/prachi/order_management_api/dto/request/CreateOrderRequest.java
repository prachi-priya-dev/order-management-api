package com.prachi.order_management_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String userEmail, // Phase 2: simple ownership; Phase 5: will come from JWT
        @NotEmpty @Valid List<CreateOrderItemRequest> items) {
}