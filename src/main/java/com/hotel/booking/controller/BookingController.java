package com.hotel.booking.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.service.BookingService;

/* Booking-Controller
 * REST-Controller für die Verwaltung von Booking-Entitäten.
 * Stellt HTTP-Endpoints für CRUD-Operationen auf Buchungen bereit. */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    // Konstruktor-Injection (Best Practice)
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /* GET /api/bookings
     * Gibt alle Buchungen zurück. */
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.findAll();
        return ResponseEntity.ok(bookings);
    }

    /* GET /api/bookings/{id}
     * Gibt eine Buchung anhand der ID zurück. */
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> bookingOpt = bookingService.findById(id);
        return bookingOpt.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    /* GET /api/bookings/by-number/{bookingNumber}
     * Gibt eine Buchung anhand der Buchungsnummer zurück. */
    @GetMapping("/by-number/{bookingNumber}")
    public ResponseEntity<Booking> getBookingByNumber(@PathVariable String bookingNumber) {
        Optional<Booking> bookingOpt = bookingService.findByBookingNumber(bookingNumber);
        return bookingOpt.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    /* POST /api/bookings
     * Erstellt eine neue Buchung.
     * Erwartet ein Booking-Objekt im Request Body. */
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        // einfache Validierung
        if (booking.getBookingNumber() == null || booking.getBookingNumber().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            return ResponseEntity.badRequest().build();
        }

        // validateDates() wird im Service in save() aufgerufen
        Booking saved = bookingService.save(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /* PUT /api/bookings/{id}
     * Aktualisiert eine bestehende Buchung.
     * Die ID kommt aus dem Pfad, die neuen Daten aus dem Request Body. */
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id,
                                                 @RequestBody Booking bookingFromRequest) {

        Optional<Booking> bookingOpt = bookingService.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking existing = bookingOpt.get();

        // Felder aus dem Request übernehmen (ID bleibt unverändert)
        if (bookingFromRequest.getBookingNumber() != null) {
            existing.setBookingNumber(bookingFromRequest.getBookingNumber());
        }
        existing.setAmount(bookingFromRequest.getAmount());
        existing.setCheckInDate(bookingFromRequest.getCheckInDate());
        existing.setCheckOutDate(bookingFromRequest.getCheckOutDate());
        existing.setStatus(bookingFromRequest.getStatus());
        existing.setTotalPrice(bookingFromRequest.getTotalPrice());
        existing.setGuest(bookingFromRequest.getGuest());
        existing.setRoom(bookingFromRequest.getRoom());
        existing.setInvoice(bookingFromRequest.getInvoice());
        existing.setPayments(bookingFromRequest.getPayments());
        existing.setExtras(bookingFromRequest.getExtras());
        existing.setFeedback(bookingFromRequest.getFeedback());

        Booking updated = bookingService.save(existing);
        return ResponseEntity.ok(updated);
    }

    /* DELETE /api/bookings/{id}
     * Löscht eine Buchung anhand der ID. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        Optional<Booking> bookingOpt = bookingService.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
