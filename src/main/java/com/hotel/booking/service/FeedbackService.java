package com.hotel.booking.service;

import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, BookingRepository bookingRepository) {
        this.feedbackRepository = feedbackRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<Feedback> findAll() {
        return feedbackRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Feedback> findByGuestId(Long guestId) {
        return feedbackRepository.findByGuestId(guestId);
    }

    @Transactional(readOnly = true)
    public List<Feedback> findByBookingId(Long bookingId) {
        return feedbackRepository.findByBookingId(bookingId);
    }

    @Transactional(readOnly = true)
    public Optional<Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    public Feedback save(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

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
