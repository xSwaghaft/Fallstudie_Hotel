package com.hotel.booking.service;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.repository.BookingExtraRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link BookingExtra} entities.
 * <p>
 * Acts as a business layer between the controller and the repository
 * and provides basic operations for booking extras.
 * </p>
 *
 * @author Matthias Lohr
 */
@Service
public class BookingExtraService {

    private final BookingExtraRepository bookingExtraRepository;

    public BookingExtraService(BookingExtraRepository bookingExtraRepository) {
        this.bookingExtraRepository = bookingExtraRepository;
    }

    public List<BookingExtra> getAllBookingExtras() {
        return bookingExtraRepository.findAll();
    }

    public Optional<BookingExtra> getBookingExtraById(Long id) {
        return bookingExtraRepository.findById(id);
    }

    public BookingExtra saveBookingExtra(BookingExtra bookingExtra) {
        return bookingExtraRepository.save(bookingExtra);
    }

    public void deleteBookingExtra(Long id) {
        bookingExtraRepository.deleteById(id);
    }
}