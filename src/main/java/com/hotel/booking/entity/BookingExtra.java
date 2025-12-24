package com.hotel.booking.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Represents an extra service or amenity that can be added to a hotel booking.
 * <p>
 * This entity is mapped to the <code>room_extras</code> table and can be associated with multiple bookings.
 * </p>
 *
 * @author Matthias Lohr
 */
@Entity
@Table(name = "room_extras")
public class BookingExtra {


    /**
     * Unique identifier for the booking extra.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long BookingExtra_id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;


    /**
     * Indicates if the price is per person (true) or per booking (false).
     */
    @Column(name = "per_person", nullable = false)
    private boolean perPerson = true;


    /**
     * List of bookings that include this extra.
     * <p>
     * This side is ignored during JSON serialization to prevent infinite recursion in bidirectional relationships.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "extras", fetch = FetchType.EAGER)
    private List<Booking> bookings = new ArrayList<>();



    /**
     * Default constructor.
     */
    public BookingExtra() {
    }


    /**
     * Constructs a BookingExtra with all main fields.
     *
     * @param BookingExtra_id unique identifier
     * @param name name of the extra
     * @param description description of the extra
     * @param price price of the extra
     */
    public BookingExtra(Long BookingExtra_id, String name, String description, Double price) {
        this.BookingExtra_id = BookingExtra_id;
        this.name = name;
        this.description = description;
        this.price = price;
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

    public boolean isPerPerson() {
        return perPerson;
    }

    public void setPerPerson(boolean perPerson) {
        this.perPerson = perPerson;
    }
}
