package com.quickbites.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbites.model.MenuItem;
import com.quickbites.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end: login -> build cart -> GET cart -> place order, through the real HTTP layer.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CheckoutFlowIntegrationTest {

    private static final String CARD = """
            {"cardholderName":"Sample User","cardNumber":"4242424242424242",
             "expiryMonth":12,"expiryYear":2050,"cvv":"123"}""";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private MenuItemRepository menuItems;

    private String token;

    @BeforeEach
    void loginAndEmptyCart() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alex.morgan@quickbites.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        token = json.readTree(body).get("token").asText();
        mvc.perform(delete("/api/cart").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    // ---------- helpers ----------

    private Long idOf(String name) {
        return menuItems.findAll().stream().filter(m -> m.getName().equals(name))
                .map(MenuItem::getId).findFirst().orElseThrow();
    }

    private JsonNode add(String name, int qty) throws Exception {
        String body = mvc.perform(post("/api/cart/items").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuItemId\":" + idOf(name) + ",\"quantity\":" + qty + "}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private JsonNode setQty(String name, int qty) throws Exception {
        String body = mvc.perform(patch("/api/cart/items/" + idOf(name)).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":" + qty + "}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private JsonNode cart() throws Exception {
        String body = mvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private JsonNode placeOrder() throws Exception {
        String body = mvc.perform(post("/api/orders").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(CARD))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private static BigDecimal money(JsonNode node, String field) {
        return node.get(field).decimalValue();
    }

    // ---------- tests ----------

    @Test
    void requestsWithoutTokenAreRejected() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    void belowThresholdChargesFeePerUniqueRestaurant() throws Exception {
        add("Margherita Pizza", 1);
        JsonNode c = add("Nachos Supreme", 1);

        assertThat(c.get("restaurantCount").asInt()).isEqualTo(2);
        assertThat(money(c, "subtotal")).isEqualByComparingTo("28.98");
        assertThat(money(c, "deliveryFee")).isEqualByComparingTo("10.00");
        assertThat(money(c, "total")).isEqualByComparingTo("38.98");

        JsonNode order = placeOrder();
        assertThat(money(order, "deliveryFee")).isEqualByComparingTo("10.00");
        assertThat(money(order, "total")).isEqualByComparingTo("38.98");
    }

    @Test
    void cartWaivesFeeWhenSubtotalIsFiftyOrMore() throws Exception {
        add("Nachos Supreme", 4);
        JsonNode c = cart();

        assertThat(money(c, "subtotal")).isEqualByComparingTo("51.96");
        assertThat(money(c, "deliveryFee")).isEqualByComparingTo("0.00");
        assertThat(money(c, "total")).isEqualByComparingTo("51.96");
    }

    @Test
    void orderWaivesFeeWhenSubtotalIsFiftyOrMore() throws Exception {
        add("Nachos Supreme", 4);
        JsonNode order = placeOrder();

        assertThat(money(order, "subtotal")).isEqualByComparingTo("51.96");
        assertThat(money(order, "deliveryFee")).isEqualByComparingTo("0.00");
        assertThat(money(order, "total")).isEqualByComparingTo("51.96");
    }

    @Test
    void waiverAppliesAcrossMultipleRestaurants() throws Exception {
        add("Lasagna", 2);
        add("Dragon Roll", 1);
        JsonNode c = add("Edamame", 1);

        assertThat(c.get("restaurantCount").asInt()).isEqualTo(2);
        assertThat(money(c, "subtotal")).isEqualByComparingTo("57.96");
        assertThat(money(c, "deliveryFee")).isEqualByComparingTo("0.00");

        JsonNode order = placeOrder();
        assertThat(money(order, "total")).isEqualByComparingTo("57.96");
    }

    @Test
    void exactlyFiftyQualifiesForFreeDelivery() throws Exception {
        JsonNode c = add("Fiesta Party Platter", 2);

        assertThat(money(c, "subtotal")).isEqualByComparingTo("50.00");
        assertThat(money(c, "deliveryFee")).isEqualByComparingTo("0.00");
        assertThat(money(c, "total")).isEqualByComparingTo("50.00");
    }

    @Test
    void feeComesBackWhenSubtotalDropsBelowThreshold() throws Exception {
        add("Nachos Supreme", 4);
        JsonNode c = setQty("Nachos Supreme", 3);

        assertThat(money(c, "subtotal")).isEqualByComparingTo("38.97");
        assertThat(money(c, "deliveryFee")).isEqualByComparingTo("5.00");
        assertThat(money(c, "total")).isEqualByComparingTo("43.97");
    }

    @Test
    void cartAndOrderAlwaysAgree() throws Exception {
        add("Margherita Pizza", 2);
        add("Tiramisu", 1);
        add("Salmon Nigiri", 1);
        JsonNode c = cart();
        JsonNode order = placeOrder();

        assertThat(money(order, "subtotal")).isEqualByComparingTo(money(c, "subtotal"));
        assertThat(money(order, "deliveryFee")).isEqualByComparingTo(money(c, "deliveryFee"));
        assertThat(money(order, "total")).isEqualByComparingTo(money(c, "total"));
    }

    @Test
    void cartIsClearedAfterCheckout() throws Exception {
        add("Churros", 1);
        placeOrder();
        assertThat(cart().get("items").size()).isZero();
    }

    @Test
    void declinedCardDoesNotCreateOrderOrClearCart() throws Exception {
        add("Churros", 1);
        mvc.perform(post("/api/orders").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CARD.replace("4242424242424242", "4000000000000002")))
                .andExpect(status().isPaymentRequired());
        assertThat(cart().get("items").size()).isEqualTo(1);
    }
}
