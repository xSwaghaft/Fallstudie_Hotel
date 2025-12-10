package com.hotel.booking.service;

import com.hotel.booking.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    List<Payment> findAll();
    List<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByTransactionRef(String txRef);
    Payment save(Payment payment);
    void deleteById(Long id);
}
