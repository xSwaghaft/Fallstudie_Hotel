package com.hotel.booking.service;

import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing feedback and review operations.
 * <p>
 * Handles business logic for feedback management including:
 * </p>
 * <ul>
 *   <li>Retrieving feedback by guest, booking, or room category</li>
 *   <li>Creating and saving new feedback entries</li>
 *   <li>Deleting feedback while maintaining database referential integrity</li>
 *   <li>Filtering feedback by rating</li>
 * </ul>
 * <p>
 * Ensures that when feedback is deleted, the associated booking relationship is properly
 * cleaned up to prevent orphaned data. All operations are transactional to maintain data
 * consistency.
 * </p>
 *
 * @author Arman Özcanli
 * @see Feedback
 * @see FeedbackRepository
 * @see BookingRepository
 */
@Service
@Transactional
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;

    /**
     * Constructs a FeedbackService with required dependencies.
     *
     * @param feedbackRepository repository for feedback persistence operations
     * @param bookingRepository repository for booking persistence operations
     */
    public FeedbackService(FeedbackRepository feedbackRepository, BookingRepository bookingRepository) {
        this.feedbackRepository = feedbackRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Retrieves all feedback entries from the database.
     *
     * @return a list containing all feedback entries
     */
    @Transactional(readOnly = true)
    public List<Feedback> findAll() {
        return feedbackRepository.findAll();
    }

    /**
     * Retrieves all feedback submitted by a specific guest.
     *
     * @param guestId the ID of the guest
     * @return a list of feedback entries from the specified guest
     */
    @Transactional(readOnly = true)
    public List<Feedback> findByGuestId(Long guestId) {
        return feedbackRepository.findByGuestId(guestId);
    }

    /**
     * Retrieves all feedback associated with a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return a list of feedback entries for the specified booking
     */
    @Transactional(readOnly = true)
    public List<Feedback> findByBookingId(Long bookingId) {
        return feedbackRepository.findByBookingId(bookingId);
    }
    
    /**
     * Retrieves all feedback for bookings in a specific room category.
     *
     * @param categoryId the ID of the room category
     * @return a list of feedback entries for bookings in the specified category
     */
    @Transactional(readOnly = true)
    public List<Feedback> findByRoomCategoryId(Long categoryId) {
        return feedbackRepository.findByRoomCategoryId(categoryId);
    }

    /**
     * Retrieves a feedback entry by its unique identifier.
     *
     * @param id the feedback ID
     * @return an Optional containing the feedback if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    /**
     * Saves or updates a feedback entry.
     *
     * @param feedback the feedback entity to save
     * @return the saved feedback entity with generated ID if applicable
     */
    public Feedback save(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    /**
     * Deletes a feedback entry by its unique identifier.
     * <p>
     * Ensures proper cleanup of associations to prevent orphaned data. Breaks the relationship
     * from the booking side first to avoid TransientObjectException during Hibernate flush.
     * </p>
     *
     * @param id the feedback ID to delete
     */
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }

        Feedback feedback = feedbackRepository.findById(id).orElse(null);
        if (feedback == null) {
            return;
        }

        // Break association from the persistent Booking side first.
        // Otherwise Hibernate can throw TransientObjectException during flush when a managed Booking
        // references a (now deleted / transient) Feedback instance.
        Booking booking = feedback.getBooking();
        if (booking != null) {
            booking.setFeedback(null);
            // Persist the Booking change without running BookingService.save() side effects.
            bookingRepository.save(booking);
        }

        // Detach owning-side reference as well (defensive; not strictly required for delete).
        feedback.setBooking(null);

        feedbackRepository.delete(feedback);
    }
}
