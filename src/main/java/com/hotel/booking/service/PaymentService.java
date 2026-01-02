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
 * 
 * This service handles all business logic related to payment processing, including:
 * - Retrieving payments by various criteria (booking, transaction reference, status, method)
 * - Processing new payments for bookings
 * - Converting payment method strings from UI to enum values
 * - Creating associated invoices when payments are processed
 * - Updating booking status based on payment status
 * 
 * Payment processing is atomic and transactional, ensuring that payment records,
 * booking status updates, and invoice creation are consistent.
 * 
 * The service supports multiple payment methods (Card, Bank Transfer) and tracks
 * different payment statuses (Pending, Paid, Failed, Refunded, Partial).
 * 
 * All operations are transactional to maintain data consistency.
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

    public PaymentService(PaymentRepository paymentRepository,
                          BookingService bookingService,
                          InvoiceService invoiceService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.invoiceService = invoiceService;
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionRef(String txRef) {
        return paymentRepository.findByTransactionRef(txRef);
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deleteById(Long id) {
        paymentRepository.deleteById(id);
    }

    public PaymentMethod mapPaymentMethod(String uiMethod) {
        if (uiMethod == null) return PaymentMethod.CARD;
        if ("Bank Transfer".equals(uiMethod) || "Banküberweisung".equals(uiMethod)) {
            return PaymentMethod.TRANSFER;
        }
        return PaymentMethod.CARD;
    }

    @Transactional
    public Payment processPayment(Booking booking, BigDecimal amount, String paymentMethodString, PaymentStatus status) {
        if (booking == null) throw new IllegalArgumentException("Booking cannot be null");

        BigDecimal paymentAmount = amount != null ? amount : booking.getTotalPrice();
        if (paymentAmount == null) throw new IllegalArgumentException("Payment amount cannot be null");

        PaymentMethod paymentMethod = mapPaymentMethod(paymentMethodString);

        Payment payment = new Payment();
        payment.setAmount(paymentAmount);
        payment.setStatus(status);
        payment.setPaidAt(status == PaymentStatus.PAID ? LocalDateTime.now() : null);
        payment.setMethod(paymentMethod);
        payment.setBooking(booking);

        Payment savedPayment = save(payment);

        if (status == PaymentStatus.PAID) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingService.save(booking);

            updateBookingStatusIfCompleted(booking);
            createInvoiceIfNotExists(booking, paymentMethod);
        }

        return savedPayment;
    }

    @Transactional
    public Payment processPaymentForBooking(Long bookingId, BigDecimal amount, String paymentMethodString, PaymentStatus status) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");

        var bookingOpt = bookingService.findById(bookingId);
        if (bookingOpt.isEmpty()) throw new IllegalArgumentException("Booking not found for ID: " + bookingId);

        Booking booking = bookingOpt.get();

        // ✅ always use booking price as fallback if amount not provided
        BigDecimal effectiveAmount = amount != null ? amount : booking.getTotalPrice();
        if (effectiveAmount == null) throw new IllegalArgumentException("Payment amount cannot be null");

        List<Payment> payments = findByBookingId(bookingId);
        Payment existingPendingPayment = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (existingPendingPayment != null) {
            existingPendingPayment.setStatus(status);
            existingPendingPayment.setMethod(mapPaymentMethod(paymentMethodString));

            // ✅ Sync amount to current booking price (or explicit amount)
            existingPendingPayment.setAmount(effectiveAmount);

            if (status == PaymentStatus.PAID) {
                existingPendingPayment.setPaidAt(LocalDateTime.now());
            }

            Payment savedPayment = save(existingPendingPayment);

            // ✅ allow PENDING or MODIFIED -> CONFIRMED when paid
            if (status == PaymentStatus.PAID
                    && (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.MODIFIED)) {

                booking.setStatus(BookingStatus.CONFIRMED);
                bookingService.save(booking);

                updateBookingStatusIfCompleted(booking);

                // If an invoice exists, it should be updated to PAID + amount as well (see note below)
                createInvoiceIfNotExists(booking, existingPendingPayment.getMethod());
            }

            return savedPayment;
        }

        return processPayment(booking, effectiveAmount, paymentMethodString, status);
    }

    private void updateBookingStatusIfCompleted(Booking booking) {
        if (booking.getCheckOutDate() != null && booking.getCheckOutDate().isBefore(java.time.LocalDate.now())) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingService.save(booking);
        }
    }

    private void createInvoiceIfNotExists(Booking booking, PaymentMethod paymentMethod) {
        if (booking.getId() != null) {
            boolean invoiceExists = invoiceService.findByBookingId(booking.getId()).isPresent();
            if (!invoiceExists) {
                invoiceService.createInvoiceForBooking(booking, paymentMethod, PaymentStatus.PAID);
            }
        }
    }
}
