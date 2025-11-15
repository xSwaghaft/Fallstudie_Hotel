package com.hotel.booking.controller;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.service.BookingExtraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/booking-extras")
public class BookingExtraController {

    private final BookingExtraService bookingExtraService;

    @Autowired
    public BookingExtraController(BookingExtraService bookingExtraService) {
        this.bookingExtraService = bookingExtraService;
    }

    @GetMapping
    public List<BookingExtra> getAllBookingExtras() {
        return bookingExtraService.getAllBookingExtras();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingExtra> getBookingExtraById(@PathVariable Long id) {
        Optional<BookingExtra> bookingExtra = bookingExtraService.getBookingExtraById(id);
        return bookingExtra.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public BookingExtra createBookingExtra(@RequestBody BookingExtra bookingExtra) {
        return bookingExtraService.saveBookingExtra(bookingExtra);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingExtra> updateBookingExtra(@PathVariable Long id, @RequestBody BookingExtra updatedBookingExtra) {
        if (bookingExtraService.getBookingExtraById(id).isPresent()) {
            updatedBookingExtra.setBookingExtra_id(id);
            return ResponseEntity.ok(bookingExtraService.saveBookingExtra(updatedBookingExtra));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookingExtra(@PathVariable Long id) {
        if (bookingExtraService.getBookingExtraById(id).isPresent()) {
            bookingExtraService.deleteBookingExtra(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}