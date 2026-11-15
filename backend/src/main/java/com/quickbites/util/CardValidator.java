package com.quickbites.util;

import java.time.YearMonth;

public final class CardValidator {

    private CardValidator() {
    }

    public static boolean isValidNumber(String number) {
        if (number == null) return false;
        String digits = number.replaceAll("\\s", "");
        if (!digits.matches("\\d{13,19}")) return false;
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    public static boolean isNotExpired(int month, int year) {
        if (month < 1 || month > 12) return false;
        return !YearMonth.of(year, month).isBefore(YearMonth.now());
    }

    public static boolean isValidCvv(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }

    public static String last4(String number) {
        String digits = number.replaceAll("\\s", "");
        return digits.substring(digits.length() - 4);
    }
}
