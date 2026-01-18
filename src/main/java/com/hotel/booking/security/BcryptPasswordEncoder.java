package com.hotel.booking.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Password encoder implementation using BCrypt for secure password hashing.
 * <p>
 * This class implements Spring Security's PasswordEncoder interface and provides
 * the following security features:
 * </p>
 * <ul>
 *   <li>Automatic salt generation</li>
 *   <li>Adaptive hashing costs that adjust to computing power</li>
 *   <li>Protection against rainbow table attacks</li>
 *   <li>Industry-standard password hashing algorithm</li>
 *   <li>Resistance against timing attacks and brute-force attempts</li>
 * </ul>
 *
 * @author Artur Derr
 */
@Component
public class BcryptPasswordEncoder implements PasswordEncoder {

    private static final int BCRYPT_STRENGTH = 10; // Spring Security Best Practice: 10 (Balance zwischen Sicherheit und Performance)
    
    private final BCryptPasswordEncoder bCryptEncoder;

    public BcryptPasswordEncoder() {
        this.bCryptEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    /**
     * Encodes a raw password using BCrypt hashing.
     * <p>
     * The salt is automatically generated and embedded in the returned hash.
     * Implements Spring Security's {@link PasswordEncoder#encode(CharSequence)}.
     * </p>
     *
     * @param rawPassword the plaintext password to be hashed
     * @return the BCrypt-hashed password including the embedded salt
     * @throws IllegalArgumentException if rawPassword is null or empty
     * @throws RuntimeException if an error occurs during encoding
     */
    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null || rawPassword.length() == 0) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            return bCryptEncoder.encode(rawPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding password", e);
        }
    }

    /**
     * Verifies whether a raw password matches the provided encoded password.
     * <p>
     * Implements Spring Security's {@link PasswordEncoder#matches(CharSequence, String)}.
     * Uses BCrypt's secure comparison method to protect against timing attacks.
     * </p>
     *
     * @param rawPassword the plaintext password provided by the user
     * @param encodedPassword the BCrypt-hashed password from the database
     * @return {@code true} if the passwords match, {@code false} otherwise
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        try {
            return bCryptEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            // Wrong format for encodedPassword
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determines whether the encoded password should be re-hashed using a stronger hash.
     * <p>
     * Implements Spring Security's {@link PasswordEncoder#upgradeEncoding(String)}.
     * With BCrypt and fixed strength settings.
     * </p>
     *
     * @param encodedPassword the already-encoded password
     * @return {@code false} as upgrades are not necessary with BCrypt and fixed strength
     */
    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return false;
    }

    /**
     * Checks whether a password string is a valid BCrypt hash.
     * <p>
     * This method is useful during migrations from legacy systems to ensure
     * passwords are in the correct format.
     * </p>
     *
     * @param password the password string to validate
     * @return {@code true} if the password is a valid BCrypt hash format, {@code false} otherwise
     */
    public boolean isBCryptHash(String password) {
        if (password == null) {
            return false;
        }
        // BCrypt hashes begin with $2a$, $2b$, $2y$ or $2x$
        // Format: $2a$10$... (Prefix + cost + Salt + Hash)
        return password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
