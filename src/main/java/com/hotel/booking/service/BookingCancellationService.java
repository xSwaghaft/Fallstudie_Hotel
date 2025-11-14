// Ruslan Krause
package com.hotel.booking.service;

import com.hotel.booking.dto.BookingCancellationRequest;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingCancellationRepository;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingCancellationService {

    // Repositories werden für die Datenbankoperationen benötigt
    private final BookingCancellationRepository cancellationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    // Konstruktor-Injektion der benötigten Repositories
    public BookingCancellationService(
            BookingCancellationRepository cancellationRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        this.cancellationRepository = cancellationRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // Gibt eine Liste aller BookingCancellation-Objekte aus der Datenbank zurück
    public List<BookingCancellation> getAll() {
        return cancellationRepository.findAll();
    }

    // Sucht eine BookingCancellation anhand der ID und gibt sie als Optional zurück
    public Optional<BookingCancellation> getById(Long id) {
        return cancellationRepository.findById(id);
    }

    // Erstellt eine neue BookingCancellation aus den Daten des Request-Objekts
    public BookingCancellation createFromRequest(BookingCancellationRequest request) {
        BookingCancellation cancellation = new BookingCancellation();
        // Holt die zugehörige Buchung anhand der bookingId aus der Datenbank
        Booking booking = bookingRepository.findById(request.bookingId).orElse(null);
        cancellation.setBooking(booking);
        // Setzt das Datum der Stornierung
        cancellation.setCancelledAt(request.cancelledAt);
        // Setzt den Grund der Stornierung
        cancellation.setReason(request.reason);
        // Setzt den erstatteten Betrag
        cancellation.setRefundedAmount(request.refundedAmount);
        // Holt den bearbeitenden User anhand der handledById aus der Datenbank
        User handledBy = request.handledById != null ? userRepository.findById(request.handledById).orElse(null) : null;
        cancellation.setHandledBy(handledBy);
        // Speichert die neue Stornierung in der Datenbank und gibt sie zurück
        return cancellationRepository.save(cancellation);
    }

    // Aktualisiert eine bestehende BookingCancellation anhand der ID und des Request-Objekts
    public BookingCancellation updateFromRequest(Long id, BookingCancellationRequest request) {
        // Holt die bestehende Stornierung aus der Datenbank
        BookingCancellation cancellation = cancellationRepository.findById(id).orElseThrow();
        // Aktualisiert die zugehörige Buchung
        Booking booking = bookingRepository.findById(request.bookingId).orElse(null);
        cancellation.setBooking(booking);
        // Aktualisiert das Datum der Stornierung
        cancellation.setCancelledAt(request.cancelledAt);
        // Aktualisiert den Grund der Stornierung
        cancellation.setReason(request.reason);
        // Aktualisiert den erstatteten Betrag
        cancellation.setRefundedAmount(request.refundedAmount);
        // Aktualisiert den bearbeitenden User
        User handledBy = request.handledById != null ? userRepository.findById(request.handledById).orElse(null) : null;
        cancellation.setHandledBy(handledBy);
        // Speichert die aktualisierte Stornierung in der Datenbank und gibt sie zurück
        return cancellationRepository.save(cancellation);
    }

    // Löscht eine BookingCancellation anhand der ID aus der Datenbank
    public void delete(Long id) {
        cancellationRepository.deleteById(id);
    }
}