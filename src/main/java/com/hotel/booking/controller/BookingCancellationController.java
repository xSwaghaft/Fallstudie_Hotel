// Ruslan Krause
package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingCancellationRequest;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.service.BookingCancellationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-cancellations")
public class BookingCancellationController {

    private final BookingCancellationService service;

    public BookingCancellationController(BookingCancellationService service) {
        this.service = service;
    }

    // Gibt alle Stornierungen zurück
    @GetMapping
    public List<BookingCancellation> getAll() {
        return service.getAll();
    }

    // Gibt eine einzelne Stornierung anhand der ID zurück
    @GetMapping("/{id}")
    public ResponseEntity<BookingCancellation> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Erstellt eine neue Stornierung, die Logik liegt im Service
    @PostMapping
    public ResponseEntity<BookingCancellation> create(@RequestBody BookingCancellationRequest request) {
        BookingCancellation saved = service.createFromRequest(request);
        return ResponseEntity.ok(saved);
    }

    // Aktualisiert eine bestehende Stornierung, die Logik liegt im Service
    @PutMapping("/{id}")
    public ResponseEntity<BookingCancellation> update(@PathVariable Long id, @RequestBody BookingCancellationRequest request) {
        BookingCancellation updated = service.updateFromRequest(id, request);
        return ResponseEntity.ok(updated);
    }

    // Löscht eine Stornierung anhand der ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}