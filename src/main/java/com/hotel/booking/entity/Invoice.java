package com.hotel.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Invoice for a booking
 * @author Arman Özcanli
 */
@Entity
@Table(name = "invoices")
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String invoiceNumber;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime issuedAt;
    
    @Column
    private LocalDateTime paidAt;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus invoiceStatus;
    
    // TODO: Activate when Booking entity is created
    // @OneToOne
    // @JoinColumn(name = "booking_id", nullable = false)
    // private Booking booking;
    
    // Temporary field until Booking is ready
    @Column(name = "booking_id")
    private Long bookingId;
    
    // Default constructor
    public Invoice() {
        this.issuedAt = LocalDateTime.now();
        this.invoiceStatus = PaymentStatus.PENDING;
    }
    
    // Constructor with parameters
    public Invoice(String invoiceNumber, BigDecimal amount, PaymentMethod paymentMethod) {
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.issuedAt = LocalDateTime.now();
        this.invoiceStatus = PaymentStatus.PENDING;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getInvoiceStatus() {
        return invoiceStatus;
    }
    
    public void setInvoiceStatus(PaymentStatus invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
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
        return "Invoice{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", amount=" + amount +
                ", issuedAt=" + issuedAt +
                ", paidAt=" + paidAt +
                ", paymentMethod=" + paymentMethod +
                ", invoiceStatus=" + invoiceStatus +
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