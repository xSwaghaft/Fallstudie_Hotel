package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice.PaymentMethod;
import com.hotel.booking.entity.Invoice.PaymentStatus;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing payment operations.
 * <p>
 * Handles all business logic related to payment processing, including:
 * </p>
 * <ul>
 *   <li>Retrieving payments by various criteria (booking, transaction reference, status, method)</li>
 *   <li>Processing new payments for bookings</li>
 *   <li>Converting payment method strings from UI to enum values</li>
 *   <li>Creating associated invoices when payments are processed</li>
 *   <li>Updating booking status based on payment status</li>
 * </ul>
 * <p>
 * Payment processing is atomic and transactional, ensuring that payment records, booking status
 * updates, and invoice creation are consistent. Supports multiple payment methods (Card, Bank
 * Transfer) and tracks different payment statuses (Pending, Paid, Failed, Refunded, Partial).
 * All operations are transactional to maintain data consistency.
 * </p>
 *
 * @author Arman Özcanli
 * @see Payment
 * @see PaymentRepository
 * @see InvoiceService
 * @see BookingService
 * @see Booking
 */
@Service
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final InvoiceService invoiceService;

    /**
     * Constructs a PaymentService with required dependencies.
     *
     * @param paymentRepository repository for payment persistence operations
     * @param bookingService service for booking management
     * @param invoiceService service for invoice management
     */
    public PaymentService(PaymentRepository paymentRepository, 
                         BookingService bookingService,
                         InvoiceService invoiceService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.invoiceService = invoiceService;
    }

    /**
     * Retrieves all payment transactions from the database.
     *
     * @return a list containing all payment transactions
     */
    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    /**
     * Retrieves all payments associated with a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return a list of payment transactions for the specified booking
     */
    @Transactional(readOnly = true)
    public List<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    /**
     * Retrieves a payment transaction by its unique identifier.
     *
     * @param id the payment ID
     * @return an Optional containing the payment if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    /**
     * Retrieves a payment by its external transaction reference.
     *
     * @param txRef the transaction reference from the payment provider
     * @return an Optional containing the payment if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionRef(String txRef) {
        return paymentRepository.findByTransactionRef(txRef);
    }

    /**
     * Saves or updates a payment transaction.
     *
     * @param payment the payment entity to save
     * @return the saved payment with generated ID if applicable
     */
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deleteById(Long id) {
        paymentRepository.deleteById(id);
    }
    
    /**
     * Maps UI payment method string to PaymentMethod enum.
     * 
     * @param uiMethod the payment method string from UI (can be null)
     * @return the corresponding PaymentMethod enum value (defaults to CARD)
     */
    public PaymentMethod mapPaymentMethod(String uiMethod) {
        if (uiMethod == null) {
            return PaymentMethod.CARD;
        }
        if ("Bank Transfer".equals(uiMethod) || "Banküberweisung".equals(uiMethod)) {
            return PaymentMethod.TRANSFER;
        }
        return PaymentMethod.CARD;
    }
    
    /**
     * Processes a payment for a booking.
     * Creates the payment, updates booking status if paid, and creates invoice if needed.
     * 
     * @param booking the booking to process payment for
     * @param amount the payment amount (if null, uses booking.getTotalPrice())
     * @param paymentMethodString the payment method string from UI
     * @param status the payment status (PAID or PENDING)
     * @return the created payment
     */
    @Transactional
    public Payment processPayment(Booking booking, BigDecimal amount, String paymentMethodString, PaymentStatus status) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        
        // Use provided amount or fall back to booking total price
        BigDecimal paymentAmount = amount != null ? amount : booking.getTotalPrice();
        if (paymentAmount == null) {
            throw new IllegalArgumentException("Payment amount cannot be null");
        }
        
        PaymentMethod paymentMethod = mapPaymentMethod(paymentMethodString);
        
        // Create and save payment
        Payment payment = new Payment();
        payment.setAmount(paymentAmount);
        payment.setStatus(status);
        payment.setPaidAt(status == PaymentStatus.PAID ? LocalDateTime.now() : null);
        payment.setMethod(paymentMethod);
        payment.setBooking(booking);
        Payment savedPayment = save(payment);
        
        // If payment is PAID, update booking status and create invoice
        if (status == PaymentStatus.PAID) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingService.save(booking);
            
            updateBookingStatusIfCompleted(booking);
            createInvoiceIfNotExists(booking, paymentMethod);
        }
        // If status is PENDING, booking status stays PENDING until payment is made
        
        return savedPayment;
    }
    
    /**
     * Processes payment for a booking: updates existing PENDING payment to the new status,
     * or creates a new payment if none exists.
     * Also updates booking status to CONFIRMED if payment is PAID.
     * 
     * @param bookingId the booking ID to process payment for
     * @param amount the payment amount (if null, uses booking.getTotalPrice())
     * @param paymentMethodString the payment method string from UI
     * @param status the payment status (PAID or PENDING)
     * @return the updated or created payment
     */
    @Transactional
    public Payment processPaymentForBooking(Long bookingId, BigDecimal amount, String paymentMethodString, PaymentStatus status) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }
        
        var bookingOpt = bookingService.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found for ID: " + bookingId);
        }
        Booking booking = bookingOpt.get();
        
        // Check for existing PENDING payment
        List<Payment> payments = findByBookingId(bookingId);
        Payment existingPendingPayment = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst()
                .orElse(null);
        
        if (existingPendingPayment != null) {
            // Update existing PENDING payment
            existingPendingPayment.setStatus(status);
            if (status == PaymentStatus.PAID) {
                existingPendingPayment.setPaidAt(LocalDateTime.now());
            }
            existingPendingPayment.setMethod(mapPaymentMethod(paymentMethodString));
            // Update amount if provided
            if (amount != null) {
                existingPendingPayment.setAmount(amount);
            }
            Payment savedPayment = save(existingPendingPayment);
            
            // If payment is PAID, update booking status and create invoice
            if (status == PaymentStatus.PAID && booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingService.save(booking);
                
                updateBookingStatusIfCompleted(booking);
                createInvoiceIfNotExists(booking, existingPendingPayment.getMethod());
            }
            
            return savedPayment;
        } else {
            // Create new payment if none exists (e.g., booking created by manager/receptionist)
            return processPayment(booking, amount, paymentMethodString, status);
        }
    }
    
    /**
     * Updates booking status to COMPLETED if check-out date is in the past.
     */
    private void updateBookingStatusIfCompleted(Booking booking) {
        if (booking.getCheckOutDate() != null && booking.getCheckOutDate().isBefore(java.time.LocalDate.now())) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingService.save(booking);
        }
    }
    
    /**
     * Creates an invoice for the booking if it doesn't already exist.
     */
    private void createInvoiceIfNotExists(Booking booking, PaymentMethod paymentMethod) {
        if (booking.getId() != null) {
            boolean invoiceExists = invoiceService.findByBookingId(booking.getId()).isPresent();
            if (!invoiceExists) {
                invoiceService.createInvoiceForBooking(booking, paymentMethod, PaymentStatus.PAID);
            }
        }
    }
}
