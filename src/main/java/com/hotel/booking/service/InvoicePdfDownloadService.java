package com.hotel.booking.service;

import com.hotel.booking.entity.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service class for handling invoice PDF download operations.
 * 
 * This service provides utility methods for generating and preparing invoices for download.
 * It acts as a facade layer between controllers and the PDF generation service,
 * simplifying the process of converting invoice data to downloadable PDF files.
 * 
 * Main responsibilities:
 * - Generate PDF content from invoice data
 * - Construct appropriate file names for downloads
 * 
 * All operations are delegated to the underlying InvoicePdfService for PDF generation.
 * 
 * @author Arman Özcanli
 * @see InvoicePdfService
 * @see InvoiceService
 * @see Invoice
 */
@Service
public class InvoicePdfDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(InvoicePdfDownloadService.class);
    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    public InvoicePdfDownloadService(InvoiceService invoiceService, InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
    }

    public byte[] generatePdfForInvoice(Long invoiceId) {
        logger.info("Generating PDF for invoice ID: {}", invoiceId);
        
        Invoice invoice = invoiceService.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        return invoicePdfService.generateInvoicePdf(invoice);
    }

    public String getInvoiceFileName(Long invoiceId) {
        Invoice invoice = invoiceService.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        return "Invoice_" + invoice.getInvoiceNumber() + ".pdf";
    }
}
