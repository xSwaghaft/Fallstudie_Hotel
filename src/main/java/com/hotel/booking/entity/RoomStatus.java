package com.hotel.booking.entity;

/**
 * RoomStatus Enum - Definiert die möglichen Zustände eines Zimmers
 */
public enum RoomStatus {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    CLEANING("Cleaning"),
    RENOVATING("Renovating"),
    OUT_OF_SERVICE("Out of Service"),
    INACTIVE("Inactive");

    private final String displayName;

    RoomStatus(String displayName) {
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
