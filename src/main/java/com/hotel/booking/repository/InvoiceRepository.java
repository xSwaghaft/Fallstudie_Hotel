package com.hotel.booking.repository;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.PaymentStatus;
import com.hotel.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity
 * @author Arman Özcanli
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    // Find invoice by invoice number
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    // Find all invoices by status
    List<Invoice> findByInvoiceStatus(PaymentStatus status);
    
    // Find all invoices for a specific booking (using temporary bookingId field)
    Optional<Invoice> findByBookingId(Long bookingId);
    
    // Find invoices issued between dates
    List<Invoice> findByIssuedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find unpaid invoices
    List<Invoice> findByInvoiceStatusAndPaidAtIsNull(PaymentStatus status);
    
    // Find invoice by booking entity
    Optional<Invoice> findByBooking(Booking booking);
}
