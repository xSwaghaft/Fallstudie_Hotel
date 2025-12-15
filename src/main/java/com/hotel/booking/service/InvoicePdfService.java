package com.hotel.booking.service;

import com.hotel.booking.entity.Invoice;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class InvoicePdfService {

    private static final Logger logger = LoggerFactory.getLogger(InvoicePdfService.class);

    public byte[] generateInvoicePdf(Invoice invoice) {
        try {
            logger.info("Generating PDF for invoice: {}", invoice.getInvoiceNumber());
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Very simple content
            document.add(new Paragraph("RECHNUNG").setFontSize(24).setBold());
            document.add(new Paragraph(""));
            document.add(new Paragraph("Rechnungsnummer: " + (invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A")));
            document.add(new Paragraph("Betrag: €" + (invoice.getAmount() != null ? invoice.getAmount() : "N/A")));
            document.add(new Paragraph("Status: " + (invoice.getInvoiceStatus() != null ? invoice.getInvoiceStatus() : "N/A")));
            
            if (invoice.getIssuedAt() != null) {
                document.add(new Paragraph("Ausgestellt: " + invoice.getIssuedAt()));
            }
            
            document.close();
            logger.info("PDF generated successfully, size: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Error generating PDF", e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }
}

