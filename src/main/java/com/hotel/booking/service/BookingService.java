package com.hotel.booking.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.hotel.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.RoomRepository;

@Service
@Transactional
public class BookingService {

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;


    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public Optional<Booking> findByBookingNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber);
    }

    public Booking save(Booking booking) {
        booking.setBookingNumber(generateBookingNumber());
        booking.setRoom(assignRoom(booking));
        booking.validateDates(); // nutzt deine Validierung in der Entity
        return bookingRepository.save(booking);
    }

    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    //Matthias Lohr
    public List<Booking> getActiveBookings(LocalDate start, LocalDate end) {
        return bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(start, end);
    }

    //Buchungen der letzten 7 Tage
    //Matthias Lohr
    public List<Booking> getRecentBookings() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        return bookingRepository.findAll();
        // .stream()
        //         .filter(booking -> !booking.created_at().isBefore(sevenDaysAgo))
        //         .toList();
    }

    //Matthias Lohr
    public int getNumberOfGuestsPresent() {
        List<Booking> todayBookings = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(LocalDate.now(), LocalDate.now());
        return todayBookings.stream()
                .mapToInt(Booking::getAmount)
                .sum();
    }

    //Matthias Lohr
    public int getNumberOfCheckoutsToday() {
        List<Booking> todayCheckouts = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(LocalDate.now(), LocalDate.now());
        return (int) todayCheckouts.stream()
                .filter(booking -> booking.getCheckOutDate().isEqual(LocalDate.now()))
                .count();
    }

    //Matthias Lohr
    public int getNumberOfCheckinsToday() {
        List<Booking> todayCheckins = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(LocalDate.now(), LocalDate.now());
        return (int) todayCheckins.stream()
                .filter(booking -> booking.getCheckInDate().isEqual(LocalDate.now()))
                .count();
    }

    private String generateBookingNumber() {
    // Beispiel: YYYYMMDD-xxxxx
    String prefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // 8 zufällige Zeichen durch universally unique identifier
    return prefix + "-" + random;
    }

    // Sucht ein verfügbares Zimmer der gewünschten Kategorie im Zeitraum der Buchung
    //Matthias Lohr
    private Room assignRoom(Booking booking) {
        //Zur Sicherheit prüfen, sollte aber nicht null sein
        if (booking.getRoomCategory() == null || booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            return null;
        }
        // Alle Zimmer der gewünschten Kategorie
        List<Room> rooms = roomRepository.findByCategory(booking.getRoomCategory());
        for (Room room : rooms) {
            // Prüfe, ob das Zimmer im Zeitraum frei ist
            boolean overlaps = bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                room.getId(),
                booking.getCheckOutDate(),
                booking.getCheckInDate()
            );
            if (!overlaps) {
                return room;
            }
        }
        return null; // Kein freies Zimmer gefunden
    }

    // Prüft, ob ein Zimmer der Kategorie im Zeitraum verfügbar ist (Für den Validator des Formulars)
    //Matthias Lohr
    public boolean isRoomAvailable(RoomCategory category, LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findByCategory(category);
        for (Room room : rooms) {
            boolean overlaps = bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                room.getId(),
                checkOut,
                checkIn
            );
            if (!overlaps) {
                return true; // Mindestens ein Zimmer ist verfügbar
            }
        }
        return false; // Kein Zimmer verfügbar
    }

    //In diesem Service, da die Methode so nur fürs Booking verwendet wird
    //Matthias Lohr
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null); //orElse da ein Optional<> zurückkommt
    }
}