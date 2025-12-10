package com.hotel.booking.view;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.AdressEmbeddable;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.service.UserService;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.view.components.RegistrationForm;
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
 * RegistrationView - Registration form following Vaadin and Spring Security best practices.
 * Uses RegistrationForm component for form layout and validation.
 */
@Route("register")
@AnonymousAllowed
@PageTitle("Registration for Hotelium")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/registration.css")
public class RegistrationView extends Div implements BeforeEnterObserver {

    private final UserService userService;
    private final SessionService sessionService;

    public RegistrationView(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;

        addClassName("registration-view");
        setSizeFull();

        add(createLeftSection());
        add(createRightSection());
    }

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

        // Use RegistrationForm Component
        RegistrationForm registrationForm = new RegistrationForm();
        
        registrationForm.setOnRegisterClick(() -> handleRegistration(registrationForm));
        registrationForm.setOnCancelClick(() -> UI.getCurrent().navigate(LoginView.class));

        card.add(title, info, registrationForm);
        right.add(card);
        
        return right;
    }

    private void handleRegistration(RegistrationForm registrationForm) {
        RegistrationForm.RegistrationFormData formData = registrationForm.getFormData();
        
        // Binder validiert bereits die Daten - hier können wir direkt verarbeiten
        try {
            AdressEmbeddable address = new AdressEmbeddable();
            address.setStreet(formData.getStreet());
            address.setHouseNumber(formData.getHouseNumber());
            address.setPostalCode(formData.getPostalCode());
            address.setCity(formData.getCity());
            address.setCountry(formData.getCountry());

            User newUser = new User(
                    formData.getUsername(),
                    formData.getFirstName(),
                    formData.getLastName(),
                    address,
                    formData.getEmail(),
                    formData.getPassword(),
                    UserRole.GUEST,
                    true
            );

            userService.registerUser(newUser);
            showRegistrationSuccessDialog();
        } catch (IllegalArgumentException e) {
            Notification.show("Registration failed: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("An unexpected error occurred during registration", 4000, Notification.Position.MIDDLE);
        }
    }

    private void showRegistrationSuccessDialog() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Grüner Haken Icon
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

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionService != null && sessionService.getCurrentUser() != null) {
            UI.getCurrent().navigate(DashboardView.class);
        }
    }
}
