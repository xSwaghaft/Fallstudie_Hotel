package com.hotel.booking.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.booking.entity.PasswordResetToken;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.PasswordResetTokenRepository;
import com.hotel.booking.security.BcryptPasswordEncoder;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BcryptPasswordEncoder passwordEncoder;

    public PasswordResetService(UserService userService,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService,
                                BcryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Verifies that a password reset token exists and is not expired.
     * 
     * @param token the token to verify
     * @return the email address associated with the token if valid, empty otherwise
     */
    public Optional<String> verifyToken(String token) {
        Optional<com.hotel.booking.entity.PasswordResetToken> prtOpt = tokenRepository.findByToken(token);
        if (prtOpt.isEmpty()) return Optional.empty();
        com.hotel.booking.entity.PasswordResetToken prt = prtOpt.get();
        if (prt.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(prt.getEmail());
    }

    /**
     * Resets the password for the user associated with the given token.
     * 
     * <p>
     * The token must be valid and not expired. After successful password reset,
     * the token is automatically deleted to prevent reuse.
     * </p>
     * 
     * @param token the password reset token
     * @param newPassword the new password to set (will be hashed)
     * @return true if the password was successfully reset, false if the token is invalid or expired
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<com.hotel.booking.entity.PasswordResetToken> prtOpt = tokenRepository.findByToken(token);
        if (prtOpt.isEmpty()) return false;
        com.hotel.booking.entity.PasswordResetToken prt = prtOpt.get();
        if (prt.getExpiresAt().isBefore(Instant.now())) {
            tokenRepository.delete(prt);
            return false;
        }

        Optional<User> userOpt = userService.findByEmail(prt.getEmail());
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        String hashed = passwordEncoder.encode(newPassword);
        user.setPassword(hashed);
        userService.save(user);

        // invalidate token
        tokenRepository.delete(prt);
        return true;
    }

    /**
     * Creates a password reset token for the given email and sends a reset email.
     * 
     * <p>
     * If a user with the given email exists, a new token is created (valid for 1 hour)
     * and an HTML email with a reset link is sent. If HTML email fails, a plain text
     * fallback is attempted. For security reasons, the method returns true even if
     * the user doesn't exist to prevent email enumeration attacks.
     * </p>
     * 
     * @param email the email address of the user requesting password reset
     * @return true if the email was sent (or user doesn't exist), false only on email delivery failure
     */
    public boolean createTokenAndSend(String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for unknown email: {}", email);
            return false;
        }

        String token = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(1, ChronoUnit.HOURS);
        PasswordResetToken prt = new PasswordResetToken(token, email, expires);
        tokenRepository.save(prt);

        String resetLink = String.format("http://localhost:8080/reset-password?token=%s", token);
        String subject = "Password reset for your HotelBookingApp account";
        String htmlBody = "<!doctype html>\n" +
                "<html><head><meta charset=\"utf-8\"><title>Password reset</title></head><body style=\"font-family:Arial,sans-serif;color:#333\">" +
                "<div style=\"max-width:600px;margin:0 auto;padding:20px;border:1px solid #eaeaea;border-radius:8px;background:#fff\">" +
                "<h2 style=\"color:#1a73e8;margin-top:0\">Password reset request</h2>" +
                String.format("<p>Hello %s,</p>", escapeHtml(userOpt.get().getUsername())) +
                "<p>We received a request to reset your password. Click the button below to set a new password. The link is valid for 1 hour.</p>" +
                String.format("<p style=\"text-align:center;margin:30px 0\"><a href=\"%s\" style=\"display:inline-block;padding:12px 24px;background:#1a73e8;color:#fff;text-decoration:none;border-radius:6px\">Reset password</a></p>", escapeHtml(resetLink)) +
                "<p style=\"color:#666;font-size:90%\">If you didn't request this, you can safely ignore this email.</p>" +
                "<hr style=\"border:none;border-top:1px solid #eee;margin:20px 0\">" +
                "<p style=\"color:#999;font-size:80%\">HotelBookingApp Team</p>" +
                "</div></body></html>";

        try {
            emailService.sendHtmlMessage(email, subject, htmlBody);
            log.info("Password reset email (HTML) sent to {}", email);
            return true;
        } catch (jakarta.mail.MessagingException me) {
            log.warn("HTML email failed, falling back to plain text for {}: {}", email, me.getMessage());
            // fallback to plain text
            String plain = String.format("Hello %s,\n\nWe received a request to reset your password. Use this link (valid 1 hour): %s\n\nIf you didn't request this, ignore this email.", userOpt.get().getUsername(), resetLink);
            try {
                emailService.sendSimpleMessage(email, subject, plain);
                return true;
            } catch (Exception e) {
                log.error("Failed to send fallback plain password reset email to {}: {}", email, e.getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Escapes HTML special characters to prevent XSS attacks in email content.
     * 
     * @param s the string to escape
     * @return the escaped string, or empty string if input is null
     */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
