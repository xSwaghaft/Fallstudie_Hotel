package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.InvoiceService;
import com.hotel.booking.service.RoomService;
import com.hotel.booking.view.components.CardFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard view displaying KPIs, recent bookings, and actions for hotel operations.
 * Adjusts content depending on the user's role (Manager or Receptionist).
 * 
 * Author: Matthias Lohr
 */
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/dashboard.css")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final InvoiceService invoiceService;
    private final BookingFormService formService;

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Grid<Booking> grid = new Grid<>(Booking.class, false);

    public DashboardView(SessionService sessionService, RoomService roomService, BookingService bookingService, InvoiceService invoiceService, BookingFormService formService) {
        this.sessionService = sessionService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.invoiceService = invoiceService;
        this.formService = formService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        UserRole role = sessionService.getCurrentRole();

        add(createHeader(role));
        add(createKpiRow(role));
        add(createRecentBookingsCard());
    }

    /**
     * Creates the header section of the dashboard with title, subtitle, and action buttons
     * depending on user role.
     * @param role Current user role
     * @return Header component
     */
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

        headerRight.setSpacing(true); // space between buttons
        headerRight.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(headerLeft, headerRight);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); // distribute horizontally
        header.setAlignItems(FlexComponent.Alignment.CENTER); // align vertically

        return header;
    }

    /**
     * Opens a dialog to create or edit a booking.
     * @param existingBooking Booking to edit (null for new booking)
     */
    private void openAddBookingDialog(Booking existingBooking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingBooking != null ? "Edit Booking" : "New Booking");
        dialog.setWidth("600px");

        createNewBookingForm form = new createNewBookingForm(sessionService.getCurrentUser(),
                sessionService, existingBooking, formService);

        Button saveButton = new Button("Save", e -> {
            try {
                form.writeBean(); // transfer form data to booking object
                bookingService.save(form.getBooking()); // save booking to database
                dialog.close();
                Notification.show("Booking saved successfully.", 3000, Notification.Position.BOTTOM_START);
                grid.setItems(bookingService.getRecentBookings());
            } catch (ValidationException ex) {
                Notification.show("Please fix validation errors before saving.", 3000, Notification.Position.MIDDLE);
            }
        });
        saveButton.addClassName("primary-button");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(form, buttonLayout);
        dialog.open();
    }

    /**
     * Creates a horizontal row of KPI cards depending on the user role.
     * @param role Current user role
     * @return HorizontalLayout with KPI cards
     */
    private Component createKpiRow(UserRole role) {
        int currentGuests = bookingService.getNumberOfGuestsPresent();
        int checkoutsToday = bookingService.getNumberOfCheckoutsToday();
        int checkinsToday = bookingService.getNumberOfCheckinsToday();
        int occupiedRooms = bookingService.getActiveBookings(LocalDate.now(), LocalDate.now()).size();
        int availableRooms = roomService.getAllRooms().size() - occupiedRooms;
        BigDecimal revenueToday = bookingService.getRevenueToday();
        int pendingInvoices = invoiceService.getNumberOfPendingInvoices();
        // Verwende HorizontalLayout statt FlexLayout für gleichmäßige Verteilung
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        if (role == UserRole.RECEPTIONIST) {
            Div card1 = CardFactory.createStatCard("Check-ins Today", String.valueOf(checkinsToday), VaadinIcon.USERS);
            Div card2 = CardFactory.createStatCard("Check-outs Today", String.valueOf(checkoutsToday), VaadinIcon.USERS);
            Div card3 = CardFactory.createStatCard("Occupied Rooms", String.valueOf(occupiedRooms), VaadinIcon.BED);
            Div card4 = CardFactory.createStatCard("Pending Invoices", String.valueOf(pendingInvoices), VaadinIcon.FILE_TEXT);
            row.add(card1, card2, card3, card4);
            row.expand(card1, card2, card3, card4);
        } else if (role == UserRole.MANAGER) {
            Div card1 = CardFactory.createStatCard("Occupied Rooms", String.valueOf(occupiedRooms), VaadinIcon.BED);
            Div card2 = CardFactory.createStatCard("Available Rooms", String.valueOf(availableRooms), VaadinIcon.BED);
            Div card3 = CardFactory.createStatCard("Revenue Today", String.valueOf(revenueToday), VaadinIcon.DOLLAR);
            Div card4 = CardFactory.createStatCard("Current Guests", String.valueOf(currentGuests), VaadinIcon.USERS);

            row.add(card1, card2, card3, card4);
            row.expand(card1, card2, card3, card4);
        }

        return row;
    }

    /**
     * Creates a card displaying recent bookings with a grid and action buttons.
     * @return Div containing recent bookings
     */
    private Component createRecentBookingsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

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

        // Configure columns manually
        grid.addColumn(Booking::getBookingNumber).setHeader("Booking ID").setWidth("170px").setFlexGrow(0);
        grid.addColumn(booking -> booking.getGuest().getFullName()).setHeader("Guest Name").setFlexGrow(2);
        grid.addColumn(booking -> booking.getRoom().getRoomNumber()).setHeader("Room").setWidth("100px").setFlexGrow(0);
        grid.addColumn(booking -> booking.getCheckInDate().format(GERMAN_DATE_FORMAT))
                .setHeader("Check-in Date").setWidth("140px").setFlexGrow(0);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Status").setWidth("120px").setFlexGrow(0);
        grid.addComponentColumn(b -> {
            Button viewBtn = new Button("View");
            viewBtn.addClickListener(e -> UI.getCurrent().navigate(BookingManagementView.class));
            return viewBtn;
        }).setHeader("Actions").setWidth("100px").setFlexGrow(0);

        grid.setItems(bookingService.getRecentBookings());
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        card.add(cardHeader, grid);
        return card;
    }

    /**
     * Creates a badge representing the booking status.
     * @param booking Booking object
     * @return Span component with status styling
     */
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