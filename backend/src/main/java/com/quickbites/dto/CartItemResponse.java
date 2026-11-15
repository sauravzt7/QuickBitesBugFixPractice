package com.quickbites.dto;

import java.math.BigDecimal;

public record CartItemResponse(Long menuItemId, String name, Long restaurantId, String restaurantName,
                               BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
