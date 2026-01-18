package com.hotel.booking.view;

import java.util.List;
import java.util.Optional;

import com.hotel.booking.service.PasswordResetService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * View for resetting user passwords using a reset token.
 * <p>
 * This view provides a password reset form that validates a reset token from the URL
 * and allows users to set a new password. The form uses Vaadin Binder for validation
 * and follows the same design pattern as the registration and login views.
 * </p>
 * 
 * @author Viktor Götting
 */
@Route("reset-password")
@AnonymousAllowed
@PageTitle("Reset Password")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/login.css")
@CssImport("./themes/hotel/views/reset-password.css")
public class ResetPasswordView extends Div implements BeforeEnterObserver {

    /** Route path for password reset view */
    public static final String ROUTE = "reset-password";

    private static final int PASSWORD_MIN_LENGTH = 8;

    private final PasswordResetService passwordResetService;
    private final Binder<PasswordData> binder = new Binder<>(PasswordData.class);
    private final PasswordData passwordData = new PasswordData();
    
    private String token;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Button submitButton;
    private Button backToLoginButton;

    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
        addClassName("reset-password-view");
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        token = extractTokenFromQuery(event);
        
        if (token == null) {
            Notification.show("Missing token", 3000, Notification.Position.MIDDLE);
            event.forwardTo("/login");
            return;
        }

        Optional<String> emailOpt = passwordResetService.verifyToken(token);
        if (emailOpt.isEmpty()) {
            Notification.show("Invalid or expired token", 3000, Notification.Position.MIDDLE);
            event.forwardTo("/login");
            return;
        }

        buildView();
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
     * Builds the complete view with left and right sections.
     */
    private void buildView() {
        removeAll();
        add(createLeftSection());
        add(createRightSection());
    }

    /**
     * Creates the left section with branding (similar to login/registration views).
     */
    private Div createLeftSection() {
        Div left = new Div();
        left.addClassName("login-left");
        
        Div overlay = new Div();
        overlay.addClassName("login-overlay");
        overlay.add(new H1("Hotelium"),
                    new Paragraph("Reset your password to regain access to your account"));
        left.add(overlay);
        
        return left;
    }

    /**
     * Creates the right section with the password reset form card.
     */
    private VerticalLayout createRightSection() {
        VerticalLayout right = new VerticalLayout();
        right.addClassName("login-right");
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div card = new Div();
        card.addClassName("reset-password-card");

        H2 title = new H2("Reset Password");
        title.addClassName("reset-password-title");
            
        Paragraph info = new Paragraph("Enter your new password below");
        info.addClassName("reset-password-subtitle");

        FormLayout form = createForm();
        configureBinder();

        card.add(title, info, form, createBackToLoginButton());
        right.add(card);
        
        return right;
    }

    /**
     * Creates and configures the password reset form.
     */
    private FormLayout createForm() {
        FormLayout form = new FormLayout();
        form.addClassName("reset-password-form");

        newPasswordField = new PasswordField("New password");
        newPasswordField.setPlaceholder("Enter new password");
        newPasswordField.setWidthFull();
        newPasswordField.setRequiredIndicatorVisible(true);
        newPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
        newPasswordField.setHelperText("At least 8 characters with uppercase, lowercase, number and special character");

        confirmPasswordField = new PasswordField("Confirm password");
        confirmPasswordField.setPlaceholder("Confirm new password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());

        submitButton = new Button("Set new password", VaadinIcon.CHECK.create(), e -> handlePasswordReset());
        submitButton.addClassName("gold-button");
        submitButton.setWidthFull();
        submitButton.addClickShortcut(Key.ENTER);

        form.add(newPasswordField, confirmPasswordField, submitButton);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1)
        );

        return form;
    }

    /**
     * Creates the back to login button.
     */
    private Button createBackToLoginButton() {
        backToLoginButton = new Button("Back to Login", VaadinIcon.ARROW_LEFT.create(), 
            e -> UI.getCurrent().navigate(LoginView.class));
        backToLoginButton.addClassName("back-login-button");
        backToLoginButton.setWidthFull();
        return backToLoginButton;
    }

    /**
     * Configures the Vaadin Binder for form validation.
     * Uses the same password strength rules as RegistrationForm.
     */
    private void configureBinder() {
        binder.forField(newPasswordField)
            .asRequired("Password is required")
            .withValidator(this::validatePasswordStrength)
            .bind(PasswordData::getNewPassword, PasswordData::setNewPassword);

        binder.forField(confirmPasswordField)
            .asRequired("Please confirm your password")
            .withValidator(this::validatePasswordMatch)
            .bind(PasswordData::getConfirmPassword, PasswordData::setConfirmPassword);

        binder.setBean(passwordData);
        
        // Real-time validation: update confirm field when new password changes
        newPasswordField.addValueChangeListener(e -> {
            if (confirmPasswordField.getValue() != null && !confirmPasswordField.getValue().isEmpty()) {
                confirmPasswordField.setInvalid(!e.getValue().equals(confirmPasswordField.getValue()));
            }
        });
    }

    /**
     * Validates that the password meets all strength criteria (same as RegistrationForm).
     * Requires: minimum 8 characters, uppercase, lowercase, digit, and special character.
     *
     * @param value password value
     * @param context binder context
     * @return validation result
     */
    private ValidationResult validatePasswordStrength(String value, ValueContext context) {
        if (value == null || value.isBlank()) {
            return ValidationResult.error("Password is required");
        }
        
        boolean hasMinLength = value.length() >= PASSWORD_MIN_LENGTH;
        boolean hasUppercase = value.matches(".*[A-Z].*");
        boolean hasLowercase = value.matches(".*[a-z].*");
        boolean hasDigit = value.matches(".*\\d.*");
        boolean hasSpecialChar = value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        if (!hasMinLength || !hasUppercase || !hasLowercase || !hasDigit || !hasSpecialChar) {
            return ValidationResult.error("Password must be at least 8 characters with uppercase, lowercase, number and special character");
        }
        
        return ValidationResult.ok();
    }

    /**
     * Validates that the password confirmation matches the original password.
     *
     * @param value confirmation value
     * @param context binder context
     * @return validation result
     */
    private ValidationResult validatePasswordMatch(String value, ValueContext context) {
        String newPassword = passwordData.getNewPassword();
        if (value == null || value.isBlank()) {
            return ValidationResult.error("Password confirmation is required");
        }
        if (newPassword == null || !newPassword.equals(value)) {
            return ValidationResult.error("Passwords do not match");
        }
        return ValidationResult.ok();
    }

    /**
     * Handles the password reset submission.
     */
    private void handlePasswordReset() {
        try {
            binder.writeBean(passwordData);
            
            // Disable button during processing
            submitButton.setEnabled(false);
            submitButton.setText("Processing...");
            
            boolean success = passwordResetService.resetPassword(token, passwordData.getNewPassword());
            
            if (success) {
                Notification.show("Password changed successfully. You can now login.", 
                        3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate(LoginView.class);
            } else {
                Notification.show("Failed to reset password. Token might be invalid or expired.", 
                        3000, Notification.Position.MIDDLE);
                submitButton.setEnabled(true);
                submitButton.setText("Set new password");
                UI.getCurrent().navigate(LoginView.class);
            }
        } catch (ValidationException ex) {
            Notification.show("Please fix validation errors", 
                    3000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Internal model class for password reset data.
     * Used with Binder for form binding and validation.
     */
    private static class PasswordData {
        private String newPassword;
        private String confirmPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
