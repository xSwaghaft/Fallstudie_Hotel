package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingExtraService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomService;
import com.hotel.booking.service.RoomService.RoomStatistics;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/dashboard.css")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final BookingExtraService bookingExtraService;
    private final RoomCategoryService roomCategoryService;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Grid<Booking> grid = new Grid<>(Booking.class, false);

    public DashboardView(SessionService sessionService, RoomService service, BookingService bookingService, BookingExtraService bookingExtraService, RoomCategoryService roomCategoryService) {
        this.sessionService = sessionService;
        this.roomService = service; 
        this.bookingService = bookingService;
        this.bookingExtraService = bookingExtraService;
        this.roomCategoryService = roomCategoryService;
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
        
        Paragraph subtitle = new Paragraph("Overview of hotel operations - " + dateStr);
        
        Div headerLeft = new Div(title, subtitle);
        
        HorizontalLayout headerRight = new HorizontalLayout();
        
        if (role == UserRole.MANAGER) {
            Button viewReports = new Button("View Reports");
            viewReports.addClickListener(e -> UI.getCurrent().navigate(ReportsView.class));
            
            Button newBooking = new Button("New Booking", VaadinIcon.PLUS.create());
            newBooking.addClickListener(e -> openAddBookingDialog(null));
            newBooking.addClassName("primary-button");
            
            headerRight.add(viewReports, newBooking);
        } else if (role == UserRole.RECEPTIONIST) {
            Button newBooking = new Button("New Booking", VaadinIcon.PLUS.create());
            newBooking.addClickListener(e -> openAddBookingDialog(null));
            newBooking.addClassName("primary-button");
            headerRight.add(newBooking);
        }
        
        headerRight.setSpacing(true); // Abstand zwischen den Buttons
        headerRight.setAlignItems(FlexComponent.Alignment.CENTER);
        
        HorizontalLayout header = new HorizontalLayout(headerLeft, headerRight);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); //Layout der längsachse: Verteilt die Elemente gleichmäßig von links nach rechts (argument-BETWEEN ist ein Enum)
        header.setAlignItems(FlexComponent.Alignment.CENTER); //Layout der querachse: Zentriert die Elemente vertikal
        
        return header;
    }

    //Matthias Lohr
    private void openAddBookingDialog(Booking existingBooking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingBooking != null ? "Edit Booking" : "New Booking");
        dialog.setWidth("600px");

        createNewBookingForm form = new createNewBookingForm(sessionService.getCurrentUser(), sessionService, existingBooking, bookingService, bookingExtraService, roomCategoryService);

        Button saveButton = new Button("Save", e -> {
            try {
                form.writeBean(); // Überträgt die Formulardaten in das User-Objekt
                bookingService.save(form.getBooking()); // Speichert das User-Objekt aus dem Formular in der Datenbank
                dialog.close();
                Notification.show("Booking saved successfully.", 3000, Notification.Position.BOTTOM_START);
                grid.setItems(bookingService.getRecentBookings());
            } catch (ValidationException ex) {
                Notification.show("Please fix validation errors before saving.", 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addClassName("primary-button");

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(form, buttonLayout);
        dialog.open();
    }


    private Component createKpiRow(UserRole role) {
        RoomStatistics stats = roomService.getStatistics();
        int currentGuests = bookingService.getNumberOfGuestsPresent();
        int checkoutsToday = bookingService.getNumberOfCheckoutsToday();
        int checkinsToday = bookingService.getNumberOfCheckinsToday();
        // Verwende HorizontalLayout statt FlexLayout für gleichmäßige Verteilung
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        if (role == UserRole.RECEPTIONIST) {
            Div card1 = createKpiCard("Check-ins Today", String.valueOf(checkinsToday), VaadinIcon.USERS);
            Div card2 = createKpiCard("Check-outs Today", String.valueOf(checkoutsToday), VaadinIcon.USERS);
            Div card3 = createKpiCard("Occupied Rooms", String.valueOf(stats.getOccupiedRooms()), VaadinIcon.BED);
            Div card4 = createKpiCard("Pending Invoices", "12", VaadinIcon.FILE_TEXT);
            
            row.add(card1, card2, card3, card4);
            // Alle Karten gleichmäßig expandieren
            row.expand(card1, card2, card3, card4);
        } else if (role == UserRole.MANAGER) {
            Div card1 = createKpiCard("Occupied Rooms", String.valueOf(stats.getOccupiedRooms()), VaadinIcon.BED);
            Div card2 = createKpiCard("Available Rooms", String.valueOf(stats.getAvailableRooms()), VaadinIcon.BED);
            Div card3 = createKpiCard("Revenue Today", "€8.450", VaadinIcon.DOLLAR);
            Div card4 = createKpiCard("Current Guests", String.valueOf(currentGuests), VaadinIcon.USERS);
            
            row.add(card1, card2, card3, card4);
            row.expand(card1, card2, card3, card4);
        }

        return row;
    }

    private Div createKpiCard(String title, String value, VaadinIcon iconType) {
        Div card = new Div();
        card.addClassName("kpi-card");
        
        // Header with title and icon
        Span titleSpan = new Span(title);
        titleSpan.addClassName("kpi-card-title");
        
        Icon icon = iconType.create();
        icon.addClassName("kpi-card-icon");
        
        HorizontalLayout cardHeader = new HorizontalLayout(titleSpan, icon);
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        cardHeader.addClassName("kpi-card-header");
        
        // Value
        H2 valueHeading = new H2(value);
        
        card.add(cardHeader, valueHeading);
        return card;
    }

    private Component createRecentBookingsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull(); // Card nutzt volle Breite
        
        // Header
        HorizontalLayout cardHeader = new HorizontalLayout();
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        cardHeader.addClassName("recent-bookings-header");
        
        Div headerLeft = new Div();
        H3 title = new H3("Recent Bookings");
        
        Paragraph subtitle = new Paragraph("Latest booking activity and status");
        
        headerLeft.add(title, subtitle);
        
        Button viewAll = new Button("View All");
        viewAll.addClassName("view-all-button");
        viewAll.addClickListener(e -> UI.getCurrent().navigate(BookingManagementView.class));
        
        cardHeader.add(headerLeft, viewAll);
        
        //Spalten manuell definieren, da true zu viele Spalten anzeigt (die Abhängigkeit wäre auch da, weil neue Spalten gelöscht werden müssen)
        //Matthias Lohr
        
        grid.addColumn(Booking::getBookingNumber)
            .setHeader("Booking ID")
            .setWidth("170px")
            .setFlexGrow(0);
        
        grid.addColumn(booking -> booking.getGuest().getFullName())
            .setHeader("Guest Name")
            .setFlexGrow(2);
        
        grid.addColumn(booking -> booking.getRoom().getRoomNumber())
            .setHeader("Room")
            .setWidth("100px")
            .setFlexGrow(0);
        
        // Check-in mit deutschem Datumsformat
        grid.addColumn(booking -> booking.getCheckInDate().format(GERMAN_DATE_FORMAT))
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

        grid.setItems(bookingService.getRecentBookings());
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        
        card.add(cardHeader, grid);
        return card;
    }

    //Matthias Lohr
    private Component createStatusBadge(Booking booking) {
        Span badge = new Span(booking.getStatus().name());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + booking.getStatus().toString().toLowerCase());
        return badge;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn()) {
            event.rerouteTo(LoginView.class);
        }
    }
}