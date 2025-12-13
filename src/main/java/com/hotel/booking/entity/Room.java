package com.hotel.booking.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
//Matthias Lohr
@Entity
@Table(name = "rooms")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long room_id;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "floor")
    private Integer floor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = true) // Kann NULL sein wenn Kategorie gelöscht wird
    @JsonBackReference
    private RoomCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "information")
    private String information;


    // TODO: Mapping überprüfen
    @JsonIgnore
    @ManyToMany
    @JoinTable(
         name = "room_bookings",
         joinColumns = @JoinColumn(name = "room_id"),
         inverseJoinColumns = @JoinColumn(name = "booking_id")
     )
    private List<Booking> booking;

    // Default constructor
    public Room() {
    }

    // Constructor with parameters
    public Room(RoomCategory category, RoomStatus status, Boolean active) {
        this.category = category;
        this.status = status;
        this.active = active;
    }

    // Getters and Setters
    public Long getId() {
        return room_id;
    }

    public void setId(Long id) {
        this.room_id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public void setCategory(RoomCategory category) {
        this.category = category;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    // ==================== JPA Lifecycle Hooks ====================

    /**
     * Synchronisiert das active-Flag basierend auf dem Status:
     * - INACTIVE → active = false
     * - Alles andere → active = true
     */
    @PrePersist
    @PreUpdate
    private void syncActiveFlag() {
        if (status != null) {
            this.active = !RoomStatus.INACTIVE.equals(status);
        }
    }

    // ==================== Object Methods ====================

    @Override
    public String toString() {
        return "Room{" +
            "id=" + room_id +
            ", category=" + (category != null ? category.toString() : "null") +
            ", status=" + status +
            ", active=" + active +
            '}';
    }
}
