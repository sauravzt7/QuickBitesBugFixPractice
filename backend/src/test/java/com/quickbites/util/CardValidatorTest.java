package com.quickbites.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardValidatorTest {

    @Test
    void acceptsLuhnValidTestCard() {
        assertThat(CardValidator.isValidNumber("4242424242424242")).isTrue();
        assertThat(CardValidator.isValidNumber("4242 4242 4242 4242")).isTrue();
    }

    @Test
    void rejectsInvalidNumbers() {
        assertThat(CardValidator.isValidNumber("4242424242424241")).isFalse();
        assertThat(CardValidator.isValidNumber("abcd")).isFalse();
        assertThat(CardValidator.isValidNumber(null)).isFalse();
    }

    @Test
    void validatesExpiryAndCvv() {
        assertThat(CardValidator.isNotExpired(12, 2050)).isTrue();
        assertThat(CardValidator.isNotExpired(1, 2020)).isFalse();
        assertThat(CardValidator.isNotExpired(13, 2050)).isFalse();
        assertThat(CardValidator.isValidCvv("123")).isTrue();
        assertThat(CardValidator.isValidCvv("12")).isFalse();
    }

    @Test
    void extractsLast4() {
        assertThat(CardValidator.last4("4242 4242 4242 1234")).isEqualTo("1234");
    }
}
