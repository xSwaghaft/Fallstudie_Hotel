package com.hotel.booking.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.List;

// Hilfsklasse für die Daten der Grid
class Booking {
    String bookingId;
    String guestName;
    String room;
    LocalDate checkIn;
    LocalDate checkOut;
    String status;

    public Booking(String bookingId, String guestName, String room, LocalDate checkIn, LocalDate checkOut, String status) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
    }
    
    public String getBookingId() { return bookingId; }
    public String getGuestName() { return guestName; }
    public String getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public String getStatus() { return status; }
}

@Route(value = "mdashboard", layout = MainLayout.class)
@CssImport("./styles/views/dashboard-view.css")
public class MDashboardView extends VerticalLayout {

    // --- Konstruktor ---
    public MDashboardView() {
        addClassName("hotel-dashboard-view");
        setSizeFull();
        setPadding(true);
        add(createDashboardContent());
    }

    // --- 3. Hauptinhalt (Dashboard-Ansicht) ---
    private VerticalLayout createDashboardContent() {
        VerticalLayout content = new VerticalLayout();
        content.addClassName("dashboard-content-area");

        // Top-Kacheln (Key Metrics)
        HorizontalLayout stats = new HorizontalLayout();

        stats.add(
            createStatCard("Active Bookings", "2", "Upcoming stays"),
            createStatCard("Total Stays", "12", "Lifetime bookings"),
            createStatCard("Occupied Rooms", "42", "Out of 60 total rooms"),
            createStatCard("Available Rooms", "18", "Ready for booking"),
            createStatCard("Revenue Today", "$8,450", "+12% from yesterday"),
            createStatCard("Current Guests", "87", "Checked in today: 12")
        );
        stats.setWidthFull();
        stats.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        stats.setSpacing(true);

        // Aktions-Buttons
        HorizontalLayout actions = new HorizontalLayout(
            createActionButton(VaadinIcon.PLUS, "New Booking", true), // Orange Button
            createActionButton(VaadinIcon.HOME, "Manage Rooms", false),
            createActionButton(VaadinIcon.FILE_TEXT, "Generate Report", false)
        );
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        actions.addClassName("actions-row");
        
        // Aktuelle Buchungen (Grid)
        VerticalLayout recentBookings = new VerticalLayout();
        recentBookings.add(new H3("Recent Bookings"));
        recentBookings.add(createBookingsGrid());
        recentBookings.setWidthFull();
        recentBookings.addClassName("recent-bookings-panel");

        content.add(stats, actions, recentBookings);
        return content;
    }
    //stat - card erstellen
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

    // Hilfsmethode zum Erstellen des Grids
    private Grid<Booking> createBookingsGrid() {
        Grid<Booking> grid = new Grid<>(Booking.class, false);
        grid.addClassName("bookings-grid"); // Für spezifisches Grid-Styling

        // Datenquelle
        List<Booking> data = List.of(
            new Booking("BK001", "John Smith", "201 - Deluxe", LocalDate.of(2025, 11, 2), LocalDate.of(2025, 11, 5), "confirmed"),
            new Booking("BK002", "Sarah Johnson", "105 - Suite", LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 3), "checked-in"),
            new Booking("BK003", "Michael Brown", "302 - Standard", LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 7), "confirmed"),
            new Booking("BK004", "Emily Davis", "210 - Deluxe", LocalDate.of(2025, 10, 30), LocalDate.of(2025, 11, 1), "completed"),
            new Booking("BK005", "David Wilson", "150 - Suite", LocalDate.of(2025, 11, 4), LocalDate.of(2025, 11, 8), "confirmed")
        );
        grid.setItems(data);

        // Spalten definieren
        grid.addColumn(Booking::getBookingId).setHeader("Booking ID");
        grid.addColumn(Booking::getGuestName).setHeader("Guest Name");
        grid.addColumn(Booking::getRoom).setHeader("Room");
        grid.addColumn(Booking::getCheckIn).setHeader("Check-in");
        grid.addColumn(Booking::getCheckOut).setHeader("Check-out");

        // Status-Spalte mit individuellem Rendering
        grid.addComponentColumn(booking -> {
            Span statusBadge = new Span(booking.getStatus());
            statusBadge.addClassName("status-badge");
            // Fügt die Klasse basierend auf dem Status hinzu (z.B. "confirmed", "checkedin")
            statusBadge.addClassName(booking.getStatus().toLowerCase().replace("-", ""));
            return statusBadge;
        }).setHeader("Status");

        return grid;
    }
}