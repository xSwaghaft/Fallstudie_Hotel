package com.hotel.booking.service;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Invoice.PaymentStatus;
import com.hotel.booking.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;

    public InvoiceService(InvoiceRepository invoiceRepository, EmailService emailService) {
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

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
                System.err.println("Failed to send invoice created email: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return savedInvoice;
    }

    public void deleteById(Long id) {
        invoiceRepository.deleteById(id);
    }

    public int getNumberOfPendingInvoices() {
        return invoiceRepository.findByInvoiceStatus(PaymentStatus.PENDING).size();
    }
}
