package com.quickbites.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Quantity 0 removes the line. */
public record UpdateCartItemRequest(@Min(0) @Max(50) int quantity) {
}
