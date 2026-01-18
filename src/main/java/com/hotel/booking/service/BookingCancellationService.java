package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.repository.BookingCancellationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing booking cancellation operations.
 * <p>
 * Handles all business logic related to booking cancellations, including:
 * </p>
 * <ul>
 *   <li>Calculating cancellation fees based on days until check-in</li>
 *   <li>Processing complete cancellation workflows (booking status, payments, invoices)</li>
 *   <li>Saving cancellation records with automatic email notifications</li>
 *   <li>Retrieving cancellation history for bookings</li>
 * </ul>
 * <p>
 * Cancellation fees are calculated using a tiered system:
 * </p>
 * <ul>
 *   <li>30+ days before check-in: 0% fee (free cancellation)</li>
 *   <li>7-29 days before check-in: 20% fee</li>
 *   <li>1-6 days before check-in: 50% fee</li>
 *   <li>0 days (arrival day): 100% fee (no refund)</li>
 * </ul>
 * <p>
 * All cancellation operations are transactional to ensure data consistency.
 * </p>
 *
 * @author Ruslan Krause
 * @see BookingCancellation
 * @see BookingCancellationRepository
 */
@Service
public class BookingCancellationService {

    private static final Logger log = LoggerFactory.getLogger(BookingCancellationService.class);

    private final BookingCancellationRepository cancellationRepository;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    // Constructor injection of the required repositories
    public BookingCancellationService(
            BookingCancellationRepository cancellationRepository,
            BookingService bookingService,
            PaymentService paymentService,
            InvoiceService invoiceService,
            EmailService emailService) {
        this.cancellationRepository = cancellationRepository;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
        this.emailService = emailService;
    }

    /**
     * Retrieves all cancellation records from the database.
     *
     * @return a list containing all cancellation records
     */
    public List<BookingCancellation> getAll() {
        return cancellationRepository.findAll();
    }

    /**
     * Saves a cancellation record and sends a notification email to the guest.
     * <p>
     * If the cancellation is successfully saved and the booking has a valid guest email,
     * an automatic cancellation notification email is sent. Email failures are logged
     * but do not prevent the cancellation from being saved.
     * </p>
     *
     * @param cancellation the cancellation record to save
     * @return the saved cancellation record with generated ID if applicable
     */
    public BookingCancellation save(BookingCancellation cancellation) {
        BookingCancellation saved = cancellationRepository.save(cancellation);
        
        // Send cancellation email
        if (saved.getBooking() != null && saved.getBooking().getGuest() != null && saved.getBooking().getGuest().getEmail() != null) {
            try {
                emailService.sendBookingCancellation(saved.getBooking(), saved);
            } catch (Exception e) {
                // Log error but don't fail the cancellation save
                log.error("Failed to send booking cancellation email for booking {}", saved.getBooking().getId(), e);
            }
        }
        
        return saved;
    }

    /**
     * Retrieves the most recent cancellation record for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return an Optional containing the latest cancellation record, or empty if none exists
     */
    public java.util.Optional<BookingCancellation> findLatestByBookingId(Long bookingId) {
        return cancellationRepository.findTopByBookingIdOrderByCancelledAtDesc(bookingId);
    }

    /**
     * Retrieves a cancellation record by its unique identifier.
     *
     * @param id the cancellation ID
     * @return an Optional containing the cancellation if found, or empty if not found
     */
    public Optional<BookingCancellation> getById(Long id) {
        return cancellationRepository.findById(id);
    }

    /**
     * Deletes a cancellation record by its unique identifier.
     *
     * @param id the cancellation ID to delete
     */
    public void delete(Long id) {
        cancellationRepository.deleteById(id);
    }
    
    /**
     * Calculates the cancellation fee based on the days until check-in:
     * - 30+ days: 0% (free)
     * - 7-29 days: 20% fee
     * - 1-6 days: 50% fee
     * - 0 days (arrival day): 100% fee
     */
    public BigDecimal calculateCancellationFee(Booking booking, BigDecimal totalPrice) {
        if (booking.getCheckInDate() == null) {
            return BigDecimal.ZERO;
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime checkInAtStart = booking.getCheckInDate().atStartOfDay();
        long daysBefore = java.time.Duration.between(now, checkInAtStart).toDays();
        
        BigDecimal feePercentage;
        if (daysBefore >= 30) {
            feePercentage = BigDecimal.ZERO;  // Free
        } else if (daysBefore >= 7) {
            feePercentage = new BigDecimal("0.20");  // 20%
        } else if (daysBefore >= 1) {
            feePercentage = new BigDecimal("0.50");  // 50%
        } else {
            feePercentage = new BigDecimal("1.00");  // 100%
        }
        
        BigDecimal fee = totalPrice.multiply(feePercentage).setScale(2, java.math.RoundingMode.HALF_UP);
        return fee;
    }

    /**
     * Processes the complete cancellation logic: updates Booking and Payment Status.
     * This method is @Transactional to ensure that all changes are persisted.
     */
    @Transactional
    public void processCancellation(Booking booking, BookingCancellation cancellation, BigDecimal refundedAmount) {
        // 1. Booking to CANCELLED
        booking.setStatus(com.hotel.booking.entity.BookingStatus.CANCELLED);
        bookingService.save(booking);
        log.debug("Booking {} status changed to CANCELLED", booking.getId());
        
        // 2. Save BookingCancellation
        save(cancellation);
        log.debug("BookingCancellation saved for booking {}", booking.getId());

        final BigDecimal safeRefund = refundedAmount == null
            ? BigDecimal.ZERO
            : refundedAmount.max(BigDecimal.ZERO);

        // 3. Update Payment Status (REFUNDED / PARTIAL / PAID)
        List<Payment> payments = paymentService.findByBookingId(booking.getId());
        for (Payment p : payments) {
            if (p.getStatus() == Invoice.PaymentStatus.PAID) {
                BigDecimal paidAmount = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
                BigDecimal appliedRefund = safeRefund.min(paidAmount);

                if (appliedRefund.compareTo(BigDecimal.ZERO) == 0) {
                    // No refund (100% cancellation fee)
                    p.setStatus(Invoice.PaymentStatus.PAID);
                    p.setRefundedAmount(null);
                } else if (appliedRefund.compareTo(paidAmount) >= 0) {
                    // Full refund (appliedRefund equals or exceeds paidAmount)
                    p.setStatus(Invoice.PaymentStatus.REFUNDED);
                    p.setRefundedAmount(appliedRefund);
                } else {
                    // Partial refund (appliedRefund < paidAmount)
                    p.setStatus(Invoice.PaymentStatus.PARTIAL);
                    p.setRefundedAmount(appliedRefund);
                }

                paymentService.save(p);
                log.debug("Payment {} status changed to {}, refundAmount: {}", p.getId(), p.getStatus(), p.getRefundedAmount());
                break;
            }
        }

        // 4. Update Invoice Status (if exists)
        if (booking.getId() != null) {
            invoiceService.findByBookingId(booking.getId()).ifPresent(inv -> {
                BigDecimal invoiceAmount = inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO;
                BigDecimal appliedRefund = safeRefund.min(invoiceAmount);

                if (appliedRefund.compareTo(BigDecimal.ZERO) == 0) {
                    // No refund (100% cancellation fee)
                    inv.setInvoiceStatus(Invoice.PaymentStatus.PAID);
                } else if (appliedRefund.compareTo(invoiceAmount) >= 0) {
                    // Full refund (appliedRefund equals or exceeds invoiceAmount)
                    inv.setInvoiceStatus(Invoice.PaymentStatus.REFUNDED);
                } else {
                    // Partial refund (appliedRefund < invoiceAmount)
                    inv.setInvoiceStatus(Invoice.PaymentStatus.PARTIAL);
                }

                invoiceService.save(inv);
                log.debug("Invoice {} status changed to {}", inv.getId(), inv.getInvoiceStatus());
            });
        }
    }
}
