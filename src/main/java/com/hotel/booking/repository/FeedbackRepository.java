package com.hotel.booking.repository;

<<<<<<< HEAD
import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.Guest;
import com.hotel.booking.entity.Booking;
=======
import java.util.List;
import java.util.Optional;

>>>>>>> 3f2601670b17036cdc8f0536c74f3a281afbb46c
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.Guest;

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
    
<<<<<<< HEAD
    // Find feedback by guest entity
    List<Feedback> findByGuest(Guest guest);
    
    // Find feedback by booking entity
    List<Feedback> findByBooking(Booking booking);
=======
    // TODO: Activate when Guest entity is created
    List<Feedback> findByGuest(Guest guest);
    
    // TODO: Activate when Booking entity is created
    Optional<Feedback> findByBooking(Booking booking);
>>>>>>> 3f2601670b17036cdc8f0536c74f3a281afbb46c
}
