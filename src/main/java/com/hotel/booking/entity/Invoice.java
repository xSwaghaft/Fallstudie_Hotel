package com.hotel.booking.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Invoice for a booking
 * @author Arman Özcanli
 */
@Entity
@Table(name = "invoices")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Size(max = 50)
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;
    
    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime issuedAt;
    
    @Column
    private LocalDateTime paidAt;
    
    /**
     * Zahlungsmethode: CARD, CASH, INVOICE, TRANSFER
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Invoice.PaymentMethod paymentMethod = Invoice.PaymentMethod.CARD;
    
    /**
     * Rechnungsstatus: PENDING, PAID, FAILED, REFUNDED, PARTIAL
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus invoiceStatus;
    
    /** Associated booking. */
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_invoice_booking"))
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
    
    
    // Activate when Booking entity
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
                ", bookingId=" + booking +
                '}';
    }

     /**
     * Zahlungsmethode: CARD, CASH, INVOICE, TRANSFER
     */
    public enum PaymentMethod {
        CARD("Card"),
        CASH("Cash"),
        INVOICE("Invoice"),
        TRANSFER("Bank Transfer");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Rechnungsstatus: PENDING, PAID, FAILED, REFUNDED, PARTIAL
     */
    public enum PaymentStatus {
        PENDING("Pending"),
        PAID("Paid"),
        FAILED("Failed"),
        REFUNDED("Refunded"),
        PARTIAL("Partial");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}