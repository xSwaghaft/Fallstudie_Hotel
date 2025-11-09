package com.hotel.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment transaction for a booking
 * @author Arman Özcanli
 */
@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Column
    private String transactionRef;
    
    @Column
    private LocalDateTime paidAt;
    
    // TODO: Activate when Booking entity is created
    // @ManyToOne
    // @JoinColumn(name = "booking_id", nullable = false)
    // private Booking booking;
    
    // Temporary field until Booking is ready
    @Column(name = "booking_id")
    private Long bookingId;
    
    // Default constructor
    public Payment() {
        this.status = PaymentStatus.PENDING;
    }
    
    // Constructor with parameters
    public Payment(BigDecimal amount, PaymentMethod method) {
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getMethod() {
        return method;
    }
    
    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getTransactionRef() {
        return transactionRef;
    }
    
    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    // Temporary getter/setter for bookingId
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    // TODO: Activate when Booking entity is created
    // public Booking getBooking() {
    //     return booking;
    // }
    
    // public void setBooking(Booking booking) {
    //     this.booking = booking;
    // }
    
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", amount=" + amount +
                ", method=" + method +
                ", status=" + status +
                ", transactionRef='" + transactionRef + '\'' +
                ", paidAt=" + paidAt +
                ", bookingId=" + bookingId +
                '}';
    }
    
    // Enums
    public enum PaymentMethod {
        CARD,
        CASH,
        ONLINE,
        INVOICE
    }
    
    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED,
        REFUNDED
    }
}