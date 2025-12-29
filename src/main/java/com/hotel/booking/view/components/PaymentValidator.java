package com.hotel.booking.view.components;

public class PaymentValidator {

    private PaymentValidator() {
    }

    public static boolean isValidCardNumber(String value) {
        if (value == null || value.isEmpty()) return false;
        String cleaned = value.replaceAll("\\s", "");
        return cleaned.matches("\\d{16}");
    }

    public static boolean isValidExpiryDate(String value) {
        if (value == null || value.isEmpty()) return false;
        return value.matches("^(0[1-9]|1[0-2])/\\d{2}$");
    }

    public static boolean isValidCvc(String value) {
        if (value == null || value.isEmpty()) return false;
        return value.matches("\\d{3,4}");
    }

    public static boolean isValidIban(String value) {
        if (value == null || value.isEmpty()) return false;
        String cleaned = value.replaceAll("\\s", "").toUpperCase();
        return cleaned.matches("^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$") && cleaned.length() >= 15 && cleaned.length() <= 34;
    }

    public static boolean isValidBic(String value) {
        if (value == null || value.isEmpty()) return false;
        String cleaned = value.replaceAll("\\s", "").toUpperCase();
        return cleaned.matches("^[A-Z0-9]{8}([A-Z0-9]{3})?$");
    }
}
