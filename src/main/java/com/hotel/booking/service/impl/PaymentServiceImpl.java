package com.hotel.booking.service.impl;

import com.hotel.booking.entity.Payment;
import com.hotel.booking.repository.PaymentRepository;
import com.hotel.booking.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;

	public PaymentServiceImpl(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Payment> findAll() {
		return paymentRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Payment> findByBookingId(Long bookingId) {
		return paymentRepository.findByBookingId(bookingId);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Payment> findById(Long id) {
		return paymentRepository.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Payment> findByTransactionRef(String txRef) {
		return paymentRepository.findByTransactionRef(txRef);
	}

	@Override
	public Payment save(Payment payment) {
		return paymentRepository.save(payment);
	}

	@Override
	public void deleteById(Long id) {
		paymentRepository.deleteById(id);
	}
}

