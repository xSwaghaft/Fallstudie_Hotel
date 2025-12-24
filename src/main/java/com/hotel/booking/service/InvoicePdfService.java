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

            // ===== HEADER: HOTEL ADRESSE =====
            document.add(new Paragraph("Hotel am See").setFontSize(12).setBold());
            document.add(new Paragraph("Seestraße 42, 82211 Herrsching am Ammersee").setFontSize(10));
            document.add(new Paragraph("Tel: +49 8152 1234567 | E-Mail: info@hotel-am-see.de").setFontSize(10));
            document.add(new Paragraph("USt-IdNr: DE123456789").setFontSize(9));
            document.add(new Paragraph(""));
            
            // ===== TITLE =====
            document.add(new Paragraph("RECHNUNG").setFontSize(22).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(""));
            
            // ===== RECHNUNG DETAILS =====
            document.add(new Paragraph("Rechnungsnummer: " + (invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A")).setFontSize(11).setBold());
            document.add(new Paragraph("Ausgestellt: " + (invoice.getIssuedAt() != null ? invoice.getIssuedAt().format(DATE_TIME_FORMATTER) : "N/A")).setFontSize(11));
            document.add(new Paragraph(""));
            
            // ===== GAST DATEN =====
            document.add(new Paragraph("GAST").setFontSize(12).setBold());
            Booking booking = invoice.getBooking();
            if (booking != null && booking.getGuest() != null) {
                User guest = booking.getGuest();
                String guestName = (guest.getFirstName() != null ? guest.getFirstName() : "") + " " + (guest.getLastName() != null ? guest.getLastName() : "");
                document.add(new Paragraph(guestName.trim().isEmpty() ? "N/A" : guestName).setFontSize(11));
                document.add(new Paragraph(guest.getEmail() != null ? guest.getEmail() : "").setFontSize(11));
            }
            document.add(new Paragraph(""));
            
            // ===== BUCHUNGS DETAILS =====
            document.add(new Paragraph("BUCHUNGSDETAILS").setFontSize(12).setBold());
            if (booking != null) {
                document.add(new Paragraph("Buchungsnummer: " + (booking.getBookingNumber() != null ? booking.getBookingNumber() : "N/A")).setFontSize(11));
                
                if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                    document.add(new Paragraph("Check-in: " + booking.getCheckInDate().format(DATE_FORMATTER)).setFontSize(11));
                    document.add(new Paragraph("Check-out: " + booking.getCheckOutDate().format(DATE_FORMATTER)).setFontSize(11));
                }
                
                document.add(new Paragraph("Gesamtpreis Buchung: €" + (booking.getTotalPrice() != null ? String.format("%.2f", booking.getTotalPrice()) : "N/A")).setFontSize(11).setBold());
            }
            document.add(new Paragraph(""));
            
            // ===== RECHNUNGSBETRAG =====
            document.add(new Paragraph("RECHNUNGSBETRAG").setFontSize(12).setBold());
            document.add(new Paragraph("Summe: €" + (invoice.getAmount() != null ? String.format("%.2f", invoice.getAmount()) : "N/A")).setFontSize(11).setBold());
            document.add(new Paragraph(""));
            
            // ===== ZAHLUNGSINFORMATIONEN =====
            document.add(new Paragraph("ZAHLUNGSINFORMATIONEN").setFontSize(12).setBold());
            document.add(new Paragraph("Zahlungsart: " + (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "N/A")).setFontSize(11));
            document.add(new Paragraph(""));
            
            // ===== BANK DATEN =====
            document.add(new Paragraph("BANKVERBINDUNG").setFontSize(11).setBold());
            document.add(new Paragraph("Kontoinhaber: Hotel am See GmbH").setFontSize(10));
            document.add(new Paragraph("IBAN: DE89 3704 0044 0532 0130 00").setFontSize(10));
            document.add(new Paragraph("BIC: COBADEFF").setFontSize(10));
            document.add(new Paragraph(""));
            
            // ===== PAYMENT STATUS & DEADLINE =====
            document.add(new Paragraph("ZAHLUNGSSTATUS").setFontSize(12).setBold());
            boolean isPaid = invoice.getInvoiceStatus() == Invoice.PaymentStatus.PAID;
            
            if (isPaid) {
                document.add(new Paragraph("Status: BEZAHLT").setFontSize(11));
                if (invoice.getPaidAt() != null) {
                    document.add(new Paragraph("Bezahlt am: " + invoice.getPaidAt().format(DATE_TIME_FORMATTER)).setFontSize(11));
                }
            } else {
                document.add(new Paragraph("Status: AUSSTEHEND").setFontSize(11));
                // Deadline: 14 Tage nach Rechnungsdatum
                if (invoice.getIssuedAt() != null) {
                    LocalDate deadline = invoice.getIssuedAt().toLocalDate().plusDays(14);
                    document.add(new Paragraph("Zahlungsfrist: " + deadline.format(DATE_FORMATTER)).setFontSize(11));
                }
            }
            document.add(new Paragraph(""));
            
            // ===== FOOTER =====
            document.add(new Paragraph("Vielen Dank für Ihren Aufenthalt!").setTextAlignment(TextAlignment.CENTER).setFontSize(10));
            document.add(new Paragraph(""));
            document.add(new Paragraph("---").setTextAlignment(TextAlignment.CENTER).setFontSize(9));
            document.add(new Paragraph("Hotel am See GmbH | Geschäftsführer: Max Mustermann | Amtsgericht München HRB 123456").setTextAlignment(TextAlignment.CENTER).setFontSize(8));
            
            document.close();
            logger.info("PDF generated successfully, size: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Error generating PDF", e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }
}



