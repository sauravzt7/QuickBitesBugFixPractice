package com.quickbites.dto;

import com.quickbites.model.MenuItem;

import java.math.BigDecimal;

public record MenuItemResponse(Long id, String name, String description, String category, BigDecimal price) {
    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getName(), item.getDescription(), item.getCategory(), item.getPrice());
    }
}
