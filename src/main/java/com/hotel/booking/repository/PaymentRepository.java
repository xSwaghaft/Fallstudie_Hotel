package com.hotel.booking.repository;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity persistence operations.
 * 
 * This repository provides database access methods for payment transaction data.
 * It supports querying payments by booking, transaction reference, status, method, and date range.
 * Includes methods for finding payments by combined criteria (status and method).
 * Extends JpaRepository to provide standard CRUD operations.
 * 
 * @author Arman Özcanli
 * @see Payment
 * @see PaymentService
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find all payments for a specific booking (using temporary bookingId field)
    List<Payment> findByBookingId(Long bookingId);
    
    // Find payment by transaction reference
    Optional<Payment> findByTransactionRef(String transactionRef);
    
    // Find payments by status
    List<Payment> findByStatus(Invoice.PaymentStatus status);
    
    // Find payments by payment method
    List<Payment> findByMethod(Invoice.PaymentMethod method);
    
    // Find payments paid between dates
    List<Payment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find all payments by status and method
    List<Payment> findByStatusAndMethod(Invoice.PaymentStatus status, Invoice.PaymentMethod method);
}
