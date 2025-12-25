package com.hotel.booking.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Represents a room booking.
 *
 * <p>
 * Contains metadata such as booking number, time period, status, and the
 * total monetary price, as well as references to guest, room, payments,
 * extras, invoice, and feedback.
 * </p>
 *
 * <p>
 * <strong>Persistence Notes</strong>:
 * <ul>
 * <li>Relationships are annotated so that child objects (Payments/Extras)
 * are persisted by default and deleted on removal (orphanRemoval).</li>
 * <li>Enum values are stored as strings.</li>
 * </ul>
 * </p>
 *
 * @author Viktor Götting
 * @since 1.0
 */
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_booking_number", columnList = "booking_number", unique = true),
        @Index(name = "idx_booking_dates", columnList = "check_in_date,check_out_date")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Booking {

    /** Primary key ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public booking number. */
    @Column(name = "booking_number", nullable = false, length = 64)
    private String bookingNumber;

    /** Quantity/Amount */
    @Column(name = "amount")
    private Integer amount;

    /** Check-in date. */
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    /** Check-out date. */
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    /** Current booking status. */

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private BookingStatus status;

    /** Total price of the booking including all items. */
    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    /** Associated guest (owner of the booking). */

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_booking_guest"))
    private User guest;

    /** Booked room. */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_room"))
    private Room room = new Room();

    /** Room category. */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "room_category_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_booking_room_category"))
    private RoomCategory roomCategory = new RoomCategory();

    /** Associated invoice. */

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_id",
            foreignKey = @ForeignKey(name = "fk_booking_invoice"))
    private Invoice invoice;

    /** Payments that have been booked to this booking. */

    @OneToMany(mappedBy = "booking",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    /** Extras for this booking. */

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
    name = "booking_extra",
    joinColumns = @JoinColumn(name = "booking_id"),
    inverseJoinColumns = @JoinColumn(name = "extra_id")
    )
    private Set<BookingExtra> extras = new HashSet<>();

    /** Optional feedback for the booking. */

    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY)
    private Feedback feedback;

    /** Creation date of the booking. */
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    /** Empty constructor for JPA. */
    protected Booking() {
    }

    /**
     * Convenience constructor for required fields.
     */

    public Booking(String bookingNumber,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    BookingStatus status,
    User guest,
    RoomCategory roomCategory) {
    this.bookingNumber = bookingNumber;
    this.checkInDate = checkInDate;
    this.checkOutDate = checkOutDate;
    this.status = status;
    this.guest = guest;
    this.roomCategory = roomCategory;
    }

    // ------------------------------------------------------------
    // Getters/Setters
    // ------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public User getGuest() { return guest; }
    public void setGuest(User user) { this.guest = user; }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public RoomCategory getRoomCategory() {
        return roomCategory;
    }

    public void setRoomCategory(RoomCategory roomCategory) {
        this.roomCategory = roomCategory;
    }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public Set<BookingExtra> getExtras() { return extras; }
    public void setExtras(Set<BookingExtra> extras) { this.extras = extras; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
    
    //Methode soll beim ersten persistieren einmalig ausgeführt werden -> Lifecycle
    //Matthias Lohr
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }


    /**
     * Simple validation: ensures that check-out date is after check-in date.
     * Throws an {@link IllegalArgumentException} if the data is inconsistent.
     */
    public void validateDates() {
        if (checkInDate != null && checkOutDate != null && !checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("checkOutDate must be after checkInDate");
        }
    }

}
