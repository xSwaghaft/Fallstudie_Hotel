package com.hotel.booking.view;

import com.hotel.booking.security.SessionService;
import com.hotel.booking.security.UserRole;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "dashboard", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    record BookingRecord(String id, String guest, String room, LocalDate checkIn, String status) {}

    @Autowired
    public DashboardView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        UserRole role = sessionService.getCurrentRole();
        
        // Header
        add(createHeader(role));
        
        // KPI Row - mit gleichmäßiger Verteilung
        add(createKpiRow(role));
        
        // Recent Bookings Table
        add(createRecentBookingsCard());
    }

    private Component createHeader(UserRole role) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        
        H1 title = new H1("Dashboard");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Overview of hotel operations - " + dateStr);
        subtitle.getStyle().set("margin", "0");
        
        Div headerLeft = new Div(title, subtitle);
        
        HorizontalLayout headerRight = new HorizontalLayout();
        
        if (role == UserRole.MANAGER) {
            Button viewReports = new Button("View Reports");
            viewReports.addClickListener(e -> UI.getCurrent().navigate(ReportsView.class));
            
            Button newBooking = new Button("New Booking", VaadinIcon.PLUS.create());
            newBooking.addClassName("primary-button");
            
            headerRight.add(viewReports, newBooking);
        } else if (role == UserRole.RECEPTIONIST) {
            Button newBooking = new Button("New Booking", VaadinIcon.PLUS.create());
            newBooking.addClassName("primary-button");
            headerRight.add(newBooking);
        }
        
        headerRight.setSpacing(true);
        headerRight.setAlignItems(FlexComponent.Alignment.CENTER);
        
        HorizontalLayout header = new HorizontalLayout(headerLeft, headerRight);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    private Component createKpiRow(UserRole role) {
        // Verwende HorizontalLayout statt FlexLayout für gleichmäßige Verteilung
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        if (role == UserRole.RECEPTIONIST) {
            Div card1 = createKpiCard("Check-ins Today", "8", VaadinIcon.USERS, null);
            Div card2 = createKpiCard("Check-outs Today", "5", VaadinIcon.USERS, null);
            Div card3 = createKpiCard("Occupied Rooms", "42/60", VaadinIcon.BED, null);
            Div card4 = createKpiCard("Pending Invoices", "12", VaadinIcon.FILE_TEXT, null);
            
            row.add(card1, card2, card3, card4);
            // Alle Karten gleichmäßig expandieren
            row.expand(card1, card2, card3, card4);
        } else if (role == UserRole.MANAGER) {
            Div card1 = createKpiCard("Occupied Rooms", "42/60", VaadinIcon.BED, null);
            Div card2 = createKpiCard("Available Rooms", "18", VaadinIcon.BED, null);
            Div card3 = createKpiCard("Revenue Today", "€8.450", VaadinIcon.DOLLAR, null);
            Div card4 = createKpiCard("Current Guests", "67", VaadinIcon.USERS, null);
            
            row.add(card1, card2, card3, card4);
            row.expand(card1, card2, card3, card4);
        }

        return row;
    }

    private Div createKpiCard(String title, String value, VaadinIcon iconType, String color) {
        Div card = new Div();
        card.addClassName("kpi-card");
        
        // Header with title and icon
        Span titleSpan = new Span(title);
        titleSpan.addClassName("kpi-card-title");
        
        Icon icon = iconType.create();
        icon.addClassName("kpi-card-icon");
        if (color != null) {
            icon.getStyle().set("color", color);
        }
        
        HorizontalLayout cardHeader = new HorizontalLayout(titleSpan, icon);
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        cardHeader.getStyle().set("margin-bottom", "0.5rem");
        
        // Value
        H2 valueHeading = new H2(value);
        valueHeading.getStyle().set("margin", "0");
        
        card.add(cardHeader, valueHeading);
        return card;
    }

    private Component createRecentBookingsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull(); // WICHTIG: Card nutzt volle Breite
        
        // Header
        HorizontalLayout cardHeader = new HorizontalLayout();
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        cardHeader.getStyle().set("margin-bottom", "1rem");
        
        Div headerLeft = new Div();
        H3 title = new H3("Recent Bookings");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Latest booking activity and status");
        subtitle.getStyle().set("margin", "0");
        
        headerLeft.add(title, subtitle);
        
        Button viewAll = new Button("View All");
        viewAll.getStyle()
            .set("color", "var(--color-primary)")
            .set("background", "transparent")
            .set("border", "none")
            .set("cursor", "pointer")
            .set("font-weight", "500")
            .set("padding", "0");
        viewAll.addClickListener(e -> UI.getCurrent().navigate(BookingManagementView.class));
        
        cardHeader.add(headerLeft, viewAll);
        
        // Grid - vollständige Spalten mit optimaler Platznutzung
        Grid<BookingRecord> grid = new Grid<>(BookingRecord.class, false);
        
        grid.addColumn(BookingRecord::id)
            .setHeader("Booking ID")
            .setWidth("120px")
            .setFlexGrow(0);
        
        grid.addColumn(BookingRecord::guest)
            .setHeader("Guest Name")
            .setFlexGrow(2);
        
        grid.addColumn(BookingRecord::room)
            .setHeader("Room")
            .setFlexGrow(2);
        
        // Check-in mit deutschem Datumsformat
        grid.addColumn(booking -> booking.checkIn().format(GERMAN_DATE_FORMAT))
            .setHeader("Check-in Date")
            .setWidth("140px")
            .setFlexGrow(0);
        
        grid.addComponentColumn(this::createStatusBadge)
            .setHeader("Status")
            .setWidth("120px")
            .setFlexGrow(0);
        
        grid.addComponentColumn(b -> {
            Button viewBtn = new Button("View");
            viewBtn.addClickListener(e -> UI.getCurrent().navigate(BookingManagementView.class));
            return viewBtn;
        })
            .setHeader("Actions")
            .setWidth("100px")
            .setFlexGrow(0);
        
        grid.setItems(getMockBookings());
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        
        card.add(cardHeader, grid);
        return card;
    }

    private Component createStatusBadge(BookingRecord booking) {
        Span badge = new Span(booking.status());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + booking.status());
        return badge;
    }

    private List<BookingRecord> getMockBookings() {
        return List.of(
            new BookingRecord("BK001", "Emma Wilson", "302 - Deluxe", LocalDate.of(2025, 11, 5), "confirmed"),
            new BookingRecord("BK002", "Michael Brown", "105 - Suite", LocalDate.of(2025, 11, 8), "pending"),
            new BookingRecord("BK003", "Sarah Davis", "201 - Standard", LocalDate.of(2025, 11, 3), "checked-in"),
            new BookingRecord("BK004", "James Miller", "401 - Deluxe", LocalDate.of(2025, 11, 10), "confirmed"),
            new BookingRecord("BK005", "Lisa Anderson", "305 - Suite", LocalDate.of(2025, 11, 6), "confirmed")
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn()) {
            event.rerouteTo(LoginView.class);
        }
    }
}