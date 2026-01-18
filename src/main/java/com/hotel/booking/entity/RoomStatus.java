package com.hotel.booking.entity;

/**
 * Enumeration of possible room status states in the hotel management system.
 *
 * <p>Each room in the system must have one of these statuses to track its current condition
 * and availability. Statuses range from available for booking to temporarily out of service
 * for maintenance or renovation.
 *
 * @author Artur Derr
 */
public enum RoomStatus {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    CLEANING("Cleaning"),
    RENOVATING("Renovating"),
    OUT_OF_SERVICE("Out of Service"),
    INACTIVE("Inactive");

    /** Display name for user-facing interfaces. */
    private final String displayName;

    /**
     * Creates a new room status with the given display name.
     *
     * @param displayName the user-friendly display name for this status
     */
    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name of this room status.
     *
     * @return the display name
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Gets the display name for this room status.
     *
     * @return the display name (same as {@link #toString()})
     */
    public String getDisplayName() {
        return displayName;
    }
}
