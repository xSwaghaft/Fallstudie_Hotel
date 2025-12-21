package com.hotel.booking.view;

import java.util.List;
import java.util.Optional;

import com.hotel.booking.service.PasswordResetService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * View for resetting user passwords using a reset token.
 * 
 * <p>
 * Accessible via URL parameter "token" (e.g., /reset-password?token=xxx).
 * Validates the token before displaying the password reset form. If the token
 * is missing, invalid, or expired, the user is redirected to the login page.
 * </p>
 * 
 * <p>
 * Related: LoginView (forgot password link), PasswordResetService (token validation).
 * </p>
 */
@Route("reset-password")
@AnonymousAllowed
@PageTitle("Reset Password")
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private static final int NOTIFICATION_DURATION_SHORT = 2500;
    private static final int NOTIFICATION_DURATION_MEDIUM = 3000;
    private static final int NOTIFICATION_DURATION_LONG = 3500;
    private static final String LOGIN_ROUTE = "/login";
    private static final int FORM_FIELD_WIDTH = 320;

    private final PasswordResetService passwordResetService;
    private String token;

    /**
     * Constructs a new ResetPasswordView with the password reset service.
     * Initializes the layout configuration.
     */
    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
        setWidth("100%");
        setAlignItems(Alignment.CENTER);
    }

    /**
     * Validates the reset token from URL parameters before displaying the form.
     * Redirects to login if token is missing, invalid, or expired.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        token = extractTokenFromQuery(event);
        
        if (token == null) {
            Notification.show("Missing token", NOTIFICATION_DURATION_MEDIUM, Notification.Position.MIDDLE);
            event.forwardTo(LOGIN_ROUTE);
            return;
        }

        Optional<String> emailOpt = passwordResetService.verifyToken(token);
        if (emailOpt.isEmpty()) {
            Notification.show("Invalid or expired token", NOTIFICATION_DURATION_LONG, Notification.Position.MIDDLE);
            event.forwardTo(LOGIN_ROUTE);
            return;
        }

        buildForm();
    }

    /**
     * Extracts the token from the URL query parameters.
     */
    private String extractTokenFromQuery(BeforeEnterEvent event) {
        List<String> tokenParams = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("token", List.of());
        return tokenParams.stream().findFirst().orElse(null);
    }

    /**
     * Builds and displays the password reset form with two password fields.
     */
    private void buildForm() {
        removeAll();
        
        FormLayout form = new FormLayout();
        PasswordField newPasswordField = new PasswordField("New password");
        PasswordField confirmPasswordField = new PasswordField("Confirm password");
        
        newPasswordField.setWidth(FORM_FIELD_WIDTH + "px");
        confirmPasswordField.setWidth(FORM_FIELD_WIDTH + "px");

        Button submitButton = new Button("Set new password", e -> handlePasswordReset(newPasswordField, confirmPasswordField));

        form.add(newPasswordField, confirmPasswordField, submitButton);
        add(form);
    }

    /**
     * Handles the password reset submission with validation.
     */
    private void handlePasswordReset(PasswordField newPasswordField, PasswordField confirmPasswordField) {
        String newPassword = newPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();
        
        if (!validatePasswords(newPassword, confirmPassword)) {
            return;
        }

        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (success) {
            Notification.show("Password changed. You can now login.", 
                    NOTIFICATION_DURATION_MEDIUM, Notification.Position.MIDDLE);
            navigateToLogin();
        } else {
            Notification.show("Failed to reset password. Token might be invalid or expired.", 
                    NOTIFICATION_DURATION_LONG, Notification.Position.MIDDLE);
            navigateToLogin();
        }
    }

    /**
     * Validates that passwords are provided and match.
     * 
     * @return true if validation passes, false otherwise
     */
    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            Notification.show("Please enter a password", 
                    NOTIFICATION_DURATION_SHORT, Notification.Position.MIDDLE);
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Notification.show("Passwords do not match", 
                    NOTIFICATION_DURATION_SHORT, Notification.Position.MIDDLE);
            return false;
        }
        
        return true;
    }

    /**
     * Navigates to the login page.
     */
    private void navigateToLogin() {
        UI.getCurrent().navigate(LOGIN_ROUTE);
    }
}
