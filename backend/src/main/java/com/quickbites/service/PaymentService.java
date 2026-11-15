package com.quickbites.service;

import com.quickbites.dto.CheckoutRequest;
import com.quickbites.exception.BadRequestException;
import com.quickbites.exception.PaymentDeclinedException;
import com.quickbites.util.CardValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Fake payment gateway. Validates the card and "charges" it.
 * Card 4000000000000002 is always declined (handy for testing the failure path).
 */
@Service
public class PaymentService {

    private static final String ALWAYS_DECLINED = "4000000000000002";

    public String charge(CheckoutRequest card, BigDecimal amount) {
        if (!CardValidator.isValidNumber(card.cardNumber())) {
            throw new BadRequestException("Invalid card number");
        }
        if (!CardValidator.isNotExpired(card.expiryMonth(), card.expiryYear())) {
            throw new BadRequestException("Card is expired");
        }
        if (!CardValidator.isValidCvv(card.cvv())) {
            throw new BadRequestException("Invalid CVV");
        }
        if (amount.signum() <= 0) {
            throw new BadRequestException("Nothing to charge");
        }
        if (card.cardNumber().replaceAll("\\s", "").equals(ALWAYS_DECLINED)) {
            throw new PaymentDeclinedException("Card was declined");
        }
        return CardValidator.last4(card.cardNumber());
    }
}
