package com.hotel.booking.entity;

/**
 * Shared payment status enum used by Payment and Invoice entities.
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
