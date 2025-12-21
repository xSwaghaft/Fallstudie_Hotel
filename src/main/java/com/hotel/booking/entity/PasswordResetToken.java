package com.hotel.booking.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a password reset token used for secure password recovery.
 * 
 * <p>
 * When a user requests a password reset, a unique token is generated and stored
 * with their email address. This token is sent to the user via email and can be
 * used to reset their password within a limited time period (defined by expiresAt).
 * The token ensures that only the person with access to the email account can
 * reset the password.
 * </p>
 * 
 * @author Viktor Götting
 * @since 1.0
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    /** Primary key ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique token string used for password reset verification. */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /** Email address of the user requesting the password reset. */
    @Column(nullable = false)
    private String email;

    /** Expiration timestamp after which the token becomes invalid. */
    @Column(nullable = false)
    private Instant expiresAt;

    /** Empty constructor for JPA. */
    protected PasswordResetToken() {}

    /**
     * Creates a new password reset token.
     * 
     * @param token the unique token string
     * @param email the email address of the user
     * @param expiresAt the expiration timestamp
     */
    public PasswordResetToken(String token, String email, Instant expiresAt) {
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
