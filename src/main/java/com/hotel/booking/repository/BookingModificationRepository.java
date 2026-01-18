package com.hotel.booking.repository;

import com.hotel.booking.entity.BookingModification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BookingModification entity persistence operations.
 * <p>
 * Provides database access methods for booking modification records. Supports querying
 * modifications by booking ID, handler ID, and retrieving the latest modification for a booking.
 * Extends JpaRepository to provide standard CRUD operations.
 * </p>
 *
 * @author Ruslan Krause
 * @see BookingModification
 * @see BookingModificationService
 */
@Repository
public interface BookingModificationRepository extends JpaRepository<BookingModification, Long> {

    /**
     * Finds all modification records for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return a list of modification records for the specified booking
     */
    List<BookingModification> findByBookingId(Long bookingId);

    /**
     * Finds all modification records handled by a specific user.
     *
     * @param handledById the ID of the user who performed the modifications
     * @return a list of modification records handled by the specified user
     */
    List<BookingModification> findByHandledById(Long handledById);

    /**
     * Finds the most recent modification record for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return an Optional containing the latest modification record, or empty if none exists
     */
    Optional<BookingModification> findTopByBookingIdOrderByModifiedAtDesc(Long bookingId);
}