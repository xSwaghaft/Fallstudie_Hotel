package com.hotel.booking.controller;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.service.InvoiceService;
import com.hotel.booking.service.InvoicePdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Controller for handling invoice PDF download requests.
 * <p>
 * Provides HTTP endpoints for downloading invoices as PDF documents. This controller
 * orchestrates the retrieval of invoice data and PDF generation, returning the generated
 * PDF file to the client with appropriate HTTP response headers.
 * </p>
 *
 * @author Arman Özcanli
 * @see InvoiceService
 * @see InvoicePdfService
 */
@RestController
@RequestMapping("/api")
public class InvoiceDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceDownloadController.class);
    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    /**
     * Constructs an InvoiceDownloadController with required dependencies.
     *
     * @param invoiceService service for retrieving invoice data
     * @param invoicePdfService service for generating invoice PDFs
     */
    public InvoiceDownloadController(InvoiceService invoiceService, InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
    }

    /**
     * Downloads an invoice as a PDF file.
     * <p>
     * Retrieves the invoice by ID, generates a PDF from the invoice data, and returns it
     * to the client with appropriate HTTP headers for file download.
     * </p>
     *
     * @param id the ID of the invoice to download
     * @return a ResponseEntity containing the PDF bytes with appropriate content type and headers
     * @apiNote The response includes a Content-Disposition header with the attachment filename
     */
    @GetMapping("/invoice/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        try {
            logger.info("Downloading PDF for invoice ID: {}", id);
            
            Invoice invoice = invoiceService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));
            
            byte[] pdfData = invoicePdfService.generateInvoicePdf(invoice);
            
            String fileName = "Invoice_" + invoice.getInvoiceNumber() + ".pdf";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(pdfData);
                    
        } catch (Exception e) {
            logger.error("Error downloading PDF for invoice ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
