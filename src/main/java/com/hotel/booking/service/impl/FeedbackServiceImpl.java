package com.hotel.booking.service.impl;

import com.hotel.booking.entity.Feedback;
import com.hotel.booking.repository.FeedbackRepository;
import com.hotel.booking.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

	private final FeedbackRepository feedbackRepository;

	public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Feedback> findAll() {
		return feedbackRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Feedback> findByGuestId(Long guestId) {
		return feedbackRepository.findByGuestId(guestId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Feedback> findByBookingId(Long bookingId) {
		return feedbackRepository.findByBookingId(bookingId);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Feedback> findById(Long id) {
		return feedbackRepository.findById(id);
	}

	@Override
	public Feedback save(Feedback feedback) {
		return feedbackRepository.save(feedback);
	}

	@Override
	public void deleteById(Long id) {
		feedbackRepository.deleteById(id);
	}
}

