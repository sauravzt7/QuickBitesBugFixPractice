package com.quickbites.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(@NotNull Long menuItemId, @Min(1) @Max(50) int quantity) {
}
