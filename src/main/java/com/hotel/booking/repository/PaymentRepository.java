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
 * <p>
 * Provides database access methods for payment transaction data. Supports querying payments
 * by booking, transaction reference, status, method, and date range. Includes methods for
 * finding payments by combined criteria (status and method). Extends JpaRepository to provide
 * standard CRUD operations.
 * </p>
 *
 * @author Arman Özcanli
 * @see Payment
 * @see PaymentService
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Finds all payments associated with a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return a list of all payment transactions for the specified booking
     */
    // Find all payments for a specific booking (using temporary bookingId field)
    List<Payment> findByBookingId(Long bookingId);
    
    /**
     * Finds a payment by its external transaction reference.
     *
     * @param transactionRef the transaction reference from the payment provider
     * @return an Optional containing the payment if found
     */
    // Find payment by transaction reference
    Optional<Payment> findByTransactionRef(String transactionRef);
    
    /**
     * Finds all payments with a specific payment status.
     *
     * @param status the payment status to filter by
     * @return a list of payments with the specified status
     */
    // Find payments by status
    List<Payment> findByStatus(Invoice.PaymentStatus status);
    
    /**
     * Finds all payments made using a specific payment method.
     *
     * @param method the payment method to filter by
     * @return a list of payments made using the specified method
     */
    // Find payments by payment method
    List<Payment> findByMethod(Invoice.PaymentMethod method);
    
    /**
     * Finds all payments processed within a specified date range.
     *
     * @param start the start date (inclusive)
     * @param end the end date (inclusive)
     * @return a list of payments processed between the specified dates
     */
    // Find payments paid between dates
    List<Payment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Finds all payments matching both a specific status and payment method.
     *
     * @param status the payment status to filter by
     * @param method the payment method to filter by
     * @return a list of payments with the specified status and method
     */
    // Find all payments by status and method
    List<Payment> findByStatusAndMethod(Invoice.PaymentStatus status, Invoice.PaymentMethod method);
    
    // TODO: Activate when Booking entity is created
    // List<Payment> findByBooking(Booking booking);
}
