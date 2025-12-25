package com.hotel.booking.service;

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
import com.hotel.booking.repository.BookingModificationRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final InvoicePdfService invoicePdfService;
    private final BookingModificationRepository modificationRepository;

    /** Default sender email address, configurable via app.mail.from property. */
    @Value("${app.mail.from:no-reply@example.com}")
    private String defaultFrom;

    /**
     * Constructs an EmailService with the given JavaMailSender and InvoicePdfService.
     * 
     * @param emailSender the mail sender instance
     * @param invoicePdfService the PDF service for generating invoice PDFs
     * @param modificationRepository the booking modification repository
     */
    public EmailService(JavaMailSender emailSender, InvoicePdfService invoicePdfService, BookingModificationRepository modificationRepository) {
        this.emailSender = emailSender;
        this.invoicePdfService = invoicePdfService;
        this.modificationRepository = modificationRepository;
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
     * @param modification the modification details (used to find all modifications at the same time)
     * @throws MessagingException if the email cannot be sent
     */
    public void sendBookingModification(Booking booking, BookingModification modification) throws MessagingException {
        if (booking == null || booking.getGuest() == null || modification == null || booking.getId() == null) {
            return;
        }
        String email = booking.getGuest().getEmail();
        String subject = "Booking Modified - " + booking.getBookingNumber();
        String htmlBody = buildBookingModificationTemplate(booking, modification.getModifiedAt());
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

    // ==================== User registration emails ====================

    /**
     * Sends a welcome email to a newly registered user.
     * 
     * @param user the newly registered user
     * @throws MessagingException if the email cannot be sent
     */
    public void sendWelcomeEmail(com.hotel.booking.entity.User user) throws MessagingException {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        String email = user.getEmail();
        String subject = "Welcome to HotelBookingApp";
        String htmlBody = buildWelcomeEmailTemplate(user);
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
        
        // Generate PDF using InvoicePdfService and attach it
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoice);
        sendHtmlMessageWithAttachment(email, subject, htmlBody, pdfBytes, 
            "invoice_" + invoice.getInvoiceNumber() + ".pdf", "application/pdf");
    }

    /**
     * Sends an invoice modification notification email to the guest with updated PDF attachment.
     * 
     * @param before the invoice before modification
     * @param after the modified invoice
     * @throws MessagingException if the email cannot be sent
     */
    public void sendInvoiceModified(Invoice before, Invoice after) throws MessagingException {
        if (after == null || after.getBooking() == null || after.getBooking().getGuest() == null) {
            return;
        }
        String email = after.getBooking().getGuest().getEmail();
        String subject = "Invoice Updated - " + after.getInvoiceNumber();
        String htmlBody = buildInvoiceModifiedTemplate(before, after);
        
        // Generate updated PDF using InvoicePdfService and attach it
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(after);
        sendHtmlMessageWithAttachment(email, subject, htmlBody, pdfBytes, 
            "invoice_" + after.getInvoiceNumber() + ".pdf", "application/pdf");
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
        
        // Build extras list
        StringBuilder extrasList = new StringBuilder();
        if (booking.getExtras() != null && !booking.getExtras().isEmpty()) {
            extrasList.append("<ul style=\"margin:10px 0;padding-left:20px\">");
            for (com.hotel.booking.entity.BookingExtra extra : booking.getExtras()) {
                if (extra != null && extra.getName() != null) {
                    String extraName = escapeHtml(extra.getName());
                    String extraPrice = extra.getPrice() != null ? String.format("%.2f", extra.getPrice()) : "0.00";
                    String perPerson = extra.isPerPerson() ? " (per person)" : "";
                    extrasList.append(String.format("<li>%s - €%s%s</li>", extraName, extraPrice, perPerson));
                }
            }
            extrasList.append("</ul>");
        } else {
            extrasList.append("<p style=\"color:#666;font-style:italic\">No extras selected</p>");
        }

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
                        <p><strong>Extras:</strong></p>
                        %s
                        <p style="margin-top:15px"><strong>Total Price:</strong> €%s</p>
                    </div>
                    
                    <p>If you have any questions, please don't hesitate to contact us.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, guestName, bookingNumber, checkIn, checkOut, roomCategory, roomNumber, amount, extrasList.toString(), totalPrice);
    }

    /**
     * Builds HTML template for booking modification email.
     */
    private String buildBookingModificationTemplate(Booking booking, LocalDateTime modificationTime) {
        String guestName = booking.getGuest() != null ? escapeHtml(booking.getGuest().getUsername()) : "Guest";
        String bookingNumber = escapeHtml(booking.getBookingNumber());
        
        // Get all modifications and group them by timestamp
        // All modifications created in the same batch will have the same or very similar timestamps
        List<BookingModification> allMods = modificationRepository.findByBookingId(booking.getId());
        List<BookingModification> modsAtTime;
        
        if (allMods.isEmpty()) {
            modsAtTime = List.of();
        } else {
            // Group modifications by their timestamp (rounded to seconds to handle millisecond differences)
            Map<LocalDateTime, List<BookingModification>> grouped = allMods.stream()
                    .collect(Collectors.groupingBy(m -> {
                        LocalDateTime modTime = m.getModifiedAt();
                        // Remove nanoseconds to group by second
                        return modTime.withNano(0);
                    }, Collectors.toList()));
            
            // Find the group that contains the modification with the given timestamp
            // or use the most recent group if exact match not found
            LocalDateTime targetTime = modificationTime != null ? modificationTime.withNano(0) : null;
            if (targetTime != null && grouped.containsKey(targetTime)) {
                modsAtTime = grouped.get(targetTime);
            } else {
                // Use the most recent group
                LocalDateTime mostRecentTime = grouped.keySet().stream()
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
                modsAtTime = mostRecentTime != null ? grouped.get(mostRecentTime) : List.of();
            }
        }
        
        // Build changes list
        StringBuilder changesList = new StringBuilder();
        if (modsAtTime.isEmpty()) {
            changesList.append("<p style=\"color:#666;font-style:italic\">No changes details available</p>");
        } else {
            changesList.append("<ul style=\"margin:10px 0;padding-left:20px;list-style:none\">");
            for (BookingModification m : modsAtTime) {
                String field = escapeHtml(m.getFieldChanged() != null ? m.getFieldChanged() : "Unknown");
                String oldVal = escapeHtml(m.getOldValue() != null ? m.getOldValue() : "<null>");
                String newVal = escapeHtml(m.getNewValue() != null ? m.getNewValue() : "<null>");
                changesList.append(String.format("<li style=\"margin-bottom:8px\"><strong>%s:</strong> %s → %s</li>", field, oldVal, newVal));
            }
            changesList.append("</ul>");
        }

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
                        <p><strong>Changes:</strong></p>
                        %s
                    </div>
                    
                    <p>If you have any questions about these changes, please contact us.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, guestName, bookingNumber, changesList.toString());
    }

    /**
     * Builds HTML template for booking cancellation email.
     */
    private String buildBookingCancellationTemplate(Booking booking, BookingCancellation cancellation) {
        String guestName = booking.getGuest() != null ? escapeHtml(booking.getGuest().getUsername()) : "Guest";
        String bookingNumber = escapeHtml(booking.getBookingNumber());
        String reason = escapeHtml(cancellation.getReason() != null ? cancellation.getReason() : "No reason provided");
        
        // Format amounts
        String originalPrice = booking.getTotalPrice() != null ? String.format("%.2f", booking.getTotalPrice()) : "0.00";
        String cancellationFee = cancellation.getCancellationFee() != null ? String.format("%.2f", cancellation.getCancellationFee()) : "0.00";
        String refundAmount = cancellation.getRefundedAmount() != null ? String.format("%.2f", cancellation.getRefundedAmount()) : "0.00";

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
                        <hr style="border:none;border-top:1px solid #ddd;margin:15px 0">
                        <p><strong>Original Booking Price:</strong> €%s</p>
                        <p><strong>Cancellation Fee:</strong> €%s</p>
                        <p style="margin-top:10px;padding-top:10px;border-top:1px solid #ddd"><strong>Refund Amount:</strong> <span style="color:#2e7d32;font-size:110%%">€%s</span></p>
                    </div>
                    
                    <p>We hope to welcome you again in the future.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, guestName, bookingNumber, reason, originalPrice, cancellationFee, refundAmount);
    }

    /**
     * Builds HTML template for invoice modification email.
     */
    private String buildInvoiceModifiedTemplate(Invoice before, Invoice after) {
        Booking booking = after.getBooking();
        String guestName = booking != null && booking.getGuest() != null ? escapeHtml(booking.getGuest().getUsername()) : "Guest";
        String invoiceNumber = escapeHtml(after.getInvoiceNumber());
        
        // Build changes list
        StringBuilder changesList = new StringBuilder();
        changesList.append("<ul style=\"margin:10px 0;padding-left:20px;list-style:none\">");
        
        if (before != null) {
            // Amount change
            if (before.getAmount() != null && after.getAmount() != null && !before.getAmount().equals(after.getAmount())) {
                String oldAmount = String.format("%.2f", before.getAmount());
                String newAmount = String.format("%.2f", after.getAmount());
                changesList.append(String.format("<li style=\"margin-bottom:8px\"><strong>Amount:</strong> €%s → €%s</li>", oldAmount, newAmount));
            }
            
            // Payment Method change
            if (before.getPaymentMethod() != null && after.getPaymentMethod() != null && !before.getPaymentMethod().equals(after.getPaymentMethod())) {
                String oldMethod = escapeHtml(before.getPaymentMethod().toString());
                String newMethod = escapeHtml(after.getPaymentMethod().toString());
                changesList.append(String.format("<li style=\"margin-bottom:8px\"><strong>Payment Method:</strong> %s → %s</li>", oldMethod, newMethod));
            }
            
            // Status change
            if (before.getInvoiceStatus() != null && after.getInvoiceStatus() != null && !before.getInvoiceStatus().equals(after.getInvoiceStatus())) {
                String oldStatus = escapeHtml(before.getInvoiceStatus().toString());
                String newStatus = escapeHtml(after.getInvoiceStatus().toString());
                changesList.append(String.format("<li style=\"margin-bottom:8px\"><strong>Status:</strong> %s → %s</li>", oldStatus, newStatus));
            }
        }
        
        changesList.append("</ul>");
        
        String amount = after.getAmount() != null ? String.format("%.2f", after.getAmount()) : "0.00";
        String paymentMethod = after.getPaymentMethod() != null ? escapeHtml(after.getPaymentMethod().toString()) : "N/A";
        String status = after.getInvoiceStatus() != null ? escapeHtml(after.getInvoiceStatus().toString()) : "PENDING";
        String issuedAt = after.getIssuedAt() != null ? after.getIssuedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "N/A";
        String bookingNumber = booking != null ? escapeHtml(booking.getBookingNumber()) : "N/A";

        return String.format("""
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Invoice Updated</title>
            </head>
            <body style="font-family:Arial,sans-serif;color:#333;margin:0;padding:0;background:#f5f5f5">
                <div style="max-width:600px;margin:20px auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff">
                    <h2 style="color:#ff9800;margin-top:0">Invoice Updated</h2>
                    <p>Dear %s,</p>
                    <p>Your invoice has been updated. Please review the changes below.</p>
                    
                    <div style="background:#fff3cd;padding:15px;border-radius:6px;margin:20px 0;border-left:4px solid #ff9800">
                        <h3 style="margin-top:0;color:#333">Invoice Details</h3>
                        <p><strong>Invoice Number:</strong> %s</p>
                        <p><strong>Booking Number:</strong> %s</p>
                        <p><strong>Changes:</strong></p>
                        %s
                        <hr style="border:none;border-top:1px solid #ddd;margin:15px 0">
                        <p><strong>Current Amount:</strong> €%s</p>
                        <p><strong>Payment Method:</strong> %s</p>
                        <p><strong>Status:</strong> %s</p>
                        <p><strong>Issued At:</strong> %s</p>
                    </div>
                    
                    <p>Please find the updated invoice PDF attached to this email.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, guestName, invoiceNumber, bookingNumber, changesList.toString(), amount, paymentMethod, status, issuedAt);
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
     * Builds HTML template for welcome email.
     */
    private String buildWelcomeEmailTemplate(com.hotel.booking.entity.User user) {
        String userName = escapeHtml(user.getUsername() != null ? user.getUsername() : "Guest");
        String firstName = escapeHtml(user.getFirstName() != null ? user.getFirstName() : "");
        String displayName = !firstName.isEmpty() ? firstName : userName;

        return String.format("""
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Welcome</title>
            </head>
            <body style="font-family:Arial,sans-serif;color:#333;margin:0;padding:0;background:#f5f5f5">
                <div style="max-width:600px;margin:20px auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff">
                    <h2 style="color:#1a73e8;margin-top:0">Welcome to HotelBookingApp!</h2>
                    <p>Dear %s,</p>
                    <p>Thank you for registering with us! We're excited to have you as part of our community.</p>
                    
                    <div style="background:#e3f2fd;padding:15px;border-radius:6px;margin:20px 0;border-left:4px solid #1a73e8">
                        <h3 style="margin-top:0;color:#333">Your Account</h3>
                        <p><strong>Username:</strong> %s</p>
                        <p>You can now log in and start booking rooms at our hotel.</p>
                    </div>
                    
                    <p>If you have any questions, please don't hesitate to contact us.</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">HotelBookingApp Team</p>
                </div>
            </body>
            </html>
            """, displayName, userName);
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
