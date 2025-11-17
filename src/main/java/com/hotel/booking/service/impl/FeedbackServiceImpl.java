package com.hotel.booking.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.booking.entity.Feedback;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.repository.FeedbackRepository;
import com.hotel.booking.service.FeedbackService;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository repository;

    public FeedbackServiceImpl(FeedbackRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Feedback> findAll() {
        return repository.findAll();
    }

    @Override
    public Feedback findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));
    }

    @Override
    public Feedback create(Feedback feedback) {
        return repository.save(feedback);
    }

    @Override
    public Feedback update(Long id, Feedback feedback) {
        Feedback existing = findById(id);
        existing.setRating(feedback.getRating());
        existing.setComment(feedback.getComment());
        existing.setCreatedAt(feedback.getCreatedAt());
        existing.setBooking(feedback.getBooking());
        existing.setGuest(feedback.getGuest());
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Feedback", "id", id);
        }
        repository.deleteById(id);
    }
}
