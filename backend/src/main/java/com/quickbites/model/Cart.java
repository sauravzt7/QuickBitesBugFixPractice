package com.quickbites.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    public Cart(User user) {
        this.user = user;
    }

    public Optional<CartItem> findItem(Long menuItemId) {
        return items.stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItemId))
                .findFirst();
    }

    public void addItem(MenuItem menuItem, int quantity) {
        findItem(menuItem.getId()).ifPresentOrElse(
                existing -> existing.setQuantity(existing.getQuantity() + quantity),
                () -> items.add(new CartItem(this, menuItem, quantity)));
    }

    public void removeItem(Long menuItemId) {
        items.removeIf(i -> i.getMenuItem().getId().equals(menuItemId));
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public List<CartItem> getItems() { return items; }
}
