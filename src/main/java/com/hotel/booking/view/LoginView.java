package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.UserService;
import com.hotel.booking.service.PasswordResetService;
import com.hotel.booking.view.components.LoginForm;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@AnonymousAllowed
@PageTitle("Welcome to Hotelium!")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/login.css")
public class LoginView extends Div implements BeforeEnterObserver {

    private final UserService userService;
    private final SessionService sessionService;
    private final PasswordResetService passwordResetService;

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

        add(left, right);
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
     * Creates the right panel with login form component.
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

        // Use LoginForm Component
        LoginForm loginForm = new LoginForm();
        
        loginForm.setOnLoginClick(() -> handleLogin(loginForm));
        loginForm.setOnForgotClick(this::openForgotPasswordDialog);
        loginForm.setOnSignupClick(() -> UI.getCurrent().navigate(RegistrationView.class));

        card.add(welcome, info, loginForm);
        right.add(card);
        
        return right;
    }

    /**
     * Handles login attempt by validating and authenticating via UserService.
     */
    private void handleLogin(LoginForm loginForm) {
        LoginForm.LoginCredentials credentials = loginForm.getCredentials();
        
        var user = userService.authenticate(credentials.getUsername(), credentials.getPassword());
        if (user.isPresent()) {
            sessionService.login(user.get());
            navigateAfterLogin(user.get().getRole());
        } else {
            Notification.show("Invalid username or password", 3000, Notification.Position.MIDDLE);
            loginForm.clearPassword();
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
     * Checks if user is already logged in and redirects if necessary.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionService.isLoggedIn()) {
            navigateAfterLogin(sessionService.getCurrentRole());
        }
    }
}