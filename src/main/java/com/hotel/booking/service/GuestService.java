package com.hotel.booking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hotel.booking.entity.Guest;
import com.hotel.booking.repository.GuestRepository;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    /**
     * Alle Gäste abrufen
     */
    public List<Guest> findAll() {
        return guestRepository.findAll();
    }

    /**
     * Gast per ID finden
     */
    public Optional<Guest> findById(Long id) {
        return guestRepository.findById(id);
    }

    /**
     * Neuen Gast speichern oder bestehenden updaten
     */
    public Guest save(Guest guest) {
        return guestRepository.save(guest);
    }

    /**
     * Gast löschen
     */
    public void delete(Long id) {
        guestRepository.deleteById(id);
    }

    /**
     * Gast per E-Mail finden
     */
    public Optional<Guest> findByEmail(String email) {
        return guestRepository.findByEmail(email);
    }

    /**
     * Prüfen, ob ein Gast mit E-Mail existiert
     */
    public boolean existsByEmail(String email) {
        return guestRepository.existsByEmail(email);
    }
}
