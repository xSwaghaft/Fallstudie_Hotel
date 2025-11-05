package com.hotel.booking.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")               // reagiert auf http://localhost:8080/
@AnonymousAllowed        // Zugriff ohne Login erlauben
public class RootRedirectView extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Sauberer Redirect, keine UI-Endlosschleife
        event.forwardTo(LoginView.class);
    }
}
