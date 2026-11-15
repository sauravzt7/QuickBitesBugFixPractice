package com.quickbites.service;

import com.quickbites.config.PricingProperties;
import com.quickbites.util.MoneyUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Computes delivery fees for a cart / order.
 * <p>
 * Pricing rules live in {@code technical-specs/DeliveryFeeWaiver.md}.
 */
@Component
public class DeliveryFeeCalculator {

    private final PricingProperties pricing;

    public DeliveryFeeCalculator(PricingProperties pricing) {
        this.pricing = pricing;
    }

    /**
     * Base fee (no waiver): one delivery fee per unique restaurant.
     * Private so callers can't bypass the free-delivery rule.
     */
    private BigDecimal baseFee(int restaurantCount) {
        if (restaurantCount <= 0) {
            return MoneyUtil.ZERO;
        }
        return MoneyUtil.round(pricing.deliveryFeePerRestaurant().multiply(BigDecimal.valueOf(restaurantCount)));
    }

    /**
     * Single source of truth for the delivery fee (spec R1-R3, R5):
     * $0 when subtotal >= threshold, otherwise fee x unique restaurants.
     */
    public BigDecimal calculate(BigDecimal subtotal, int restaurantCount) {
        if (qualifiesForFreeDelivery(subtotal)) {
            return MoneyUtil.ZERO;
        }
        return baseFee(restaurantCount);
    }

    public boolean qualifiesForFreeDelivery(BigDecimal subtotal) {
        return subtotal != null && subtotal.compareTo(pricing.freeDeliveryThreshold()) >= 0;
    }

    public BigDecimal getFreeDeliveryThreshold() {
        return MoneyUtil.round(pricing.freeDeliveryThreshold());
    }
}
