package com.quickbites.service;

import com.quickbites.dto.CartItemResponse;
import com.quickbites.dto.CartResponse;
import com.quickbites.exception.NotFoundException;
import com.quickbites.model.Cart;
import com.quickbites.model.CartItem;
import com.quickbites.model.MenuItem;
import com.quickbites.model.User;
import com.quickbites.repository.CartRepository;
import com.quickbites.repository.MenuItemRepository;
import com.quickbites.repository.UserRepository;
import com.quickbites.util.MoneyUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final DeliveryFeeCalculator deliveryFeeCalculator;

    public CartService(CartRepository cartRepository,
                       MenuItemRepository menuItemRepository,
                       UserRepository userRepository,
                       DeliveryFeeCalculator deliveryFeeCalculator) {
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
        this.deliveryFeeCalculator = deliveryFeeCalculator;
    }

    public CartResponse getCart(Long userId) {
        return toResponse(getOrCreateCart(userId));
    }

    public CartResponse addItem(Long userId, Long menuItemId, int quantity) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new NotFoundException("Menu item " + menuItemId + " not found"));
        Cart cart = getOrCreateCart(userId);
        cart.addItem(menuItem, quantity);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse updateQuantity(Long userId, Long menuItemId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.findItem(menuItemId)
                .orElseThrow(() -> new NotFoundException("Item " + menuItemId + " is not in the cart"));
        if (quantity == 0) {
            cart.removeItem(menuItemId);
        } else {
            item.setQuantity(quantity);
        }
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse removeItem(Long userId, Long menuItemId) {
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(menuItemId);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse clear(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        return toResponse(cartRepository.save(cart));
    }

    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
            return cartRepository.save(new Cart(user));
        });
    }

    static int countUniqueRestaurants(List<CartItem> items) {
        return (int) items.stream()
                .map(i -> i.getMenuItem().getRestaurant().getId())
                .distinct()
                .count();
    }

    static BigDecimal subtotalOf(List<CartItem> items) {
        return MoneyUtil.round(items.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getMenuItem().getId(),
                        i.getMenuItem().getName(),
                        i.getMenuItem().getRestaurant().getId(),
                        i.getMenuItem().getRestaurant().getName(),
                        i.getMenuItem().getPrice(),
                        i.getQuantity(),
                        MoneyUtil.round(i.getLineTotal())))
                .toList();

        int restaurantCount = countUniqueRestaurants(cart.getItems());
        BigDecimal subtotal = subtotalOf(cart.getItems());
        BigDecimal deliveryFee = deliveryFeeCalculator.calculate(subtotal, restaurantCount);
        BigDecimal total = MoneyUtil.round(subtotal.add(deliveryFee));

        return new CartResponse(items, restaurantCount, subtotal, deliveryFee, total,
                deliveryFeeCalculator.getFreeDeliveryThreshold());
    }
}
