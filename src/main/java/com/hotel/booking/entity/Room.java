package com.hotel.booking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Represents a hotel room entity.
 * <p>
 * This class contains details about a room, such as its number, floor, category, status, and associated bookings.
 * It is mapped to the <code>rooms</code> table in the database.
 * </p>
 *
 * @author Matthias Lohr
 */
@Entity
@Table(name = "rooms")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Room {


    /**
     * Unique identifier for the room.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long room_id;


    /**
     * Room number (e.g., "101").
     */
    @Column(name = "room_number", nullable = false)
    private String roomNumber;


    /**
     * Floor on which the room is located.
     */
    @Column(name = "floor")
    private Integer floor;


    /**
     * Category of the room (e.g., single, double, suite).
     * Can be null if the category is deleted.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = true)
    @JsonBackReference
    private RoomCategory category;


    /**
     * Current status of the room (e.g., AVAILABLE, OCCUPIED, INACTIVE).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status;


    /**
     * Indicates if the room is active (true) or inactive (false).
     */
    @Column(name = "active", nullable = false)
    private Boolean active;


    /**
     * Additional information about the room.
     */
    @Column(name = "information")
    private String information;


    /**
     * Default constructor.
     */
    public Room() {
    }


    /**
     * Constructs a Room with the specified category, status, and active flag.
     *
     * @param category the room category
     * @param status   the room status
     * @param active   whether the room is active
     */
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
     * Synchronizes the active flag based on the room status:
     * - INACTIVE → active = false
     * - Any other status → active = true
     * This method is called before persisting or updating the entity.
     */
    @PrePersist
    @PreUpdate
    private void syncActiveFlag() {
        if (status != null) {
            this.active = !RoomStatus.INACTIVE.equals(status);
        }
    }

    // ==================== Object Methods ====================


    /**
     * Returns a string representation of the Room object.
     * @return string representation
     */
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
