package com.hotel.booking.service;

import com.hotel.booking.entity.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service class for handling invoice PDF download operations.
 * <p>
 * Provides utility methods for generating and preparing invoices for download. Acts as a
 * facade layer between controllers and the PDF generation service, simplifying the process
 * of converting invoice data to downloadable PDF files.
 * </p>
 * <ul>
 *   <li>Generate PDF content from invoice data</li>
 *   <li>Construct appropriate file names for downloads</li>
 * </ul>
 * <p>
 * All operations are delegated to the underlying InvoicePdfService for PDF generation.
 * </p>
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

    /**
     * Constructs an InvoicePdfDownloadService with required dependencies.
     *
     * @param invoiceService service for retrieving invoice data
     * @param invoicePdfService service for generating invoice PDFs
     */
    public InvoicePdfDownloadService(InvoiceService invoiceService, InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
    }

    /**
     * Generates a PDF document for a specific invoice.
     *
     * @param invoiceId the ID of the invoice to generate PDF for
     * @return the generated PDF as a byte array
     * @throws RuntimeException if the invoice cannot be found
     */
    public byte[] generatePdfForInvoice(Long invoiceId) {
        logger.info("Generating PDF for invoice ID: {}", invoiceId);
        
        Invoice invoice = invoiceService.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        return invoicePdfService.generateInvoicePdf(invoice);
    }

    /**
     * Generates an appropriate file name for downloading an invoice PDF.
     *
     * @param invoiceId the ID of the invoice
     * @return the suggested file name for the invoice PDF (e.g., "Invoice_INV-2024-12345.pdf")
     * @throws RuntimeException if the invoice cannot be found
     */
    public String getInvoiceFileName(Long invoiceId) {
        Invoice invoice = invoiceService.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        return "Invoice_" + invoice.getInvoiceNumber() + ".pdf";
    }
}
