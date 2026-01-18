package com.hotel.booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a user in the hotel booking system.
 *
 * <p>A {@code User} is a person who can interact with the system, either as a guest making
 * reservations, a receptionist managing bookings, or a manager overseeing operations.
 * Each user has authentication credentials, a role, and personal information.
 *
 * <p><b>Roles:</b>
 * <ul>
 *   <li>{@link UserRole#GUEST} – a customer booking rooms</li>
 *   <li>{@link UserRole#RECEPTIONIST} – hotel staff handling bookings</li>
 *   <li>{@link UserRole#MANAGER} – hotel management with system access</li>
 * </ul>
 *
 * <p><b>Relationships:</b> A user may have an optional associated {@link Guest} entity
 * (one-to-one relationship) for storing guest-specific metadata.
 *
 * <p><b>Security:</b> Passwords are hashed using BCrypt and are never included in JSON serialization.
 *
 * @author Artur Derr
 * @see UserRole
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique user identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique login username. */
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /** BCrypt-hashed password, never exposed via JSON. */
    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password;

    /** Email address of the user. */
    @Column(length = 100)
    private String email;

    /** User's first name. */
    @Column(length = 100)
    private String firstName;

    /** User's last name. */
    @Column(length = 100)
    private String lastName;

    /** Embedded address information. */
    @Embedded
    private AdressEmbeddable address = new AdressEmbeddable();

    /** User's date of birth. */
    @Column
    private LocalDate birthdate;

    /** Role of this user in the system. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    /** Whether this user account is active. */
    @Column(nullable = false)
    private boolean active = true;

    /** Timestamp when this user account was created. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Protected no-arg constructor for JPA/Hibernate.
     */
    protected User() {}

    /**
     * Creates a new user with the specified details.
     *
     * @param username unique login username
     * @param firstName user's first name
     * @param lastName user's last name
     * @param address embedded address information
     * @param email user's email address
     * @param password plaintext password (will be hashed before storage)
     * @param role the user's role in the system
     * @param active whether the account is active
     */
    public User(String username, String firstName, String lastName, AdressEmbeddable address, String email, String password, UserRole role, boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Automatically sets the creation timestamp before persisting if not already set.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * Returns the full name of this user (firstName + lastName).
     *
     * @return the user's full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the address as a formatted string (e.g., "Street, House Number, Postal Code, City, Country").
     *
     * @return the formatted address, or empty string if no address is set
     */
    public String getAdressString() {
        if(address == null) {
            return "";
        }
        return address.getFormatted();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public AdressEmbeddable getAddress() {
        return address;
    }

    public void setAddress(AdressEmbeddable address) {
        this.address = address;
    }
}