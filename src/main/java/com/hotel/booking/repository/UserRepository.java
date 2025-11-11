package com.hotel.booking.repository;

import com.hotel.booking.entity.User;
import com.hotel.booking.security.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für User-Entitäten.
 * Bietet Datenbankzugriff und zusätzliche Query-Methoden für Benutzer.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Findet einen User anhand von Username und Passwort (für Login)
     */
    Optional<User> findByUsernameAndPassword(String username, String password);

    /**
     * Findet einen User anhand des Usernames
     */
    Optional<User> findByUsername(String username);

    /**
     * Findet einen User anhand der E-Mail-Adresse
     */
    Optional<User> findByEmail(String email);

    /**
     * Findet alle User mit einer bestimmten Rolle
     */
    List<User> findByRole(UserRole role);

    /**
     * Findet User anhand des Nachnamens (case-insensitive)
     */
    List<User> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Findet User anhand des Vornamens (case-insensitive)
     */
    List<User> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * Prüft, ob ein User mit dem Username existiert
     */
    boolean existsByUsername(String username);

    /**
     * Prüft, ob ein User mit der E-Mail existiert
     */
    boolean existsByEmail(String email);
}