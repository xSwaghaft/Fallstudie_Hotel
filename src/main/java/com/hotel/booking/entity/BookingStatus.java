package com.hotel.booking.entity;

/**
 * Represents the status of a booking in the booking system.
 * 
 * <p>
 * Describes the current state of a booking during its lifecycle,
 * from creation through confirmation to check-in, check-out,
 * or a possible cancellation.
 * </p>
 * 
 * @author Viktor Götting
 * @since 1.0
 */
public enum BookingStatus {
    
    PENDING,
    CONFIRMED,
    CHECKED_IN,
    CANCELLED,
    COMPLETED,
    MODIFIED
}
