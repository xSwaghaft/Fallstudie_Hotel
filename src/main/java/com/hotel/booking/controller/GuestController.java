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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.booking.entity.Guest;
import com.hotel.booking.service.GuestService;

/*
 * REST-Controller für Gäste-Verwaltung.
 * Stellt HTTP-Endpoints für CRUD-Operationen auf Guest-Entitäten bereit.
 */
@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    // Konstruktor-Injection
    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    /* GET /api/guests
     * Gibt alle Gäste zurück. */
    @GetMapping
    public ResponseEntity<List<Guest>> getAllGuests() {
        List<Guest> guests = guestService.findAll();
        return ResponseEntity.ok(guests);
    }

    /* GET /api/guests/{id}
     * Gibt einen Gast anhand der ID zurück. */
    @GetMapping("/{id}")
    public ResponseEntity<Guest> getGuestById(@PathVariable Long id) {
        Optional<Guest> guestOpt = guestService.findById(id);
        return guestOpt.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    /* GET /api/guests/by-email?email=...
     * Holt einen Gast über die E-Mail-Adresse. */
    @GetMapping("/by-email")
    public ResponseEntity<Guest> getGuestByEmail(@RequestParam String email) {
        Optional<Guest> guestOpt = guestService.findByEmail(email);
        return guestOpt.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    /* POST /api/guests
     * Erstellt einen neuen Gast. */
    @PostMapping
    public ResponseEntity<Guest> createGuest(@RequestBody Guest guest) {
        // einfache Validierung
        if (guest.getEmail() == null || guest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (guestService.existsByEmail(guest.getEmail())) {
            // E-Mail bereits vergeben
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (guest.getUser() == null) {
            // Falls bei euch jeder Guest zwingend einen User haben muss:
            return ResponseEntity.badRequest().build();
        }

        Guest saved = guestService.save(guest);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /* PUT /api/guests/{id}
     * Aktualisiert einen bestehenden Gast. */
    @PutMapping("/{id}")
    public ResponseEntity<Guest> updateGuest(@PathVariable Long id,
                                             @RequestBody Guest guestFromRequest) {

        Optional<Guest> guestOpt = guestService.findById(id);
        if (guestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Guest existing = guestOpt.get();

        // Felder aktualisieren (ID bleibt unverändert)
        if (guestFromRequest.getEmail() != null && !guestFromRequest.getEmail().trim().isEmpty()) {
            existing.setEmail(guestFromRequest.getEmail());
        }
        if (guestFromRequest.getFirstName() != null) {
            existing.setFirstName(guestFromRequest.getFirstName());
        }
        if (guestFromRequest.getLastName() != null) {
            existing.setLastName(guestFromRequest.getLastName());
        }
        existing.setAddress(guestFromRequest.getAddress());
        existing.setPhoneNumber(guestFromRequest.getPhoneNumber());
        existing.setBirthdate(guestFromRequest.getBirthdate());

        // User würde ich im Normalfall NICHT per Update ändern,
        // aber wenn ihr das wollt:
        if (guestFromRequest.getUser() != null) {
            existing.setUser(guestFromRequest.getUser());
        }

        Guest updated = guestService.save(existing);
        return ResponseEntity.ok(updated);
    }

    /* DELETE /api/guests/{id}
     * Löscht einen Gast anhand der ID. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        Optional<Guest> guestOpt = guestService.findById(id);
        if (guestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        guestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
