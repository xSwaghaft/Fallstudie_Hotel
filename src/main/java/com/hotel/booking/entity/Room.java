package com.hotel.booking.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
    @JoinColumn(name = "category_id", nullable = false) // DB-Spaltenname
    @JsonBackReference
    private RoomCategory category;


    @Column(name = "price", nullable = false) //Hat der Raum, oder nur die Kategorie einen Preis?
    private Double price;

    @Column(name = "availability", nullable = false)
    private String availability;  // Changed to String für Available/Maintenance/Occupied

    @Column(name = "information")
    private String information;

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
    public Room(RoomCategory category, Double price, String availability) {
        this.category = category;
        this.price = price;
        this.availability = availability;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    @Override
    public String toString() {
    return "Room{" +
        "id=" + room_id +
        ", category=" + (category != null ? category.toString() : "null") +
        ", price=" + price +
        ", availability=" + availability +
        '}';
    }
}
