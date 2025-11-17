package com.hotel.booking.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

//Matthias Lohr
@Entity
@Table(name = "room_extras")
public class BookingExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long BookingExtra_id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "extra_type", nullable = false)
    private ExtraTypeEnum extraType;

    @ManyToMany(mappedBy = "extras")
    private List<Booking> bookings = new ArrayList<>();

    // Default constructor
    public BookingExtra() {
    }

    // Parameterized constructor
    public BookingExtra(Long BookingExtra_id, String name, String description, Double price, ExtraTypeEnum extraType) {
        this.BookingExtra_id = BookingExtra_id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.extraType = extraType;
    }

    // Getters and setters
    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
    
    public Long getBookingExtra_id() {
        return BookingExtra_id;
    }

    public void setBookingExtra_id(Long BookingExtra_id) {
        this.BookingExtra_id = BookingExtra_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public ExtraTypeEnum getExtraType() {
        return extraType;
    }

    public void setExtraType(ExtraTypeEnum extraType) {
        this.extraType = extraType;
    }
}
