package com.hotel.booking.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import java.util.List;

@Route(value = "dashboard", layout = MainLayout.class)
@CssImport("./styles/views/dashboard-view.css")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        addClassName("hotel-dashboard-view");
        setSizeFull();
        setPadding(true);
        add(createDashboardContent());
    }

    // ----------------------------------------
    // Hauptinhalt (Dashboard)
    // ----------------------------------------
    private VerticalLayout createDashboardContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setSizeFull();

        // Stat-Karten
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);

        stats.add(
            createStatCard("Active Bookings", "2", "Upcoming stays"),
            createStatCard("Total Stays", "12", "Lifetime bookings")
            // createStatCard("Loyalty Points", "1,250", "Redeem for discounts")
        );

        // Buttons
        HorizontalLayout actions = new HorizontalLayout(
            createActionButton(VaadinIcon.PLUS, "New Booking", true), // Orange Button
            createActionButton(VaadinIcon.HOME, "My Bookings", false),
            createActionButton(VaadinIcon.FILE_TEXT, "Leave Review", false)
        );
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        actions.addClassName("actions-row"); 

        // Tabelle (My Recent Bookings)
        H3 tableTitle = new H3("My Recent Bookings");
        Grid<Booking> grid = createBookingsGrid();

        content.add(stats, actions, tableTitle, grid);
        return content;
    }

    private Div createStatCard(String title, String value, String subtitle) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 6px rgba(0,0,0,0.05)")
            .set("padding", "var(--lumo-space-m)")
            .set("flex", "1");

        H4 header = new H4(title);
        H3 val = new H3(value);
        Paragraph sub = new Paragraph(subtitle);
        sub.getStyle().set("color", "gray").set("font-size", "14px");

        card.add(header, val, sub);
        return card;
    }

      // Hilfsmethode zum Erstellen eines Aktions-Buttons
    private Button createActionButton(VaadinIcon icon, String text, boolean isPrimary) {
        Button button = new Button(text, new Icon(icon));
        button.addClassName("action-button");
        if (isPrimary) {
            button.addThemeName("primary"); 
        }
        return button;
    }

    // ----------------------------------------
    // Tabelle mit Grid
    // ----------------------------------------
    private Grid<Booking> createBookingsGrid() {
        Grid<Booking> grid = new Grid<>(Booking.class, false);

        grid.addColumn(Booking::getId).setHeader("Booking ID");
        grid.addColumn(Booking::getRoom).setHeader("Room");
        grid.addColumn(Booking::getCheckIn).setHeader("Check-in");
        grid.addColumn(Booking::getCheckOut).setHeader("Check-out");
        grid.addColumn(Booking::getStatus).setHeader("Status");

        grid.setItems(List.of(
                new Booking("BK001", "201 - Deluxe", "2025-11-02", "2025-11-05", "confirmed"),
                new Booking("BK002", "105 - Suite", "2025-11-01", "2025-11-03", "checked-in")
        ));

        grid.setWidthFull();
        grid.getStyle().set("border-radius", "10px").set("background", "white");
        return grid;
    }

    // ----------------------------------------
    // Innere Datenklasse für Buchungen
    // ----------------------------------------
    public static class Booking {
        private String id;
        private String room;
        private String checkIn;
        private String checkOut;
        private String status;

        public Booking(String id, String room, String checkIn, String checkOut, String status) {
            this.id = id;
            this.room = room;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.status = status;
        }

        public String getId() { return id; }
        public String getRoom() { return room; }
        public String getCheckIn() { return checkIn; }
        public String getCheckOut() { return checkOut; }
        public String getStatus() { return status; }
    }
}