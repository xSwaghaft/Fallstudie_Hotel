package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.RoomCategoryService;
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
import java.util.ArrayList;
import java.util.List;

//Matthias Lohr
@Route(value = "bookings", layout = MainLayout.class)
@PageTitle("Booking Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/booking-management.css")
public class BookingManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService formService;

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Grid<Booking> grid = new Grid<>(Booking.class, false);
    private final List<Booking> bookings = new ArrayList<>();
    private TextField searchField;
    private Select<String> statusFilter;
    private DatePicker dateFilter;
    private Select<String> categoryFilter;
    private List<String> categoryNames;
    private final String ALL_STATUS = "All Status";

    public BookingManagementView(SessionService sessionService, BookingService bookingService, RoomCategoryService roomCategoryService, BookingFormService formService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.formService = formService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        bookings.addAll(bookingService.findAll());

        categoryNames = new ArrayList<>();
        categoryNames.add("All Rooms");
        roomCategoryService.getAllRoomCategories().forEach(cat -> categoryNames.add(cat.getName()));

        add(createHeader(), createFilters(), createBookingsCard());
    }

    private Component createHeader() {
        H1 title = new H1("Booking Management");
        
        Paragraph subtitle = new Paragraph("Manage all hotel bookings and reservations");
        subtitle.addClassName("booking-subtitle");
        
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
        card.setWidthFull();
        H3 title = new H3("Search & Filter");
        title.addClassName("booking-section-title");
        Paragraph subtitle = new Paragraph("Find specific bookings quickly");
        subtitle.addClassName("booking-subtitle");

        searchField = new TextField("Search");
        searchField.setPlaceholder("Booking ID, Guest name...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(e -> filterBookings());

        statusFilter = new Select<>();
        statusFilter.setLabel("Status");
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add(ALL_STATUS);
        for (BookingStatus s : BookingStatus.values()) {
            statusOptions.add(s.name());
        }
        statusFilter.setItems(statusOptions);
        statusFilter.setValue(ALL_STATUS);
        statusFilter.addValueChangeListener(e -> filterBookings());

        dateFilter = new DatePicker("Date");
        dateFilter.setClearButtonVisible(true);
        dateFilter.addValueChangeListener(e -> filterBookings());

        categoryFilter = new Select<>();
        categoryFilter.setLabel("Category");
        categoryFilter.setItems(categoryNames);
        categoryFilter.setValue("All Rooms");
        categoryFilter.addValueChangeListener(e -> filterBookings());

        FormLayout form = new FormLayout(searchField, statusFilter, dateFilter, categoryFilter);
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
        title.addClassName("booking-section-title");
        grid.removeAllColumns();
        grid.addColumn(Booking::getBookingNumber)
            .setHeader("Booking ID")
            .setWidth("130px")
            .setFlexGrow(0);
        grid.addColumn(Booking::getAmount)
            .setHeader("People")
            .setAutoWidth(true)
            .setFlexGrow(2);
        grid.addColumn(booking -> booking.getRoom().getRoomNumber())
            .setHeader("Room")
            .setAutoWidth(true)
            .setFlexGrow(2);
        grid.addColumn(booking -> booking.getGuest().getFullName())
            .setHeader("Guest Name")
            .setAutoWidth(true)
            .setFlexGrow(1);
        grid.addColumn(booking -> booking.getCheckInDate().format(GERMAN_DATE_FORMAT))
            .setHeader("Check-in Date")
            .setWidth("140px")
            .setFlexGrow(0);
        grid.addColumn(booking -> booking.getCheckOutDate().format(GERMAN_DATE_FORMAT))
            .setHeader("Check-out")
            .setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(booking -> "€" + booking.getTotalPrice())
            .setHeader("Amount")
            .setAutoWidth(true)
            .setFlexGrow(0);
        grid.addComponentColumn(this::createStatusBadge)
            .setHeader("Status")
            .setAutoWidth(true)
            .setFlexGrow(0);
        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);
        grid.setItems(bookings);
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
        
        return actions;
    }

    private void openDetails(Booking b) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Booking Details - " + b.getBookingNumber());
        d.setWidth("800px");

        Tabs tabs = new Tabs(new Tab("Details"), new Tab("Payments"), new Tab("History"), new Tab("Extras"));
        
        Div details = new Div();
        details.add(new Paragraph("Guest Name: " + (b.getGuest() != null ? b.getGuest().getFullName() : "N/A")));
        details.add(new Paragraph("Booking Number: " + b.getBookingNumber()));
        details.add(new Paragraph("Total Price: €" + b.getTotalPrice()));
        details.add(new Paragraph("Check-in: " + b.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Check-out: " + b.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Guests: " + b.getAmount()));
        details.add(new Paragraph("Status: " + b.getStatus()));

        Div payments = new Div(new Paragraph("Payment information not available"));

        Div history = new Div(new Paragraph("Booking confirmed - 28.10.2025 10:30"),
                new Paragraph("Booking created - 28.10.2025 10:25"));

        Div extras = new Div(new Paragraph(b.getExtras().isEmpty() ? "No additional services requested" : b.getExtras().size() + " services added"));

        Div pages = new Div(details, payments, history, extras);
        pages.addClassName("booking-details-container");
        payments.setVisible(false); 
        history.setVisible(false); 
        extras.setVisible(false);

        tabs.addSelectedChangeListener(ev -> {
            details.setVisible(tabs.getSelectedIndex() == 0);
            payments.setVisible(tabs.getSelectedIndex() == 1);
            history.setVisible(tabs.getSelectedIndex() == 2);
            extras.setVisible(tabs.getSelectedIndex() == 3);
        });

        Button edit = new Button("Edit Booking", e -> { d.close(); openAddBookingDialog(b); });
        Button cancel = new Button("Cancel", e -> d.close());

        d.add(new VerticalLayout(tabs, pages));
        d.getFooter().add(new HorizontalLayout(edit, cancel));
        d.open();
    }

    // Filtert die Buchungen nach den gesetzten Filtern
    private void filterBookings() {
        String search = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        String selectedStatus = statusFilter.getValue();
        LocalDate date = dateFilter.getValue();
        String selectedCategory = categoryFilter.getValue();
        List<Booking> filtered = bookings.stream()
            .filter(b -> {
                boolean matchesSearch = search.isEmpty()
                    || (b.getBookingNumber() != null && b.getBookingNumber().toLowerCase().contains(search))
                    || (b.getGuest() != null && b.getGuest().getFullName().toLowerCase().contains(search));

                boolean matchesStatus = ALL_STATUS.equals(selectedStatus)
                    || (b.getStatus() != null && b.getStatus().name().equals(selectedStatus));

                boolean matchesDate = date == null
                    || (b.getCreatedAt() != null && b.getCreatedAt().isAfter(date));

                boolean matchesCategory = "All Rooms".equals(selectedCategory)
                    || (b.getRoomCategory() != null && b.getRoomCategory().getName().equalsIgnoreCase(selectedCategory));

                return matchesSearch && matchesStatus && matchesDate && matchesCategory;
            })
            .toList();
        grid.setItems(filtered);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}