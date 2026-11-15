package com.quickbites.controller;

import com.quickbites.dto.AddToCartRequest;
import com.quickbites.dto.CartResponse;
import com.quickbites.dto.UpdateCartItemRequest;
import com.quickbites.filter.AuthTokenFilter;
import com.quickbites.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(@RequestAttribute(AuthTokenFilter.USER_ID_ATTR) Long userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public CartResponse add(@RequestAttribute(AuthTokenFilter.USER_ID_ATTR) Long userId,
                            @Valid @RequestBody AddToCartRequest request) {
        return cartService.addItem(userId, request.menuItemId(), request.quantity());
    }

    @PatchMapping("/items/{menuItemId}")
    public CartResponse update(@RequestAttribute(AuthTokenFilter.USER_ID_ATTR) Long userId,
                               @PathVariable Long menuItemId,
                               @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(userId, menuItemId, request.quantity());
    }

    @DeleteMapping("/items/{menuItemId}")
    public CartResponse remove(@RequestAttribute(AuthTokenFilter.USER_ID_ATTR) Long userId,
                               @PathVariable Long menuItemId) {
        return cartService.removeItem(userId, menuItemId);
    }

    @DeleteMapping
    public CartResponse clear(@RequestAttribute(AuthTokenFilter.USER_ID_ATTR) Long userId) {
        return cartService.clear(userId);
    }
}
