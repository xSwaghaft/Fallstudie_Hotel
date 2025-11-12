package com.hotel.booking.repository;

import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.Payment.PaymentMethod;
import com.hotel.booking.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity
 * @author Arman Özcanli
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find all payments for a specific booking (using temporary bookingId field)
    List<Payment> findByBookingId(Long bookingId);
    
    // Find payment by transaction reference
    Optional<Payment> findByTransactionRef(String transactionRef);
    
    // Find payments by status
    List<Payment> findByStatus(PaymentStatus status);
    
    // Find payments by payment method
    List<Payment> findByMethod(PaymentMethod method);
    
    // Find payments paid between dates
    List<Payment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find all payments by status and method
    List<Payment> findByStatusAndMethod(PaymentStatus status, PaymentMethod method);
    
    // TODO: Activate when Booking entity is created
    // List<Payment> findByBooking(Booking booking);
}
