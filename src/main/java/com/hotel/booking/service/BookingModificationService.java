package com.hotel.booking.service;

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

    // Löscht eine Buchungsänderung anhand der ID aus der Datenbank
    public void delete(Long id) {
        modificationRepository.deleteById(id);
    }
}