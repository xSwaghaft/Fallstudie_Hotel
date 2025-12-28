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

@RestController
@RequestMapping("/api")
public class InvoiceDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceDownloadController.class);
    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceDownloadController(InvoiceService invoiceService, InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
    }

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
