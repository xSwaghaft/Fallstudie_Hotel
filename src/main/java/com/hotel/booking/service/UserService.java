package com.hotel.booking.service;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.repository.UserRepository;
import com.hotel.booking.repository.BookingModificationRepository;
import com.hotel.booking.repository.BookingCancellationRepository;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.security.BcryptPasswordEncoder;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for user entity management and authentication.
 * 
 * This service provides business logic, CRUD operations, and authentication
 * functionality for user management. It handles user registration, authentication,
 * password hashing, and user-related database operations.
 * 
 * @author Artur Derr
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final BookingModificationRepository bookingModificationRepository;
    private final BookingCancellationRepository bookingCancellationRepository;
    private final BookingRepository bookingRepository;
    private final BcryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       BookingModificationRepository bookingModificationRepository,
                       BookingCancellationRepository bookingCancellationRepository, BookingRepository bookingRepository,
                       BcryptPasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.bookingModificationRepository = bookingModificationRepository;
        this.bookingCancellationRepository = bookingCancellationRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Authenticates a user by username or email and password.
     * 
     * This method attempts to authenticate a user using either username or email
     * address along with the provided password. If the password is stored in plain text,
     * it automatically hashes it and saves the update. BCrypt hashing is used for
     * password comparison and storage.
     * 
     * @param username the username or email address of the user
     * @param password the plain text password to verify
     * @return an Optional containing the authenticated user if credentials are valid,
     *         otherwise an empty Optional
     */
    public Optional<User> authenticate(String username, String password) {
        log.debug("Authentication attempt for username: {}", username);
        // Try username first, if not found -> try email
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.debug("User with username '{}' not found, trying email search...", username);
            userOpt = userRepository.findByEmail(username);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String storedPassword = user.getPassword();
            
            // Check if password is already hashed (BCrypt hashes start with $2a$, $2b$, $2y$)
            boolean isHashed = storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$");
            
            boolean passwordMatches = false;
            if (isHashed) {
                // Hashed password - use BCrypt
                passwordMatches = passwordEncoder.matches(password, storedPassword);
            } else {
                // Plaintext password - direct comparison (for migration)
                // IMPORTANT: After migration, the password should be hashed!
                passwordMatches = password.equals(storedPassword);
                
                if (passwordMatches) {
                    log.warn("User '{}' still has plaintext password! Hashing now...", username);
                    // Hash the password immediately and save it
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    log.info("Password for user '{}' has been hashed and saved", username);
                }
            }
            
            if (passwordMatches) {
                if (!user.isActive()) {
                    log.warn("Authentication blocked - user is inactive: {}", username);
                    throw new IllegalStateException("User account is inactive");
                }
                log.info("Authentication successful for user: {}", username);
                return Optional.of(user);
            } else {
                log.warn("Authentication failed - wrong password for user: {}", username);
            }
        } else {
            log.warn("Authentication failed - user not found: {} (neither username nor email)", username);
        }
        
        return Optional.empty();
    }

    /**
     * Finds a user by email address.
     * 
     * Used for login and registration operations to locate a user
     * by their email address.
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, otherwise an empty Optional
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds a user by email address and returns the user object directly.
     * 
     * This method is used for booking operations and returns the user
     * instead of an Optional wrapper. Returns null if the user is not found.
     * 
     * @param email the email address to search for
     * @return the user if found, otherwise null
     * @author Matthias Lohr
     */
    public User findUserByEmail(String email) {
        return findByEmail(email).orElse(null);
    }

    /**
     * Retrieves all users from the database.
     * 
     * @return a list of all users in the system
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by their ID.
     * 
     * @param id the unique identifier of the user
     * @return an Optional containing the user if found, otherwise an empty Optional
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Saves or updates a user.
     * 
     * @param user the user object to save or update
     * @return the saved or updated user object
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Creates a new user with comprehensive validation.
     * 
     * This method is the preferred way to create a new user instead of
     * registerUser(). It validates all required fields, checks for duplicate
     * username and email, hashes the password using BCrypt, and sets the
     * default role to GUEST.
     * 
     * @param user the user object containing user details
     * @return the newly created user object
     * @throws IllegalArgumentException if validation fails (null fields,
     *         duplicate username/email, etc.)
     */
    public User create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        // Username validation
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + user.getUsername());
        }
        
        // Email validation
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }
        
        // Hash password
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        
        // Set role to GUEST by default
        if (user.getRole() == null) {
            user.setRole(UserRole.GUEST);
        }
        
        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUsername());
        
        // Viktor Götting Send welcome email
        if (savedUser.getEmail() != null && !savedUser.getEmail().isBlank()) {
            try {
                emailService.sendWelcomeEmail(savedUser);
            } catch (Exception e) {
                // Log error but don't fail the user creation
                log.warn("Failed to send welcome email to {}: {}", savedUser.getEmail(), e.getMessage());
            }
        }
        
        return savedUser;
    }

    /**
     * Registers a new user.
     * 
     * This is an alias method for create(). Prefer using create() directly.
     * 
     * @param user the user object containing user details
     * @return the newly registered user object
     */
    public User registerUser(User user) {
        return create(user);
    }

    /**
     * Updates an existing user with new details.
     * 
     * This method updates a user's information including username, email,
     * password, name, birthdate, and role. It validates all inputs and
     * checks for duplicate username/email with other users.
     * 
     * @param id the ID of the user to update
     * @param userDetails the object containing the new user details
     * @return the updated user object
     * @throws IllegalArgumentException if the user is not found or validation fails
     */
    public User update(Long id, User userDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be valid");
        }
        
        if (userDetails == null) {
            throw new IllegalArgumentException("User details must not be null");
        }
        
        Optional<User> existingUserOpt = findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        
        User existingUser = existingUserOpt.get();
        
        // Username update with duplicate check
        if (userDetails.getUsername() != null && !userDetails.getUsername().trim().isEmpty()) {
            if (!existingUser.getUsername().equals(userDetails.getUsername()) 
                && userRepository.existsByUsername(userDetails.getUsername())) {
                throw new IllegalArgumentException("Username already taken: " + userDetails.getUsername());
            }
            existingUser.setUsername(userDetails.getUsername());
        }
        
        // Email update with duplicate check
        if (userDetails.getEmail() != null && !userDetails.getEmail().trim().isEmpty()) {
            if (!existingUser.getEmail().equals(userDetails.getEmail()) 
                && userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Email already registered: " + userDetails.getEmail());
            }
            existingUser.setEmail(userDetails.getEmail());
        }
        
        // Hash password only if set
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(userDetails.getPassword());
            existingUser.setPassword(hashedPassword);
        }
        
        // Update additional fields
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
        log.info("User updated: {}", updatedUser.getUsername());
        
        return updatedUser;
    }

    /**
     * Deletes a user by their ID.
     * 
     * This method safely deletes a user and handles all related data
     * (booking modifications and cancellations) by decoupling them
     * from the user before deletion.
     * 
     * @param id the ID of the user to delete
     * @throws IllegalArgumentException if the user ID is invalid or user not found
     */
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be valid");
        }
        
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        
        User userToDelete = userOpt.get();
        
        // 2. Decouple BookingModification entries (set handled_by = NULL)
        List<com.hotel.booking.entity.BookingModification> modifications = bookingModificationRepository.findByHandledById(id);
        if (!modifications.isEmpty()) {
            for (com.hotel.booking.entity.BookingModification modification : modifications) {
                modification.setHandledBy(null);
            }
            bookingModificationRepository.saveAll(modifications);
            log.info("Decoupled {} BookingModification entries from user {}", modifications.size(), userToDelete.getUsername());
        }
        
        // 3. Decouple BookingCancellation entries (set handled_by = NULL)
        List<com.hotel.booking.entity.BookingCancellation> cancellations = bookingCancellationRepository.findByHandledById(id);
        if (!cancellations.isEmpty()) {
            for (com.hotel.booking.entity.BookingCancellation cancellation : cancellations) {
                cancellation.setHandledBy(null);
            }
            bookingCancellationRepository.saveAll(cancellations);
            log.info("Decoupled {} BookingCancellation entries from user {}", cancellations.size(), userToDelete.getUsername());
        }
        
        // 4. Delete the user
        log.info("Deleting user with ID: {} ({})", id, userToDelete.getUsername());
        userRepository.deleteById(id);
    }

    /**
     * Deletes a user object.
     * 
     * @param user the user object to delete
     */
    public void delete(User user) {
        if (user == null) {
            return;
        }

        log.info("Deleting user: {}", user.getUsername());
        if (user.getId() != null) {
            delete(user.getId());
        }
    }

    /**
     * Sets a user account to inactive (soft delete).
     *
     * @param userId the ID of the user to deactivate
     * @throws IllegalArgumentException if the user is not found
     */
    public void setInactive(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be valid");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        if (!user.isActive()) {
            return;
        }

        user.setActive(false);
        userRepository.save(user);
        log.info("User set to inactive: {} (ID {})", user.getUsername(), userId);
    }

    /**
     * Finds all users with a specific role.
     * 
     * @param role the user role to search for
     * @return a list of all users with the specified role
     */
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Searches for users by last name.
     * 
     * The search is case-insensitive and supports partial matches.
     * 
     * @param lastName the last name to search for
     * @return a list of users matching the search criteria
     */
    public List<User> searchByLastName(String lastName) {
        return userRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    /**
     * Searches for users by first name.
     * 
     * The search is case-insensitive and supports partial matches.
     * 
     * @param firstName the first name to search for
     * @return a list of users matching the search criteria
     */
    public List<User> searchByFirstName(String firstName) {
        return userRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    /**
     * Checks if a username already exists in the system.
     * 
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Checks if an email address already exists in the system.
     * 
     * @param email the email address to check
     * @return true if the email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Checks whether a user can be deleted and returns the deletion action status.
     * 
     * This method verifies if there are any dependencies (e.g., bookings)
     * that would prevent user deletion.
     * 
     * @param userId the ID of the user to check
     * @return a DeleteAction object indicating if deletion is blocked and the reason
     * @throws IllegalArgumentException if the user is not found
     */
    public DeleteAction getDeletionAction(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Match RoomManagement behavior:
            // - If user is ACTIVE, allow "soft delete" (set inactive) regardless of bookings.
            // - Only block PERMANENT deletion for INACTIVE users if bookings reference them.
            if (!user.isActive()) {
                List<com.hotel.booking.entity.Booking> bookings = bookingRepository.findByGuest_Id(userId);
                if (bookings != null && !bookings.isEmpty()) {
                    return new DeleteAction(true,
                        "Cannot delete user \"" + user.getUsername() + "\": " + bookings.size() + " bookings reference this user",
                        "Cannot Delete User");
                }
            }
            
            return new DeleteAction(false, null, null);
        }
        
        throw new IllegalArgumentException("User with ID " + userId + " not found");
    }

    // ==================== Inner Class ====================

    /**
     * Inner class representing the result of a user deletion check.
     * 
     * This class encapsulates information about whether a user can be deleted
     * and provides error messages and dialog titles if deletion is blocked.
     */
    public static class DeleteAction {
        public final boolean isBlocked;
        public final String errorMessage;
        public final String dialogTitle;

        public DeleteAction(boolean isBlocked, String errorMessage, String dialogTitle) {
            this.isBlocked = isBlocked;
            this.errorMessage = errorMessage;
            this.dialogTitle = dialogTitle;
        }
    }
}