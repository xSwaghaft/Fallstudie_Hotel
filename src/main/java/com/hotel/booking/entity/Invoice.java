package com.hotel.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
    
    @NotNull
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String invoiceNumber;
    
    @NotNull
    @DecimalMin("0.00")
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
    
    // Link to booking (use relationship, remove temporary bookingId column)
     @OneToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "booking_id", nullable = false)
     private Booking booking;
    
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(id, invoice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Getter/setter for booking relationship
    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
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
                ", bookingId=" + (booking != null ? booking.getId() : null) +
                '}';
    }
    
    // Enums are centralized in separate files: PaymentMethod, PaymentStatus
}