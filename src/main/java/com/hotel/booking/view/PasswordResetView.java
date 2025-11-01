package com.hotel.booking.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("forgot-password")
public class PasswordResetView extends VerticalLayout {
    public PasswordResetView() {
        add(new H2("Passwort zurücksetzen"));
        add(new Paragraph("Hier könntest du dein Passwort ändern."));
    }
}
