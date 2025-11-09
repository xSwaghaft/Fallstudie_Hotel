package com.hotel.booking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Guest feedback for a booking
 * @author Arman Özcanli
 */
@Entity
@Table(name = "feedback")
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(length = 1000)
    private String comment;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // TODO: Activate when Booking entity is created
    // @OneToOne
    // @JoinColumn(name = "booking_id")
    // private Booking booking;
    
    // TODO: Activate when Guest entity is created
    // @ManyToOne
    // @JoinColumn(name = "guest_id")
    // private Guest guest;
    
    // Temporary fields until Booking/Guest are ready
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "guest_id")
    private Long guestId;
    
    // Default constructor
    public Feedback() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public Feedback(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Temporary getters/setters for IDs
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public Long getGuestId() {
        return guestId;
    }
    
    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }
    
    // TODO: Activate when Booking entity is created
    // public Booking getBooking() {
    //     return booking;
    // }
    
    // public void setBooking(Booking booking) {
    //     this.booking = booking;
    // }
    
    // TODO: Activate when Guest entity is created
    // public Guest getGuest() {
    //     return guest;
    // }
    
    // public void setGuest(Guest guest) {
    //     this.guest = guest;
    // }
    
    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", bookingId=" + bookingId +
                ", guestId=" + guestId +
                '}';
    }
}
