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
 * <p>
 * Represents individual payment transactions made towards booking invoices. Multiple payments
 * can be made for a single booking to handle partial payments and refunds. Each payment
 * transaction is tracked with its amount, method, status, and transaction reference.
 * </p>
 * <ul>
 *   <li>amount: payment amount in currency (precision 10, scale 2)</li>
 *   <li>method: payment method used (CARD, CASH, INVOICE, TRANSFER)</li>
 *   <li>status: current payment status (PENDING, PAID, FAILED, REFUNDED, PARTIAL)</li>
 *   <li>transactionRef: external transaction reference from payment provider (max 100 characters)</li>
 *   <li>paidAt: timestamp when payment was processed</li>
 *   <li>refundedAmount: amount refunded (may be less than original for partial refunds)</li>
 *   <li>booking: many-to-one relationship with the associated booking</li>
 * </ul>
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
    
    /**
     * Payment amount in currency.
     * Stored with precision of 10 digits and 2 decimal places (e.g., 9999999.99).
     * Must be greater than or equal to 0.00.
     */
    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    /**
     * Payment method used for this transaction.
     * <p>
     * Possible values: CARD, CASH, INVOICE, TRANSFER
     * </p>
     */
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Invoice.PaymentMethod method;
    
    /**
     * Current status of this payment transaction.
     * <p>
     * Possible values: PENDING, PAID, FAILED, REFUNDED, PARTIAL
     * </p>
     */
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Invoice.PaymentStatus status;
    
    /**
     * External transaction reference from the payment provider.
     * Used for tracking and reconciliation with payment gateways.
     * Maximum length of 100 characters.
     */
    @Size(max = 100)
    @Column
    private String transactionRef;
    
    /**
     * Timestamp when the payment was processed.
     * Null if the payment has not been completed yet.
     */
    @Column
    private LocalDateTime paidAt;
    
    /**
     * Amount refunded for this payment.
     * May be less than the original amount in case of partial refunds or cancellation fees.
     * Stored with precision of 10 digits and 2 decimal places.
     */
    @DecimalMin("0.00")
    @Column(precision = 10, scale = 2)
    private BigDecimal refundedAmount;
    
    /**
     * The booking associated with this payment.
     * Many-to-one relationship: a booking can have multiple payments.
     */
    // Activate when Booking entity 
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    /**
     * Constructs a default Payment instance.
     * <p>
     * Automatically sets status to PENDING.
     * </p>
     */
    public Payment() {
        this.status = Invoice.PaymentStatus.PENDING;
    }
    
    /**
     * Constructs a Payment instance with amount and payment method.
     *
     * @param amount the payment amount
     * @param method the payment method used
     */
    public Payment(BigDecimal amount, Invoice.PaymentMethod method) {
        this.method = method;
        this.status = Invoice.PaymentStatus.PENDING;
        setAmount(amount);
    }
    
    // ===== GETTERS AND SETTERS =====
    
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