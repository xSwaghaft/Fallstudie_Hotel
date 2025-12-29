package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.service.EmailService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route("email-test")
@PageTitle("Email Test")
@RolesAllowed(UserRole.MANAGER_VALUE)
public class EmailTestView extends VerticalLayout {

    @SuppressWarnings("unused")
    private final EmailService emailService;

    public EmailTestView(EmailService emailService) {
        this.emailService = emailService;

        TextField to = new TextField("To");
        TextField subject = new TextField("Subject");
        TextArea body = new TextArea("Body");
        body.setWidthFull();

        Button send = new Button("Send test email", event -> {
            String t = to.getValue();
            if (t == null || t.isBlank()) {
                Notification.show("'To' is required", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                emailService.sendSimpleMessage(t, subject.getValue(), body.getValue());
                Notification.show("Email sent to " + t, 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Error sending email: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        add(to, subject, body, send);
        setMaxWidth("800px");
        setPadding(true);
        setSpacing(true);
    }
}
