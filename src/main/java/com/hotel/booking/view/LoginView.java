package com.hotel.booking.view;

import org.springframework.beans.factory.annotation.Autowired;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.UserService;
import com.hotel.booking.service.PasswordResetService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@AnonymousAllowed
@CssImport("./themes/hotel/styles.css")
public class LoginView extends Div implements BeforeEnterObserver {

    private final UserService userService;
    private final SessionService sessionService;
    private final PasswordResetService passwordResetService;

    private final Binder<LoginCredentials> binder = new Binder<>(LoginCredentials.class);
    private final Div themeToggle = new Div();
    private boolean darkMode = false;

    @Autowired
    public LoginView(UserService userService, SessionService sessionService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordResetService = passwordResetService;

        setSizeFull();
        getStyle().set("display", "flex")
                  .set("flex-direction", "row")
                  .set("min-height", "100vh")
                  .set("overflow", "hidden");

        // Build UI components
        Div left = createLeftPanel();
        VerticalLayout right = createRightPanel();
        
        // Theme toggle button
        Icon icon = VaadinIcon.ADJUST.create();
        icon.setSize("20px");
        themeToggle.add(icon);
        themeToggle.addClassName("theme-toggle");
        themeToggle.addClickListener(e -> toggleTheme());

        add(left, right, themeToggle);
    }

    /**
     * Creates the left panel with background image and branding.
     */
    private Div createLeftPanel() {
        Div left = new Div();
        left.addClassName("login-left");
        Div overlay = new Div();
        overlay.addClassName("login-overlay");
        overlay.add(new H1("Hotelium"),
                    new Paragraph("Experience luxury and comfort with our comprehensive booking management system"));
        left.add(overlay);
        return left;
    }

    /**
     * Creates the right panel with login form using Binder.
     */
    private VerticalLayout createRightPanel() {
        VerticalLayout right = new VerticalLayout();
        right.addClassName("login-right");
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div card = new Div();
        card.addClassName("login-card");

        H2 welcome = new H2("Welcome!");
        Paragraph info = new Paragraph("Sign in to access your account");

        // Create form fields
        TextField usernameField = new TextField("Username");
        usernameField.setPlaceholder("Enter your username");
        usernameField.setWidthFull();
        usernameField.addClassName("login-input");
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");
        
        PasswordField passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Enter your password");
        passwordField.setWidthFull();
        passwordField.addClassName("login-input");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        Button loginBtn = new Button("Login");
        loginBtn.addClassName("gold-button");
        loginBtn.setWidthFull();

        Button forgotBtn = new Button("Forgot password?");
        forgotBtn.addClassName("forgot-link");
        forgotBtn.addClickListener(e -> openForgotPasswordDialog());

        Button signupBtn = new Button("Sign up now");
        signupBtn.addClassName("forgot-link");
        signupBtn.addClickListener(e -> UI.getCurrent().navigate(RegistrationView.class));

        HorizontalLayout linkLayout = new HorizontalLayout(forgotBtn, signupBtn);
        linkLayout.setWidthFull();
        linkLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Configure Binder for login credentials
        LoginCredentials credentials = new LoginCredentials();
        configureBinder(binder, usernameField, passwordField);

        // Handle login button click
        loginBtn.addClickListener(e -> handleLogin(credentials, usernameField, passwordField));
        
        // Allow Enter key to trigger login
        passwordField.addKeyDownListener(Key.ENTER, e -> loginBtn.click());
        usernameField.addKeyDownListener(Key.ENTER, e -> loginBtn.click());

        card.add(welcome, info, usernameField, passwordField, loginBtn, linkLayout);
        right.add(card);
        
        return right;
    }

    /**
     * Configures the Binder with validation rules for username and password.
     */
    private void configureBinder(Binder<LoginCredentials> binder, TextField usernameField, PasswordField passwordField) {
        binder.forField(usernameField)
            .asRequired("Username is required")
            .withValidator(new StringLengthValidator("Username must be at least 2 characters", 2, null))
            .bind(LoginCredentials::getUsername, LoginCredentials::setUsername);

        binder.forField(passwordField)
            .asRequired("Password is required")
            .withValidator(new StringLengthValidator("Password must be at least 6 characters", 6, null))
            .bind(LoginCredentials::getPassword, LoginCredentials::setPassword);
    }

    /**
     * Handles login attempt by validating input via Binder and authenticating via UserService.
     */
    private void handleLogin(LoginCredentials credentials, TextField usernameField, PasswordField passwordField) {
        try {
            binder.writeBean(credentials);
            
            var user = userService.authenticate(credentials.getUsername(), credentials.getPassword());
            if (user.isPresent()) {
                sessionService.login(user.get());
                navigateAfterLogin(user.get().getRole());
            } else {
                Notification.show("Invalid username or password", 3000, Notification.Position.MIDDLE);
                passwordField.clear();
                passwordField.focus();
            }
        } catch (ValidationException ex) {
            Notification.show("Please fix validation errors", 3000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Navigates the user to the appropriate view based on their role.
     */
    private void navigateAfterLogin(UserRole role) {
        if (role == UserRole.GUEST) {
            UI.getCurrent().navigate(GuestPortalView.class);
        } else {
            UI.getCurrent().navigate(DashboardView.class);
        }
    }

    /**
     * Opens a dialog for password reset.
     */
    private void openForgotPasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reset Password");
        dialog.setWidth("400px");

        TextField emailField = new TextField("Email address");
        emailField.setPlaceholder("Enter your email");
        emailField.setWidthFull();
        
        Button sendBtn = new Button("Send reset email");
        Button cancelBtn = new Button("Cancel");

        sendBtn.addClickListener(ev -> {
            String email = emailField.getValue();
            if (email == null || email.isBlank()) {
                Notification.show("Please enter your email", 3000, Notification.Position.MIDDLE);
                return;
            }

            boolean sent = passwordResetService.createTokenAndSend(email);
            if (sent) {
                Notification.show("If an account with that email exists, a reset link was sent.", 4000, Notification.Position.MIDDLE);
            } else {
                Notification.show("If an account with that email exists, a reset link was sent.", 4000, Notification.Position.MIDDLE);
            }
            dialog.close();
        });

        cancelBtn.addClickListener(ev -> dialog.close());

        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout buttonLayout = new HorizontalLayout(sendBtn, cancelBtn);
        layout.add(emailField, buttonLayout);
        dialog.add(layout);
        dialog.open();
    }

    /**
     * Toggles between light and dark theme.
     */
    private void toggleTheme() {
        darkMode = !darkMode;
        UI ui = UI.getCurrent();
        if (darkMode) {
            ui.getElement().getClassList().add("dark-theme");
            ui.getPage().executeJs("document.documentElement.classList.add('dark-theme')");
        } else {
            ui.getElement().getClassList().remove("dark-theme");
            ui.getPage().executeJs("document.documentElement.classList.remove('dark-theme')");
        }
        VaadinSession.getCurrent().setAttribute("darkMode", darkMode);
    }

    /**
     * Checks if user is already logged in and redirects if necessary.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionService.isLoggedIn()) {
            navigateAfterLogin(sessionService.getCurrentRole());
        }
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