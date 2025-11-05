package com.hotel.booking.view;

import com.hotel.booking.security.SessionService;
import com.hotel.booking.security.UserRole;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Einfache Platzhalter-View für App-Einstellungen.
 * Zugriff nur für MANAGER.
 */
@Route(value = "settings", layout = MainLayout.class)
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    @Autowired
    public SettingsView(SessionService sessionService) {
        this.sessionService = sessionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Settings"),
            new Paragraph("This is a placeholder settings view. (Manager only)"));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionService.getCurrentRole() != UserRole.MANAGER) {
            event.rerouteTo(LoginView.class);
        }
    }
}
