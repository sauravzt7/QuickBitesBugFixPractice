package com.quickbites.dto;

import java.math.BigDecimal;

public record OrderItemResponse(String name, String restaurantName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
