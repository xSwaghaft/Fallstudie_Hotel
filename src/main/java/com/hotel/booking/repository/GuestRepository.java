package com.hotel.booking.repository;

import com.hotel.booking.entity.Guest;
import com.hotel.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository für Guest-Entitäten.
 * Bietet Datenbankzugriff und zusätzliche Query-Methoden für Gäste.
 */
@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    /**
     * Findet einen Guest anhand des zugeordneten Users
     */
    Optional<Guest> findByUser(User user);

    /**
     * Findet einen Guest anhand der User-ID
     */
    Optional<Guest> findByUserId(Long userId);

    /**
     * Findet einen Guest anhand der E-Mail-Adresse
     */
    Optional<Guest> findByEmail(String email);

    /**
     * Findet Guests anhand des Nachnamens (case-insensitive)
     */
    List<Guest> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Findet Guests anhand des Vornamens (case-insensitive)
     */
    List<Guest> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * Findet Guests anhand von Vor- und Nachname
     */
    List<Guest> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Prüft, ob ein Guest für einen bestimmten User existiert
     */
    boolean existsByUser(User user);

    /**
     * Prüft, ob ein Guest mit der E-Mail existiert
     */
    boolean existsByEmail(String email);
}
