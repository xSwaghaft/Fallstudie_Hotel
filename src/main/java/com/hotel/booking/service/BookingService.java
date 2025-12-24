package com.hotel.booking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.RoomCategoryRepository;
import com.hotel.booking.repository.RoomRepository;

/**
 * Service class for Booking entity operations.
 * 
 * <p>
 * Provides business logic for managing bookings including creation, retrieval,
 * availability checks, and price calculations. Handles room assignment and
 * booking number generation.
 * </p>
 * 
 * @author Viktor Götting
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    
   
    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, RoomCategoryRepository roomCategoryRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.roomCategoryRepository = roomCategoryRepository;
    }

    /**
     * Retrieves all bookings.
     * 
     * @return list of all bookings
     */
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public Optional<Booking> findByBookingNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber);
    }

    // Durchschnittliches Rating für eine Kategorie (0, wenn keine Bewertungen)
    public double getAverageRatingForCategory(RoomCategory category) {
        if (category == null || category.getCategory_id() == null) {
            return 0d;
        }
        return bookingRepository.findByRoomCategoryId(category.getCategory_id())
                .stream()
                .map(Booking::getFeedback)
                .filter(f -> f != null && f.getRating() != null)
                .mapToInt(f -> f.getRating())
                .average()
                .orElse(0d);
    }

    //Matthias Lohr
    /**
     * Speichert eine Booking-Entität.
     *
     * Besonderheiten:
     * - Neue Buchungen (booking.getId() == null) erhalten eine generierte `bookingNumber`.
     * - Beim Editieren wird eine bereits vorhandene `room`-Zuordnung beibehalten; es
     *   wird nicht blind versucht, ein neues Zimmer zuzuweisen.
     * - Falls das `room`-Objekt aus dem UI detached ist (Transient), wird die verwaltete
     *   Instanz aus dem Repository nachgeladen, um Hibernate-Fehler zu vermeiden.
     * - Vor dem Persistieren wird `calculateBookingPrice` aufgerufen. Wenn kein Room
     *   zugewiesen werden kann, wird eine `IllegalStateException` geworfen statt
     *   einen DB-Fehler zu provozieren.
     */
    public Booking save(Booking booking) {
        // If this is a new booking, generate a booking number
        if (booking.getId() == null) {
            booking.setBookingNumber(generateBookingNumber());
        }

        // Only assign a room when none is set yet. For edits, preserve the existing room if present.
        if (booking.getRoom() == null) {
            booking.setRoom(assignRoom(booking));
        } else {
            // Ensure the Room instance attached to the booking is a managed entity.
            // If the binder or caller provided a detached/transient Room object (e.g. with only id),
            // load the managed instance from the repository to avoid TransientPropertyValueException.
            try {
                Long roomId = booking.getRoom().getId();
                if (roomId != null) {
                    roomRepository.findById(roomId).ifPresent(booking::setRoom);
                } else {
                    // if room has no id, try to assign one via available rooms
                    booking.setRoom(assignRoom(booking));
                }
            } catch (Exception ignored) {
                // fallback: try to assign a room
                booking.setRoom(assignRoom(booking));
            }
        }

        booking.validateDates(); // nutzt deine Validierung in der Entity

        // Recalculate total price using proper method
        calculateBookingPrice(booking);

        // Ensure we have a room before saving (DB constraint room_id NOT NULL)
        if (booking.getRoom() == null) {
            throw new IllegalStateException("No room available for selected category and dates");
        }

        return bookingRepository.save(booking);
    }


    /**
     * Gets all active bookings (excluding cancelled) in the specified date range.
     * TODO: Note: Currently not used in the codebase.
     * 
     * @param start start date
     * @param end end date
     * @return list of active bookings
     */
    //Matthias Lohr
    public List<Booking> getActiveBookings(LocalDate start, LocalDate end) {
        return bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(start, end, BookingStatus.CANCELLED);
    }

    /**
     * Gets bookings from the last 5 days.
     * 
     * @return list of recent bookings
     */
    //Matthias Lohr
    public List<Booking> getRecentBookings() {
        LocalDate today = LocalDate.now();
        LocalDate fiveDaysAgo = today.minusDays(5);
        return bookingRepository.findAll()
        .stream()
                .filter(booking -> !booking.getCreatedAt().isBefore(fiveDaysAgo))
                .toList();
    }

    /**
     * Calculates the total number of guests currently present in the hotel.
     * 
     * @return total number of guests present
     */
    //Matthias Lohr
    public int getNumberOfGuestsPresent() {
        List<Booking> todayBookings = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(LocalDate.now(), LocalDate.now(), BookingStatus.CANCELLED);
        return todayBookings.stream()
                .mapToInt(Booking::getAmount)
                .sum();
    }

    /**
     * Gets the number of checkouts scheduled for today.
     * 
     * @return number of checkouts today
     */
    //Matthias Lohr
    public int getNumberOfCheckoutsToday() {
        List<Booking> todayCheckouts = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(LocalDate.now(), LocalDate.now(), BookingStatus.CANCELLED);
        return (int) todayCheckouts.stream()
                .filter(booking -> booking.getCheckOutDate().isEqual(LocalDate.now()))
                .count();
    }

    /**
     * Gets the number of check-ins scheduled for today.
     * 
     * @return number of check-ins today
     */
    //Matthias Lohr
    public int getNumberOfCheckinsToday() {
        List<Booking> todayCheckins = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(LocalDate.now(), LocalDate.now(), BookingStatus.CANCELLED);
        return (int) todayCheckins.stream()
                .filter(booking -> booking.getCheckInDate().isEqual(LocalDate.now()))
                .count();
    }

    //Matthias Lohr
    public BigDecimal getRevenueToday() {
        List<Booking> todayBookings = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(LocalDate.now(), LocalDate.now(), BookingStatus.CANCELLED);
        return todayBookings.stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Generates a unique booking number in format YYYYMMDD-xxxxx.
     * 
     * @return unique booking number
     */
    private String generateBookingNumber() {
    // Beispiel: YYYYMMDD-xxxxx
    String prefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // 8 zufällige Zeichen durch universally unique identifier
    return prefix + "-" + random;
    }

    /**
     * Searches for an available room of the desired category in the booking period.
     * 
     * @param booking the booking to assign a room for
     * @return available room or null if none found
     */
    //Matthias Lohr
    private Room assignRoom(Booking booking) {
        // Check for safety, should not be null
        if (booking.getRoomCategory() == null || booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            return null;
        }
        // All rooms of the desired category
        List<Room> rooms = roomRepository.findByCategory(booking.getRoomCategory());
        for (Room room : rooms) {
            // Check if the room is free in the period
            boolean overlaps = bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                room.getId(),
                booking.getCheckOutDate(),
                booking.getCheckInDate()
            );
            if (!overlaps) {
                return room;
            }
        }
        return null; // No free room found
    }

    /**
     * Checks if at least one room of the given category is available in the specified date range.
     * Used for form validation.
     * 
     * @param category the room category
     * @param checkIn check-in date
     * @param checkOut check-out date
     * @return true if at least one room is available
     */
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
                return true; // At least one room is available
            }
        }
        return false; // No room available
    }

    /**
     * Like {@link #isRoomAvailable(RoomCategory, LocalDate, LocalDate)} but ignores
     * a specific existing booking (useful when editing a booking so the booking
     * itself doesn't block availability checks).
     */
    public boolean isRoomAvailable(RoomCategory category, LocalDate checkIn, LocalDate checkOut, Long excludeBookingId) {
        /**
         * Verfügbarkeitsprüfung für eine Raumkategorie im Zeitraum.
         *
         * Diese Überladung erlaubt das Ignorieren einer bestimmten Buchung (excludeBookingId).
         * Das ist nützlich beim Editieren: die aktuelle Buchung soll die Verfügbarkeit
         * nicht blockieren, weil sie bereits auf diesem Zeitraum liegen kann.
         */
        List<Room> rooms = roomRepository.findByCategory(category);
        for (Room room : rooms) {
            boolean overlaps = bookingRepository.overlapsInRoomExcludingBooking(
                room.getId(),
                excludeBookingId == null ? -1L : excludeBookingId,
                checkIn,
                checkOut
            );
            if (!overlaps) {
                return true;
            }
        }
        return false;
    }


    /**
     * Searches for available room categories in the specified time period.
     * Filters out categories that are not active, require more guests than MaxOccupancy allows,
     * or have no available rooms in the period.
     * 
     * @param checkIn check-in date
     * @param checkOut check-out date
     * @param occupancy number of guests
     * @param categoryName category name filter (null or "All Types" for all categories)
     * @return list of available room categories
     */
    public List<RoomCategory> availableRoomCategoriesSearch(
        LocalDate checkIn,
        LocalDate checkOut,
        int occupancy,
        String categoryName
    ) {
    if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
        return List.of();
    }

    
    List<RoomCategory> categoriesToCheck;

    if (categoryName == null || categoryName.equals("All Types")) {
        categoriesToCheck = roomCategoryRepository.findAll();
    } else {
        var opt = roomCategoryRepository.findByName(categoryName);
        if (!opt.isPresent()) return List.of();
        categoriesToCheck = List.of(opt.get());
    }

    
    List<RoomCategory> availableCategories = new ArrayList<>();

    for (RoomCategory category : categoriesToCheck) {
        if (category == null || category.getMaxOccupancy() == null) {
            continue;
        }

        // Check if category is active
        if (category.getActive() == null || !category.getActive()) {
            continue;
        }

        // Check MaxOccupancy
        if (occupancy > category.getMaxOccupancy()) {
            continue;
        }

        // Check if at least one room of the category is available in the period
        if (isRoomAvailable(category, checkIn, checkOut)) {
            availableCategories.add(category);
        }
    }

    return availableCategories;
}

    /**
     * Counts all unique bookings created within a date range. Used for reports.
     * In this service, as this method is only used for bookings.
     * 
     * @param from start date
     * @param to end date
     * @return number of unique bookings in the period
     */
    //Matthias Lohr
    public int getNumberOfBookingsInPeriod(LocalDate from, LocalDate to){
        List<Booking> bookings = bookingRepository.findByCreatedAtLessThanEqualAndCreatedAtGreaterThanEqual(to, from);
        int uniqueBookingsCount = new HashSet<>(bookings).size();
        return uniqueBookingsCount;
    }

    /**
     * Retrieves all bookings created within a date range.
     * 
     * @param from start date
     * @param to end date
     * @return list of bookings created in the period
     */
    //Matthias Lohr
    public List<Booking> getAllBookingsInPeriod(LocalDate from, LocalDate to) {
        return bookingRepository.findByCreatedAtLessThanEqualAndCreatedAtGreaterThanEqual(to, from);
    }

    /**
     * Finds all past completed bookings for a guest.
     * 
     * @param guestId the guest ID
     * @return list of past completed bookings
     */
    //Viktor Götting Sucht alle vergangenen CONFIRMED Buchungen für einen Gast
    public List<Booking> findPastBookingsForGuest(Long guestId) {
        if (guestId == null) {
            return List.of();
        }
        return bookingRepository.findByGuest_IdAndCheckOutDateBeforeAndStatus(
                guestId,
                LocalDate.now(),
                com.hotel.booking.entity.BookingStatus.COMPLETED
        );
    }

    /**
     * Finds all bookings for a specific guest.
     * 
     * @param guestId the guest ID
     * @return list of all bookings for the guest
     */
    public List<Booking> findAllBookingsForGuest(Long guestId) {
        if (guestId == null) {
            return List.of();
        }
        return bookingRepository.findByGuest_Id(guestId);
    }

    /**
     * Calculates the total price of a booking including room price per night and extras.
     * 
     * @param booking the booking to calculate price for
     */
    public void calculateBookingPrice(Booking booking) {

    if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
        booking.setTotalPrice(BigDecimal.ZERO);
        return;
    }

    long nights = booking.getCheckOutDate().toEpochDay() - booking.getCheckInDate().toEpochDay();
    if (nights <= 0) {
        booking.setTotalPrice(BigDecimal.ZERO);
        return;
    }

    BigDecimal total = BigDecimal.ZERO;

    // Preis pro Nacht
    if (booking.getRoomCategory() != null && booking.getRoomCategory().getPricePerNight() != null) {
        BigDecimal pricePerNight = booking.getRoomCategory().getPricePerNight();
        total = total.add(pricePerNight.multiply(BigDecimal.valueOf(nights)));
    }

    // Extras
    int persons = (booking.getAmount() != null && booking.getAmount() > 0) ? booking.getAmount() : 1;
    BigDecimal guestCount = BigDecimal.valueOf(persons);

    BigDecimal extrasTotal = (booking.getExtras() == null ? java.util.Set.<BookingExtra>of() : booking.getExtras())
        .stream()
        .filter(extra -> extra != null && extra.getPrice() != null)
        .map(extra -> {
            BigDecimal extraPrice = BigDecimal.valueOf(extra.getPrice());
            return extra.isPerPerson()
                    ? extraPrice.multiply(guestCount)
                    : extraPrice;
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    total = total.add(extrasTotal);

    booking.setTotalPrice(total.setScale(2, RoundingMode.HALF_UP));
}

}