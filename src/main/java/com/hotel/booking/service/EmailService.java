package com.hotel.booking.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.entity.Invoice;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending email messages.
 * 
 * <p>
 * Provides functionality to send both plain text and HTML emails.
 * Uses Spring's JavaMailSender for email delivery. The sender address
 * can be configured via application properties (app.mail.from).
 * </p>
 * 
 * @author Viktor Götting
 */
@Service
public class EmailService {

    private final JavaMailSender emailSender;

    /** Default sender email address, configurable via app.mail.from property. */
    @Value("${app.mail.from:no-reply@example.com}")
    private String defaultFrom;

    /**
     * Constructs an EmailService with the given JavaMailSender.
     * 
     * @param emailSender the mail sender instance
     */
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * Sends a plain text email message.
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param text plain text message body
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(defaultFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    /**
     * Sends an HTML email message.
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param htmlBody HTML message body
     * @throws MessagingException if the message cannot be created or sent
     */
    public void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setFrom(defaultFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indicates HTML
        emailSender.send(mimeMessage);
    }

    /**
     * Sends an HTML email message with attachment.
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param htmlBody HTML message body
     * @param attachment attachment data
     * @param attachmentName name of the attachment file
     * @param contentType MIME type of the attachment
     * @throws MessagingException if the message cannot be created or sent
     */
    public void sendHtmlMessageWithAttachment(String to, String subject, String htmlBody, 
            byte[] attachment, String attachmentName, String contentType) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
        helper.setFrom(defaultFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indicates HTML
        helper.addAttachment(attachmentName, () -> new java.io.ByteArrayInputStream(attachment), contentType);
        emailSender.send(mimeMessage);
    }

    // ==================== Booking-related emails ====================

    /**
     * Sends a booking confirmation email to the guest.
     * 
     * @param booking the confirmed booking
     * @throws MessagingException if the email cannot be sent
     */
    public void sendBookingConfirmation(Booking booking) throws MessagingException {
        if (booking == null || booking.getGuest() == null) {
            return;
        }
        String email = booking.getGuest().getEmail();
        String subject = "Booking Confirmation - " + booking.getBookingNumber();
        String htmlBody = buildBookingConfirmationTemplate(booking);
        sendHtmlMessage(email, subject, htmlBody);
    }

    /**
     * Sends a booking modification notification email to the guest.
     * 
     * @param booking the modified booking
     * @param modification the modification details
     * @throws MessagingException if the email cannot be sent
     */
    public void sendBookingModification(Booking booking, BookingModification modification) throws MessagingException {
        if (booking == null || booking.getGuest() == null || modification == null) {
            return;
        }
        String email = booking.getGuest().getEmail();
        String subject = "Booking Modified - " + booking.getBookingNumber();
        String htmlBody = buildBookingModificationTemplate(booking, modification);
        sendHtmlMessage(email, subject, htmlBody);
    }

    /**
     * Sends a booking cancellation notification email to the guest.
     * 
     * @param booking the cancelled booking
     * @param cancellation the cancellation details
     * @throws MessagingException if the email cannot be sent
     */
    public void sendBookingCancellation(Booking booking, BookingCancellation cancellation) throws MessagingException {
        if (booking == null || booking.getGuest() == null || cancellation == null) {
            return;
        }
        String email = booking.getGuest().getEmail();
        String subject = "Booking Cancelled - " + booking.getBookingNumber();
        String htmlBody = buildBookingCancellationTemplate(booking, cancellation);
        sendHtmlMessage(email, subject, htmlBody);
    }

    // ==================== Invoice-related emails ====================

    /**
     * Sends an invoice notification email to the guest with PDF attachment.
     * 
     * @param invoice the invoice
     * @throws MessagingException if the email cannot be sent
     */
    public void sendInvoiceCreated(Invoice invoice) throws MessagingException {
        if (invoice == null || invoice.getBooking() == null || invoice.getBooking().getGuest() == null) {
            return;
        }
        String email = invoice.getBooking().getGuest().getEmail();
        String subject = "Invoice - " + invoice.getInvoiceNumber();
        String htmlBody = buildInvoiceTemplate(invoice);
        
        // Generate PDF and attach it
        byte[] pdfBytes = generateInvoicePdf(invoice);
        sendHtmlMessageWithAttachment(email, subject, htmlBody, pdfBytes, 
            "invoice_" + invoice.getInvoiceNumber() + ".pdf", "application/pdf");
    }

    // ==================== Email template builders ====================

    /**
     * Builds HTML template for booking confirmation email.
     */
    private String buildBookingConfirmationTemplate(Booking booking) {
        String guestName = booking.getGuest() != null ? escapeHtml(booking.getGuest().getUsername()) : "Guest";
        String bookingNumber = escapeHtml(booking.getBookingNumber());
        String checkIn = booking.getCheckInDate() != null ? booking.getCheckInDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "N/A";
        String checkOut = booking.getCheckOutDate() != null ? booking.getCheckOutDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "N/A";
        String roomCategory = booking.getRoomCategory() != null ? escapeHtml(booking.getRoomCategory().getName()) : "N/A";
        String roomNumber = booking.getRoom() != null && booking.getRoom().getRoomNumber() != null ? escapeHtml(booking.getRoom().getRoomNumber()) : "TBD";
        String totalPrice = booking.getTotalPrice() != null ? booking.getTotalPrice().toString() : "0.00";
        String amount = booking.getAmount() != null ? booking.getAmount().toString() : "1";

        return String.format("""
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Booking Confirmation</title>
            </head>
            <body style="font-family:Arial,sans-serif;color:#333;margin:0;padding:0;background:#f5f5f5">
                <div style="max-width:600px;margin:20px auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff">
                    <h2 style="color:#1a73e8;margin-top:0">Booking Confirmation</h2>
                    <p>Dear %s,</p>
                    <p>Your booking has been confirmed! We look forward to welcoming you.</p>
                    
                    <div style="background:#f9f9f9;padding:15px;border-radius:6px;margin:20px 0">
                        <h3 style="margin-top:0;color:#333">Booking Details</h3>
                        <p><strong>Booking Number:</strong> %s</p>
                        <p><strong>Check-in:</strong> %s</p>
                        <p><strong>Check-out:</strong> %s</p>
                        <p><strong>Room Category:</strong> %s</p>
                        <p><strong>Room Number:</strong> %s</p>
                        <p><strong>Guests:</strong> %s</p>
                        <p><strong>Total Price:</strong> €%s</p>
                    </div>
                    
                    <p>If you have any questions, please don't hesitate to contact us.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, guestName, bookingNumber, checkIn, checkOut, roomCategory, roomNumber, amount, totalPrice);
    }

    /**
     * Builds HTML template for booking modification email.
     */
    private String buildBookingModificationTemplate(Booking booking, BookingModification modification) {
        String guestName = booking.getGuest() != null ? escapeHtml(booking.getGuest().getUsername()) : "Guest";
        String bookingNumber = escapeHtml(booking.getBookingNumber());
        String fieldChanged = escapeHtml(modification.getFieldChanged() != null ? modification.getFieldChanged() : "Details");
        String oldValue = escapeHtml(modification.getOldValue() != null ? modification.getOldValue() : "N/A");
        String newValue = escapeHtml(modification.getNewValue() != null ? modification.getNewValue() : "N/A");
        String reason = escapeHtml(modification.getReason() != null ? modification.getReason() : "No reason provided");

        return String.format("""
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Booking Modified</title>
            </head>
            <body style="font-family:Arial,sans-serif;color:#333;margin:0;padding:0;background:#f5f5f5">
                <div style="max-width:600px;margin:20px auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff">
                    <h2 style="color:#ff9800;margin-top:0">Booking Modified</h2>
                    <p>Dear %s,</p>
                    <p>Your booking has been modified. Please review the changes below.</p>
                    
                    <div style="background:#fff3cd;padding:15px;border-radius:6px;margin:20px 0;border-left:4px solid #ff9800">
                        <h3 style="margin-top:0;color:#333">Modification Details</h3>
                        <p><strong>Booking Number:</strong> %s</p>
                        <p><strong>Field Changed:</strong> %s</p>
                        <p><strong>Previous Value:</strong> %s</p>
                        <p><strong>New Value:</strong> %s</p>
                        <p><strong>Reason:</strong> %s</p>
                    </div>
                    
                    <p>If you have any questions about these changes, please contact us.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, guestName, bookingNumber, fieldChanged, oldValue, newValue, reason);
    }

    /**
     * Builds HTML template for booking cancellation email.
     */
    private String buildBookingCancellationTemplate(Booking booking, BookingCancellation cancellation) {
        String guestName = booking.getGuest() != null ? escapeHtml(booking.getGuest().getUsername()) : "Guest";
        String bookingNumber = escapeHtml(booking.getBookingNumber());
        String reason = escapeHtml(cancellation.getReason() != null ? cancellation.getReason() : "No reason provided");
        String refundAmount = cancellation.getRefundedAmount() != null ? cancellation.getRefundedAmount().toString() : "0.00";

        return String.format("""
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Booking Cancelled</title>
            </head>
            <body style="font-family:Arial,sans-serif;color:#333;margin:0;padding:0;background:#f5f5f5">
                <div style="max-width:600px;margin:20px auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff">
                    <h2 style="color:#d32f2f;margin-top:0">Booking Cancelled</h2>
                    <p>Dear %s,</p>
                    <p>Your booking has been cancelled.</p>
                    
                    <div style="background:#ffebee;padding:15px;border-radius:6px;margin:20px 0;border-left:4px solid #d32f2f">
                        <h3 style="margin-top:0;color:#333">Cancellation Details</h3>
                        <p><strong>Booking Number:</strong> %s</p>
                        <p><strong>Reason:</strong> %s</p>
                        <p><strong>Refund Amount:</strong> €%s</p>
                    </div>
                    
                    <p>We hope to welcome you again in the future.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, guestName, bookingNumber, reason, refundAmount);
    }

    /**
     * Builds HTML template for invoice email.
     */
    private String buildInvoiceTemplate(Invoice invoice) {
        Booking booking = invoice.getBooking();
        String guestName = booking != null && booking.getGuest() != null ? escapeHtml(booking.getGuest().getUsername()) : "Guest";
        String invoiceNumber = escapeHtml(invoice.getInvoiceNumber());
        String amount = invoice.getAmount() != null ? invoice.getAmount().toString() : "0.00";
        String issuedAt = invoice.getIssuedAt() != null ? invoice.getIssuedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "N/A";
        String paymentMethod = invoice.getPaymentMethod() != null ? escapeHtml(invoice.getPaymentMethod().toString()) : "N/A";
        String status = invoice.getInvoiceStatus() != null ? escapeHtml(invoice.getInvoiceStatus().toString()) : "PENDING";
        String bookingNumber = booking != null ? escapeHtml(booking.getBookingNumber()) : "N/A";

        return String.format("""
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Invoice</title>
            </head>
            <body style="font-family:Arial,sans-serif;color:#333;margin:0;padding:0;background:#f5f5f5">
                <div style="max-width:600px;margin:20px auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff">
                    <h2 style="color:#1a73e8;margin-top:0">Invoice</h2>
                    <p>Dear %s,</p>
                    <p>Please find your invoice details below.</p>
                    
                    <div style="background:#f9f9f9;padding:15px;border-radius:6px;margin:20px 0">
                        <h3 style="margin-top:0;color:#333">Invoice Details</h3>
                        <p><strong>Invoice Number:</strong> %s</p>
                        <p><strong>Booking Number:</strong> %s</p>
                        <p><strong>Amount:</strong> €%s</p>
                        <p><strong>Payment Method:</strong> %s</p>
                        <p><strong>Status:</strong> %s</p>
                        <p><strong>Issued At:</strong> %s</p>
                    </div>
                    
                    <p>Please find the invoice PDF attached to this email.</p>
                    <p>Please ensure payment is made by the due date.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, guestName, invoiceNumber, bookingNumber, amount, paymentMethod, status, issuedAt);
    }

    /**
     * Generates a PDF document for the given invoice.
     * 
     * @param invoice the invoice to generate PDF for
     * @return PDF as byte array
     * @throws MessagingException if PDF generation fails
     */
    private byte[] generateInvoicePdf(Invoice invoice) throws MessagingException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Invoice details
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Booking booking = invoice.getBooking();
            String guestName = booking != null && booking.getGuest() != null 
                ? booking.getGuest().getUsername() : "Guest";
            String invoiceNumber = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A";
            String bookingNumber = booking != null && booking.getBookingNumber() != null 
                ? booking.getBookingNumber() : "N/A";
            String amount = invoice.getAmount() != null ? invoice.getAmount().toString() : "0.00";
            String paymentMethod = invoice.getPaymentMethod() != null 
                ? invoice.getPaymentMethod().toString() : "N/A";
            String status = invoice.getInvoiceStatus() != null 
                ? invoice.getInvoiceStatus().toString() : "PENDING";
            String issuedAt = invoice.getIssuedAt() != null 
                ? invoice.getIssuedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "N/A";

            document.add(new Paragraph("Invoice Number: ", labelFont));
            document.add(new Paragraph(invoiceNumber, valueFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Guest Name: ", labelFont));
            document.add(new Paragraph(guestName, valueFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Booking Number: ", labelFont));
            document.add(new Paragraph(bookingNumber, valueFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Amount: ", labelFont));
            document.add(new Paragraph("€" + amount, valueFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Payment Method: ", labelFont));
            document.add(new Paragraph(paymentMethod, valueFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Status: ", labelFont));
            document.add(new Paragraph(status, valueFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Issued At: ", labelFont));
            document.add(new Paragraph(issuedAt, valueFont));
            document.add(new Paragraph(" "));

            // Footer
            Paragraph footer = new Paragraph("Thank you for your business!", valueFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new MessagingException("Failed to generate PDF", e);
        }
    }

    /**
     * Escapes HTML special characters to prevent XSS attacks.
     */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
