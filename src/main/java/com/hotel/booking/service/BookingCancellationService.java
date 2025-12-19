// Ruslan Krause
package com.hotel.booking.service;

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

    // Speichert einen BookingCancellation-Eintrag
    public BookingCancellation save(BookingCancellation cancellation) {
        return cancellationRepository.save(cancellation);
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
}
