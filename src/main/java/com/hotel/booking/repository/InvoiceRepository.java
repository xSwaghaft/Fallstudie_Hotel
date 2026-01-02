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
 * 
 * This repository provides database access methods for invoice data.
 * It supports querying invoices by number, status, booking, date range, and associated room.
 * Includes methods for finding unpaid invoices and retrieving invoices by various criteria.
 * Extends JpaRepository to provide standard CRUD operations.
 * 
 * @author Arman Özcanli
 * @see Invoice
 * @see InvoiceService
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    // Find invoice by invoice number
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    // Find all invoices by status
    List<Invoice> findByInvoiceStatus(Invoice.PaymentStatus invoiceStatus);
    
    // Find all invoices for a specific booking
    @Query("SELECT i FROM Invoice i WHERE i.booking.id = :bookingId")
    Optional<Invoice> findByBookingId(@Param("bookingId") Long bookingId);
    
    // Find invoices issued between dates
    List<Invoice> findByIssuedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find unpaid invoices
    List<Invoice> findByInvoiceStatusAndPaidAtIsNull(Invoice.PaymentStatus invoiceStatus);
    
    // Find all invoices for a specific room (via booking)
    @Query("SELECT i FROM Invoice i WHERE i.booking.room.id = :roomId")
    List<Invoice> findByBookingRoomId(@Param("roomId") Long roomId);
    
    // Find invoice by booking entity
    Optional<Invoice> findByBooking(com.hotel.booking.entity.Booking booking);
}
