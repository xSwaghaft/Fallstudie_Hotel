package com.hotel.booking.entity;

// Enums
    public enum PaymentMethod {
        CARD("Card"),
        CASH("Cash"),
        INVOICE("Invoice"),
        TRANSFER("Bank Transfer");

        private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
