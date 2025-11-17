package com.hotel.booking.service;

import com.hotel.booking.dto.BookingModificationRequest;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingModificationRepository;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingModificationService {

    // Repositories für Datenbankzugriffe werden über den Konstruktor injiziert
    private final BookingModificationRepository modificationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingModificationService(
            BookingModificationRepository modificationRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        this.modificationRepository = modificationRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // Gibt alle Buchungsänderungen als Liste zurück
    public List<BookingModification> getAll() {
        return modificationRepository.findAll();
    }

    // Sucht eine Buchungsänderung anhand der ID und gibt sie als Optional zurück
    public Optional<BookingModification> getById(Long id) {
        return modificationRepository.findById(id);
    }

    // Erstellt eine neue Buchungsänderung aus den Daten des Request-Objekts
    public BookingModification createFromRequest(BookingModificationRequest request) {
        // Erstellt ein neues BookingModification-Objekt und setzt die Felder aus dem Request
        BookingModification modification = new BookingModification();
        Booking booking = bookingRepository.findById(request.bookingId).orElse(null);
        modification.setBooking(booking);
        modification.setModifiedAt(request.modifiedAt);
        modification.setFieldChanged(request.fieldChanged);
        modification.setOldValue(request.oldValue);
        modification.setNewValue(request.newValue);
        modification.setReason(request.reason);
        User handledBy = request.handledById != null ? userRepository.findById(request.handledById).orElse(null) : null;
        modification.setHandledBy(handledBy);
        // Speichert die neue Buchungsänderung in der Datenbank und gibt sie zurück
        return modificationRepository.save(modification);
    }

    // Aktualisiert eine bestehende Buchungsänderung anhand der ID und des Request-Objekts
    public BookingModification updateFromRequest(Long id, BookingModificationRequest request) {
        // Holt die bestehende Buchungsänderung aus der Datenbank
        BookingModification modification = modificationRepository.findById(id).orElseThrow();
        // Setzt die neuen Werte aus dem Request
        Booking booking = bookingRepository.findById(request.bookingId).orElse(null);
        modification.setBooking(booking);
        modification.setModifiedAt(request.modifiedAt);
        modification.setFieldChanged(request.fieldChanged);
        modification.setOldValue(request.oldValue);
        modification.setNewValue(request.newValue);
        modification.setReason(request.reason);
        User handledBy = request.handledById != null ? userRepository.findById(request.handledById).orElse(null) : null;
        modification.setHandledBy(handledBy);
        // Speichert die aktualisierte Buchungsänderung in der Datenbank und gibt sie zurück
        return modificationRepository.save(modification);
    }

    // Löscht eine Buchungsänderung anhand der ID aus der Datenbank
    public void delete(Long id) {
        modificationRepository.deleteById(id);
    }
}