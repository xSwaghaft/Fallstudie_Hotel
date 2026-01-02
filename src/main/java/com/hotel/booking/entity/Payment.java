package com.hotel.booking.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payment transaction entity for hotel bookings.
 * 
 * This entity represents individual payment transactions made towards booking invoices.
 * Multiple payments can be made for a single booking to handle partial payments and refunds.
 * Each payment transaction is tracked with its amount, method, status, and transaction reference.
 * 
 * Key attributes:
 * - amount: Payment amount in currency (precision 10, scale 2)
 * - method: Payment method used (CARD, CASH, INVOICE, TRANSFER)
 * - status: Current payment status (PENDING, PAID, FAILED, REFUNDED, PARTIAL)
 * - transactionRef: External transaction reference from payment provider (max 100 characters)
 * - paidAt: Timestamp when payment was processed
 * - refundedAmount: Amount refunded (may be less than original for partial refunds)
 * - booking: Many-to-one relationship with the associated booking
 * 
 * @author Arman Özcanli
 * @see Booking
 * @see Invoice
 */
@Entity
@Table(name = "payments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    /**
     * Zahlungsmethode: CARD, CASH, INVOICE, TRANSFER
     */
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Invoice.PaymentMethod method;
    
    /**
     * Zahlungsstatus: PENDING, PAID, FAILED, REFUNDED, PARTIAL
     */
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Invoice.PaymentStatus status;
    
    //Referenz ID des Zahlungsanbieters
    @Size(max = 100)
    @Column
    private String transactionRef;
    
    @Column
    private LocalDateTime paidAt;
    
    /**
     * Betrag, der bei Refund zurückerstattet wird (kann weniger als amount sein bei Stornierungsgebühren)
     */
    @DecimalMin("0.00")
    @Column(precision = 10, scale = 2)
    private BigDecimal refundedAmount;
    
    // Activate when Booking entity 
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    
    
    // Default constructor
    public Payment() {
        this.status = Invoice.PaymentStatus.PENDING;
    }
    
    // Constructor with parameters
    public Payment(BigDecimal amount, Invoice.PaymentMethod method) {
        this.method = method;
        this.status = Invoice.PaymentStatus.PENDING;
        setAmount(amount);
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
        if (amount == null) {
            this.amount = null;
            return;
        }
        // Geld-Berechnung auf 2 Dezimalstellen runden
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    public Invoice.PaymentMethod getMethod() {
        return method;
    }
    
    public void setMethod(Invoice.PaymentMethod method) {
        this.method = method;
    }
    
    public Invoice.PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(Invoice.PaymentStatus status) {
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
    
    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }
    
    public void setRefundedAmount(BigDecimal refundedAmount) {
        if (refundedAmount == null) {
            this.refundedAmount = null;
            return;
        }
        // Geld-Berechnung auf 2 Dezimalstellen runden
        this.refundedAmount = refundedAmount.setScale(2, RoundingMode.HALF_UP);
    }
    
    
    
    
    
    // Activate when Booking entity
    public Booking getBooking() {
         return booking;
     }
    
     public void setBooking(Booking booking) {
         this.booking = booking;
     }
    
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", amount=" + amount +
                ", method=" + method +
                ", status=" + status +
                ", transactionRef='" + transactionRef + '\'' +
                ", paidAt=" + paidAt +
                ", bookingId=" + booking +
                '}';
    }
}