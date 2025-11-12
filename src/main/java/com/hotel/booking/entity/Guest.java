package com.hotel.booking.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entity für Gäste im Hotelbuchungssystem.
 * Jeder Guest ist 1:1 mit einem User verknüpft und enthält persönliche Informationen.
 */
@Entity
@Table(name = "guests")
public class Guest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 1:1 Beziehung zum zugehörigen User
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * E-Mail-Adresse des Gastes
     */
    @Column(nullable = false, length = 100)
    private String email;

    /**
     * Vorname des Gastes
     */
    @Column(nullable = false, length = 100)
    private String firstName;

    /**
     * Nachname des Gastes
     */
    @Column(nullable = false, length = 100)
    private String lastName;

    /**
     * Adresse des Gastes
     */
    @Column(length = 255)
    private String address;

    /**
     * Telefonnummer des Gastes
     */
    @Column(length = 50)
    private String phoneNumber;

    /**
     * Geburtsdatum des Gastes
     */
    @Column
    private LocalDate birthdate;

    /**
     * Standard-Konstruktor für JPA
     */
    protected Guest() {}

    /**
     * Konstruktor mit allen Parametern
     */
    public Guest(User user, String email, String firstName, String lastName, 
                 String address, String phoneNumber, LocalDate birthdate) {
        this.user = user;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.birthdate = birthdate;
    }

    // Getter und Setter

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }
}
