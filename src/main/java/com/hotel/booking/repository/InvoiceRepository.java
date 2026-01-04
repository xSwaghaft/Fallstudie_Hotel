package com.hotel.booking.repository;

import com.hotel.booking.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity persistence operations.
 * <p>
 * Provides database access methods for invoice data. Supports querying invoices by number,
 * status, booking, date range, and associated room. Includes methods for finding unpaid
 * invoices and retrieving invoices by various criteria. Extends JpaRepository to provide
 * standard CRUD operations.
 * </p>
 *
 * @author Arman Özcanli
 * @see Invoice
 * @see InvoiceService
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Finds an invoice by its unique invoice number.
     *
     * @param invoiceNumber the invoice number to search for
     * @return an Optional containing the invoice if found
     */
    // Find invoice by invoice number
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    /**
     * Finds all invoices with a specific payment status.
     *
     * @param invoiceStatus the payment status to filter by
     * @return a list of invoices with the specified status
     */
    // Find all invoices by status
    List<Invoice> findByInvoiceStatus(Invoice.PaymentStatus invoiceStatus);
    
    /**
     * Finds the invoice associated with a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return an Optional containing the invoice if it exists
     */
    // Find all invoices for a specific booking
    @Query("SELECT i FROM Invoice i WHERE i.booking.id = :bookingId")
    Optional<Invoice> findByBookingId(@Param("bookingId") Long bookingId);
    
    /**
     * Finds all invoices issued within a specified date range.
     *
     * @param start the start date (inclusive)
     * @param end the end date (inclusive)
     * @return a list of invoices issued between the specified dates
     */
    // Find invoices issued between dates
    List<Invoice> findByIssuedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Finds all unpaid invoices with a specific status that have not been marked as paid.
     *
     * @param invoiceStatus the invoice status to filter by
     * @return a list of unpaid invoices with the specified status
     */
    // Find unpaid invoices
    List<Invoice> findByInvoiceStatusAndPaidAtIsNull(Invoice.PaymentStatus invoiceStatus);
    
    /**
     * Finds all invoices for bookings associated with a specific room.
     *
     * @param roomId the ID of the room
     * @return a list of invoices for bookings in the specified room
     */
    // Find all invoices for a specific room (via booking)
    @Query("SELECT i FROM Invoice i WHERE i.booking.room.id = :roomId")
    List<Invoice> findByBookingRoomId(@Param("roomId") Long roomId);
    
    /**
     * Finds the invoice associated with a booking entity.
     *
     * @param booking the booking entity
     * @return an Optional containing the invoice if it exists
     */
    // Find invoice by booking entity
    Optional<Invoice> findByBooking(com.hotel.booking.entity.Booking booking);
}
