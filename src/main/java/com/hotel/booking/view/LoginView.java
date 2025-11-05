package com.hotel.booking.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("login")
public class LoginView extends VerticalLayout {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        // --- Seitenlayout ---
        setSizeFull();
        // getStyle().set("background-image", "url('images/Hintergrund_login.jpg')");
        getStyle().set("background-size", "cover");
        getStyle().set("background-position", "center");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // --- Card Layout ---
        Div card = new Div();
        card.addClassName(LumoUtility.Padding.LARGE);
        card.getStyle().set("background", "white");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");
        card.getStyle().set("max-width", "400px");
        card.getStyle().set("width", "100%");

        // --- Icon / Header ---
        Image icon = new Image("images/hotel-icon.png", "Hotel Icon");
        icon.setWidth("60px");
        icon.getStyle().set("margin", "auto");

        H2 title = new H2("Hotelium");
        title.getStyle().set("text-align", "center");

        Paragraph subtitle = new Paragraph("Sign in to access system");
        subtitle.getStyle().set("text-align", "center");
        subtitle.getStyle().set("color", "gray");

        // --- LoginForm ---
        loginForm.setAction("login");
        loginForm.getElement().getStyle().set("width", "100%");
        loginForm.getElement().getStyle().set("margin-top", "var(--lumo-space-m)");

        // --- Demo Accounts Box ---
        Div demoBox = new Div();
        demoBox.getStyle().set("background-color", "#fff8e1");
        demoBox.getStyle().set("border-radius", "8px");
        demoBox.getStyle().set("padding", "var(--lumo-space-s)");
        demoBox.getStyle().set("font-size", "14px");

        demoBox.add(
                new Text("Demo accounts:"),
                new Html("<ul>" +
                        "<li>Username: guest (Guest role)</li>" +
                        "<li>Username: receptionist (Receptionist role)</li>" +
                        "<li>Username: manager (Manager role)</li>" +
                        "</ul>")
        );

        // --- Alles zusammenbauen ---
        card.add(icon, title, subtitle, loginForm, demoBox);
        add(card);
    }
}
