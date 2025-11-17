package com.hotel.booking.service;

import java.util.List;

import com.hotel.booking.entity.Feedback;

public interface FeedbackService {
    List<Feedback> findAll();
    Feedback findById(Long id);
    Feedback create(Feedback feedback);
    Feedback update(Long id, Feedback feedback);
    void delete(Long id);
}
