package com.hotel.booking.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * Repräsentiert eine Zimmerbuchung.
 *
 * <p>
 * Enthält Metadaten wie Buchungsnummer, Zeitraum, Status und den
 * monetären Gesamtpreis sowie Referenzen zu Gast, Zimmer, Zahlungen,
 * Extras, Rechnung und Feedback.
 * </p>
 *
 * <p>
 * <strong>Persistenz-Hinweise</strong>:
 * <ul>
 * <li>Beziehungen sind so annotiert, dass Kind-Objekte (Payments/Extras)
 * standardmäßig mitpersistiert und bei Entfernung gelöscht werden
 * (orphanRemoval).</li>
 * <li>Enum-Werte werden als String gespeichert.</li>
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
public class Booking {

    /** Primärschlüssel-ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Öffentliche Buchungsnummer. */
    @Column(name = "booking_number", nullable = false, unique = true, length = 64)
    private String bookingNumber;

    /** Menge/Anzahl */
    @Column(name = "amount")
    private Integer amount;

    /** Anreisedatum (Check-in). */
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    /** Abreisedatum (Check-out). */
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    /** Aktueller Buchungsstatus. */

    // @Enumerated(EnumType.STRING)
    // @Column(name = "status", nullable = false, length = 32)
    // private BookingStatus status;

    /** Gesamtsumme der Buchung inkl. aller Positionen. */
    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    /** Zugehöriger Gast (Eigentümer der Buchung). */

    // @ManyToOne(optional = false, fetch = FetchType.LAZY)
    // @JoinColumn(name = "guest_id", nullable = false,
    // foreignKey = @ForeignKey(name = "fk_booking_guest"))
    // private Guest guest;

    /** Gebuchtes Zimmer. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_room"))
    private Room room;

    /**
     * Optionale Rechnung zur Buchung.
     * <p>
     * Wenn die Gegenseite das FK hält, nutze {@code mappedBy="booking"}.
     * </p>
     */

    // @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // @JoinColumn(name = "invoice_id",
    // foreignKey = @ForeignKey(name = "fk_booking_invoice"))
    // private Invoice invoice;

    /** Zahlungen, die auf diese Buchung verbucht wurden. */

    // @OneToMany(mappedBy = "booking",
    // cascade = CascadeType.ALL,
    // orphanRemoval = true)
    // private List<Payment> payments = new ArrayList<>();

    /** Zusatzleistungen (Extras) dieser Buchung. */

    // @OneToMany(mappedBy = "booking",
    // cascade = CascadeType.ALL,
    // orphanRemoval = true)
    // private List<BookingExtra> extras = new ArrayList<>();

    /** Optionales Feedback zur Buchung. */

    // @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY)
    // private Feedback feedback;

    // ------------------------------------------------------------
    // Konstruktoren
    // ------------------------------------------------------------

    /** Leerer Konstruktor für JPA. */
    protected Booking() {
    }

    /**
     * Komfort-Konstruktor für Pflichtangaben.
     */

    // public Booking(String bookingNumber,
    // LocalDate checkInDate,
    // LocalDate checkOutDate,
    // BookingStatus status,
    // Guest guest,
    // Room room) {
    // this.bookingNumber = bookingNumber;
    // this.checkInDate = checkInDate;
    // this.checkOutDate = checkOutDate;
    // this.status = status;
    // this.guest = guest;
    // this.room = room;
    // }

    // ------------------------------------------------------------
    // Getter/Setter
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

    // public BookingStatus getStatus() { return status; }
    // public void setStatus(BookingStatus status) { this.status = status; }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // public Guest getGuest() { return guest; }
    // public void setGuest(Guest guest) { this.guest = guest; }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    // public Invoice getInvoice() { return invoice; }
    // public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    // public List<Payment> getPayments() { return payments; }
    // public void setPayments(List<Payment> payments) { this.payments = payments; }

    // public List<BookingExtra> getExtras() { return extras; }
    // public void setExtras(List<BookingExtra> extras) { this.extras = extras; }

    // public Feedback getFeedback() { return feedback; }
    // public void setFeedback(Feedback feedback) { this.feedback = feedback; }

    // ------------------------------------------------------------
    // Hilfs- und Konsistenzmethoden
    // ------------------------------------------------------------

    /**
     * Fügt eine Zahlung hinzu und hält die bidirektionale Beziehung konsistent.
     * 
     * @param payment Zahlung, die dieser Buchung zugeordnet wird
     */

    // public void addPayment(Payment payment) {
    // if (payment == null) return;
    // payments.add(payment);
    // payment.setBooking(this);
    // }

    /**
     * Entfernt eine Zahlung und hält die bidirektionale Beziehung konsistent.
     * 
     * @param payment Zahlung, die entfernt werden soll
     */

    // public void removePayment(Payment payment) {
    // if (payment == null) return;
    // payments.remove(payment);
    // if (payment.getBooking() == this) {
    // payment.setBooking(null);
    // }
    // }

    /**
     * Fügt ein Extra hinzu und hält die bidirektionale Beziehung konsistent.
     * 
     * @param extra Extra, das hinzugefügt wird
     */
    // public void addExtra(BookingExtra extra) {
    // if (extra == null) return;
    // extras.add(extra);
    // extra.setBooking(this);
    // }

    /**
     * Entfernt ein Extra und hält die bidirektionale Beziehung konsistent.
     * 
     * @param extra Extra, das entfernt wird
     */
    // public void removeExtra(BookingExtra extra) {
    // if (extra == null) return;
    // extras.remove(extra);
    // if (extra.getBooking() == this) {
    // extra.setBooking(null);
    // }
    // }

    /**
     * Einfache Validierung: stellt sicher, dass das Check-out nach dem Check-in
     * liegt.
     * Wirft eine {@link IllegalArgumentException}, wenn die Daten inkonsistent
     * sind.
     */
    public void validateDates() {
        if (checkInDate != null && checkOutDate != null && !checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("checkOutDate must be after checkInDate");
        }
    }

}
