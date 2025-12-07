package com.hotel.booking.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt-basierter PasswordEncoder für sichere Passwort-Verwaltung.
 * Implementiert Spring Security's PasswordEncoder Interface.
 * 
 * BCrypt bietet:
 * - Automatische Salt-Generierung
 * - Adaptive Hashing-Kosten (passt sich der Rechenleistung an)
 * - Schutz vor Rainbow-Table Attacken
 * - Industry-Standard für Passwort-Hashing
 * - Timing-Attack Resistenz gegen Brute-Force
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
     * Hasht ein Klartext-Passwort mit BCrypt.
     * Der Salt wird automatisch generiert und im Hash eingebettet.
     * Implementiert Spring Security's PasswordEncoder.encode(CharSequence)
     * 
     * @param rawPassword Klartext-Passwort
     * @return BCrypt-gehashtes Passwort (inkl. Salt)
     * @throws IllegalArgumentException wenn rawPassword null oder leer ist
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
     * Überprüft, ob ein Klartext-Passwort mit einem gehashten Passwort übereinstimmt.
     * Implementiert Spring Security's PasswordEncoder.matches(CharSequence, String)
     * Nutzt BCrypt's sichere Vergleichsmethode gegen Timing-Attacks.
     * 
     * @param rawPassword Klartext-Passwort vom Benutzer
     * @param encodedPassword BCrypt-gehashtes Passwort aus der DB
     * @return true wenn Passwörter übereinstimmen, false sonst
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        try {
            return bCryptEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            // Ungültiges BCrypt-Format in der DB
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Prüft, ob das gespeicherte Passwort neu gehashed werden sollte.
     * Spring Security upgradet automatisch zu stärkeren Hashes wenn konfiguriert.
     * Mit BCrypt und fester Strength ist kein Upgrade nötig.
     * 
     * @param encodedPassword bereits gehashtes Passwort
     * @return true wenn Password upgrade benötigt
     */
    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        // Mit BCrypt und fester Strength: kein Upgrade nötig
        return false;
    }

    /**
     * Überprüft, ob ein Passwort bereits mit BCrypt gehasht ist.
     * Hilft bei Migrationen von alten zu neuen Systemen.
     * 
     * @param password zu überprüfendes Passwort
     * @return true wenn Format gültig ist
     */
    public boolean isBCryptHash(String password) {
        if (password == null) {
            return false;
        }
        // BCrypt hashes beginnen mit $2a$, $2b$, $2y$ oder $2x$
        // Format: $2a$10$... (Prefix + cost + Salt + Hash)
        return password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
