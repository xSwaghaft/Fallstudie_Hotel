package com.hotel.booking.security;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.stereotype.Service;

/**
 * Service class for managing user session and authentication state.
 * <p>
 * This service handles user login/logout operations, session management,
 * and provides methods for checking user authentication status and roles.
 * It uses Vaadin's VaadinSession to store user information during the session.
 * </p>
 *
 * @author Artur Derr
 */
@Service
public class SessionService {

    private static final String KEY_USER = "hotel.user";

    /**
     * Logs in a user by storing the user object in the current Vaadin session.
     *
     * @param user the user object to be stored in the session
     */
    public void login(User user) {
        VaadinSession.getCurrent().setAttribute(KEY_USER, user);
    }

    /**
     * Logs out the current user by removing the user from the session and closing it.
     * <p>
     * This method safely handles null session references before closing.
     * </p>
     */
    public void logout() {
        var session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(KEY_USER, null);
            session.close();
        }
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return {@code true} if a user is logged in, {@code false} otherwise
     */
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Retrieves the currently logged-in user from the Vaadin session.
     *
     * @return the current user, or {@code null} if no user is logged in or session is unavailable
     */
    public User getCurrentUser() {
        var session = VaadinSession.getCurrent();
        return session != null ? (User) session.getAttribute(KEY_USER) : null;
    }

    /**
     * Retrieves the role of the currently logged-in user.
     *
     * @return the role of the current user, or {@code null} if no user is logged in
     */
    public UserRole getCurrentRole() {
        var u = getCurrentUser();
        return u != null ? u.getRole() : null;
    }

    /**
     * Checks if the current user has the specified role.
     *
     * @param role the role to check for
     * @return {@code true} if the current user has the specified role, {@code false} otherwise
     */
    public boolean hasRole(UserRole role) {
        var r = getCurrentRole();
        return r != null && r.equals(role);
    }

    /**
     * Checks if the current user has any of the specified roles.
     *
     * @param roles the roles to check for
     * @return {@code true} if the current user has any of the specified roles, {@code false} otherwise
     */
    public boolean hasAnyRole(UserRole... roles) {
        var r = getCurrentRole();
        if (r == null) return false;
        for (var role : roles) if (r == role) return true;
        return false;
    }
}
