package com.hotel.booking.service;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.User;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InvoicePdfService {

    private static final Logger logger = LoggerFactory.getLogger(InvoicePdfService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(Locale.GERMANY);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(Locale.GERMANY);

    public byte[] generateInvoicePdf(Invoice invoice) {
        try {
            logger.info("Generating PDF for invoice: {}", invoice.getInvoiceNumber());
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // ===== HEADER: HOTEL ADDRESS =====
            document.add(new Paragraph("Hotel am See").setFontSize(12).setBold());
            document.add(new Paragraph("Seestraße 42, 82211 Herrsching am Ammersee").setFontSize(10));
            document.add(new Paragraph("Tel: +49 8152 1234567 | E-Mail: info@hotel-am-see.de").setFontSize(10));
            document.add(new Paragraph("USt-IdNr: DE123456789").setFontSize(9));
            document.add(new Paragraph(""));
            
            // ===== TITLE =====
            document.add(new Paragraph("INVOICE").setFontSize(22).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(""));
            
            // ===== INVOICE DETAILS =====
            document.add(new Paragraph("Invoice Number: " + (invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A")).setFontSize(11).setBold());
            document.add(new Paragraph("Issued: " + (invoice.getIssuedAt() != null ? invoice.getIssuedAt().format(DATE_TIME_FORMATTER) : "N/A")).setFontSize(11));
            document.add(new Paragraph(""));
            
            // ===== GUEST DATA =====
            document.add(new Paragraph("GUEST").setFontSize(12).setBold());
            Booking booking = invoice.getBooking();
            if (booking != null && booking.getGuest() != null) {
                User guest = booking.getGuest();
                String guestName = (guest.getFirstName() != null ? guest.getFirstName() : "") + " " + (guest.getLastName() != null ? guest.getLastName() : "");
                document.add(new Paragraph(guestName.trim().isEmpty() ? "N/A" : guestName).setFontSize(11));
                document.add(new Paragraph(guest.getEmail() != null ? guest.getEmail() : "").setFontSize(11));
            }
            document.add(new Paragraph(""));
            
            // ===== BOOKING DETAILS =====
            document.add(new Paragraph("BOOKING DETAILS").setFontSize(12).setBold());
            if (booking != null) {
                document.add(new Paragraph("Booking Number: " + (booking.getBookingNumber() != null ? booking.getBookingNumber() : "N/A")).setFontSize(11));
                
                if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                    document.add(new Paragraph("Check-in: " + booking.getCheckInDate().format(DATE_FORMATTER)).setFontSize(11));
                    document.add(new Paragraph("Check-out: " + booking.getCheckOutDate().format(DATE_FORMATTER)).setFontSize(11));
                }
                
                document.add(new Paragraph("Total Booking Price: €" + (booking.getTotalPrice() != null ? String.format("%.2f", booking.getTotalPrice()) : "N/A")).setFontSize(11).setBold());
            }
            document.add(new Paragraph(""));
            
            // ===== INVOICE AMOUNT =====
            document.add(new Paragraph("INVOICE AMOUNT").setFontSize(12).setBold());
            document.add(new Paragraph("Total: €" + (invoice.getAmount() != null ? String.format("%.2f", invoice.getAmount()) : "N/A")).setFontSize(11).setBold());
            document.add(new Paragraph(""));
            
            // ===== PAYMENT INFORMATION =====
            document.add(new Paragraph("PAYMENT INFORMATION").setFontSize(12).setBold());
            document.add(new Paragraph("Payment Method: " + (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "N/A")).setFontSize(11));
            document.add(new Paragraph(""));
            
            // ===== BANK DETAILS =====
            document.add(new Paragraph("BANK DETAILS").setFontSize(11).setBold());
            document.add(new Paragraph("Account Holder: Hotel am See GmbH").setFontSize(10));
            document.add(new Paragraph("IBAN: DE89 3704 0044 0532 0130 00").setFontSize(10));
            document.add(new Paragraph("BIC: COBADEFF").setFontSize(10));
            document.add(new Paragraph(""));
            
            // ===== PAYMENT STATUS & DEADLINE =====
            document.add(new Paragraph("PAYMENT STATUS").setFontSize(12).setBold());
            boolean isPaid = invoice.getInvoiceStatus() == Invoice.PaymentStatus.PAID;
            
            if (isPaid) {
                document.add(new Paragraph("Status: PAID").setFontSize(11));
                if (invoice.getPaidAt() != null) {
                    document.add(new Paragraph("Paid on: " + invoice.getPaidAt().format(DATE_TIME_FORMATTER)).setFontSize(11));
                }
            } else {
                document.add(new Paragraph("Status: PENDING").setFontSize(11));
                // Deadline: 14 days after invoice date
                if (invoice.getIssuedAt() != null) {
                    LocalDate deadline = invoice.getIssuedAt().toLocalDate().plusDays(14);
                    document.add(new Paragraph("Payment Deadline: " + deadline.format(DATE_FORMATTER)).setFontSize(11));
                }
            }
            document.add(new Paragraph(""));
            
            // ===== FOOTER =====
            document.add(new Paragraph("Thank you for your stay!").setTextAlignment(TextAlignment.CENTER).setFontSize(10));
            document.add(new Paragraph(""));
            document.add(new Paragraph("---").setTextAlignment(TextAlignment.CENTER).setFontSize(9));
            document.add(new Paragraph("Hotel am See GmbH | Managing Director: Max Mustermann | Munich Regional Court HRB 123456").setTextAlignment(TextAlignment.CENTER).setFontSize(8));
            
            document.close();
            logger.info("PDF generated successfully, size: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Error generating PDF", e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }
}



