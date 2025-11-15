package com.hotel.booking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.repository.BookingRepository;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
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
        booking.validateDates(); // nutzt deine Validierung in der Entity
        return bookingRepository.save(booking);
    }

    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }
}