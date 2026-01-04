package com.hotel.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.User;

/**
 * Repository interface for Feedback entity persistence operations.
 * <p>
 * Provides database access methods for feedback and review data. Supports querying feedback
 * by guest, booking, rating, and room category. Extends JpaRepository to provide standard
 * CRUD operations.
 * </p>
 *
 * @author Arman Özcanli
 * @see Feedback
 * @see FeedbackService
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * Finds all feedback submitted by a specific guest.
     *
     * @param guestId the ID of the guest
     * @return a list of all feedback entries from the specified guest
     */
    // Find all feedback for a specific guest (via booking.guest)
    @Query("SELECT f FROM Feedback f WHERE f.booking.guest.id = :guestId")
    List<Feedback> findByGuestId(@Param("guestId") Long guestId);
    
    /**
     * Finds all feedback for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return a list of feedback entries associated with the specified booking
     */
    // Find all feedback for a specific booking
    @Query("SELECT f FROM Feedback f WHERE f.booking.id = :bookingId")
    List<Feedback> findByBookingId(@Param("bookingId") Long bookingId);
    
    /**
     * Finds all feedback entries with a specific star rating.
     *
     * @param rating the rating value (1-5)
     * @return a list of feedback entries with the specified rating
     */
    // Find feedback by rating
    List<Feedback> findByRating(Integer rating);
    
    /**
     * Finds all feedback entries with a rating greater than or equal to the specified value.
     *
     * @param rating the minimum rating threshold
     * @return a list of feedback entries with rating >= specified value
     */
    // Find feedback with rating greater than or equal to specified value
    List<Feedback> findByRatingGreaterThanEqual(Integer rating);
    
    /**
     * Finds all feedback submitted by a specific guest user.
     *
     * @param guest the guest user entity
     * @return a list of all feedback entries from the specified guest
     */
    // Find feedback by User (Guest) via booking.guest
    @Query("SELECT f FROM Feedback f WHERE f.booking.guest = :guest")
    List<Feedback> findByGuest(@Param("guest") User guest);
    
    /**
     * Finds the feedback entry for a specific booking.
     *
     * @param booking the booking entity
     * @return an Optional containing the feedback if it exists, or empty if no feedback found
     */
    // Find feedback by booking
    Optional<Feedback> findByBooking(Booking booking);
    
    /**
     * Finds all feedback for bookings associated with a specific room category.
     *
     * @param categoryId the ID of the room category
     * @return a list of feedback entries for bookings in the specified category
     */
    // Find all feedback for a specific room category
    @Query("SELECT f FROM Feedback f WHERE f.booking.roomCategory.category_id = :categoryId")
    List<Feedback> findByRoomCategoryId(@Param("categoryId") Long categoryId);
}
