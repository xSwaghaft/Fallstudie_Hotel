package com.hotel.booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.booking.entity.PasswordResetToken;

/**
 * Repository interface for PasswordResetToken entity operations.
 * 
 * <p>
 * Provides methods to query password reset tokens used for secure
 * password recovery functionality.
 * </p>
 * 
 * @author Viktor Götting
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Finds a password reset token by its token string.
     * 
     * @param token the token string
     * @return optional password reset token
     */
    Optional<PasswordResetToken> findByToken(String token);
}
