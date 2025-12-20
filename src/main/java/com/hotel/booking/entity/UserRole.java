package com.hotel.booking.entity;

/**
 * Enumeration of user roles in the hotel booking system.
 *
 * <p>Each user in the system must be assigned one of these roles to determine
 * their permissions and access level within the application.
 *
 * @author Artur Derr
 */
public enum UserRole {
    /** Guest role – a customer who books and manages reservations. */
    GUEST,
    /** Receptionist role – hotel staff handling check-ins, bookings, and guest inquiries. */
    RECEPTIONIST,
    /** Manager role – administrative staff with full system access and management capabilities. */
    MANAGER
}
