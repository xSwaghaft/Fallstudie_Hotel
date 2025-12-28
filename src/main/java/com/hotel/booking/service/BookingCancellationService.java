// Ruslan Krause
package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingCancellationRepository;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookingCancellationService {

    private final BookingCancellationRepository cancellationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    // Konstruktor-Injektion der benötigten Repositories
    public BookingCancellationService(
            BookingCancellationRepository cancellationRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            BookingService bookingService,
            PaymentService paymentService,
            InvoiceService invoiceService,
            EmailService emailService) {
        this.cancellationRepository = cancellationRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
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
        cancellationRepository.save(cancellation);
        System.out.println("DEBUG: BookingCancellation saved for booking " + booking.getId());
        
        // 3. Payment zu REFUNDED mit refundedAmount
        List<Payment> payments = paymentService.findByBookingId(booking.getId());
        for (Payment p : payments) {
            if (p.getStatus() == Invoice.PaymentStatus.PAID) {
                p.setStatus(Invoice.PaymentStatus.REFUNDED);
                p.setRefundedAmount(refundedAmount);
                paymentService.save(p);
                System.out.println("DEBUG: Payment " + p.getId() + " status changed to REFUNDED, refundAmount: " + refundedAmount);
                break;
            }
        }
    }
}
