package com.hotel.booking.security;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.repository.UserRepository;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;

    public SessionService(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
        this.userRepository = userRepository;
    }

    /**
     * Logs in a user by storing the user object in the current Vaadin session.
     *
     * @param user the user object to be stored in the session
     */
    public void login(User user) {
        // Kept for backwards compatibility with existing code; authentication is based on username/password.
        if (user == null) {
            return;
        }
        VaadinSession.getCurrent().setAttribute(KEY_USER, user);
    }

    /**
     * Authenticates the user against Spring Security and establishes a session.
     */
    public void login(String usernameOrEmail, String rawPassword) {
        var request = VaadinServletRequest.getCurrent() != null
                ? VaadinServletRequest.getCurrent().getHttpServletRequest()
                : null;
        var response = VaadinServletResponse.getCurrent() != null
                ? VaadinServletResponse.getCurrent().getHttpServletResponse()
                : null;

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usernameOrEmail, rawPassword));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        if (request != null && response != null) {
            securityContextRepository.saveContext(context, request, response);
        }

        // Clear any cached user from the Vaadin session so it can be reloaded using the authenticated principal.
        var vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            vaadinSession.setAttribute(KEY_USER, null);
        }
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
        }

        var request = VaadinServletRequest.getCurrent() != null
                ? VaadinServletRequest.getCurrent().getHttpServletRequest()
                : null;
        var response = VaadinServletResponse.getCurrent() != null
                ? VaadinServletResponse.getCurrent().getHttpServletResponse()
                : null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (request != null && response != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        SecurityContextHolder.clearContext();
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return {@code true} if a user is logged in, {@code false} otherwise
     */
    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.isAuthenticated();
    }

    /**
     * Retrieves the currently logged-in user from the Vaadin session.
     *
     * @return the current user, or {@code null} if no user is logged in or session is unavailable
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }

        var session = VaadinSession.getCurrent();
        if (session != null) {
            Object cached = session.getAttribute(KEY_USER);
            if (cached instanceof User cachedUser) {
                return cachedUser;
            }
        }

        String username = getCurrentUsername();
        if (username == null || username.isBlank()) {
            return null;
        }

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username).orElse(null));

        if (session != null) {
            session.setAttribute(KEY_USER, user);
        }
        return user;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String s) {
            return s;
        }
        return null;
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
