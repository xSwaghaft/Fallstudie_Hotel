package com.hotel.booking.service;

import com.hotel.booking.entity.Feedback;

import java.util.List;
import java.util.Optional;

public interface FeedbackService {
    List<Feedback> findAll();
    List<Feedback> findByGuestId(Long guestId);
    List<Feedback> findByBookingId(Long bookingId);
    Optional<Feedback> findById(Long id);
    Feedback save(Feedback feedback);
    void deleteById(Long id);
}
