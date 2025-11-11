package com.hotel.booking.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity für Benutzer im Hotelbuchungssystem.
 * Enthält Authentifizierungsdaten, Rolle und persönliche Informationen.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Eindeutiger Benutzername für Login
     */
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /**
     * Passwort (Demo: im Klartext, kein Spring Security)
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * E-Mail-Adresse des Benutzers
     */
    @Column(length = 100)
    private String email;

    /**
     * Vorname des Benutzers
     */
    @Column(length = 100)
    private String firstName;

    /**
     * Nachname des Benutzers
     */
    @Column(length = 100)
    private String lastName;

    /**
     * Geburtsdatum des Benutzers
     */
    @Column
    private LocalDate birthdate;

    /**
     * Rolle des Benutzers (GUEST, RECEPTIONIST, MANAGER, ADMIN)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    /**
     * Zeitpunkt der Erstellung des Benutzerkontos
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Liste der vom User erstellten Reports (1:n Beziehung)
     */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Report> reports = new ArrayList<>();

    /**
     * Zugeordneter Guest (1:1 Beziehung, optional)
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Guest guest;

    /**
     * Standard-Konstruktor für JPA
     */
    protected User() {}

    /**
     * Konstruktor für einfache User-Erstellung (kompatibel mit bestehendem Code)
     */
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Setzt createdAt automatisch vor dem Persistieren
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * Hilfsmethode zum Hinzufügen eines Reports
     */
    public void addReport(Report report) {
        reports.add(report);
        report.setCreatedBy(this);
    }

    /**
     * Hilfsmethode zum Entfernen eines Reports
     */
    public void removeReport(Report report) {
        reports.remove(report);
        report.setCreatedBy(null);
    }

    // Getter und Setter

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }
}