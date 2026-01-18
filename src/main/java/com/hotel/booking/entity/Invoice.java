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
 * Invoice entity for hotel bookings.
 * 
 * This entity represents financial invoices generated for hotel bookings.
 * Each invoice corresponds to exactly one booking and tracks payment status and method.
 * Invoices can be issued as PDF documents and support multiple payment methods and statuses.
 * 
 * Key attributes:
 * - invoiceNumber: Unique invoice identifier (max 50 characters)
 * - amount: Total invoice amount in currency (precision 10, scale 2)
 * - paymentMethod: Payment method used (CARD, CASH, INVOICE, TRANSFER)
 * - invoiceStatus: Current payment status (PENDING, PAID, FAILED, REFUNDED, PARTIAL)
 * - issuedAt: Timestamp when invoice was created
 * - paidAt: Timestamp when invoice was paid (nullable)
 * - booking: One-to-one relationship with the associated booking
 * 
 * @author Arman Özcanli
 * @see Booking
 * @see Payment
 */
@Entity
@Table(name = "invoices")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique invoice number assigned to this invoice.
     * Format typically: INV-YYYY-UUID
     * Maximum length of 50 characters.
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    /**
     * Total invoice amount in currency.
     * Stored with precision of 10 digits and 2 decimal places (e.g., 9999999.99).
     * Must be greater than or equal to 0.00.
     */
    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Timestamp when the invoice was issued.
     * Automatically set to the current date and time upon creation.
     */
    @Column(nullable = false)
    private LocalDateTime issuedAt;

    /**
     * Timestamp when the invoice was paid.
     * Null if the invoice has not been paid yet.
     */
    @Column
    private LocalDateTime paidAt;

    /**
     * Payment method used for this invoice.
     * <p>
     * Possible values: CARD, CASH, INVOICE, TRANSFER
     * </p>
     * Defaults to CARD.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Invoice.PaymentMethod paymentMethod = Invoice.PaymentMethod.CARD;

    /**
     * Current payment status of this invoice.
     * <p>
     * Possible values: PENDING, PAID, FAILED, REFUNDED, PARTIAL
     * </p>
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus invoiceStatus;

    /**
     * The booking associated with this invoice.
     * One-to-one relationship: each invoice corresponds to exactly one booking.
     */
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_invoice_booking"))
    private Booking booking;
    

    /**
     * Constructs a default Invoice instance.
     * <p>
     * Automatically sets:
     * </p>
     * <ul>
     *   <li>issuedAt to the current date and time</li>
     *   <li>invoiceStatus to PENDING</li>
     * </ul>
     */
    public Invoice() {
        this.issuedAt = LocalDateTime.now();
        this.invoiceStatus = PaymentStatus.PENDING;
    }

    /**
     * Constructs an Invoice instance with invoice number, amount, and payment method.
     *
     * @param invoiceNumber the unique invoice number
     * @param amount the invoice amount
     * @param paymentMethod the payment method used
     */
    public Invoice(String invoiceNumber, BigDecimal amount, PaymentMethod paymentMethod) {
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.issuedAt = LocalDateTime.now();
        this.invoiceStatus = PaymentStatus.PENDING;
    }

    // ===== GETTERS AND SETTERS =====
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
    

    /**
     * Gets the booking associated with this invoice.
     *
     * @return the associated booking, or null if not set
     */
    public Booking getBooking() {
        return booking;
    }

    /**
     * Sets the booking associated with this invoice.
     *
     * @param booking the booking to associate with this invoice
     */
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
     * Payment method enumeration for invoices.
     * <p>
     * Defines the available payment methods that can be used for invoice payments:
     * </p>
     * <ul>
     *   <li>CARD: Credit or debit card payment</li>
     *   <li>CASH: Cash payment</li>
     *   <li>INVOICE: Invoice-based payment (payment on account)</li>
     *   <li>TRANSFER: Bank transfer payment</li>
     * </ul>
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
     * Payment status enumeration for invoices.
     * <p>
     * Defines the possible payment states of an invoice:
     * </p>
     * <ul>
     *   <li>PENDING: Invoice has been issued but payment is not yet received</li>
     *   <li>PAID: Invoice has been fully paid</li>
     *   <li>FAILED: Payment attempt failed</li>
     *   <li>REFUNDED: Payment has been refunded to the customer</li>
     *   <li>PARTIAL: Partial payment has been received</li>
     * </ul>
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