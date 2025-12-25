package com.hotel.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.entity.Invoice;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending email messages.
 * @author Viktor Götting
 */
@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private final InvoicePdfService invoicePdfService;
    private final BookingModificationService modificationService;

    @Value("${app.mail.from:no-reply@example.com}")
    private String defaultFrom;
    public EmailService(JavaMailSender emailSender, InvoicePdfService invoicePdfService, @Lazy BookingModificationService modificationService) {
        this.emailSender = emailSender;
        this.invoicePdfService = invoicePdfService;
        this.modificationService = modificationService;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(defaultFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setFrom(defaultFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indicates HTML
        emailSender.send(mimeMessage);
    }

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

    public void sendBookingConfirmation(Booking booking) throws MessagingException {
        String email = getGuestEmail(booking);
        if (email == null) return;
        sendHtmlMessage(email, "Booking Confirmation - " + booking.getBookingNumber(), 
                buildBookingConfirmationTemplate(booking));
    }

    public void sendBookingModification(Booking booking, BookingModification modification) throws MessagingException {
        if (booking == null || booking.getId() == null || modification == null) return;
        String email = getGuestEmail(booking);
        if (email == null) return;
        sendHtmlMessage(email, "Booking Modified - " + booking.getBookingNumber(), 
                buildBookingModificationTemplate(booking, modification.getModifiedAt()));
    }

    public void sendBookingCancellation(Booking booking, BookingCancellation cancellation) throws MessagingException {
        if (cancellation == null) return;
        String email = getGuestEmail(booking);
        if (email == null) return;
        sendHtmlMessage(email, "Booking Cancelled - " + booking.getBookingNumber(), 
                buildBookingCancellationTemplate(booking, cancellation));
    }

    // ==================== User registration emails ====================

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

    public void sendInvoiceCreated(Invoice invoice) throws MessagingException {
        if (invoice == null || invoice.getBooking() == null) return;
        String email = getGuestEmail(invoice.getBooking());
        if (email == null) return;
        
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoice);
        sendHtmlMessageWithAttachment(email, "Invoice - " + invoice.getInvoiceNumber(), 
                buildInvoiceTemplate(invoice), pdfBytes, 
                "invoice_" + invoice.getInvoiceNumber() + ".pdf", "application/pdf");
    }

    // ==================== Email template builders ====================

    private String buildEmailWrapper(String title, String recipientName, String content, String footerText, String titleColor) {
        String footer = footerText != null ? footerText : "HotelBookingApp Team";
        String color = titleColor != null ? titleColor : "#1a73e8";
        return String.format("""
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>%s</title>
            </head>
            <body style="font-family:Arial,sans-serif;color:#333;margin:0;padding:0;background:#f5f5f5">
                <div style="max-width:600px;margin:20px auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff">
                    <h2 style="color:%s;margin-top:0">%s</h2>
                    <p>Dear %s,</p>
                    %s
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0">
                    <p style="color:#999;font-size:80%%">%s</p>
                </div>
            </body>
            </html>
            """, title, color, title, recipientName, content, footer);
    }
    
    private String buildEmailWrapper(String title, String recipientName, String content, String footerText) {
        return buildEmailWrapper(title, recipientName, content, footerText, null);
    }

    private String buildContentBox(String title, String content, String color, String backgroundColor) {
        return String.format("""
            <div style="background:%s;padding:15px;border-radius:6px;margin:20px 0;border-left:4px solid %s">
                <h3 style="margin-top:0;color:#333">%s</h3>
                %s
            </div>
            """, backgroundColor, color, title, content);
    }

    private String buildBookingConfirmationTemplate(Booking booking) {
        String guestName = getGuestName(booking);
        String bookingNumber = escapeHtml(booking.getBookingNumber());
        String roomCategory = booking.getRoomCategory() != null ? escapeHtml(booking.getRoomCategory().getName()) : "N/A";
        String roomNumber = booking.getRoom() != null && booking.getRoom().getRoomNumber() != null 
                ? escapeHtml(booking.getRoom().getRoomNumber()) : "TBD";
        String amount = booking.getAmount() != null ? booking.getAmount().toString() : "1";
        String extrasList = buildExtrasList(booking.getExtras());

        String detailsContent = String.format("""
            <p><strong>Booking Number:</strong> %s</p>
            <p><strong>Check-in:</strong> %s</p>
            <p><strong>Check-out:</strong> %s</p>
            <p><strong>Room Category:</strong> %s</p>
            <p><strong>Room Number:</strong> %s</p>
            <p><strong>Guests:</strong> %s</p>
            <p><strong>Extras:</strong></p>
            %s
            <p style="margin-top:15px"><strong>Total Price:</strong> €%s</p>
            """, bookingNumber, formatDate(booking.getCheckInDate()), formatDate(booking.getCheckOutDate()), 
            roomCategory, roomNumber, amount, extrasList, formatAmount(booking.getTotalPrice()));
        
        String contentBox = buildContentBox("Booking Details", detailsContent, "#1a73e8", "#f9f9f9");
        String mainContent = "<p>Your booking has been confirmed! We look forward to welcoming you.</p>\n" + contentBox + 
                            "\n<p>If you have any questions, please don't hesitate to contact us.</p>";
        
        return buildEmailWrapper("Booking Confirmation", guestName, mainContent, null);
    }
    
    private String buildExtrasList(java.util.Set<com.hotel.booking.entity.BookingExtra> extras) {
        if (extras == null || extras.isEmpty()) {
            return "<p style=\"color:#666;font-style:italic\">No extras selected</p>";
        }
        
        StringBuilder sb = new StringBuilder("<ul style=\"margin:10px 0;padding-left:20px\">");
        for (com.hotel.booking.entity.BookingExtra extra : extras) {
            if (extra != null && extra.getName() != null) {
                String extraName = escapeHtml(extra.getName());
                String extraPrice = formatAmount(extra.getPrice());
                String perPerson = extra.isPerPerson() ? " (per person)" : "";
                sb.append(String.format("<li>%s - €%s%s</li>", extraName, extraPrice, perPerson));
            }
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private String buildBookingModificationTemplate(Booking booking, LocalDateTime modificationTime) {
        String guestName = getGuestName(booking);
        String bookingNumber = escapeHtml(booking.getBookingNumber());
        List<BookingModification> modsAtTime = findModificationsAtTime(booking.getId(), modificationTime);
        String changesList = buildChangesList(modsAtTime);

        String detailsContent = String.format("""
            <p><strong>Booking Number:</strong> %s</p>
            <p><strong>Changes:</strong></p>
            %s
            """, bookingNumber, changesList);
        
        String contentBox = buildContentBox("Modification Details", detailsContent, "#ff9800", "#fff3cd");
        String mainContent = "<p>Your booking has been modified. Please review the changes below.</p>\n" + contentBox + 
                            "\n<p>If you have any questions about these changes, please contact us.</p>";
        
        return buildEmailWrapper("Booking Modified", guestName, mainContent, null, "#ff9800");
    }
    
    private List<BookingModification> findModificationsAtTime(Long bookingId, LocalDateTime modificationTime) {
        List<BookingModification> allMods = modificationService.findByBookingId(bookingId);
        if (allMods.isEmpty()) {
            return List.of();
        }
        
        Map<LocalDateTime, List<BookingModification>> grouped = allMods.stream()
                .collect(Collectors.groupingBy(m -> m.getModifiedAt().withNano(0), Collectors.toList()));
        
        LocalDateTime targetTime = modificationTime != null ? modificationTime.withNano(0) : null;
        if (targetTime != null && grouped.containsKey(targetTime)) {
            return grouped.get(targetTime);
        }
        
        return grouped.keySet().stream()
                .max(LocalDateTime::compareTo)
                .map(grouped::get)
                .orElse(List.of());
    }
    
    private String buildChangesList(List<BookingModification> modifications) {
        if (modifications.isEmpty()) {
            return "<p style=\"color:#666;font-style:italic\">No changes details available</p>";
        }
        
        StringBuilder sb = new StringBuilder("<ul style=\"margin:10px 0;padding-left:20px;list-style:none\">");
        for (BookingModification m : modifications) {
            String field = escapeHtml(m.getFieldChanged() != null ? m.getFieldChanged() : "Unknown");
            String oldVal = escapeHtml(m.getOldValue() != null ? m.getOldValue() : "<null>");
            String newVal = escapeHtml(m.getNewValue() != null ? m.getNewValue() : "<null>");
            sb.append(String.format("<li style=\"margin-bottom:8px\"><strong>%s:</strong> %s → %s</li>", field, oldVal, newVal));
        }
        sb.append("</ul>");
        return sb.toString();
    }
    
    private String getGuestEmail(Booking booking) {
        return booking != null && booking.getGuest() != null && booking.getGuest().getEmail() != null 
                && !booking.getGuest().getEmail().isBlank() 
                ? booking.getGuest().getEmail() : null;
    }
    
    private String getGuestName(Booking booking) {
        return booking.getGuest() != null ? escapeHtml(booking.getGuest().getUsername()) : "Guest";
    }
    
    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "N/A";
    }
    
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "N/A";
    }
    
    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("%.2f", amount) : "0.00";
    }
    
    private String formatAmount(Double amount) {
        return amount != null ? String.format("%.2f", amount) : "0.00";
    }

    private String buildBookingCancellationTemplate(Booking booking, BookingCancellation cancellation) {
        String guestName = getGuestName(booking);
        String bookingNumber = escapeHtml(booking.getBookingNumber());
        String reason = escapeHtml(cancellation.getReason() != null ? cancellation.getReason() : "No reason provided");

        String detailsContent = String.format("""
            <p><strong>Booking Number:</strong> %s</p>
            <p><strong>Reason:</strong> %s</p>
            <hr style="border:none;border-top:1px solid #ddd;margin:15px 0">
            <p><strong>Original Booking Price:</strong> €%s</p>
            <p><strong>Cancellation Fee:</strong> €%s</p>
            <p style="margin-top:10px;padding-top:10px;border-top:1px solid #ddd"><strong>Refund Amount:</strong> <span style="color:#2e7d32;font-size:110%%">€%s</span></p>
            """, bookingNumber, reason, formatAmount(booking.getTotalPrice()), 
            formatAmount(cancellation.getCancellationFee()), formatAmount(cancellation.getRefundedAmount()));
        
        String contentBox = buildContentBox("Cancellation Details", detailsContent, "#d32f2f", "#ffebee");
        String mainContent = "<p>Your booking has been cancelled.</p>\n" + contentBox + 
                            "\n<p>We hope to welcome you again in the future.</p>";
        
        return buildEmailWrapper("Booking Cancelled", guestName, mainContent, null, "#d32f2f");
    }

    private String buildInvoiceTemplate(Invoice invoice) {
        Booking booking = invoice.getBooking();
        String guestName = booking != null ? getGuestName(booking) : "Guest";
        String invoiceNumber = escapeHtml(invoice.getInvoiceNumber());
        String paymentMethod = invoice.getPaymentMethod() != null 
                ? escapeHtml(invoice.getPaymentMethod().toString()) : "N/A";
        String status = invoice.getInvoiceStatus() != null 
                ? escapeHtml(invoice.getInvoiceStatus().toString()) : "PENDING";
        String bookingNumber = booking != null ? escapeHtml(booking.getBookingNumber()) : "N/A";

        String detailsContent = String.format("""
            <p><strong>Invoice Number:</strong> %s</p>
            <p><strong>Booking Number:</strong> %s</p>
            <p><strong>Amount:</strong> €%s</p>
            <p><strong>Payment Method:</strong> %s</p>
            <p><strong>Status:</strong> %s</p>
            <p><strong>Issued At:</strong> %s</p>
            """, invoiceNumber, bookingNumber, formatAmount(invoice.getAmount()), 
            paymentMethod, status, formatDateTime(invoice.getIssuedAt()));
        
        String contentBox = buildContentBox("Invoice Details", detailsContent, "#1a73e8", "#f9f9f9");
        String mainContent = "<p>Please find your invoice details below.</p>\n" + contentBox + 
                            "\n<p>Please find the invoice PDF attached to this email.</p>" +
                            "\n<p>Please ensure payment is made by the due date.</p>";
        
        return buildEmailWrapper("Invoice", guestName, mainContent, null);
    }

    private String buildWelcomeEmailTemplate(com.hotel.booking.entity.User user) {
        String userName = escapeHtml(user.getUsername() != null ? user.getUsername() : "Guest");
        String firstName = escapeHtml(user.getFirstName() != null ? user.getFirstName() : "");
        String displayName = !firstName.isEmpty() ? firstName : userName;

        String accountContent = String.format("""
            <p><strong>Username:</strong> %s</p>
            <p>You can now log in and start booking rooms at our hotel.</p>
            """, userName);
        
        String contentBox = buildContentBox("Your Account", accountContent, "#1a73e8", "#e3f2fd");
        String mainContent = "<p>Thank you for registering with us! We're excited to have you as part of our community.</p>\n" + contentBox + 
                            "\n<p>If you have any questions, please don't hesitate to contact us.</p>";
        
        return buildEmailWrapper("Welcome to HotelBookingApp!", displayName, mainContent, null);
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
