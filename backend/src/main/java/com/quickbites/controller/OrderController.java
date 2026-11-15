package com.quickbites.controller;

import com.quickbites.dto.CheckoutRequest;
import com.quickbites.dto.OrderResponse;
import com.quickbites.filter.AuthTokenFilter;
import com.quickbites.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@RequestAttribute(AuthTokenFilter.USER_ID_ATTR) Long userId,
                                  @Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(userId, request);
    }

    @GetMapping
    public List<OrderResponse> list(@RequestAttribute(AuthTokenFilter.USER_ID_ATTR) Long userId) {
        return orderService.listOrders(userId);
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse get(@RequestAttribute(AuthTokenFilter.USER_ID_ATTR) Long userId,
                             @PathVariable String orderNumber) {
        return orderService.getOrder(userId, orderNumber);
    }
}
