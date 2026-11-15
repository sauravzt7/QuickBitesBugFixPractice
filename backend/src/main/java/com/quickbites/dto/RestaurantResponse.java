package com.quickbites.dto;

import com.quickbites.model.Restaurant;

import java.util.List;

public record RestaurantResponse(Long id, String name, String cuisine, List<MenuItemResponse> menu) {
    public static RestaurantResponse from(Restaurant r) {
        return new RestaurantResponse(r.getId(), r.getName(), r.getCuisine(),
                r.getMenuItems().stream().map(MenuItemResponse::from).toList());
    }
}
