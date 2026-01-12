package com.prachi.order_management_api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 255) String name,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        Boolean active,
        @NotNull @Min(0) Integer initialStock) {
}