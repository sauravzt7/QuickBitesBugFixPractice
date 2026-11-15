package com.quickbites.service;

import com.quickbites.dto.CheckoutRequest;
import com.quickbites.dto.OrderResponse;
import com.quickbites.exception.BadRequestException;
import com.quickbites.exception.NotFoundException;
import com.quickbites.model.Cart;
import com.quickbites.model.CartItem;
import com.quickbites.model.Order;
import com.quickbites.model.OrderItem;
import com.quickbites.repository.CartRepository;
import com.quickbites.repository.OrderRepository;
import com.quickbites.util.MoneyUtil;
import com.quickbites.util.OrderNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final CartService cartService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final DeliveryFeeCalculator deliveryFeeCalculator;

    public OrderService(CartService cartService,
                        CartRepository cartRepository,
                        OrderRepository orderRepository,
                        PaymentService paymentService,
                        DeliveryFeeCalculator deliveryFeeCalculator) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.deliveryFeeCalculator = deliveryFeeCalculator;
    }

    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        List<CartItem> items = cart.getItems();

        BigDecimal subtotal = CartService.subtotalOf(items);
        int restaurantCount = CartService.countUniqueRestaurants(items);
        // Same calculator as the cart, so cart and order always agree (spec R5).
        BigDecimal deliveryFee = deliveryFeeCalculator.calculate(subtotal, restaurantCount);
        BigDecimal total = MoneyUtil.round(subtotal.add(deliveryFee));

        String last4 = paymentService.charge(request, total);

        Order order = new Order(OrderNumberGenerator.next(), cart.getUser(), last4);
        items.forEach(i -> order.addItem(new OrderItem(i.getMenuItem(), i.getQuantity())));
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(total);
        Order saved = orderRepository.save(order);

        cart.clear();
        cartRepository.save(cart);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, String orderNumber) {
        return orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new NotFoundException("Order " + orderNumber + " not found"));
    }
}
