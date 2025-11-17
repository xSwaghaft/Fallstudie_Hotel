package com.hotel.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    
    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;
    
    @Size(max = 1000)
    @Column(length = 1000)
    private String comment;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Relationships to Booking and Guest (remove duplicate id columns)
     @OneToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "booking_id")
     private Booking booking;
    
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "guest_id")
     private Guest guest;
    
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
    
    // Getter/setters for relationships
    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }
    
    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", bookingId=" + (booking != null ? booking.getId() : null) +
                ", guestId=" + (guest != null ? guest.getId() : null) +
                '}';
    }
}
