package com.hotel.booking.service;

/**
 * Enum for room deletion actions.
 * <p>
 * Defines the different types of deletion actions that can be performed on a room:
 * </p>
 * <ul>
 *   <li>{@code SET_INACTIVE} - Deactivate an active room</li>
 *   <li>{@code PERMANENT_DELETE} - Permanently delete an inactive room</li>
 *   <li>{@code BLOCKED_BY_BOOKINGS} - Deletion blocked due to related bookings</li>
 * </ul>
 *
 * @author Artur Derr
 */
public enum RoomManagementDeleteActionType {
    /**
     * Room is active and can be deactivated instead of deletion
     */
    SET_INACTIVE,

    /**
     * Room is inactive and can be permanently deleted
     */
    PERMANENT_DELETE,

    /**
     * Deletion is blocked because the room has related bookings
     */
    BLOCKED_BY_BOOKINGS
}
