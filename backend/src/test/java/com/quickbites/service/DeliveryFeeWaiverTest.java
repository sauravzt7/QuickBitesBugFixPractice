package com.quickbites.service;

import com.quickbites.config.PricingProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the delivery-fee rules in technical-specs/DeliveryFeeWaiver.md.
 */
class DeliveryFeeWaiverTest {

    private final DeliveryFeeCalculator calculator = new DeliveryFeeCalculator(
            new PricingProperties(new BigDecimal("5.00"), new BigDecimal("50.00")));

    private static BigDecimal usd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void chargesFivePerRestaurantBelowThreshold() {
        assertThat(calculator.calculate(usd("40.97"), 1)).isEqualByComparingTo("5.00");
        assertThat(calculator.calculate(usd("28.98"), 2)).isEqualByComparingTo("10.00");
        assertThat(calculator.calculate(usd("49.99"), 3)).isEqualByComparingTo("15.00");
    }

    @Test
    void waivesFeeAboveThreshold() {
        assertThat(calculator.calculate(usd("51.96"), 1)).isEqualByComparingTo("0.00");
        assertThat(calculator.calculate(usd("120.00"), 3)).isEqualByComparingTo("0.00");
    }

    @Test
    void waivesFeeAtExactlyTheThreshold() {
        assertThat(calculator.calculate(usd("50.00"), 1)).isEqualByComparingTo("0.00");
        assertThat(calculator.calculate(usd("50.00"), 2)).isEqualByComparingTo("0.00");
    }

    @Test
    void emptyCartHasNoFee() {
        assertThat(calculator.calculate(usd("0.00"), 0)).isEqualByComparingTo("0.00");
    }

    @Test
    void feeAndThresholdComeFromConfiguration() {
        DeliveryFeeCalculator custom = new DeliveryFeeCalculator(
                new PricingProperties(new BigDecimal("3.50"), new BigDecimal("30.00")));
        assertThat(custom.calculate(usd("29.99"), 2)).isEqualByComparingTo("7.00");
        assertThat(custom.calculate(usd("30.00"), 2)).isEqualByComparingTo("0.00");
    }
}
