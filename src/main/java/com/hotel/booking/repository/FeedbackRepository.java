package com.hotel.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.Guest;

/**
 * Repository interface for Feedback entity
 *
 * Provides derived query methods for the Feedback entity. Uses both
 * id-based (findByGuestId/findByBookingId) and entity-based (findByGuest/findByBooking)
 * signatures to allow different callers to use whichever is more convenient.
 *
 * @author Arman Özcanli
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Find all feedback for a specific guest (by FK value)
    List<Feedback> findByGuestId(Long guestId);

    // Find all feedback for a specific booking (by FK value)
    List<Feedback> findByBookingId(Long bookingId);

    // Find feedback by rating
    List<Feedback> findByRating(Integer rating);

    // Find feedback with rating greater than or equal to specified value
    List<Feedback> findByRatingGreaterThanEqual(Integer rating);

    // Find feedback by guest entity
    List<Feedback> findByGuest(Guest guest);

    // Find feedback by booking entity
    List<Feedback> findByBooking(Booking booking);
}
