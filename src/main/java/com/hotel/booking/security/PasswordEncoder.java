// Datei: PasswordEncoder.java
package com.hotel.booking.security;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Einfacher PasswordEncoder für das Demo-Projekt.
 * Verwendet SHA-256 mit Salt für Passwort-Hashing.
 * 
 * Hinweis: Für produktive Anwendungen sollte BCrypt oder Argon2 verwendet werden.
 */
@Component
public class PasswordEncoder {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * Hasht ein Passwort mit Salt
     * Format: salt:hash
     */
    public String encode(String plainPassword) {
        try {
            // Salt generieren
            byte[] salt = generateSalt();
            
            // Passwort hashen
            byte[] hash = hashPassword(plainPassword, salt);
            
            // Salt und Hash als Base64 kodieren und zusammenfügen
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Fehler beim Hashen des Passworts", e);
        }
    }

    /**
     * Überprüft, ob ein Klartext-Passwort mit dem gehashten Passwort übereinstimmt
     */
    public boolean matches(String plainPassword, String hashedPassword) {
        try {
            // Für Demo-User ohne Salt (Abwärtskompatibilität)
            if (!hashedPassword.contains(":")) {
                // Einfacher Vergleich für Demo-Daten
                return plainPassword.equals(hashedPassword);
            }
            
            // Salt und Hash extrahieren
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            
            // Eingegebenes Passwort mit gleichem Salt hashen
            byte[] actualHash = hashPassword(plainPassword, salt);
            
            // Hashes vergleichen
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generiert einen zufälligen Salt
     */
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hasht ein Passwort mit dem gegebenen Salt
     */
    private byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(salt);
        return md.digest(password.getBytes());
    }
}
