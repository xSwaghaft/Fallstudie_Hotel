package com.hotel.booking.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/* Artur Derr
 * Entity für Berichte/Reports im Hotelbuchungssystem.
 * Jeder Report wird von einem User erstellt und enthält Titel, Beschreibung und Zeitstempel. */
@Entity
@Table(name = "reports")
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Titel des Reports
    @Column(nullable = false, length = 200)
    private String title;

    // Detaillierte Beschreibung des Reports
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Zeitpunkt der Erstellung des Reports
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // User, der diesen Report erstellt hat (n:1 Beziehung)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    // Standard-Konstruktor für JPA
    protected Report() {}

    // Konstruktor mit allen Parametern
    public Report(String title, String description, User createdBy) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Setzt createdAt automatisch vor dem Persistieren
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getter und Setter

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
