package com.hotel.booking.service;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.repository.UserRepository;
import com.hotel.booking.repository.GuestRepository;
import com.hotel.booking.repository.ReportRepository;
import com.hotel.booking.repository.BookingModificationRepository;
import com.hotel.booking.repository.BookingCancellationRepository;
import com.hotel.booking.security.BcryptPasswordEncoder;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

 
/* Artur Derr
 * Service-Klasse für User-Entitäten.
 * Enthält Business-Logik, CRUD-Operationen und Authentifizierung für Benutzer. */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final ReportRepository reportRepository;
    private final BookingModificationRepository bookingModificationRepository;
    private final BookingCancellationRepository bookingCancellationRepository;
    private final BcryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GuestRepository guestRepository, 
                       ReportRepository reportRepository, BookingModificationRepository bookingModificationRepository,
                       BookingCancellationRepository bookingCancellationRepository, BcryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.guestRepository = guestRepository;
        this.reportRepository = reportRepository;
        this.bookingModificationRepository = bookingModificationRepository;
        this.bookingCancellationRepository = bookingCancellationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Demo user seeding removed; create users via SQL or admin flows if needed

    /* Authentifiziert einen User anhand von Username und Passwort */
    public Optional<User> authenticate(String username, String password) {
        log.debug("Authentifizierungsversuch für Username: {}", username);
        // Versuche zunächst Username, falls nicht gefunden -> E-Mail
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.debug("User mit Username '{}' nicht gefunden, versuche Suche per E-Mail...", username);
            userOpt = userRepository.findByEmail(username);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String storedPassword = user.getPassword();
            
            // Check ob Passwort bereits gehashed ist (BCrypt hashes beginnen mit $2a$, $2b$, $2y$)
            boolean isHashed = storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$");
            
            boolean passwordMatches = false;
            if (isHashed) {
                // Gehashtes Passwort - verwende BCrypt
                passwordMatches = passwordEncoder.matches(password, storedPassword);
            } else {
                // Plaintext Passwort - direkter Vergleich (für Migration)
                // WICHTIG: Nach Migration sollte das Passwort gehashed werden!
                passwordMatches = password.equals(storedPassword);
                
                if (passwordMatches) {
                    log.warn("User '{}' hat noch plaintext Passwort! Hashing...", username);
                    // Hash das Passwort sofort und speichere es
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    log.info("Passwort für User '{}' wurde gehashed und gespeichert", username);
                }
            }
            
            if (passwordMatches) {
                log.info("Authentifizierung erfolgreich für User: {}", username);
                return Optional.of(user);
            } else {
                log.warn("Authentifizierung fehlgeschlagen - falsches Passwort für User: {}", username);
            }
        } else {
            log.warn("Authentifizierung fehlgeschlagen - User nicht gefunden: {} (weder Username noch E-Mail)", username);
        }
        
        return Optional.empty();
    }

    /* Findet einen User anhand der E-Mail (für Login/Registrierung) */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //Methode für Booking, gibt User statt Optional zurück
    //Matthias Lohr
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null); //orElse da ein Optional<> zurückkommt
    }

    /* Gibt alle Users zurück */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /* Findet einen User anhand der ID */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /* Speichert oder aktualisiert einen User */
    public User save(User user) {
        return userRepository.save(user);
    }

    /* Erstellt einen neuen User mit Validierung - nutze diese statt registerUser() */
    public User create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User darf nicht null sein");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username ist erforderlich");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("E-Mail ist erforderlich");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Passwort ist erforderlich");
        }
        
        // Username-Validierung
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username bereits vergeben: " + user.getUsername());
        }
        
        // E-Mail-Validierung
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("E-Mail bereits registriert: " + user.getEmail());
        }
        
        // Passwort hashen
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        
        // Rolle standardmäßig auf GUEST setzen
        if (user.getRole() == null) {
            user.setRole(UserRole.GUEST);
        }
        
        User savedUser = userRepository.save(user);
        log.info("User erstellt: {}", savedUser.getUsername());
        
        return savedUser;
    }

    /* Registriert einen neuen User - Alias für create() */
    public User registerUser(User user) {
        return create(user);
    }

    /* Aktualisiert einen existierenden User */
    public User update(Long id, User userDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID muss gültig sein");
        }
        
        if (userDetails == null) {
            throw new IllegalArgumentException("User Details dürfen nicht null sein");
        }
        
        Optional<User> existingUserOpt = findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("User mit ID " + id + " nicht gefunden");
        }
        
        User existingUser = existingUserOpt.get();
        
        // Username Update mit Duplikat-Check
        if (userDetails.getUsername() != null && !userDetails.getUsername().trim().isEmpty()) {
            if (!existingUser.getUsername().equals(userDetails.getUsername()) 
                && userRepository.existsByUsername(userDetails.getUsername())) {
                throw new IllegalArgumentException("Username bereits vergeben: " + userDetails.getUsername());
            }
            existingUser.setUsername(userDetails.getUsername());
        }
        
        // E-Mail Update mit Duplikat-Check
        if (userDetails.getEmail() != null && !userDetails.getEmail().trim().isEmpty()) {
            if (!existingUser.getEmail().equals(userDetails.getEmail()) 
                && userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("E-Mail bereits registriert: " + userDetails.getEmail());
            }
            existingUser.setEmail(userDetails.getEmail());
        }
        
        // Passwort nur hashen, wenn gesetzt
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(userDetails.getPassword());
            existingUser.setPassword(hashedPassword);
        }
        
        // Update weitere Felder
        if (userDetails.getFirstName() != null) {
            existingUser.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            existingUser.setLastName(userDetails.getLastName());
        }
        if (userDetails.getBirthdate() != null) {
            existingUser.setBirthdate(userDetails.getBirthdate());
        }
        if (userDetails.getRole() != null) {
            existingUser.setRole(userDetails.getRole());
        }
        
        User updatedUser = userRepository.save(existingUser);
        log.info("User aktualisiert: {}", updatedUser.getUsername());
        
        return updatedUser;
    }

    /* Löscht einen User anhand der ID */
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID muss gültig sein");
        }
        
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User mit ID " + id + " nicht gefunden");
        }
        
        User userToDelete = userOpt.get();
        
        // 1. Lösche alle Reports, die von diesem User erstellt wurden
        List<com.hotel.booking.entity.Report> reports = reportRepository.findByCreatedBy(userToDelete);
        if (!reports.isEmpty()) {
            reportRepository.deleteAll(reports);
            log.info("Gelöschte {} Reports die von User {} erstellt wurden", reports.size(), userToDelete.getUsername());
        }
        
        // 2. Entkopple den User vom zugehörigen Guest (1:1 Beziehung)
        // Der Guest wird NICHT gelöscht - nur vom User entkoppelt, damit die Historie erhalten bleibt
        Optional<com.hotel.booking.entity.Guest> guestOpt = guestRepository.findByUserId(id);
        if (guestOpt.isPresent()) {
            com.hotel.booking.entity.Guest guest = guestOpt.get();
            // Guest vom User entkoppeln (setze user = NULL)
            guest.setUser(null);
            guestRepository.save(guest);
            log.info("Guest mit ID {} wurde vom User entkoppelt (User wird gelöscht)", guest.getId());
        }
        
        // 3. Entkopple BookingModification-Einträge (setze handled_by = NULL)
        List<com.hotel.booking.entity.BookingModification> modifications = bookingModificationRepository.findByHandledById(id);
        if (!modifications.isEmpty()) {
            for (com.hotel.booking.entity.BookingModification modification : modifications) {
                modification.setHandledBy(null);
                bookingModificationRepository.save(modification);
            }
            log.info("Entkoppelt {} BookingModification-Einträge vom User {}", modifications.size(), userToDelete.getUsername());
        }
        
        // 4. Entkopple BookingCancellation-Einträge (setze handled_by = NULL)
        List<com.hotel.booking.entity.BookingCancellation> cancellations = bookingCancellationRepository.findByHandledById(id);
        if (!cancellations.isEmpty()) {
            for (com.hotel.booking.entity.BookingCancellation cancellation : cancellations) {
                cancellation.setHandledBy(null);
                bookingCancellationRepository.save(cancellation);
            }
            log.info("Entkoppelt {} BookingCancellation-Einträge vom User {}", cancellations.size(), userToDelete.getUsername());
        }
        
        // 5. Lösche den User
        log.info("Lösche User mit ID: {} ({})", id, userToDelete.getUsername());
        userRepository.deleteById(id);
    }

    /* Löscht einen User */
    public void delete(User user) {
        log.info("Lösche User: {}", user.getUsername());
        if (user != null && user.getId() != null) {
            delete(user.getId());
        }
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
}