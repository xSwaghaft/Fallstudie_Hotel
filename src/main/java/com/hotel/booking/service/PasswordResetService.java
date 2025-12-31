package com.hotel.booking.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.booking.entity.PasswordResetToken;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.PasswordResetTokenRepository;
import com.hotel.booking.security.BcryptPasswordEncoder;
import com.hotel.booking.view.ResetPasswordView;


/**
 * Service for password reset operations.
 * @author Viktor Götting
 */
@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    /** Token expiration time in hours */
    private static final long TOKEN_EXPIRATION_HOURS = 1L;
    
    /** Email subject for password reset */
    private static final String EMAIL_SUBJECT = "Password reset for your HotelBookingApp account";
    
    /** Email template text for token validity */
    private static final String TOKEN_VALIDITY_TEXT = "1 hour";

    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BcryptPasswordEncoder passwordEncoder;
    private final String baseUrl;

    public PasswordResetService(UserService userService,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService,
                                BcryptPasswordEncoder passwordEncoder,
                                @Value("${app.base-url}") String baseUrl) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.baseUrl = baseUrl;
    }

    public Optional<String> verifyToken(String token) {
        Optional<PasswordResetToken> prtOpt = tokenRepository.findByToken(token);
        if (prtOpt.isEmpty()) {
            return Optional.empty();
        }
        PasswordResetToken prt = prtOpt.get();
        if (prt.getExpiresAt().isBefore(Instant.now())) {
            // Clean up expired token
            tokenRepository.delete(prt);
            return Optional.empty();
        }
        // Get email from user relationship
        return Optional.ofNullable(prt.getEmail());
    }

    public boolean resetPassword(String token, String newPassword) {
        if (token == null || newPassword == null || newPassword.isBlank()) {
            log.warn("Attempt to reset password with invalid token or password");
            return false;
        }

        Optional<PasswordResetToken> prtOpt = tokenRepository.findByToken(token);
        if (prtOpt.isEmpty()) {
            log.warn("Password reset attempted with non-existent token");
            return false;
        }
        
        PasswordResetToken prt = prtOpt.get();
        if (prt.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Password reset attempted with expired token");
            tokenRepository.delete(prt);
            return false;
        }

        // Get user directly from token relationship
        User user = prt.getUser();
        if (user == null) {
            log.error("Password reset token has no associated user");
            return false;
        }

        String hashed = passwordEncoder.encode(newPassword);
        user.setPassword(hashed);
        userService.save(user);

        // Invalidate token after successful password reset
        tokenRepository.delete(prt);
        log.info("Password successfully reset for user: {}", user.getEmail());
        return true;
    }

    public boolean createTokenAndSend(String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for unknown email: {}", email);
            return false;
        }

        User user = userOpt.get();
        
        // Delete old tokens for this user to prevent multiple active tokens
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS);
        PasswordResetToken prt = new PasswordResetToken(token, user, expires);
        tokenRepository.save(prt);

        String resetLink = String.format("%s/%s?token=%s", baseUrl, ResetPasswordView.ROUTE, token);
        String username = user.getUsername();
        
        String htmlBody = buildHtmlEmailBody(username, resetLink);
        String plainTextBody = buildPlainTextEmailBody(username, resetLink);

        try {
            emailService.sendHtmlMessage(email, EMAIL_SUBJECT, htmlBody);
            log.info("Password reset email (HTML) sent to {}", email);
            return true;
        } catch (jakarta.mail.MessagingException me) {
            log.warn("HTML email failed, falling back to plain text for {}: {}", email, me.getMessage());
            try {
                emailService.sendSimpleMessage(email, EMAIL_SUBJECT, plainTextBody);
                log.info("Password reset email (plain text) sent to {}", email);
                return true;
            } catch (Exception e) {
                log.error("Failed to send fallback plain password reset email to {}: {}", email, e.getMessage());
                // Clean up token if email sending fails
                tokenRepository.delete(prt);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            // Clean up token if email sending fails
            tokenRepository.delete(prt);
            return false;
        }
    }

    /**
     * Builds the HTML email body for password reset.
     *
     * @param username the username of the user requesting password reset
     * @param resetLink the password reset link
     * @return HTML formatted email body
     */
    private String buildHtmlEmailBody(String username, String resetLink) {
        return "<!doctype html>\n" +
                "<html><head><meta charset=\"utf-8\"><title>Password reset</title></head><body style=\"font-family:Arial,sans-serif;color:#333\">" +
                "<div style=\"max-width:600px;margin:0 auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff\">" +
                "<h2 style=\"color:#1a73e8;margin-top:0\">Password reset request</h2>" +
                String.format("<p>Hello %s,</p>", escapeHtml(username)) +
                String.format("<p>We received a request to reset your password. Click the button below to set a new password. The link is valid for %s.</p>", TOKEN_VALIDITY_TEXT) +
                String.format("<p style=\"text-align:center;margin:30px 0\"><a href=\"%s\" style=\"display:inline-block;padding:12px 24px;background:#1a73e8;color:#fff;text-decoration:none;border-radius:6px\">Reset password</a></p>", escapeHtml(resetLink)) +
                "<p style=\"color:#666;font-size:90%\">If you didn't request this, you can safely ignore this email.</p>" +
                "<hr style=\"border:none;border-top:1px solid #eee;margin:20px 0\">" +
                "<p style=\"color:#999;font-size:80%\">HotelBookingApp Team</p>" +
                "</div></body></html>";
    }

    /**
     * Builds the plain text email body for password reset.
     *
     * @param username the username of the user requesting password reset
     * @param resetLink the password reset link
     * @return plain text email body
     */
    private String buildPlainTextEmailBody(String username, String resetLink) {
        return String.format("Hello %s,\n\nWe received a request to reset your password. Use this link (valid %s): %s\n\nIf you didn't request this, ignore this email.", 
                username, TOKEN_VALIDITY_TEXT, resetLink);
    }

    /**
     * Escapes HTML special characters to prevent XSS attacks.
     *
     * @param s the string to escape
     * @return escaped string, or empty string if input is null
     */
    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
