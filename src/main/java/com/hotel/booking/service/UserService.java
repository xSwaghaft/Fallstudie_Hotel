package com.hotel.booking.service;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service-Klasse für User-Entitäten.
 * Enthält Business-Logik, CRUD-Operationen und Authentifizierung für Benutzer.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Demo-Daten, damit Login out-of-the-box funktioniert
     */
    @PostConstruct
    void initDemoUsers() {
        if (userRepository.count() == 0) {
            userRepository.save(new User("john.guest", "guest", UserRole.GUEST));
            userRepository.save(new User("sarah.receptionist", "reception", UserRole.RECEPTIONIST));
            userRepository.save(new User("david.manager", "manager", UserRole.MANAGER));
        }
    }

    /**
     * Authentifiziert einen User anhand von Username und Passwort
     */
    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    /**
     * Registriert einen neuen User
     * Prüft ob E-Mail bereits existiert, hasht das Passwort und setzt Rolle auf GUEST
     */
    public User registerUser(User user) {
        // Prüfe ob E-Mail bereits existiert
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("E-Mail bereits registriert");
        }

        // Passwort hashen
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Rolle standardmäßig auf GUEST setzen
        user.setRole(UserRole.GUEST);

        // User speichern
        return userRepository.save(user);
    }

    /**
     * Login-Methode: Authentifiziert User mit E-Mail und Passwort
     */
    public Optional<User> login(String email, String password) {
        Optional<User> userOpt = findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Passwort überprüfen
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Findet einen User anhand der E-Mail (für Login/Registrierung)
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Gibt alle Users zurück
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Findet einen User anhand der ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Findet einen User anhand des Usernames
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Speichert oder aktualisiert einen User
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Löscht einen User anhand der ID
     */
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Löscht einen User
     */
    public void delete(User user) {
        userRepository.delete(user);
    }

    /**
     * Findet alle User mit einer bestimmten Rolle
     */
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Sucht User nach Nachname
     */
    public List<User> searchByLastName(String lastName) {
        return userRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    /**
     * Sucht User nach Vorname
     */
    public List<User> searchByFirstName(String firstName) {
        return userRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    /**
     * Prüft, ob ein Username bereits existiert
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Prüft, ob eine E-Mail bereits existiert
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Zählt die Anzahl aller Users
     */
    public long count() {
        return userRepository.count();
    }

    /**
     * Prüft, ob ein User mit der ID existiert
     */
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}