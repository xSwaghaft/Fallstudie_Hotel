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
 * <p>
 * Represents customer feedback and reviews submitted for completed bookings. Each feedback
 * entry includes a star rating (1-5) and an optional textual comment. Feedback is linked
 * to a specific booking and can be retrieved by guest or room category.
 * </p>
 * <ul>
 *   <li>rating: integer rating from 1 to 5 stars (required)</li>
 *   <li>comment: optional text feedback up to 1000 characters</li>
 *   <li>createdAt: timestamp when feedback was submitted</li>
 *   <li>booking: one-to-one relationship with the booking being reviewed</li>
 * </ul>
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
    
    /**
     * The star rating of this feedback (1-5 stars).
     * Must be between 1 and 5 (inclusive).
     */
    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;
    
    /**
     * Optional comment or review text provided by the guest.
     * Maximum length of 1000 characters.
     */
    @Size(max = 1000)
    @Column(length = 1000)
    private String comment;
    
    /**
     * Timestamp when the feedback was submitted.
     * Automatically set to the current date and time upon creation.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * The booking this feedback is associated with.
     * One-to-one relationship: each booking can have one feedback entry.
     */
    // Activate when Booking entity 
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    /**
     * Constructs a default Feedback instance.
     * Automatically sets createdAt to the current date and time.
     */
    public Feedback() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Constructs a Feedback instance with rating and comment.
     *
     * @param rating the star rating (1-5)
     * @param comment the optional feedback comment
     */
    public Feedback(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }
    
    // ===== GETTERS AND SETTERS =====
    
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
    
    /**
     * Gets the booking associated with this feedback.
     *
     * @return the associated booking, or null if not set
     */
    // Activate when Booking entity
    public Booking getBooking() {
         return booking;
     }
    
    /**
     * Sets the booking associated with this feedback.
     *
     * @param booking the booking to associate with this feedback
     */
     public void setBooking(Booking booking) {
         this.booking = booking;
     }
     
     /**
      * Gets the guest who submitted this feedback.
      * <p>
      * Convenience method that retrieves the guest from the associated booking.
      * Since only the booking's guest can submit feedback, this returns booking.guest.
      * </p>
      *
      * @return the guest user, or null if no booking is associated
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
