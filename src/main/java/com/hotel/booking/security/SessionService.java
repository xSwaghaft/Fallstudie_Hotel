package com.hotel.booking.security;

import com.hotel.booking.entity.User;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private static final String KEY_USER = "hotel.user";

    public void login(User user) {
        VaadinSession.getCurrent().setAttribute(KEY_USER, user);
    }

    public void logout() {
        var session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(KEY_USER, null);
            session.close();
        }
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public User getCurrentUser() {
        var session = VaadinSession.getCurrent();
        return session != null ? (User) session.getAttribute(KEY_USER) : null;
    }

    public UserRole getCurrentRole() {
        var u = getCurrentUser();
        return u != null ? u.getRole() : null;
    }

    public boolean hasRole(UserRole role) {
        var r = getCurrentRole();
        return r != null && r.equals(role);
    }

    public boolean hasAnyRole(UserRole... roles) {
        var r = getCurrentRole();
        if (r == null) return false;
        for (var role : roles) if (r == role) return true;
        return false;
    }
}
