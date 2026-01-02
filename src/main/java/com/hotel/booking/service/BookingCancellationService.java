// Ruslan Krause
package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.repository.BookingCancellationRepository;
import com.hotel.booking.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookingCancellationService {

    private static final Logger log = LoggerFactory.getLogger(BookingCancellationService.class);

    private final BookingCancellationRepository cancellationRepository;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final EmailService emailService;
    private final RoomService roomService;

    @Autowired
    public BookingCancellationService(
            BookingCancellationRepository cancellationRepository,
            BookingService bookingService,
            PaymentService paymentService,
            InvoiceService invoiceService,
            EmailService emailService,
            RoomService roomService) {
        this.cancellationRepository = cancellationRepository;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
        this.emailService = emailService;
        this.roomService = roomService;
    }

    public BookingCancellationService(
            BookingCancellationRepository cancellationRepository,
            BookingService bookingService,
            PaymentService paymentService,
            InvoiceService invoiceService,
            EmailService emailService) {
        this(cancellationRepository, bookingService, paymentService, invoiceService, emailService, null);
    }

    public List<BookingCancellation> getAll() {
        return cancellationRepository.findAll();
    }

    public BookingCancellation save(BookingCancellation cancellation) {
        BookingCancellation saved = cancellationRepository.save(cancellation);

        if (saved.getBooking() != null && saved.getBooking().getGuest() != null && saved.getBooking().getGuest().getEmail() != null) {
            try {
                emailService.sendBookingCancellation(saved.getBooking(), saved);
            } catch (Exception e) {
                log.error("Failed to send booking cancellation email for booking {}", saved.getBooking().getId(), e);
            }
        }

        return saved;
    }

    public java.util.Optional<BookingCancellation> findLatestByBookingId(Long bookingId) {
        return cancellationRepository.findTopByBookingIdOrderByCancelledAtDesc(bookingId);
    }

    public Optional<BookingCancellation> getById(Long id) {
        return cancellationRepository.findById(id);
    }

    public void delete(Long id) {
        cancellationRepository.deleteById(id);
    }

    public BigDecimal calculateCancellationFee(Booking booking, BigDecimal totalPrice) {
        if (booking.getCheckInDate() == null) {
            return BigDecimal.ZERO;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime checkInAtStart = booking.getCheckInDate().atStartOfDay();
        long daysBefore = java.time.Duration.between(now, checkInAtStart).toDays();

        BigDecimal feePercentage;
        if (daysBefore >= 30) {
            feePercentage = BigDecimal.ZERO;
        } else if (daysBefore >= 7) {
            feePercentage = new BigDecimal("0.20");
        } else if (daysBefore >= 1) {
            feePercentage = new BigDecimal("0.50");
        } else {
            feePercentage = new BigDecimal("1.00");
        }

        return totalPrice.multiply(feePercentage).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Transactional
    public void processCancellation(Booking booking, BookingCancellation cancellation, BigDecimal refundedAmount) {

        // ✅ NEW: Prevent cancelling bookings in the past (already finished)
        if (booking.getCheckOutDate() != null && booking.getCheckOutDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel a booking that is already in the past.");
        }

        // 1. Booking zu CANCELLED
        booking.setStatus(com.hotel.booking.entity.BookingStatus.CANCELLED);
        bookingService.save(booking);
        log.debug("Booking {} status changed to CANCELLED", booking.getId());

        // 2. BookingCancellation speichern
        save(cancellation);
        log.debug("BookingCancellation saved for booking {}", booking.getId());

        final BigDecimal safeRefund = refundedAmount == null
                ? BigDecimal.ZERO
                : refundedAmount.max(BigDecimal.ZERO);

        // 3. Payment Status anpassen (REFUNDED / PARTIAL / PAID)
        List<Payment> payments = paymentService.findByBookingId(booking.getId());
        for (Payment p : payments) {
            if (p.getStatus() == Invoice.PaymentStatus.PAID) {
                BigDecimal paidAmount = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
                BigDecimal appliedRefund = safeRefund.min(paidAmount);

                if (appliedRefund.compareTo(BigDecimal.ZERO) == 0) {
                    p.setStatus(Invoice.PaymentStatus.PAID);
                    p.setRefundedAmount(null);
                } else if (appliedRefund.compareTo(paidAmount) >= 0) {
                    p.setStatus(Invoice.PaymentStatus.REFUNDED);
                    p.setRefundedAmount(appliedRefund);
                } else {
                    p.setStatus(Invoice.PaymentStatus.PARTIAL);
                    p.setRefundedAmount(appliedRefund);
                }

                paymentService.save(p);
                log.debug("Payment {} status changed to {}, refundAmount: {}", p.getId(), p.getStatus(), p.getRefundedAmount());
                break;
            }
        }

        // 4. Invoice Status anpassen (falls vorhanden)
        if (booking.getId() != null) {
            invoiceService.findByBookingId(booking.getId()).ifPresent(inv -> {
                BigDecimal invoiceAmount = inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO;
                BigDecimal appliedRefund = safeRefund.min(invoiceAmount);

                if (appliedRefund.compareTo(BigDecimal.ZERO) == 0) {
                    inv.setInvoiceStatus(Invoice.PaymentStatus.PAID);
                } else if (appliedRefund.compareTo(invoiceAmount) >= 0) {
                    inv.setInvoiceStatus(Invoice.PaymentStatus.REFUNDED);
                } else {
                    inv.setInvoiceStatus(Invoice.PaymentStatus.PARTIAL);
                }

                invoiceService.save(inv);
                log.debug("Invoice {} status changed to {}", inv.getId(), inv.getInvoiceStatus());
            });

            try {
                if (roomService != null && booking.getRoom() != null && booking.getRoom().getId() != null) {
                    roomService.releaseRoomIfFree(booking.getRoom().getId(), booking.getCheckInDate(), booking.getCheckOutDate());
                }
            } catch (Exception ex) {
                System.err.println("Failed to release room after cancellation: " + ex.getMessage());
            }
        }
    }
}
