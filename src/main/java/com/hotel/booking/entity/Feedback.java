package com.hotel.booking.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Guest feedback entity for hotel bookings.
 * 
 * This entity represents customer feedback/reviews for completed bookings.
 * Each feedback entry includes a rating (1-5 stars) and an optional comment.
 * Feedback is linked to a specific booking and can be retrieved by guest or room category.
 * 
 * Key attributes:
 * - rating: Integer rating from 1 to 5 stars (required)
 * - comment: Optional text feedback up to 1000 characters
 * - createdAt: Timestamp when feedback was submitted
 * - booking: One-to-one relationship with the booking being reviewed
 * 
 * @author Arman Özcanli
 * @see Booking
 */
@Entity
@Table(name = "feedback")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    
    // Activate when Booking entity 
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
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
    
    
    // Activate when Booking entity
    public Booking getBooking() {
         return booking;
     }
    
     public void setBooking(Booking booking) {
         this.booking = booking;
     }
     
     /**
      * Convenience method to get the guest who created this feedback.
      * Since only the booking's guest can give feedback, this returns booking.guest.
      * @return the guest user, or null if booking is null
      */
     public User getGuest() {
         return booking != null ? booking.getGuest() : null;
     }
    
    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", bookingId=" + (booking != null ? booking.getId() : null) +
                ", guestId=" + (getGuest() != null ? getGuest().getId() : null) +'}';
    }
}
