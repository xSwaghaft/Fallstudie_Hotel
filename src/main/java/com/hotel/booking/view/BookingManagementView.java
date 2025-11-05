package com.hotel.booking.view;

import com.hotel.booking.security.SessionService;
import com.hotel.booking.security.UserRole;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "bookings", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class BookingManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public record Booking(
            String id, String guest, String email, String phone,
            String room, String roomType,
            LocalDate checkIn, LocalDate checkOut,
            int nights, int guests, int amount,
            String status, String paymentStatus, String createdAt) {}

    private Grid<Booking> grid;

    @Autowired
    public BookingManagementView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createFilters(), createBookingsCard());
    }

    private Component createHeader() {
        H1 title = new H1("Booking Management");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Manage all hotel bookings and reservations");
        subtitle.getStyle().set("margin", "0");
        
        Div headerLeft = new Div(title, subtitle);
        
        Button newBooking = new Button("New Booking", VaadinIcon.PLUS.create());
        newBooking.addClassName("primary-button");
        newBooking.addClickListener(e -> Notification.show("New booking wizard"));
        
        HorizontalLayout header = new HorizontalLayout(headerLeft, newBooking);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    private Component createFilters() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull(); // WICHTIG: Card nutzt volle Breite
        
        H3 title = new H3("Search & Filter");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Find specific bookings quickly");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        TextField search = new TextField("Search");
        search.setPlaceholder("Booking ID, Guest name...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());

        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems("All Status", "confirmed", "pending", "checked-in", "checked-out", "cancelled");
        status.setValue("All Status");

        DatePicker date = new DatePicker("Date");
        date.setValue(LocalDate.of(2025, 11, 1));

        Select<String> roomType = new Select<>();
        roomType.setLabel("Room Type");
        roomType.setItems("All Rooms", "Standard", "Deluxe", "Suite");
        roomType.setValue("All Rooms");

        FormLayout form = new FormLayout(search, status, date, roomType);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 4)
        );

        card.add(title, subtitle, form);
        return card;
    }

    private Component createBookingsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("All Bookings");
        title.getStyle().set("margin", "0 0 1rem 0");

        grid = new Grid<>(Booking.class, false);
        
        grid.addColumn(Booking::id)
            .setHeader("ID")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        grid.addColumn(Booking::guest)
            .setHeader("Guest")
            .setFlexGrow(1);
        
        grid.addColumn(Booking::room)
            .setHeader("Room")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        // Check-in mit deutschem Datumsformat
        grid.addColumn(booking -> booking.checkIn().format(GERMAN_DATE_FORMAT))
            .setHeader("Check-in")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        // Check-out mit deutschem Datumsformat
        grid.addColumn(booking -> booking.checkOut().format(GERMAN_DATE_FORMAT))
            .setHeader("Check-out")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        // Amount in Euro
        grid.addColumn(booking -> "€" + booking.amount())
            .setHeader("Amount")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        grid.addComponentColumn(this::createStatusBadge)
            .setHeader("Status")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        grid.addComponentColumn(this::createPaymentBadge)
            .setHeader("Payment")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.setItems(mockBookings());
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        card.add(title, grid);
        return card;
    }

    private Component createStatusBadge(Booking booking) {
        Span badge = new Span(booking.status());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + booking.status());
        return badge;
    }

    private Component createPaymentBadge(Booking booking) {
        Span badge = new Span(booking.paymentStatus());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + booking.paymentStatus());
        return badge;
    }

    private Component createActionButtons(Booking booking) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button viewBtn = new Button("View", VaadinIcon.EYE.create());
        viewBtn.addClickListener(e -> openDetails(booking));
        
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> Notification.show("Edit " + booking.id()));
        
        actions.add(viewBtn, editBtn);
        
        if ("confirmed".equals(booking.status())) {
            Button checkInBtn = new Button("Check In", VaadinIcon.SIGN_IN.create());
            checkInBtn.addClickListener(e -> Notification.show("Checked in " + booking.id()));
            actions.add(checkInBtn);
        }
        
        return actions;
    }

    private List<Booking> mockBookings() {
        return List.of(
                new Booking("BK001","Emma Wilson","emma.w@email.com","+1 555-0101",
                        "302","Deluxe",
                        LocalDate.of(2025, 11, 5), LocalDate.of(2025, 11, 8),
                        3, 2, 447, "confirmed", "paid", "2025-10-28"),
                new Booking("BK002","Michael Brown","michael.b@email.com","+1 555-0102",
                        "105","Suite",
                        LocalDate.of(2025, 11, 8), LocalDate.of(2025, 11, 12),
                        4, 3, 1196, "pending", "pending", "2025-10-30"),
                new Booking("BK003","Sarah Davis","sarah.d@email.com","+1 555-0103",
                        "201","Standard",
                        LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 6),
                        3, 1, 267, "checked-in", "paid", "2025-10-25"),
                new Booking("BK004","James Miller","james.m@email.com","+1 555-0104",
                        "401","Deluxe",
                        LocalDate.of(2025, 11, 10), LocalDate.of(2025, 11, 15),
                        5, 2, 745, "confirmed", "partial", "2025-11-01"),
                new Booking("BK005","Lisa Anderson","lisa.a@email.com","+1 555-0105",
                        "305","Suite",
                        LocalDate.of(2025, 11, 6), LocalDate.of(2025, 11, 9),
                        3, 4, 897, "confirmed", "paid", "2025-10-29")
        );
    }

    private void openDetails(Booking b) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Booking Details - " + b.id());
        d.setWidth("800px");

        Tabs tabs = new Tabs(new Tab("Details"), new Tab("Payments"), new Tab("History"), new Tab("Extras"));
        
        Div details = new Div();
        details.add(new Paragraph("Guest Name: " + b.guest()));
        details.add(new Paragraph("Email: " + b.email()));
        details.add(new Paragraph("Phone: " + b.phone()));
        details.add(new Paragraph("Room: " + b.room() + " - " + b.roomType()));
        details.add(new Paragraph("Check-in: " + b.checkIn().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Check-out: " + b.checkOut().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Total: €" + b.amount()));

        Div payments = new Div(new Paragraph("Initial Payment - 28.10.2025 - €" + b.amount()),
                new Paragraph("Status: " + b.paymentStatus()));

        Div history = new Div(new Paragraph("Booking confirmed - 28.10.2025 10:30"),
                new Paragraph("Booking created - 28.10.2025 10:25"));

        Div extras = new Div(new Paragraph("No additional services requested"));

        Div pages = new Div(details, payments, history, extras);
        pages.getStyle().set("minHeight", "200px");
        payments.setVisible(false); 
        history.setVisible(false); 
        extras.setVisible(false);

        tabs.addSelectedChangeListener(ev -> {
            details.setVisible(tabs.getSelectedIndex() == 0);
            payments.setVisible(tabs.getSelectedIndex() == 1);
            history.setVisible(tabs.getSelectedIndex() == 2);
            extras.setVisible(tabs.getSelectedIndex() == 3);
        });

        Button checkIn = new Button("Check In", e -> { d.close(); Notification.show("Checked in"); });
        Button edit = new Button("Edit Booking", e -> Notification.show("Edit not implemented"));
        Button cancel = new Button("Cancel", e -> d.close());

        d.add(new VerticalLayout(tabs, pages));
        d.getFooter().add(new HorizontalLayout(checkIn, edit, cancel));
        d.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}