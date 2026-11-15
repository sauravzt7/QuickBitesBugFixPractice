package com.quickbites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(@NotBlank String cardholderName,
                              @NotBlank String cardNumber,
                              @NotNull Integer expiryMonth,
                              @NotNull Integer expiryYear,
                              @NotBlank String cvv) {
}
