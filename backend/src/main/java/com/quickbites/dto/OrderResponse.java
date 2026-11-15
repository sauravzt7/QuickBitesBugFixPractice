package com.quickbites.dto;

import com.quickbites.model.Order;
import com.quickbites.util.MoneyUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(String orderNumber, String status, Instant createdAt, String cardLast4,
                            List<OrderItemResponse> items, int itemCount,
                            BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal total) {

    public static OrderResponse from(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(i.getItemName(), i.getRestaurantName(), i.getUnitPrice(),
                        i.getQuantity(), MoneyUtil.round(i.getLineTotal())))
                .toList();
        int count = o.getItems().stream().mapToInt(i -> i.getQuantity()).sum();
        return new OrderResponse(o.getOrderNumber(), o.getStatus().name(), o.getCreatedAt(), o.getCardLast4(),
                items, count, o.getSubtotal(), o.getDeliveryFee(), o.getTotal());
    }
}
