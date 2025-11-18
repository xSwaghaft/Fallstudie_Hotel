package com.hotel.booking.view;

import com.hotel.booking.service.PasswordResetService;
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

@Route("reset-password")
@AnonymousAllowed
@PageTitle("Reset Password")
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final PasswordResetService passwordResetService;

    private String token;

    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
        setWidth("100%");
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        token = event.getLocation().getQueryParameters().getParameters().getOrDefault("token", java.util.List.of()).stream().findFirst().orElse(null);
        if (token == null) {
            Notification.show("Missing token", 3000, Notification.Position.MIDDLE);
            event.forwardTo("/login");
            return;
        }

        var emailOpt = passwordResetService.verifyToken(token);
        if (emailOpt.isEmpty()) {
            Notification.show("Invalid or expired token", 3500, Notification.Position.MIDDLE);
            event.forwardTo("/login");
            return;
        }

        buildForm();
    }

    private void buildForm() {
        removeAll();
        FormLayout form = new FormLayout();
        PasswordField pw = new PasswordField("New password");
        PasswordField pw2 = new PasswordField("Confirm password");
        pw.setWidth("320px");
        pw2.setWidth("320px");

        Button submit = new Button("Set new password", e -> {
            String p1 = pw.getValue();
            String p2 = pw2.getValue();
            if (p1 == null || p1.isBlank()) {
                Notification.show("Please enter a password", 2500, Notification.Position.MIDDLE);
                return;
            }
            if (!p1.equals(p2)) {
                Notification.show("Passwords do not match", 2500, Notification.Position.MIDDLE);
                return;
            }

            boolean ok = passwordResetService.resetPassword(token, p1);
            if (ok) {
                Notification.show("Password changed. You can now login.", 3000, Notification.Position.MIDDLE);
                com.vaadin.flow.component.UI.getCurrent().navigate("/login");
            } else {
                Notification.show("Failed to reset password. Token might be invalid or expired.", 3500, Notification.Position.MIDDLE);
                com.vaadin.flow.component.UI.getCurrent().navigate("/login");
            }
        });

        form.add(pw, pw2, submit);
        add(form);
    }
}
