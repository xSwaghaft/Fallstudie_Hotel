package com.hotel.booking.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Represents a password reset token used for secure password recovery.
 * 
 * <p>
 * When a user requests a password reset, a unique token is generated and stored
 * with a reference to the user. This token is sent to the user via email and can be
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

    /**
     * Cached email address for convenience/testing; populated from user and not persisted.
     */
    @jakarta.persistence.Transient
    private String email;

    /** User requesting the password reset. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_password_reset_token_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /** Expiration timestamp after which the token becomes invalid. */
    @Column(nullable = false)
    private Instant expiresAt;

    /** Empty constructor for JPA. */
    protected PasswordResetToken() {}

    /**
     * Creates a new password reset token.
     */
    public PasswordResetToken(String token, User user, Instant expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.email = user != null ? user.getEmail() : null;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Convenience method to get the email address of the user.
     * @return the user's email address, or null if user is null
     */
    public String getEmail() {
        if (email != null) {
            return email;
        }
        return user != null ? user.getEmail() : null;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
