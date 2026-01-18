package com.hotel.booking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
 * Service for booking operations.
 * @author Viktor Götting
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    private final EmailService emailService;
    private final BookingModificationService modificationService;
    
   
    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, RoomCategoryRepository roomCategoryRepository, EmailService emailService, BookingModificationService modificationService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.roomCategoryRepository = roomCategoryRepository;
        this.emailService = emailService;
        this.modificationService = modificationService;
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

 

    // Average rating for a category (0 if no ratings)
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

    // Matthias Lohr
    /**
     * Saves a Booking entity.
     *
     * Specialties:
     * - New bookings (booking.getId() == null) receive a generated `bookingNumber`.
     * - When editing, an existing `room` assignment is preserved; it does not blindly
     *   attempt to assign a new room.
     * - If the `room` object from the UI is detached (Transient), the managed instance
     *   is reloaded from the repository to avoid Hibernate errors.
     * - Before persisting, `calculateBookingPrice` is called. If no room can be assigned,
     *   an `IllegalStateException` is thrown instead of provoking a DB error.
     */
    public Booking save(Booking booking) {
        // Load existing booking if editing to track changes
        Booking before = null;
        if (booking.getId() != null) {
            before = bookingRepository.findById(booking.getId()).orElse(null);
        }
        
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

        booking.validateDates(); // uses your validation in the Entity

        // Recalculate total price using proper method
        calculateBookingPrice(booking);

        // Ensure we have a room before saving (DB constraint room_id NOT NULL)
        if (booking.getRoom() == null) {
            throw new IllegalStateException("No room available for selected category and dates");
        }

        Booking savedBooking = bookingRepository.save(booking);
        
        // Send confirmation email for new bookings
        boolean isNewBooking = (before == null);
        if (isNewBooking) {
            System.out.println("DEBUG: New booking - ID was null, attempting to send email");
            System.out.println("DEBUG: Guest is null? " + (savedBooking.getGuest() == null));
            if (savedBooking.getGuest() != null) {
                System.out.println("DEBUG: Guest email: " + savedBooking.getGuest().getEmail());
            }
            
            if (savedBooking.getGuest() != null && savedBooking.getGuest().getEmail() != null && !savedBooking.getGuest().getEmail().isBlank()) {
                try {
                    System.out.println("DEBUG: Sending booking confirmation email to: " + savedBooking.getGuest().getEmail());
                    emailService.sendBookingConfirmation(savedBooking);
                    System.out.println("DEBUG: Email sent successfully");
                } catch (Exception e) {
                    // Log error but don't fail the booking save
                    System.err.println("Failed to send booking confirmation email: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("DEBUG: Email NOT sent - guest is null or email is null/blank");
            }
        }
        
        // Record changes and send modification email for existing bookings
        if (before != null && savedBooking.getGuest() != null && savedBooking.getGuest().getEmail() != null) {
            try {
                modificationService.recordChanges(before, savedBooking, null, null);
            } catch (Exception e) {
                // Log error but don't fail the booking save
                System.err.println("Failed to record booking modifications: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return savedBooking;
    }

    // Matthias Lohr
    public List<Booking> getActiveBookings(LocalDate start, LocalDate end) {
        return bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(start, end, BookingStatus.CANCELLED);
    }

    //Bookings of the last 5 days
    //Matthias Lohr
    public List<Booking> getRecentBookings() {
        LocalDate today = LocalDate.now();
        LocalDate fiveDaysAgo = today.minusDays(5);
        return bookingRepository.findAll()
        .stream()
                .filter(booking -> !booking.getCreatedAt().isBefore(fiveDaysAgo))
                .toList();
    }

    // Matthias Lohr
    public int getNumberOfGuestsPresent() {
        List<Booking> todayBookings = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(LocalDate.now(), LocalDate.now(), BookingStatus.CANCELLED);
        return todayBookings.stream()
                .mapToInt(Booking::getAmount)
                .sum();
    }

    // Matthias Lohr
    public int getNumberOfCheckoutsToday() {
        List<Booking> todayCheckouts = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(LocalDate.now(), LocalDate.now(), BookingStatus.CANCELLED);
        return (int) todayCheckouts.stream()
                .filter(booking -> booking.getCheckOutDate().isEqual(LocalDate.now()))
                .count();
    }

    // Matthias Lohr
    public int getNumberOfCheckinsToday() {
        List<Booking> todayCheckins = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(LocalDate.now(), LocalDate.now(), BookingStatus.CANCELLED);
        return (int) todayCheckins.stream()
                .filter(booking -> booking.getCheckInDate().isEqual(LocalDate.now()))
                .count();
    }

    // Matthias Lohr
    public BigDecimal getRevenueToday() {
        List<Booking> todayBookings = bookingRepository.findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(LocalDate.now(), LocalDate.now(), BookingStatus.CANCELLED);
        return todayBookings.stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateBookingNumber() {
    // Example: YYYYMMDD-xxxxx
    String prefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // 8 random characters via universally unique identifier
    return prefix + "-" + random;
    }

    // Finds a free room of the desired category in the booking period
    //Matthias Lohr
    private Room assignRoom(Booking booking) {
        //Zur Sicherheit prüfen, sollte aber nicht null sein
        if (booking.getRoomCategory() == null || booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            return null;
        }
        // All rooms of the desired category
        List<Room> rooms = roomRepository.findByCategory(booking.getRoomCategory());
        for (Room room : rooms) {
            // Check if the room is free in the period
            boolean overlaps = bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(
                room.getId(),
                booking.getCheckOutDate(),
                booking.getCheckInDate(),
                BookingStatus.CANCELLED
            );
            if (!overlaps) {
                return room;
            }
        }
        return null; // No free room found
    }

    // Checks if a room of the category is available in the period (For the validator of the form)
    //Matthias Lohr
    public boolean isRoomAvailable(RoomCategory category, LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findByCategory(category);
        for (Room room : rooms) {
            boolean overlaps = bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(
                room.getId(),
                checkOut,
                checkIn,
                BookingStatus.CANCELLED
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
         * Availability check for a room category in the period.
         *
         * This overload allows ignoring a specific booking (excludeBookingId).
         * This is useful when editing: the current booking should not block availability
         * because it may already be on this time period.
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


    //Viktor Götting Finds the available categories in the desired time and sorts all out that need more guests than MaxOccupancy allows
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

    //In this service, since this method is only used for bookings
    //Counts all bookings in a period - for the report
    //Matthias Lohr
    public int getNumberOfBookingsInPeriod(LocalDate from, LocalDate to){
        List<Booking> bookings = bookingRepository.findByCreatedAtLessThanEqualAndCreatedAtGreaterThanEqual(to, from);
        int uniqueBookingsCount = new HashSet<>(bookings).size();
        return uniqueBookingsCount;
    }

    //Matthias Lohr
    public List<Booking> getAllBookingsInPeriod (LocalDate from, LocalDate to) {
        return bookingRepository.findByCreatedAtLessThanEqualAndCreatedAtGreaterThanEqual(to, from);
    }

    //Viktor Götting Finds all past COMPLETED bookings for a guest
    // Only COMPLETED bookings allow reviews (not CONFIRMED)
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

    public List<Booking> findAllBookingsForGuest(Long guestId) {
        if (guestId == null) {
            return List.of();
        }
        return bookingRepository.findByGuest_Id(guestId);
    }

    // Viktor Götting calculates the total price of the booking
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

    // Price per night
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