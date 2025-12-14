package com.hotel.booking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.User;

//Fassade, um unnötig viele Abhängigkeiten in den Views durch benötigte Services zu vermeiden
//Matthias Lohr
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

    public User findUserByEmail(String email) {
        return userService.findUserByEmail(email);
    }

    public boolean existsByEmail (String email) {
        return userService.existsByEmail(email);
    }

}

