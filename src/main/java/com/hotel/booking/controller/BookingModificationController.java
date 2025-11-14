// Ruslan Krause
package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingModificationRequest;
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.service.BookingModificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-modifications")
public class BookingModificationController {

    private final BookingModificationService service;

    public BookingModificationController(BookingModificationService service) {
        this.service = service;
    }

    // Gibt alle Buchungsänderungen zurück
    @GetMapping
    public List<BookingModification> getAll() {
        return service.getAll();
    }

    // Gibt eine einzelne Buchungsänderung anhand der ID zurück
    @GetMapping("/{id}")
    public ResponseEntity<BookingModification> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Erstellt eine neue Buchungsänderung
    @PostMapping
    public ResponseEntity<BookingModification> create(@RequestBody BookingModificationRequest request) {
        BookingModification saved = service.createFromRequest(request);
        return ResponseEntity.ok(saved);
    }

    // Aktualisiert eine bestehende Buchungsänderung
    @PutMapping("/{id}")
    public ResponseEntity<BookingModification> update(@PathVariable Long id, @RequestBody BookingModificationRequest request) {
        BookingModification updated = service.updateFromRequest(id, request);
        return ResponseEntity.ok(updated);
    }

    // Löscht eine Buchungsänderung anhand der ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}