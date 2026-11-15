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
     * Flat fee: one delivery fee per unique restaurant.
     */
    public BigDecimal calculate(int restaurantCount) {
        if (restaurantCount <= 0) {
            return MoneyUtil.ZERO;
        }
        return MoneyUtil.round(pricing.deliveryFeePerRestaurant().multiply(BigDecimal.valueOf(restaurantCount)));
    }

    /**
     * Fee taking the free-delivery promotion into account.
     */
    public BigDecimal calculate(BigDecimal subtotal, int restaurantCount) {
        if (qualifiesForFreeDelivery(subtotal)) {
            return MoneyUtil.ZERO;
        }
        return calculate(restaurantCount);
    }

    public boolean qualifiesForFreeDelivery(BigDecimal subtotal) {
        return subtotal != null && subtotal.compareTo(pricing.freeDeliveryThreshold()) > 0;
    }

    public BigDecimal getFreeDeliveryThreshold() {
        return MoneyUtil.round(pricing.freeDeliveryThreshold());
    }
}
