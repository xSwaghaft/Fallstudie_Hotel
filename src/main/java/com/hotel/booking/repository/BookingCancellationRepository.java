package com.hotel.booking.repository;

import com.hotel.booking.entity.BookingCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BookingCancellation entity persistence operations.
 * <p>
 * Provides database access methods for booking cancellation records. Supports querying
 * cancellations by booking ID, handler ID, and retrieving the latest cancellation for a booking.
 * Extends JpaRepository to provide standard CRUD operations.
 * </p>
 *
 * @author Ruslan Krause
 * @see BookingCancellation
 * @see BookingCancellationService
 */
@Repository
public interface BookingCancellationRepository extends JpaRepository<BookingCancellation, Long> {

    /**
     * Finds all cancellation records for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return a list of cancellation records for the specified booking
     */
    List<BookingCancellation> findByBookingId(Long bookingId);

    /**
     * Finds all cancellation records handled by a specific user.
     *
     * @param handledById the ID of the user who handled the cancellations
     * @return a list of cancellation records handled by the specified user
     */
    List<BookingCancellation> findByHandledById(Long handledById);

    /**
     * Finds the most recent cancellation record for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return an Optional containing the latest cancellation record, or empty if none exists
     */
    Optional<BookingCancellation> findTopByBookingIdOrderByCancelledAtDesc(Long bookingId);
}