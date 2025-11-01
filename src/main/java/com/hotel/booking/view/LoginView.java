package com.hotel.booking.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;

@Route("login")
@CssImport("./styles/views/login-view-split.css")
public class LoginView extends VerticalLayout {

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg"); // Split-Hintergrund via CSS

        // Card
        VerticalLayout card = new VerticalLayout();
        card.addClassName("login-card");
        card.setWidthFull();
        card.setMaxWidth("420px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.STRETCH);

        // Logo: Variante A (Datei liegt in frontend/images/)
        Image logo = new Image("/images/hsbi-logo.png", "Hotelium Logo");

        // // Variante B (Datei liegt in src/main/resources/static/)
        // Image logo = new Image("/hsbi-logo.png", "Hotelium Logo");

        logo.setMaxWidth("160px");
        logo.getStyle().set("margin", "0 auto 10px").set("display", "block");

        H2 title = new H2("Hotelium – Login");

        TextField email = new TextField("E-Mail");
        email.setWidthFull();

        PasswordField pw = new PasswordField("Passwort");
        pw.setWidthFull();

        Button login = new Button("Anmelden");
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        login.setWidthFull();

        // „Passwort vergessen?“ als RouterLink
        RouterLink forgot = new RouterLink();
        forgot.setText("Passwort vergessen?");
        forgot.setRoute(PasswordResetView.class);
        forgot.getStyle().set("display", "block").set("text-align", "right");
        forgot.addClassName("forgot-link");

        card.add(logo, title, email, pw, login, forgot);
        add(card);
    }
}
