// Ruslan Krause
package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.repository.BookingCancellationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookingCancellationService {

    private final BookingCancellationRepository cancellationRepository;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    // Konstruktor-Injektion der benötigten Repositories
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

    // Gibt eine Liste aller BookingCancellation-Objekte aus der Datenbank zurück
    public List<BookingCancellation> getAll() {
        return cancellationRepository.findAll();
    }

    // Speichert einen BookingCancellation-Eintrag
    public BookingCancellation save(BookingCancellation cancellation) {
        BookingCancellation saved = cancellationRepository.save(cancellation);
        
        // Send cancellation email
        if (saved.getBooking() != null && saved.getBooking().getGuest() != null && saved.getBooking().getGuest().getEmail() != null) {
            try {
                emailService.sendBookingCancellation(saved.getBooking(), saved);
            } catch (Exception e) {
                // Log error but don't fail the cancellation save
                System.err.println("Failed to send booking cancellation email: " + e.getMessage());
            }
        }
        
        return saved;
    }

    // Liefert das zuletzt erzeugte Cancellation-Objekt für eine Booking (optional)
    public java.util.Optional<BookingCancellation> findLatestByBookingId(Long bookingId) {
        return cancellationRepository.findTopByBookingIdOrderByCancelledAtDesc(bookingId);
    }

    // Sucht eine BookingCancellation anhand der ID und gibt sie als Optional zurück
    public Optional<BookingCancellation> getById(Long id) {
        return cancellationRepository.findById(id);
    }

    // Löscht eine BookingCancellation anhand der ID aus der Datenbank
    public void delete(Long id) {
        cancellationRepository.deleteById(id);
    }
    
    /**
     * Berechnet die Stornierungsgebühr basierend auf den Tagen bis zum Check-in:
     * - 30+ Tage: 0% (kostenlos)
     * - 7-29 Tage: 20% Gebühr
     * - 1-6 Tage: 50% Gebühr
     * - 0 Tage (Anreisetag): 100% Gebühr
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
            feePercentage = BigDecimal.ZERO;  // Kostenlos
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
     * Verarbeitet die komplette Stornierungslogik: aktualisiert Booking und Payment Status.
     * Diese Methode ist @Transactional um sicherzustellen, dass alle Änderungen gepersistet werden.
     */
    @Transactional
    public void processCancellation(Booking booking, BookingCancellation cancellation, BigDecimal refundedAmount) {
        // 1. Booking zu CANCELLED
        booking.setStatus(com.hotel.booking.entity.BookingStatus.CANCELLED);
        bookingService.save(booking);
        System.out.println("DEBUG: Booking " + booking.getId() + " status changed to CANCELLED");
        
        // 2. BookingCancellation speichern
        save(cancellation);
        System.out.println("DEBUG: BookingCancellation saved for booking " + booking.getId());

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
                    // No refund (100% cancellation fee)
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
                System.out.println("DEBUG: Payment " + p.getId() + " status changed to " + p.getStatus() + ", refundAmount: " + p.getRefundedAmount());
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
                System.out.println("DEBUG: Invoice " + inv.getId() + " status changed to " + inv.getInvoiceStatus());
            });
        }
    }
}
