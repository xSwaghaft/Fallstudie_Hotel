package com.hotel.booking.service;

import com.hotel.booking.entity.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
