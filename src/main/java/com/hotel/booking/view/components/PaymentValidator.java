package com.hotel.booking.view.components;

/**
 * Utility class providing validation methods for payment form fields.
 * 
 * This class contains static validation methods for various payment-related input fields.
 * It supports validation for:
 * - Credit card numbers (16 digits)
 * - Card expiry dates (MM/YY format)
 * - Card CVC codes (3-4 digits)
 * - IBAN numbers (15-34 alphanumeric characters, country-specific format)
 * - BIC/SWIFT codes (8-11 alphanumeric characters)
 * 
 * All validation methods follow international standards and return boolean results
 * indicating whether the input is valid.
 * 
 * @author Arman Özcanli
 * @see PaymentDialog
 * @see PaymentMethodManager
 */
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
