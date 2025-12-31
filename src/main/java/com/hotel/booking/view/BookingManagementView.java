package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.RoomStatus;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.entity.User;
import com.hotel.booking.service.BookingCancellationService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingModificationService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import jakarta.annotation.security.RolesAllowed;

/**
 * Main view for managing hotel bookings.
 * <p>
 * Provides search, filtering, creation, modification, cancellation,
 * check-in/check-out handling and detailed inspection of bookings.
 */
@Route(value = "bookings", layout = MainLayout.class)
@PageTitle("Booking Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/booking-management.css")
@RolesAllowed({UserRole.RECEPTIONIST_VALUE, UserRole.MANAGER_VALUE})
public class BookingManagementView extends VerticalLayout {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService formService;
    private final RoomService roomService;
    private final BookingModificationService modificationService;
    private final BookingCancellationService bookingCancellationService;

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final LocalDate today = LocalDate.now();

    private final Grid<Booking> checkgrid = new Grid<>(Booking.class, false);
    private final Grid<Booking> grid = new Grid<>(Booking.class, false);
    private final List<Booking> bookings = new ArrayList<>();
    private TextField searchField;
    private Select<String> statusFilter;
    private DatePicker dateFilter;
    private Select<String> categoryFilter;
    private List<String> categoryNames;
    private final String ALL_STATUS = "All Status";

    //Matthias Lohr
    public BookingManagementView(SessionService sessionService, BookingService bookingService, BookingFormService formService, BookingModificationService modificationService, RoomCategoryService roomCategoryService, BookingCancellationService bookingCancellationService, RoomService roomService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.formService = formService;
        this.modificationService = modificationService;
        this.bookingCancellationService = bookingCancellationService;
        this.roomService = roomService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        bookings.addAll(bookingService.findAll());

        categoryNames = new ArrayList<>();
        categoryNames.add("All Rooms");
        roomCategoryService.getAllRoomCategories().forEach(cat -> categoryNames.add(cat.getName()));

        add(createHeader());

        Component checkInOutGrid = createCheckInOutCard();
        if(checkInOutGrid != null) {
            add(checkInOutGrid);
        }
        add(createFilters(), createBookingsCard());
    }

    //Matthias Lohr
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
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        
        return header;
    }

    /**
     * Opens the booking form dialog for creating a new booking or editing an existing one.
     * <p>
     * For existing bookings:
     * <ul>
     *   <li>Paid bookings cannot be edited.</li>
     *   <li>Original values are captured before modification.</li>
     *   <li>A preview dialog shows a before/after comparison.</li>
     *   <li>Changes are persisted and logged via {@link BookingModificationService}.</li>
     * </ul>
     *
     * @param existingBooking booking to edit, or {@code null} to create a new booking
     */
    private void openAddBookingDialog(Booking existingBooking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingBooking != null ? "Edit Booking" : "New Booking");
        dialog.setWidth("600px");

        // Wenn vorhandene Buchung bezahlt ist, keine Änderungen erlauben
        if (existingBooking != null && existingBooking.getInvoice() != null
                && existingBooking.getInvoice().getInvoiceStatus() == Invoice.PaymentStatus.PAID) {
            Notification.show("Änderung nicht möglich: Buchung bereits bezahlt.", 4000, Notification.Position.MIDDLE);
            return;
        }

        createNewBookingForm form = new createNewBookingForm(sessionService.getCurrentUser(), sessionService, existingBooking, formService);

        Button saveButton = new Button("Save", e -> {
            try {
                final AtomicReference<LocalDate> prevCheckInRef = new AtomicReference<>();
                final AtomicReference<LocalDate> prevCheckOutRef = new AtomicReference<>();
                final AtomicReference<Integer> prevAmountRef = new AtomicReference<>();
                final AtomicReference<BigDecimal> prevTotalRef = new AtomicReference<>();
                final AtomicReference<Set<BookingExtra>> prevExtrasRef = new AtomicReference<>();
                if (existingBooking != null) {
                    prevCheckInRef.set(existingBooking.getCheckInDate());
                    prevCheckOutRef.set(existingBooking.getCheckOutDate());
                    prevAmountRef.set(existingBooking.getAmount());
                    prevTotalRef.set(existingBooking.getTotalPrice());
                    prevExtrasRef.set(existingBooking.getExtras());
                }

                form.writeBean(); 
                Booking updated = form.getBooking();

                bookingService.calculateBookingPrice(updated);

                Dialog preview = new Dialog();
                preview.setHeaderTitle(existingBooking != null ? "Confirm Booking Changes" : "Confirm New Booking");
                VerticalLayout content = new VerticalLayout();
                if (existingBooking != null) {
                    content.add(new Paragraph("-- Before --"));
                    LocalDate prevCheckIn = prevCheckInRef.get();
                    LocalDate prevCheckOut = prevCheckOutRef.get();
                    Integer prevAmount = prevAmountRef.get();
                    BigDecimal prevTotal = prevTotalRef.get();
                    Set<BookingExtra> prevExtras = prevExtrasRef.get();
                    content.add(new Paragraph("Check-in: " + (prevCheckIn != null ? prevCheckIn.format(GERMAN_DATE_FORMAT) : "N/A")));
                    content.add(new Paragraph("Check-out: " + (prevCheckOut != null ? prevCheckOut.format(GERMAN_DATE_FORMAT) : "N/A")));
                    content.add(new Paragraph("Guests: " + (prevAmount != null ? prevAmount : "N/A")));
                    content.add(new Paragraph("Total Price: " + (prevTotal != null ? prevTotal.toString() : "N/A")));
                    String prevExtrasStr = "none";
                    if (prevExtras != null && !prevExtras.isEmpty()) {
                        prevExtrasStr = prevExtras.stream().map(x -> x.getName()).collect(Collectors.joining(", "));
                    }
                    content.add(new Paragraph("Extras: " + prevExtrasStr));
                }

                content.add(new Paragraph("-- After --"));
                content.add(new Paragraph("Check-in: " + (updated.getCheckInDate() != null ? updated.getCheckInDate().format(GERMAN_DATE_FORMAT) : "N/A")));
                content.add(new Paragraph("Check-out: " + (updated.getCheckOutDate() != null ? updated.getCheckOutDate().format(GERMAN_DATE_FORMAT) : "N/A")));
                content.add(new Paragraph("Guests: " + (updated.getAmount() != null ? updated.getAmount() : "N/A")));
                content.add(new Paragraph("Total Price: " + (updated.getTotalPrice() != null ? updated.getTotalPrice().toString() : "N/A")));
                String newExtrasStr = "none";
                if (updated.getExtras() != null && !updated.getExtras().isEmpty()) {
                    newExtrasStr = updated.getExtras().stream().map(x -> x.getName()).collect(Collectors.joining(", "));
                }
                content.add(new Paragraph("Extras: " + newExtrasStr));

                Button confirm = new Button("Confirm", ev -> {
                    try {
                        if (existingBooking != null) {
                                modificationService.recordChangesFromSnapshot(existingBooking,
                                    prevCheckInRef.get(), prevCheckOutRef.get(), prevAmountRef.get(), prevTotalRef.get(), prevExtrasRef.get(),
                                    updated, sessionService.getCurrentUser(), null);
                        }

                        bookingService.save(updated);
                        dialog.close();
                        preview.close();
                        grid.setItems(bookingService.findAll());
                        Notification.show("Booking saved successfully.", 3000, Notification.Position.BOTTOM_START);
                    } catch (Exception ex) {
                        String msg = ex.getMessage() != null ? ex.getMessage() : "Fehler beim Speichern der Buchung.";
                        Notification.show(msg, 6000, Notification.Position.MIDDLE);
                    }
                });

                Button back = new Button("Back", ev -> preview.close());
                HorizontalLayout actions = new HorizontalLayout(confirm, back);
                preview.add(content, actions);
                preview.open();

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
     * Creates the filter section used to search and narrow down bookings.
     * <p>
     * Supports filtering by:
     * <ul>
     *   <li>Booking number or guest name</li>
     *   <li>Booking status</li>
     *   <li>Creation date</li>
     *   <li>Room category</li>
     * </ul>
     *
     * @return filter UI component
     * @author Matthias Lohr
     */
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

    /**
     * Creates a compact grid displaying today's check-in and check-out actions.
     * <p>
     * The grid is only rendered if at least one actionable booking exists
     * and automatically adjusts its height to its content.
     *
     * @return the today actions card or {@code null} if no actions exist
     * @author Matthias Lohr
     */
    private Component createCheckInOutCard() {
        List<Booking> actionable = bookings.stream()
                .filter(this::isActionableToday)
                .toList();

        if (actionable.isEmpty()) {
            return null;
        }

        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        H3 title = new H3("Today: Check-in / Check-out");
        title.addClassName("booking-section-title");

        checkgrid.addColumn(Booking::getBookingNumber)
                .setHeader("Booking ID")
                .setAutoWidth(true);

        checkgrid.addColumn(b -> b.getGuest().getFullName())
                .setHeader("Guest");

        checkgrid.addColumn(b -> b.getRoom().getRoomNumber())
                .setHeader("Room");
        checkgrid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(0);
        checkgrid.addComponentColumn(this::createCheckInOutButtons)
                .setHeader("Action")
                .setAutoWidth(true);

        checkgrid.setItems(actionable);
        checkgrid.setWidthFull();
        checkgrid.setMaxHeight("250px");

        card.add(title, checkgrid);
        return card;
    }

    /**
     * Builds the bookings overview card containing the main bookings grid.
     *
     * @return bookings overview component
     * @author Matthias Lohr
     */
    private Component createBookingsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        H3 title = new H3("All Bookings");
        title.addClassName("booking-section-title");
        grid.removeAllColumns();
        grid.addColumn(Booking::getBookingNumber)
            .setHeader("Booking ID")
            .setWidth("100px")
            .setFlexGrow(1);
        grid.addColumn(Booking::getAmount)
            .setHeader("People")
            .setAutoWidth(true)
            .setFlexGrow(1);
        grid.addColumn(booking -> booking.getRoom().getRoomNumber())
            .setHeader("Room")
            .setAutoWidth(true)
            .setFlexGrow(1);
        grid.addColumn(booking -> booking.getGuest().getFullName())
            .setHeader("Guest Name")
            .setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(booking -> booking.getCheckInDate().format(GERMAN_DATE_FORMAT))
            .setHeader("Check-in Date")
            .setAutoWidth(true)
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

    /**
     * Creates a visual status badge for a booking.
     *
     * @param booking booking whose status is rendered
     * @return styled status badge component
     * @author Matthias Lohr
     */
    private Component createStatusBadge(Booking booking) {
        Span badge = new Span(booking.getStatus().name());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + booking.getStatus().toString().toLowerCase());
        return badge;
    }

    /**
     * Creates action buttons (view, edit, cancel) for a booking row.
     * <p>
     * The cancel action is only available for bookings
     * with {@code PENDING} or {@code MODIFIED} status.
     *
     * @param booking booking instance
     * @return action button layout
     * @author Matthias Lohr
     */
    private Component createActionButtons(Booking booking) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button viewBtn = new Button("View", VaadinIcon.EYE.create());
        viewBtn.addClickListener(e -> openDetails(booking));
        
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> openAddBookingDialog(booking));
        
        actions.add(viewBtn, editBtn);

        // Cancel button: visible/usable for PENDING or MODIFIED bookings
        if (booking.getStatus() != null && (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.MODIFIED)) {
            Button cancelBtn = new Button("Cancel", VaadinIcon.CLOSE.create());
            cancelBtn.addClickListener(e -> confirmAndCancelBooking(booking));
            actions.add(cancelBtn);
        }
        
        return actions;
    }

    /**
     * Creates contextual check-in / check-out buttons depending on:
     * <ul>
     *   <li>Current date</li>
     *   <li>Booking status</li>
     * </ul>
     *
     * @param booking booking instance
     * @return component containing check-in / check-out actions, or empty span
     * @author Matthias Lohr
     */
    private Component createCheckInOutButtons(Booking booking) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        boolean hasButton = false;

        if (today.equals(booking.getCheckInDate())
                && (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.MODIFIED)) {

            Button checkInBtn = new Button(
                    VaadinIcon.SIGN_IN.create(),
                    e -> {
                        booking.setStatus(BookingStatus.CHECKED_IN);
                        booking.getRoom().setStatus(RoomStatus.OCCUPIED);
                        roomService.save(booking.getRoom());
                        bookingService.save(booking);
                        grid.getDataProvider().refreshItem(booking);
                        checkgrid.getDataProvider().refreshItem(booking);
                    });
            checkInBtn.getElement().setAttribute("title", "Check In");
            layout.add(checkInBtn);
            hasButton = true;
        }

        if (today.equals(booking.getCheckOutDate())
                && booking.getStatus() == BookingStatus.CHECKED_IN) {

            Button checkOutBtn = new Button(
                    VaadinIcon.SIGN_OUT.create(),
                    e -> {
                        booking.setStatus(BookingStatus.COMPLETED);
                        booking.getRoom().setStatus(RoomStatus.AVAILABLE);
                        roomService.save(booking.getRoom());
                        bookingService.save(booking);
                        grid.getDataProvider().refreshItem(booking);
                    });
            checkOutBtn.getElement().setAttribute("title", "Check Out");
            layout.add(checkOutBtn);
            hasButton = true;
        }

        return hasButton ? layout : new Span();
    }

    /**
     * Determines whether a booking can be checked in or checked out today.
     * <p>
     * A booking is actionable if:
     * <ul>
     *   <li>Today is the check-in date and status is PENDING, CONFIRMED or MODIFIED</li>
     *   <li>Today is the check-out date and status is CHECKED_IN</li>
     * </ul>
     *
     * @param booking booking to evaluate
     * @return {@code true} if the booking can be processed today
     * @author Matthias Lohr
     */
    private boolean isActionableToday(Booking b) {
    return (today.equals(b.getCheckInDate()) && (b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.MODIFIED))
        || (today.equals(b.getCheckOutDate()) && b.getStatus() == BookingStatus.CHECKED_IN);
    }


    /**
     * Cancels a booking after user confirmation.
     * <p>
     * Business rules:
     * <ul>
     *   <li>Only {@code PENDING} or {@code MODIFIED} bookings can be cancelled</li>
     *   <li>If cancelled within 48 hours before check-in, a 50% penalty applies</li>
     *   <li>Cancellation details are persisted via {@link BookingCancellationService}</li>
     * </ul>
     *
     * @param booking booking to cancel
     */
    private void confirmAndCancelBooking(Booking b) {
        // Only allow cancellation via this action for bookings with PENDING or MODIFIED status
        if (b.getStatus() == null || (b.getStatus() != BookingStatus.PENDING && b.getStatus() != BookingStatus.MODIFIED)) {
            Notification.show("Nur Buchungen mit Status 'Pending' oder 'Modified' können hier storniert werden.", 4000, Notification.Position.MIDDLE);
            return;
        }

        try {
            // Calculate cancellation fee based on days before check-in
            java.math.BigDecimal penalty = bookingCancellationService.calculateCancellationFee(b, b.getTotalPrice());
            long daysBefore = java.time.Duration.between(java.time.LocalDateTime.now(), b.getCheckInDate().atStartOfDay()).toDays();

            String timeframe;
            if (daysBefore >= 30) {
                timeframe = "mehr als 30 Tage";
            } else if (daysBefore >= 7) {
                timeframe = "7-29 Tage";
            } else if (daysBefore >= 1) {
                timeframe = "1-6 Tage";
            } else {
                timeframe = "am Anreisetag";
            }

            Dialog confirm = new Dialog();
            confirm.setHeaderTitle("Stornierung bestätigen");
            VerticalLayout cnt = new VerticalLayout();
            cnt.add(new Paragraph("Stornierungszeitraum: " + timeframe + " vor Check-in"));
            cnt.add(new Paragraph("Stornierungsgebühr: " + String.format("%.2f €", penalty)));
            cnt.add(new Paragraph("Rückerstattung: " + String.format("%.2f €", b.getTotalPrice().subtract(penalty))));
            cnt.add(new Paragraph("Möchten Sie die Stornierung bestätigen?"));

            final java.math.BigDecimal penaltyFinal = penalty;
            Button confirmBtn = new Button("Bestätigen", ev -> {
                try {
                    BookingCancellation bc = new BookingCancellation();
                    bc.setBooking(b);
                    bc.setCancelledAt(java.time.LocalDateTime.now());
                    bc.setReason("Storniert vom Management");
                    bc.setCancellationFee(penaltyFinal);
                    java.math.BigDecimal refundedAmount = b.getTotalPrice().subtract(penaltyFinal);
                    bc.setRefundedAmount(refundedAmount);
                    User current = sessionService.getCurrentUser();
                    if (current != null) {
                        bc.setHandledBy(current);
                    }
                    
                    // Use centralized cancellation logic
                    bookingCancellationService.processCancellation(b, bc, refundedAmount);

                    confirm.close();
                    grid.setItems(bookingService.findAll());
                    Notification.show("Buchung storniert. Rückerstattung: " + String.format("%.2f €", refundedAmount) + " | Gebühr: " + String.format("%.2f €", penaltyFinal), 4000, Notification.Position.BOTTOM_START);
                } catch (Exception ex) {
                    Notification.show(ex.getMessage() != null ? ex.getMessage() : "Fehler beim Stornieren", 5000, Notification.Position.MIDDLE);
                }
            });

            Button backBtn = new Button("Zurück", ev -> confirm.close());
            confirm.add(cnt, new HorizontalLayout(confirmBtn, backBtn));
            confirm.open();
        } catch (Exception ex) {
            Notification.show(ex.getMessage() != null ? ex.getMessage() : "Fehler beim Stornieren", 5000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Opens a dialog displaying detailed booking information.
     * <p>
     * Includes:
     * <ul>
     *   <li>Booking details</li>
     *   <li>Payment placeholder</li>
     *   <li>Modification history grouped by timestamp</li>
     *   <li>Extras overview</li>
     * </ul>
     *
     * @param booking booking to inspect
     */
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
        details.add(new Paragraph("Room: " + (b.getRoom() != null ? b.getRoom().getRoomNumber() : "N/A")));
        details.add(new Paragraph("Guests: " + b.getAmount()));
        details.add(new Paragraph("Status: " + b.getStatus()));

        if (b.getStatus() == BookingStatus.CANCELLED && b.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(b.getId()).ifPresent(bc -> {
                    if (bc.getCancellationFee() != null) {
                        details.add(new Paragraph("Stornogebühr: " + String.format("%.2f €", bc.getCancellationFee())));
                    }
                    if (bc.getReason() != null && !bc.getReason().isBlank()) {
                        details.add(new Paragraph("Storno-Grund: " + bc.getReason()));
                    }
                });
            } catch (Exception ex) {
                // ignore
            }
        }

        Div payments = new Div(new Paragraph("Payment information not available"));

        Div history = new Div();
        if (b.getId() != null) {
            List<BookingModification> mods = modificationService.findByBookingId(b.getId());
            if (mods.isEmpty()) {
                history.add(new Paragraph("No modification history available."));
            } else {
                Map<LocalDateTime, List<BookingModification>> grouped =
                        mods.stream().collect(Collectors.groupingBy(BookingModification::getModifiedAt, LinkedHashMap::new, Collectors.toList()));

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

                for (Map.Entry<LocalDateTime, List<BookingModification>> entry : grouped.entrySet()) {
                    LocalDateTime ts = entry.getKey();
                    List<BookingModification> group = entry.getValue();

                    VerticalLayout groupBox = new VerticalLayout();
                    groupBox.getStyle().set("padding", "8px");
                    groupBox.getStyle().set("margin-bottom", "6px");
                    groupBox.getStyle().set("border", "1px solid #eee");

                    String who = "system";
                    for (BookingModification m : group) {
                        if (m.getHandledBy() != null) {
                            who = (m.getHandledBy().getFullName() != null && !m.getHandledBy().getFullName().isBlank()) ? m.getHandledBy().getFullName() : m.getHandledBy().getEmail();
                            break;
                        }
                    }
                    groupBox.add(new Paragraph(ts.format(dtf) + " — " + who));

                    for (BookingModification m : group) {
                        HorizontalLayout row = new HorizontalLayout();
                        row.setWidthFull();
                        Paragraph field = new Paragraph(m.getFieldChanged() + ": ");
                        field.getStyle().set("font-weight", "600");
                        Paragraph values = new Paragraph((m.getOldValue() != null ? m.getOldValue() : "<null>") + " → " + (m.getNewValue() != null ? m.getNewValue() : "<null>"));
                        values.getStyle().set("margin-left", "8px");
                        row.add(field, values);
                        groupBox.add(row);
                        if (m.getReason() != null && !m.getReason().isBlank()) {
                            Span note = new Span("Reason: " + m.getReason());
                            note.getElement().getStyle().set("font-style", "italic");
                            groupBox.add(note);
                        }
                    }

                    history.add(groupBox);
                }
            }
        } else {
            history.add(new Paragraph("No modification history available."));
        }

        if (b.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(b.getId()).ifPresent(bc -> {
                    VerticalLayout cancelBox = new VerticalLayout();
                    cancelBox.getStyle().set("padding", "8px");
                    cancelBox.getStyle().set("margin-bottom", "6px");
                    cancelBox.getStyle().set("border", "1px solid #f5c6cb");

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                    String who = "system";
                    if (bc.getHandledBy() != null) {
                        who = bc.getHandledBy().getFullName() != null && !bc.getHandledBy().getFullName().isBlank() ? bc.getHandledBy().getFullName() : bc.getHandledBy().getEmail();
                    }

                    cancelBox.add(new Paragraph(bc.getCancelledAt().format(dtf) + " — " + who + " (cancellation)"));
                    cancelBox.add(new Paragraph("Booking cancelled."));
                    if (bc.getReason() != null && !bc.getReason().isBlank()) {
                        cancelBox.add(new Paragraph("Reason: " + bc.getReason()));
                    }
                    if (bc.getCancellationFee() != null) {
                        cancelBox.add(new Paragraph("Cancellation fee: " + String.format("%.2f €", bc.getCancellationFee())));
                    }

                    history.addComponentAtIndex(0, cancelBox);
                });
            } catch (Exception ex) {
                // ignore any errors when loading cancellation info
            }
        }

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

    /**
     * Applies all active filter criteria to the booking list
     * and updates the grid accordingly.
     * @author Matthias Lohr
     */
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
}