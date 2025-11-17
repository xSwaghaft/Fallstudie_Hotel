package com.hotel.booking.service;

import java.util.List;

import com.hotel.booking.entity.Payment;

public interface PaymentService {
    List<Payment> findAll();
    Payment findById(Long id);
    Payment create(Payment payment);
    Payment update(Long id, Payment payment);
    void delete(Long id);
}
