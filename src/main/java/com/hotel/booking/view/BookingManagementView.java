package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingService;
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
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Route(value = "bookings", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class BookingManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService formService;

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    Grid<Booking> grid = new Grid<>(Booking.class, false);

    public BookingManagementView(SessionService sessionService, BookingService bookingService, BookingFormService formService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.formService = formService;

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
        newBooking.addClickListener(e -> openAddBookingDialog(null));
        
        HorizontalLayout header = new HorizontalLayout(headerLeft, newBooking);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    //Möglicherweise nach Bearbeitung Grid aktualisieren
    //Matthias Lohr
    private void openAddBookingDialog(Booking existingBooking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingBooking != null ? "Edit Booking" : "New Booking");
        dialog.setWidth("600px");

        createNewBookingForm form = new createNewBookingForm(sessionService.getCurrentUser(), sessionService, existingBooking, formService);

        Button saveButton = new Button("Save", e -> {
            try {
                form.writeBean(); // Überträgt die Formulardaten in das User-Objekt
                bookingService.save(form.getBooking()); // Speichert das User-Objekt aus dem Formular in der Datenbank
                dialog.close();
                grid.setItems(bookingService.findAll());
                Notification.show("Booking saved successfully.", 3000, Notification.Position.BOTTOM_START);
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

    //Matthias Lohr
    private Component createBookingsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("All Bookings");
        title.getStyle().set("margin", "0 0 1rem 0");
        
        grid.addColumn(Booking::getBookingNumber)
            .setHeader("Booking ID")
            .setWidth("130px")
            .setFlexGrow(0);
        
        grid.addColumn(Booking::getAmount)
            .setHeader("People")
            .setWidth("20px")
            .setFlexGrow(2);
        
        grid.addColumn(booking -> booking.getRoom().getRoomNumber())
            .setHeader("Room")
            .setFlexGrow(2);

        grid.addColumn(booking -> booking.getGuest().getFullName())
            .setHeader("Guest Name")
            .setFlexGrow(1);
        
        // Check-in mit deutschem Datumsformat
        grid.addColumn(booking -> booking.getCheckInDate().format(GERMAN_DATE_FORMAT))
            .setHeader("Check-in Date")
            .setWidth("140px")
            .setFlexGrow(0);
        
        // grid.addColumn(Booking::id)
        //     .setHeader("ID")
        //     .setAutoWidth(true)
        //     .setFlexGrow(0);
        
        // Check-out mit deutschem Datumsformat
        grid.addColumn(booking -> booking.getCheckOutDate().format(GERMAN_DATE_FORMAT))
            .setHeader("Check-out")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        // // Amount in Euro
        // grid.addColumn(booking -> "€" + booking.amount())
        //     .setHeader("Amount")
        //     .setAutoWidth(true)
        //     .setFlexGrow(0);
        
        grid.addComponentColumn(this::createStatusBadge)
            .setHeader("Status")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        // grid.addComponentColumn(this::createPaymentBadge)
        //     .setHeader("Payment")
        //     .setAutoWidth(true)
        //     .setFlexGrow(0);
        
        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.setItems(bookingService.findAll());
        // grid.setAllRowsVisible(true);
        grid.setWidthFull();

        card.add(title, grid);
        return card;
    }

    private Component createStatusBadge(Booking booking) {
        Span badge = new Span(booking.getStatus().name());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + booking.getStatus().toString().toLowerCase());
        return badge;
    }

    // private Component createPaymentBadge(Booking booking) {
    //     Span badge = new Span(booking.paymentStatus());
    //     badge.addClassName("status-badge");
    //     badge.addClassName("status-" + booking.paymentStatus());
    //     return badge;
    // }

    private Component createActionButtons(Booking booking) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button viewBtn = new Button("View", VaadinIcon.EYE.create());
        viewBtn.addClickListener(e -> openDetails(booking));
        
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> openAddBookingDialog(booking));
        
        actions.add(viewBtn, editBtn);
        
        if ("confirmed".equals(booking.getStatus().name())) {
            Button checkInBtn = new Button("Check In", VaadinIcon.SIGN_IN.create());
            checkInBtn.addClickListener(e -> Notification.show("Checked in " + booking.getId()));
            actions.add(checkInBtn);
        }
        
        return actions;
    }

    private void openDetails(Booking b) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Booking Details - " + b.getId());
        d.setWidth("800px");

        Tabs tabs = new Tabs(new Tab("Details"), new Tab("Payments"), new Tab("History"), new Tab("Extras"));
        
        Div details = new Div();
        details.add(new Paragraph("Guest Name: " + b.getGuest()));
        // details.add(new Paragraph("Email: " + b.getUser().getEmail()));
        // details.add(new Paragraph("Phone: " + b.getPhone()));
        // details.add(new Paragraph("Room: " + b.getRoom() + " - " + b.getRoomType()));
        // details.add(new Paragraph("Check-in: " + b.getCheckIn().format(GERMAN_DATE_FORMAT)));
        // details.add(new Paragraph("Check-out: " + b.getCheckOut().format(GERMAN_DATE_FORMAT)));
        // details.add(new Paragraph("Total: €" + b.getAmount()));

        // Div payments = new Div(new Paragraph("Initial Payment - 28.10.2025 - €" + b.getAmount()),
        //         new Paragraph("Status: " + b.getPaymentStatus()));

        Div history = new Div(new Paragraph("Booking confirmed - 28.10.2025 10:30"),
                new Paragraph("Booking created - 28.10.2025 10:25"));

        Div extras = new Div(new Paragraph("No additional services requested"));

        // Div pages = new Div(details, payments, history, extras);
        // pages.getStyle().set("minHeight", "200px");
        // payments.setVisible(false); 
        // history.setVisible(false); 
        // extras.setVisible(false);

        tabs.addSelectedChangeListener(ev -> {
            details.setVisible(tabs.getSelectedIndex() == 0);
        //     payments.setVisible(tabs.getSelectedIndex() == 1);
        //     history.setVisible(tabs.getSelectedIndex() == 2);
            extras.setVisible(tabs.getSelectedIndex() == 3);
        });

        Button checkIn = new Button("Check In", e -> { d.close(); Notification.show("Checked in"); });
        Button edit = new Button("Edit Booking", e -> Notification.show("Edit not implemented"));
        Button cancel = new Button("Cancel", e -> d.close());

        // d.add(new VerticalLayout(tabs, pages));
        // d.getFooter().add(new HorizontalLayout(checkIn, edit, cancel));
        // d.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}