package com.hotel.booking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.User;

/**
 * Facade service for booking form related operations.
 * <p>
 * This service aggregates multiple underlying services in order to
 * reduce direct dependencies in the view layer. It provides a simplified
 * interface for accessing booking-related data and validations needed
 * by booking forms.
 * </p>
 *
 * @author Matthias Lohr
 */
@Service
public class BookingFormService {

    private final UserService userService;

    private final BookingService bookingService;
    private final BookingExtraService bookingExtraService;
    private final RoomCategoryService roomCategoryService;

    public BookingFormService(BookingService bookingService,
                              BookingExtraService bookingExtraService,
                              RoomService roomService, RoomCategoryService roomCategoryService, UserService userService) {
        this.bookingService = bookingService;
        this.bookingExtraService = bookingExtraService;
        this.roomCategoryService = roomCategoryService;
        this.userService = userService;
    }

    public List<RoomCategory> getAllRoomCategories() {
        return roomCategoryService.getAllRoomCategories();
    }

    public List<BookingExtra> getAllBookingExtras() {
        return bookingExtraService.getAllBookingExtras();
    }

    public boolean isRoomAvailable(RoomCategory category, LocalDate start, LocalDate end) {
        return bookingService.isRoomAvailable(category, start, end);
    }

    /**
     * Delegated availability check that excludes an existing booking.
     * <p>
     * Serves as a simple facade for the view layer (e.g. binder validators)
     * to validate room availability while editing an existing booking.
     * </p>
     */
    public boolean isRoomAvailable(RoomCategory category, LocalDate start, LocalDate end, Long excludeBookingId) {
        
        return bookingService.isRoomAvailable(category, start, end, excludeBookingId);
    }

    public User findUserByEmail(String email) {
        return userService.findUserByEmail(email);
    }

    public boolean existsByEmail (String email) {
        return userService.existsByEmail(email);
    }

}

