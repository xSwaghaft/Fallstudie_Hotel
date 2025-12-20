package com.hotel.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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
    
    // Find all feedback for a specific guest (using temporary guestId field)
    List<Feedback> findByGuestId(Long guestId);
    
    // Find all feedback for a specific booking (using temporary bookingId field)
    List<Feedback> findByBookingId(Long bookingId);
    
    // Find feedback by rating
    List<Feedback> findByRating(Integer rating);
    
    // Find feedback with rating greater than or equal to specified value
    List<Feedback> findByRatingGreaterThanEqual(Integer rating);
    
    // Find feedback by User (Guest)
    List<Feedback> findByGuest(User guest);
    
    // Find feedback by booking
    Optional<Feedback> findByBooking(Booking booking);
}
