package com.hotel.booking.repository;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entity access.
 *
 * <p>Provides standard CRUD operations and custom query methods for retrieving users
 * from the database. These methods are typically used for authentication, user searches,
 * and role-based lookups.
 *
 * @author Artur Derr
 * @see User
 * @see UserRole
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * <p>Used for authentication to retrieve a user's credentials for password verification.
     *
     * @param username the login username
     * @return an {@code Optional} containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * <p>Used for authentication, registration, and password reset operations.
     *
     * @param email the user's email address
     * @return an {@code Optional} containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds all users with a specific role.
     *
     * @param role the {@link UserRole} to filter by
     * @return a list of users with the given role
     */
    List<User> findByRole(UserRole role);

    /**
     * Finds users by last name (case-insensitive partial match).
     *
     * @param lastName the last name substring to search for
     * @return a list of users whose last name contains the given string
     */
    List<User> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Finds users by first name (case-insensitive partial match).
     *
     * @param firstName the first name substring to search for
     * @return a list of users whose first name contains the given string
     */
    List<User> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * Checks whether a user with the given username exists.
     *
     * @param username the username to check
     * @return {@code true} if a user with this username exists, {@code false} otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether a user with the given email exists.
     *
     * @param email the email address to check
     * @return {@code true} if a user with this email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);
}