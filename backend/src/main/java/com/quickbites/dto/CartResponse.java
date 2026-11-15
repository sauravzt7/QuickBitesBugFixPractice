package com.quickbites.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartItemResponse> items,
                           int restaurantCount,
                           BigDecimal subtotal,
                           BigDecimal deliveryFee,
                           BigDecimal total,
                           BigDecimal freeDeliveryThreshold) {
}
