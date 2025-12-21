package com.hotel.booking.view;

import com.hotel.booking.service.UserService;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.view.components.AddUserForm;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * User registration view for creating new accounts.
 * <p>
 * This view provides a registration form for new users to create accounts in the system.
 * Uses AddUserForm in registration mode for form layout and validation.
 * The view is publicly accessible and redirects already-logged-in users to the dashboard.
 * </p>
 *
 * @author Artur Derr
 */
@Route("register")
@AnonymousAllowed
@PageTitle("Registration for Hotelium")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/login.css")
@CssImport("./themes/hotel/views/registration.css")
public class RegistrationView extends Div implements BeforeEnterObserver {

    private final UserService userService;
    private final SessionService sessionService;

    /**
     * Constructs a RegistrationView with necessary services.
     * <p>
     * Initializes the registration page layout with left and right sections.
     * </p>
     *
     * @param userService the service for creating new users
     * @param sessionService the service for managing user sessions
     */
    public RegistrationView(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;

        addClassName("registration-view");
        setSizeFull();

        add(createLeftSection());
        add(createRightSection());
    }

    /**
     * Creates the left section of the registration page.
     * <p>
     * Displays a branded overlay with application name and tagline.
     * </p>
     *
     * @return a Div containing the left section layout
     */
    private Div createLeftSection() {
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
     * Creates the right section of the registration page.
     * <p>
     * Displays the registration form card with title, info text, and AddUserForm.
     * </p>
     *
     * @return a VerticalLayout containing the right section layout
     */
    private VerticalLayout createRightSection() {
        VerticalLayout right = new VerticalLayout();
        right.addClassName("login-right");
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div card = new Div();
        card.addClassName("registration-card");

        H2 title = new H2("Create Your Account");
        title.addClassName("registration-title");
            
        Paragraph info = new Paragraph("Register to get started with our platform");
        info.addClassName("registration-subtitle");

        // Use AddUserForm in registration mode
        AddUserForm registrationForm = AddUserForm.forRegistration(userService);
        
        registrationForm.setOnRegisterClick(() -> handleRegistration(registrationForm));
        registrationForm.setOnCancelClick(() -> UI.getCurrent().navigate(LoginView.class));

        card.add(title, info, registrationForm);
        right.add(card);
        
        return right;
    }

    /**
     * Handles the user registration process.
     * <p>
     * Registers a new user from the registration form and displays a success dialog
     * or error notification depending on the result.
     * </p>
     *
     * @param registrationForm the AddUserForm containing the user registration data
     */
    private void handleRegistration(AddUserForm registrationForm) {
        // AddUserForm validates and writes the bean before calling this callback.
        try {
            userService.registerUser(registrationForm.getUser());
            showRegistrationSuccessDialog();
        } catch (IllegalArgumentException e) {
            Notification.show("Registration failed: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("An unexpected error occurred during registration", 4000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Displays a success dialog after successful registration.
     * <p>
     * Shows a modal dialog with a success icon and message, then redirects to the login page
     * when the user clicks the close button.
     * </p>
     */
    private void showRegistrationSuccessDialog() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Green checkmark icon
        Icon successIcon = VaadinIcon.CHECK_CIRCLE.create();
        successIcon.addClassName("success-dialog-icon");

        Paragraph title = new Paragraph("Registration successful!");
        title.addClassName("success-dialog-title");

        Paragraph message = new Paragraph(
                "Your account has been created successfully. " +
                "You can now log in with your credentials."
        );

        layout.add(successIcon, title, message);

        Button closeBtn = new Button("Back to Login", e -> {
            dialog.close();
            UI.getCurrent().navigate(LoginView.class);
        });
        closeBtn.addClassName("gold-button");

        layout.add(closeBtn);
        dialog.add(layout);
        dialog.open();
    }

    /**
     * Handles authorization before entering the registration view.
     * <p>
     * If the user is already logged in, redirects them to the dashboard.
     * Otherwise, allows access to the registration page.
     * </p>
     *
     * @param event the BeforeEnterEvent containing navigation information
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionService != null && sessionService.getCurrentUser() != null) {
            UI.getCurrent().navigate(DashboardView.class);
        }
    }
}
