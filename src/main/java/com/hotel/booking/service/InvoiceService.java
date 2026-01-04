package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Invoice.PaymentMethod;
import com.hotel.booking.entity.Invoice.PaymentStatus;
import com.hotel.booking.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing invoice operations.
 * <p>
 * Handles all business logic related to invoices, including:
 * </p>
 * <ul>
 *   <li>Retrieving invoices by various criteria (ID, number, booking, status)</li>
 *   <li>Creating and updating invoices</li>
 *   <li>Generating unique invoice numbers</li>
 *   <li>Creating invoices for bookings with automatic email notifications</li>
 *   <li>Tracking pending invoices</li>
 * </ul>
 * <p>
 * Invoice numbers are generated in the format: INV-YYYY-UUID to ensure uniqueness. When new
 * invoices are created, confirmation emails are automatically sent to guests. All operations
 * are transactional to maintain data consistency.
 * </p>
 *
 * @author Arman Özcanli
 * @see Invoice
 * @see InvoiceRepository
 * @see EmailService
 * @see Booking
 */
@Service
@Transactional
public class InvoiceService {
    
    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    
    /**
     * Invoice number prefix used in the INV-YYYY-UUID format.
     */
    private static final String INVOICE_PREFIX = "INV-";
    
    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;

    /**
     * Constructs an InvoiceService with required dependencies.
     *
     * @param invoiceRepository repository for invoice persistence operations
     * @param emailService service for sending invoice-related emails
     */
    public InvoiceService(InvoiceRepository invoiceRepository, EmailService emailService) {
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
    }

    /**
     * Retrieves all invoices from the database.
     *
     * @return a list containing all invoices
     */
    @Transactional(readOnly = true)
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    /**
     * Retrieves an invoice by its unique identifier.
     *
     * @param id the invoice ID
     * @return an Optional containing the invoice if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    /**
     * Retrieves an invoice by its unique invoice number.
     *
     * @param invoiceNumber the invoice number to search for
     * @return an Optional containing the invoice if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    /**
     * Retrieves the invoice associated with a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return an Optional containing the invoice if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> findByBookingId(Long bookingId) {
        if (bookingId == null) {
            return Optional.empty();
        }
        return invoiceRepository.findByBookingId(bookingId);
    }

    /**
     * Saves or updates an invoice.
     * <p>
     * For new invoices, automatically sends a confirmation email to the associated guest.
     * Email failures are logged but do not prevent the invoice from being saved.
     * </p>
     *
     * @param invoice the invoice entity to save
     * @return the saved invoice with generated ID if applicable
     */
    @Transactional
    public Invoice save(Invoice invoice) {
        // Check if this is a new invoice (no ID yet)
        boolean isNewInvoice = invoice.getId() == null;
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Send email only for new invoices 
        if (isNewInvoice && savedInvoice.getBooking() != null 
                && savedInvoice.getBooking().getGuest() != null 
                && savedInvoice.getBooking().getGuest().getEmail() != null 
                && !savedInvoice.getBooking().getGuest().getEmail().isBlank()) {
            try {
                emailService.sendInvoiceCreated(savedInvoice);
            } catch (Exception e) {
                // Log error but don't fail the invoice save
                log.error("Failed to send invoice created email for invoice {}", savedInvoice.getId(), e);
            }
        }
        
        return savedInvoice;
    }

    /**
     * Deletes an invoice by its unique identifier.
     *
     * @param id the invoice ID to delete
     */
    public void deleteById(Long id) {
        invoiceRepository.deleteById(id);
    }

    /**
     * Retrieves the number of pending invoices that have not yet been paid.
     *
     * @return the count of invoices with PENDING status
     */
    public int getNumberOfPendingInvoices() {
        return invoiceRepository.findByInvoiceStatus(PaymentStatus.PENDING).size();
    }
    
    /**
     * Generates a unique invoice number.
     * Format: INV-YYYY-UUID
     * 
     * @return a unique invoice number
     */
    public String generateInvoiceNumber() {
        return INVOICE_PREFIX + java.time.LocalDate.now().getYear() + "-" + 
               java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Creates an invoice for a booking if it doesn't already exist.
     * 
     * @param booking the booking to create an invoice for
     * @param paymentMethod the payment method used
     * @param status the payment status (typically PAID)
     * @return the created invoice, or the existing invoice if one already exists
     */
    @Transactional
    public Invoice createInvoiceForBooking(Booking booking, PaymentMethod paymentMethod, PaymentStatus status) {
        if (booking == null || booking.getId() == null) {
            throw new IllegalArgumentException("Booking cannot be null and must have an ID");
        }
        
        if (booking.getTotalPrice() == null) {
            throw new IllegalArgumentException("Booking must have a total price");
        }
        
        // Check if invoice already exists
        Optional<Invoice> existingInvoice = findByBookingId(booking.getId());
        if (existingInvoice.isPresent()) {
            return existingInvoice.get();
        }
        
        Invoice invoice = new Invoice();
        invoice.setBooking(booking);
        invoice.setAmount(booking.getTotalPrice());
        invoice.setInvoiceStatus(status);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setInvoiceNumber(generateInvoiceNumber());
        
        return save(invoice);
    }
}
