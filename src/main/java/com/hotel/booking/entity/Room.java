package com.hotel.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
//Matthias Lohr
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long room_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // DB-Spaltenname
    private RoomCategory category;


    @Column(name = "price", nullable = false) //Hat der Raum, oder nur die Kategorie einen Preis?
    private Double price;

    @Column(name = "availability", nullable = false)
    private Boolean availability;

    @Column(name = "information")
    private String information;

    // @ManyToMany
    // @JoinTable(
    //     name = "room_bookings",
    //     joinColumns = @JoinColumn(name = "room_id"),
    //     inverseJoinColumns = @JoinColumn(name = "booking_id")
    // )
    // private List<Booking> booking;

    // Default constructor
    public Room() {
    }

    // Constructor with parameters
    public Room(RoomCategory category, Double price, Boolean availability) {
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

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
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
