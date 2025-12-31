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
 * Repository interface for Feedback entity
 * @author Arman Özcanli
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    // Find all feedback for a specific guest (via booking.guest)
    @Query("SELECT f FROM Feedback f WHERE f.booking.guest.id = :guestId")
    List<Feedback> findByGuestId(@Param("guestId") Long guestId);
    
    // Find all feedback for a specific booking
    @Query("SELECT f FROM Feedback f WHERE f.booking.id = :bookingId")
    List<Feedback> findByBookingId(@Param("bookingId") Long bookingId);
    
    // Find feedback by rating
    List<Feedback> findByRating(Integer rating);
    
    // Find feedback with rating greater than or equal to specified value
    List<Feedback> findByRatingGreaterThanEqual(Integer rating);
    
    // Find feedback by User (Guest) via booking.guest
    @Query("SELECT f FROM Feedback f WHERE f.booking.guest = :guest")
    List<Feedback> findByGuest(@Param("guest") User guest);
    
    // Find feedback by booking
    Optional<Feedback> findByBooking(Booking booking);
    
    // Find all feedback for a specific room category
    @Query("SELECT f FROM Feedback f WHERE f.booking.roomCategory.category_id = :categoryId")
    List<Feedback> findByRoomCategoryId(@Param("categoryId") Long categoryId);
}
