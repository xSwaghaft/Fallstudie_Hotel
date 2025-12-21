package com.hotel.booking.view.components;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;

/**
 * Reusable login form component with validation and customizable callbacks.
 * <p>
 * This Vaadin form component provides:
 * </p>
 * <ul>
 *   <li>Username and password input fields with validation</li>
 *   <li>Form binding using Vaadin Binder for data validation</li>
 *   <li>Customizable click handlers for login, forgot password, and sign-up</li>
 *   <li>Enter key support for submitting the form</li>
 *   <li>Built-in password field clearing and focus management</li>
 * </ul>
 *
 * @author Artur Derr
 */
public class LoginForm extends Div {

    private final Binder<LoginCredentials> binder = new Binder<>(LoginCredentials.class);
    private final LoginCredentials credentials = new LoginCredentials();

    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final Button loginBtn = new Button("Login");
    private final Button forgotBtn = new Button("Forgot password?");
    private final Button signupBtn = new Button("Sign up now");

    private Runnable onLoginClick;
    private Runnable onForgotClick;
    private Runnable onSignupClick;

    /**
     * Constructs a new LoginForm with initialized fields and layout.
     * <p>
     * Initializes all form fields, configures data binding and validation,
     * and arranges the layout of the form components.
     * </p>
     */
    public LoginForm() {
        setWidthFull();
        initializeFields();
        configureBinder();
        layoutForm();
    }

    /**
     * Initializes and configures all form input fields and buttons.
     * <p>
     * Sets up placeholders, styles, validators, and event listeners for each field.
     * </p>
     */
    private void initializeFields() {
        usernameField.setPlaceholder("Enter your username");
        usernameField.setWidthFull();
        usernameField.addClassName("login-input");
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        passwordField.setPlaceholder("Enter your password");
        passwordField.setWidthFull();
        passwordField.addClassName("login-input");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        loginBtn.addClassName("gold-button");
        loginBtn.setWidthFull();
        loginBtn.addClickListener(e -> handleLogin());
        loginBtn.addClickShortcut(Key.ENTER);

        forgotBtn.addClassName("forgot-link");
        forgotBtn.addClickListener(e -> {
            if (onForgotClick != null) onForgotClick.run();
        });

        signupBtn.addClassName("forgot-link");
        signupBtn.addClickListener(e -> {
            if (onSignupClick != null) onSignupClick.run();
        });

        // Allow Enter key to trigger login
        passwordField.addKeyDownListener(Key.ENTER, e -> loginBtn.click());
        usernameField.addKeyDownListener(Key.ENTER, e -> loginBtn.click());
    }

    /**
     * Configures the Vaadin Binder for form validation and data binding.
     * <p>
     * Sets up validation rules for username and password fields, including
     * required field validation and string length constraints.
     * </p>
     */
    private void configureBinder() {
        binder.forField(usernameField)
            .asRequired("Username is required")
            .withValidator(new StringLengthValidator("Username must be at least 2 characters", 2, null))
            .bind(LoginCredentials::getUsername, LoginCredentials::setUsername);

        binder.forField(passwordField)
            .asRequired("Password is required")
            .withValidator(new StringLengthValidator("Password must be at least 6 characters", 6, null))
            .bind(LoginCredentials::getPassword, LoginCredentials::setPassword);

        binder.setBean(credentials);
    }

    /**
     * Arranges the form components in the layout.
     * <p>
     * Creates a FormLayout for the main input fields and a HorizontalLayout for the link buttons.
     * </p>
     */
    private void layoutForm() {
        FormLayout form = new FormLayout();
        form.add(usernameField, passwordField, loginBtn);

        HorizontalLayout linkLayout = new HorizontalLayout(forgotBtn, signupBtn);
        linkLayout.setWidthFull();
        linkLayout.getStyle().set("justify-content", "space-between");

        add(form, linkLayout);
    }

    /**
     * Handles the login button click event.
     * <p>
     * Validates the form using the configured binder, writes the bean if valid,
     * and executes the registered login callback.
     * Shows a notification if validation fails.
     * </p>
     */
    private void handleLogin() {
        try {
            binder.writeBean(credentials);
            if (onLoginClick != null) {
                onLoginClick.run();
            }
        } catch (ValidationException ex) {
            Notification.show("Please fix validation errors", 3000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Retrieves the login credentials from the form.
     *
     * @return the LoginCredentials object containing username and password
     */
    public LoginCredentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the callback to be executed when the login button is clicked.
     *
     * @param callback the Runnable to execute on login button click
     */
    public void setOnLoginClick(Runnable callback) {
        this.onLoginClick = callback;
    }

    /**
     * Sets the callback to be executed when the forgot password link is clicked.
     *
     * @param callback the Runnable to execute on forgot password link click
     * @author Viktor Götting
     */
    public void setOnForgotClick(Runnable callback) {
        this.onForgotClick = callback;
    }

    /**
     * Sets the callback to be executed when the sign-up link is clicked.
     *
     * @param callback the Runnable to execute on sign-up link click
     */
    public void setOnSignupClick(Runnable callback) {
        this.onSignupClick = callback;
    }

    /**
     * Clears the password field and sets focus to it.
     * <p>
     * Useful after a failed login attempt to allow the user to retry.
     * </p>
     */
    public void clearPassword() {
        passwordField.clear();
        passwordField.focus();
    }

    /**
     * Internal model class for login credentials.
     * Used with Binder for form binding and validation.
     */
    public static class LoginCredentials {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
