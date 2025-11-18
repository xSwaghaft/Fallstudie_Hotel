package com.hotel.booking.view;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.UserService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@AnonymousAllowed
@CssImport("./themes/hotel/styles.css")
public class LoginView extends Div implements BeforeEnterObserver {

    private final UserService userService;
    private final SessionService sessionService;
    private final com.hotel.booking.service.PasswordResetService passwordResetService;

    private final Div themeToggle = new Div();
    private boolean darkMode = false;

    @Autowired
    public LoginView(UserService userService, SessionService sessionService, com.hotel.booking.service.PasswordResetService passwordResetService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordResetService = passwordResetService;

        setSizeFull();
        getStyle().set("display", "flex")
                  .set("flex-direction", "row")
                  .set("min-height", "100vh")
                  .set("overflow", "hidden");

        // --- linke Seite (Bild + Overlay) ---
        Div left = new Div();
        left.addClassName("login-left");
        Div overlay = new Div();
        overlay.addClassName("login-overlay");
        overlay.add(new H1("Hotelium"),
                    new Paragraph("Experience luxury and comfort with our comprehensive booking management system"));
        left.add(overlay);

        // --- rechte Seite (manuelles Formular) ---
        VerticalLayout right = new VerticalLayout();
        right.addClassName("login-right");
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div card = new Div();
        card.addClassName("login-card");

        H2 welcome = new H2("Welcome!");
        Paragraph info = new Paragraph("Sign in to access your account");

        // Felder - WICHTIG: setWidthFull() für gleiche Breite wie Button
        TextField username = new TextField("Username");
        username.setPlaceholder("Enter your username");
        username.setWidthFull();
        username.addClassName("login-input");
        
        PasswordField password = new PasswordField("Password");
        password.setPlaceholder("Enter your password");
        password.setWidthFull();
        password.addClassName("login-input");
        
        Checkbox remember = new Checkbox("Remember me");
        remember.addClassName("login-checkbox");

        Button loginBtn = new Button("Sign In");
        loginBtn.addClassName("gold-button");
        loginBtn.setWidthFull();
        loginBtn.addClickListener(e -> authenticate(username.getValue(), password.getValue()));
        // Allow pressing Enter to trigger login
        loginBtn.addClickShortcut(Key.ENTER);
        password.addKeyDownListener(Key.ENTER, e -> loginBtn.click());
        username.addKeyDownListener(Key.ENTER, e -> loginBtn.click());

        Button forgot = new Button("Forgot password?");
        forgot.addClassName("forgot-link");
        forgot.addClickListener(e -> openForgotPasswordDialog());

        HorizontalLayout creds = new HorizontalLayout(remember, forgot);
        creds.setWidthFull();
        creds.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Hr sep = new Hr();

        Div signup = new Div(new Text("Don't have an account? "),
                             new Anchor("#", "Sign up now"));
        signup.addClassName("sign-up-hint");

            card.add(welcome, info, username, password, creds, loginBtn, sep, signup);
        right.add(card);

        // Theme toggle
        Icon icon = VaadinIcon.ADJUST.create();
        icon.setSize("20px");
        themeToggle.add(icon);
        themeToggle.addClassName("theme-toggle");
        themeToggle.addClickListener(e -> toggleTheme());

        add(left, right, themeToggle);
    }

    private void openForgotPasswordDialog() {
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        com.vaadin.flow.component.orderedlayout.VerticalLayout layout = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
        com.vaadin.flow.component.textfield.TextField emailField = new com.vaadin.flow.component.textfield.TextField("Your email");
        emailField.setWidth("400px");
        com.vaadin.flow.component.button.Button sendBtn = new com.vaadin.flow.component.button.Button("Send reset email");
        com.vaadin.flow.component.button.Button cancelBtn = new com.vaadin.flow.component.button.Button("Cancel");

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

        layout.add(emailField, new com.vaadin.flow.component.orderedlayout.HorizontalLayout(sendBtn, cancelBtn));
        dialog.add(layout);
        dialog.open();
    }

    private void authenticate(String username, String password) {
        Optional<User> user = userService.authenticate(username, password);
        if (user.isPresent()) {
            sessionService.login(user.get());
            navigateAfterLogin(user.get().getRole());
        } else {
            Notification.show("Login failed", 3000, Notification.Position.MIDDLE);
        }
    }


    private void navigateAfterLogin(UserRole role) {
        if (role == UserRole.GUEST) {
            UI.getCurrent().navigate(GuestPortalView.class);
        } else {
            UI.getCurrent().navigate(DashboardView.class);
        }
    }

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

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionService.isLoggedIn())
            navigateAfterLogin(sessionService.getCurrentRole());
    }
}