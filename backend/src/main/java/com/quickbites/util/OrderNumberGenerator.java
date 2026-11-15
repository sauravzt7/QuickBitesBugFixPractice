package com.quickbites.util;

import java.security.SecureRandom;
import java.util.HexFormat;

public final class OrderNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OrderNumberGenerator() {
    }

    public static String next() {
        byte[] bytes = new byte[3];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }
}
