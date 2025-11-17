package com.hotel.booking.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.booking.entity.Payment;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.repository.PaymentRepository;
import com.hotel.booking.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;

    public PaymentServiceImpl(PaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Payment> findAll() {
        return repository.findAll();
    }

    @Override
    public Payment findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Override
    public Payment create(Payment payment) {
        return repository.save(payment);
    }

    @Override
    public Payment update(Long id, Payment payment) {
        Payment existing = findById(id);
        existing.setAmount(payment.getAmount());
        existing.setMethod(payment.getMethod());
        existing.setStatus(payment.getStatus());
        existing.setTransactionRef(payment.getTransactionRef());
        existing.setPaidAt(payment.getPaidAt());
        existing.setBooking(payment.getBooking());
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Payment", "id", id);
        }
        repository.deleteById(id);
    }
}
