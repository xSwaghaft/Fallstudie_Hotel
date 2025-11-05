package com.hotel.booking.view;

import com.hotel.booking.security.SessionService;
import com.hotel.booking.security.UserRole;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "reports", layout = MainLayout.class)
public class ReportsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    @Autowired
    public ReportsView(SessionService sessionService) {
        this.sessionService = sessionService;
        add(new H1("Reports & Analytics"),
            new Paragraph("Charts and KPIs can be added here (placeholder)."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}
