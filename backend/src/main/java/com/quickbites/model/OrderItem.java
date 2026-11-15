package com.quickbites.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Snapshot of a cart line at checkout time. Prices are copied so later
 * menu changes never alter historical orders.
 */
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    @Column(nullable = false)
    private Long menuItemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {
    }

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItemId = menuItem.getId();
        this.itemName = menuItem.getName();
        this.restaurantId = menuItem.getRestaurant().getId();
        this.restaurantName = menuItem.getRestaurant().getName();
        this.unitPrice = menuItem.getPrice();
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public Long getMenuItemId() { return menuItemId; }
    public String getItemName() { return itemName; }
    public Long getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    void setOrder(Order order) { this.order = order; }
}
