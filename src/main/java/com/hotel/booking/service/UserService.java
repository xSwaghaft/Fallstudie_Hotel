package com.hotel.booking.service;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.repository.UserRepository;
import com.hotel.booking.security.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
/* Artur Derr
 * Service-Klasse für User-Entitäten.
 * Enthält Business-Logik, CRUD-Operationen und Authentifizierung für Benutzer. */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* Demo-Daten mit GEHASHTEN Passwörtern */
    @PostConstruct
    void initDemoUsers() {
        if (userRepository.count() == 0) {
            log.info("Initialisiere Demo-Benutzer...");
            
            User guest = new User("john.guest", "John", "Guest", passwordEncoder.encode("guest"), UserRole.GUEST, true);
            User receptionist = new User("sarah.receptionist", "Sarah", "Receptionist", passwordEncoder.encode("reception"), UserRole.RECEPTIONIST, true);
            User manager = new User("david.manager", "David", "Manager", passwordEncoder.encode("manager"), UserRole.MANAGER, true);
            
            userRepository.save(guest);
            userRepository.save(receptionist);
            userRepository.save(manager);
            
            log.info("Demo-Benutzer erfolgreich erstellt");
        }
    }

    /* Authentifiziert einen User anhand von Username und Passwort */
    public Optional<User> authenticate(String username, String password) {
        log.debug("Authentifizierungsversuch für Username: {}", username);
        
        Optional<User> userOpt = findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                log.info("Authentifizierung erfolgreich für User: {}", username);
                return Optional.of(user);
            } else {
                log.warn("Authentifizierung fehlgeschlagen - falsches Passwort für User: {}", username);
            }
        } else {
            log.warn("Authentifizierung fehlgeschlagen - User nicht gefunden: {}", username);
        }
        
        return Optional.empty();
    }

    /* Registriert einen neuen User und prüft auf Duplikate */
    public User registerUser(User user) {
        log.info("Registrierungsversuch für Username: {}, E-Mail: {}", user.getUsername(), user.getEmail());
        
        // Username-Validierung 
        if (existsByUsername(user.getUsername())) {
            log.warn("Registrierung fehlgeschlagen - Username bereits vergeben: {}", user.getUsername());
            throw new IllegalArgumentException("Username bereits vergeben");
        }
        
        // E-Mail-Validierung
        if (existsByEmail(user.getEmail())) {
            log.warn("Registrierung fehlgeschlagen - E-Mail bereits registriert: {}", user.getEmail());
            throw new IllegalArgumentException("E-Mail bereits registriert");
        }

        // Passwort hashen
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Rolle standardmäßig auf GUEST setzen
        if (user.getRole() == null) {
            user.setRole(UserRole.GUEST);
        }

        // User speichern
        User savedUser = userRepository.save(user);
        log.info("User erfolgreich registriert: {}", savedUser.getUsername());
        
        return savedUser;
    }

    /* Login-Methode: Authentifiziert User mit E-Mail und Passwort */
    public Optional<User> login(String email, String password) {
        log.debug("Login-Versuch für E-Mail: {}", email);
        
        Optional<User> userOpt = findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                log.info("Login erfolgreich für User: {} ({})", user.getUsername(), email);
                return Optional.of(user);
            } else {
                log.warn("Login fehlgeschlagen - falsches Passwort für E-Mail: {}", email);
            }
        } else {
            log.warn("Login fehlgeschlagen - E-Mail nicht gefunden: {}", email);
        }
        
        return Optional.empty();
    }

    /* Findet einen User anhand der E-Mail (für Login/Registrierung) */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /* Gibt alle Users zurück */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /* Findet einen User anhand der ID */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /* Findet einen User anhand des Usernames */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /* Speichert oder aktualisiert einen User */
    public User save(User user) {
        return userRepository.save(user);
    }

    /* Löscht einen User anhand der ID */
    public void deleteById(Long id) {
        log.info("Lösche User mit ID: {}", id);
        userRepository.deleteById(id);
    }

    /* Löscht einen User */
    public void delete(User user) {
        log.info("Lösche User: {}", user.getUsername());
        userRepository.delete(user);
    }

    /* Findet alle User mit einer bestimmten Rolle */
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /* Sucht User nach Nachname */
    public List<User> searchByLastName(String lastName) {
        return userRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    /* Sucht User nach Vorname */
    public List<User> searchByFirstName(String firstName) {
        return userRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    /* Prüft, ob ein Username bereits existiert */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /* Prüft, ob eine E-Mail bereits existiert */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /* Zählt die Anzahl aller Users */
    public long count() {
        return userRepository.count();
    }

    /* Prüft, ob ein User mit der ID existiert */
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}