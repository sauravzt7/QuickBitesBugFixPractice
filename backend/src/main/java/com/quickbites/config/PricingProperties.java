package com.quickbites.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Pricing knobs, bound from {@code quickbites.pricing.*} in application.yml.
 *
 * @param deliveryFeePerRestaurant flat fee charged once per unique restaurant in the cart
 * @param freeDeliveryThreshold    subtotal at or above which delivery is free (see technical-specs/DeliveryFeeWaiver.md)
 */
@ConfigurationProperties(prefix = "quickbites.pricing")
public record PricingProperties(BigDecimal deliveryFeePerRestaurant, BigDecimal freeDeliveryThreshold) {
}
