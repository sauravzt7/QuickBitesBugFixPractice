package com.quickbites.seed;

import com.quickbites.model.MenuItem;
import com.quickbites.model.Restaurant;
import com.quickbites.model.User;
import com.quickbites.repository.RestaurantRepository;
import com.quickbites.repository.UserRepository;
import com.quickbites.util.PasswordHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public DataSeeder(UserRepository userRepository, RestaurantRepository restaurantRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new User("alex.morgan@quickbites.com", PasswordHasher.hash("password123"), "Alex Morgan"));
        userRepository.save(new User("sam.lee@quickbites.com", PasswordHasher.hash("password123"), "Sam Lee"));

        Restaurant taco = new Restaurant("Taco Fiesta", "Mexican");
        taco.addMenuItem(item("Guacamole & Chips", "Fresh avocado dip with lime, onion and crispy tortilla chips", "Appetizers", "8.99"));
        taco.addMenuItem(item("Nachos Supreme", "Loaded nachos with beef, cheese, jalapenos and sour cream", "Appetizers", "12.99"));
        taco.addMenuItem(item("Chicken Burrito", "Grilled chicken, rice, beans and salsa", "Mains", "11.49"));
        taco.addMenuItem(item("Carne Asada Tacos", "Three tacos with grilled steak and cilantro", "Mains", "13.99"));
        taco.addMenuItem(item("Fiesta Party Platter", "Tacos, quesadillas and dips for four", "Mains", "25.00"));
        taco.addMenuItem(item("Horchata", "Sweet cinnamon rice drink", "Beverages", "3.99"));
        taco.addMenuItem(item("Churros", "Cinnamon sugar churros with chocolate dip", "Desserts", "5.49"));
        restaurantRepository.save(taco);

        Restaurant italian = new Restaurant("Italian Kitchen", "Italian");
        italian.addMenuItem(item("Garlic Bread", "Toasted ciabatta with garlic butter", "Appetizers", "6.49"));
        italian.addMenuItem(item("Margherita Pizza", "San Marzano tomato, mozzarella, basil", "Mains", "15.99"));
        italian.addMenuItem(item("Penne Arrabbiata", "Spicy tomato sauce with chili and garlic", "Mains", "14.49"));
        italian.addMenuItem(item("Lasagna", "Layered beef ragu and bechamel", "Mains", "17.99"));
        italian.addMenuItem(item("San Pellegrino", "Sparkling water", "Beverages", "2.99"));
        italian.addMenuItem(item("Tiramisu", "Espresso-soaked ladyfingers and mascarpone", "Desserts", "8.99"));
        restaurantRepository.save(italian);

        Restaurant sushi = new Restaurant("Sushi Spot", "Japanese");
        sushi.addMenuItem(item("Edamame", "Steamed soybeans with sea salt", "Appetizers", "4.99"));
        sushi.addMenuItem(item("California Roll", "Crab, avocado, cucumber (8 pcs)", "Mains", "9.99"));
        sushi.addMenuItem(item("Salmon Nigiri", "Fresh salmon over rice (4 pcs)", "Mains", "12.49"));
        sushi.addMenuItem(item("Dragon Roll", "Eel, cucumber, avocado topping (8 pcs)", "Mains", "16.99"));
        sushi.addMenuItem(item("Green Tea", "Hot Japanese sencha", "Beverages", "2.49"));
        sushi.addMenuItem(item("Mochi Ice Cream", "Assorted flavours (3 pcs)", "Desserts", "6.99"));
        restaurantRepository.save(sushi);
    }

    private static MenuItem item(String name, String description, String category, String price) {
        return new MenuItem(name, description, category, new BigDecimal(price));
    }
}
