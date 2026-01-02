package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Invoice.PaymentMethod;
import com.hotel.booking.entity.Invoice.PaymentStatus;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.repository.BookingRepository;
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
    private final BookingRepository bookingRepository;
    private final InvoiceService invoiceService;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          InvoiceService invoiceService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
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
            // If PAID -> booking becomes CONFIRMED (also for MODIFIED bookings)
            booking.setStatus(com.hotel.booking.entity.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            updateBookingStatusIfCompleted(booking);
            createInvoiceIfNotExists(booking, paymentMethod);
        }
        // If status is PENDING, booking status stays PENDING/MODIFIED until payment is made

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

        var bookingOpt = bookingRepository.findById(bookingId);
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
            if (status == PaymentStatus.PAID
                    && (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.MODIFIED)) {

                booking.setStatus(com.hotel.booking.entity.BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

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
            booking.setStatus(com.hotel.booking.entity.BookingStatus.COMPLETED);
            bookingRepository.save(booking);
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

    /**
     * Reacts to booking price changes so payments stay in sync.
     * - If there is a PENDING payment for the booking, its amount is updated to the new total.
     * - If the price increased and no pending payment exists, a new PENDING payment for the delta is created.
     * Price decreases are left for manual handling (refunds) to avoid unintended refunds automatically.
     */
    public void handlePriceChange(Booking booking, BigDecimal oldPrice, BigDecimal newPrice) {
        if (booking == null || booking.getId() == null) return;
        if (oldPrice == null || newPrice == null) return;
        if (oldPrice.compareTo(newPrice) == 0) return;

        List<Payment> payments = findByBookingId(booking.getId());

        // Update first pending payment to reflect new total
        Payment pending = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst()
                .orElse(null);
        if (pending != null) {
            pending.setAmount(newPrice);
            save(pending);
            updateInvoiceAmount(booking, newPrice);
            return;
        }

        // If no pending exists but a single paid payment represents the full charge, align it
        Payment paid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .findFirst()
                .orElse(null);
        if (paid != null && payments.stream().allMatch(p -> p == paid || p.getStatus() != PaymentStatus.PAID)) {
            paid.setAmount(newPrice);
            paid.setRefundedAmount(null);
            save(paid);
            updateInvoiceAmount(booking, newPrice);
            return;
        }

        // If price increased and neither pending nor (single) paid payment was adjusted, create delta pending
        if (newPrice.compareTo(oldPrice) > 0) {
            BigDecimal delta = newPrice.subtract(oldPrice);
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(delta);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setMethod(PaymentMethod.CARD);
            save(payment);
            updateInvoiceAmount(booking, newPrice);
        }
        // Price decreases across multiple payments are left for manual refund handling.
    }

    private void updateInvoiceAmount(Booking booking, BigDecimal newPrice) {
        if (booking == null || booking.getId() == null || newPrice == null) {
            return;
        }

        invoiceService.findByBookingId(booking.getId()).ifPresent(inv -> {
            if (newPrice.compareTo(inv.getAmount()) == 0) {
                return;
            }
            inv.setAmount(newPrice);
            invoiceService.save(inv);
        });
    }
}
