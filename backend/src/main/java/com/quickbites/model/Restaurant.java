package com.quickbites.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String cuisine;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("category ASC, name ASC")
    private List<MenuItem> menuItems = new ArrayList<>();

    protected Restaurant() {
    }

    public Restaurant(String name, String cuisine) {
        this.name = name;
        this.cuisine = cuisine;
    }

    public MenuItem addMenuItem(MenuItem item) {
        item.setRestaurant(this);
        menuItems.add(item);
        return item;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public List<MenuItem> getMenuItems() { return menuItems; }
}
