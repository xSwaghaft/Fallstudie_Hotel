package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.User;
import com.hotel.booking.service.BookingCancellationService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.service.InvoiceService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.util.stream.Collectors;

import jakarta.annotation.security.RolesAllowed;

// Author: Matthias Lohr
@Route(value = "bookings", layout = MainLayout.class)
@PageTitle("Booking Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/booking-management.css")
@RolesAllowed({UserRole.RECEPTIONIST_VALUE, UserRole.MANAGER_VALUE})
/**
 * BookingManagementView
 *
 * Main Vaadin view for managing bookings. Shows a searchable/filterable grid
 * of bookings and provides actions for viewing, editing and cancelling bookings.
 * Responsibilities:
 * - List bookings in a grid with custom columns and actions
 * - Provide add/edit dialog with preview + confirm step
 * - Handle cancellation flow and record modification history
 */
public class BookingManagementView extends VerticalLayout {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService formService;
    private final com.hotel.booking.service.BookingModificationService modificationService;
    private final BookingCancellationService bookingCancellationService;
    private final InvoiceService invoiceService;

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Grid<Booking> grid = new Grid<>(Booking.class, false);
    private final List<Booking> bookings = new ArrayList<>();
    private TextField searchField;
    private Select<String> statusFilter;
    private DatePicker dateFilter;
    private Select<String> categoryFilter;
    private List<String> categoryNames;
    private final String ALL_STATUS = "All Status";

    // Simple immutable snapshot holder for previous booking values.
    // Declared at class level to avoid local-record issues with some compiler setups.
    private static record PrevBooking(LocalDate checkIn, LocalDate checkOut, Integer amount, BigDecimal total, Set<BookingExtra> extras) {}

    public BookingManagementView(SessionService sessionService, BookingService bookingService, BookingFormService formService, com.hotel.booking.service.BookingModificationService modificationService, RoomCategoryService roomCategoryService, BookingCancellationService bookingCancellationService, PaymentService paymentService, InvoiceService invoiceService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.formService = formService;
        this.modificationService = modificationService;
        this.bookingCancellationService = bookingCancellationService;
        this.invoiceService = invoiceService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        bookings.addAll(bookingService.findAll());

        categoryNames = new ArrayList<>();
        categoryNames.add("All Rooms");
        roomCategoryService.getAllRoomCategories().forEach(cat -> categoryNames.add(cat.getName()));

        add(createHeader(), createFilters(), createBookingsCard());
    }

    // Create the top header area containing page title, subtitle and action button.
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

    // Edited/maintained by: Ruslan
    private void openAddBookingDialog(Booking existingBooking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingBooking != null ? "Edit Booking" : "New Booking");
        dialog.setWidth("600px");

        // Wenn vorhandene Buchung bezahlt ist, keine Änderungen erlauben
        // Use InvoiceService to handle inverted relationship (Invoice owns booking_id FK)
        if (existingBooking != null && existingBooking.getId() != null) {
            boolean isPaid = invoiceService.findByBookingId(existingBooking.getId())
                    .map(inv -> inv.getInvoiceStatus() == Invoice.PaymentStatus.PAID)
                    .orElse(false);
            if (isPaid) {
                Notification.show("Änderung nicht möglich: Buchung bereits bezahlt.", 4000, Notification.Position.MIDDLE);
                return;
            }
        }

        /**
         * Opens the booking form (new / edit).
         *
         * Behavior:
         * - Adds a two-step save flow: Form -> Preview (before/after) -> Confirm.
         * - On confirmation, previous values are recorded via `BookingModificationService`
         *   and the update is saved. Any save errors are shown in a Notification with details.
         */
        createNewBookingForm form = new createNewBookingForm(sessionService.getCurrentUser(), sessionService, existingBooking, formService);

        Button saveButton = new Button("Save", e -> {
            try {
                // For existing bookings, capture previous values as a snapshot.
                // Use the `PrevBooking` nested record declared at class level
                // instead of multiple `AtomicReference` instances — clearer and safe.
                final PrevBooking prevSnapshot = existingBooking != null
                        ? new PrevBooking(existingBooking.getCheckInDate(), existingBooking.getCheckOutDate(), existingBooking.getAmount(), existingBooking.getTotalPrice(), existingBooking.getExtras())
                        : null;

                form.writeBean(); // Transfers form values into the Booking object
                Booking updated = form.getBooking();

                // Recalculate the booking price using the service method
                bookingService.calculateBookingPrice(updated);

                // Preview dialog (before / after) to let the user confirm changes
                Dialog preview = new Dialog();
                preview.setHeaderTitle(existingBooking != null ? "Confirm Booking Changes" : "Confirm New Booking");
                VerticalLayout content = new VerticalLayout();
                if (existingBooking != null) {
                    content.add(new Paragraph("-- Before --"));
                    PrevBooking prev = prevSnapshot;
                    LocalDate prevCheckIn = prev != null ? prev.checkIn() : null;
                    LocalDate prevCheckOut = prev != null ? prev.checkOut() : null;
                    Integer prevAmount = prev != null ? prev.amount() : null;
                    BigDecimal prevTotal = prev != null ? prev.total() : null;
                    Set<BookingExtra> prevExtras = prev != null ? prev.extras() : null;
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
                        // If present, record previous values
                        if (existingBooking != null) {
                                modificationService.recordChangesFromSnapshot(existingBooking,
                                    (prevSnapshot != null ? prevSnapshot.checkIn() : null),
                                    (prevSnapshot != null ? prevSnapshot.checkOut() : null),
                                    (prevSnapshot != null ? prevSnapshot.amount() : null),
                                    (prevSnapshot != null ? prevSnapshot.total() : null),
                                    (prevSnapshot != null ? prevSnapshot.extras() : null),
                                    updated, sessionService.getCurrentUser(), null);
                        }

                        bookingService.save(updated);
                        dialog.close();
                        preview.close();
                        // Refresh Grid
                        grid.setItems(bookingService.findAll());
                        Notification.show("Booking saved successfully.", 3000, Notification.Position.BOTTOM_START);
                    } catch (Exception ex) {
                        String msg = ex.getMessage() != null ? ex.getMessage() : "Error saving booking.";
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

    // Build search and filter controls (text, status, date, category).
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

    // Assemble the bookings card: grid setup and column configuration.
    // Columns are defined manually to control widths and responsive behavior.
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

    // Create the action buttons shown in the grid for each booking.
    // Buttons vary depending on booking status (e.g. Check In only for CONFIRMED).
    private Component createActionButtons(Booking booking) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.addClassName("booking-actions");
        
        Button viewBtn = new Button("View", VaadinIcon.EYE.create());
        viewBtn.addClickListener(e -> openDetails(booking));
        
        // Edit button: only enabled when booking is not CONFIRMED
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        if (booking.getStatus() == null || booking.getStatus() != BookingStatus.CONFIRMED) {
            editBtn.addClickListener(e -> openAddBookingDialog(booking));
        } else {
            editBtn.setEnabled(false);
            editBtn.getElement().setProperty("title", "Cannot edit a confirmed booking");
        }

        actions.add(viewBtn, editBtn);
        
        if (booking.getStatus() != null && "CONFIRMED".equals(booking.getStatus().name())) {
            Button checkInBtn = new Button("Check In", VaadinIcon.SIGN_IN.create());
            checkInBtn.addClickListener(e -> Notification.show("Checked in " + booking.getBookingNumber()));
            actions.add(checkInBtn);
        }

        // Cancel button: visible/usable for PENDING, MODIFIED or CONFIRMED bookings
        if (booking.getStatus() != null && (booking.getStatus() == com.hotel.booking.entity.BookingStatus.PENDING
            || booking.getStatus() == com.hotel.booking.entity.BookingStatus.MODIFIED
            || booking.getStatus() == com.hotel.booking.entity.BookingStatus.CONFIRMED)) {
            Button cancelBtn = new Button("Cancel", VaadinIcon.CLOSE.create());
            cancelBtn.addClickListener(e -> confirmAndCancelBooking(booking));
            actions.add(cancelBtn);
        }
        
        return actions;
    }

    // Performs cancellation with a confirmation dialog and calculates tiered fees
    private void confirmAndCancelBooking(Booking b) {
        // Allow cancellation for PENDING, MODIFIED, CONFIRMED
        if (b.getStatus() == null || (b.getStatus() != com.hotel.booking.entity.BookingStatus.PENDING
                && b.getStatus() != com.hotel.booking.entity.BookingStatus.MODIFIED
                && b.getStatus() != com.hotel.booking.entity.BookingStatus.CONFIRMED)) {
            Notification.show("Only bookings with status 'Pending', 'Modified' or 'Confirmed' can be cancelled here.", 4000, Notification.Position.MIDDLE);
            return;
        }

        try {
            // Calculate cancellation fee based on days before check-in
            java.math.BigDecimal penalty = bookingCancellationService.calculateCancellationFee(b, b.getTotalPrice());
            long daysBefore = java.time.Duration.between(java.time.LocalDateTime.now(), b.getCheckInDate().atStartOfDay()).toDays();

            String timeframe;
            if (daysBefore >= 30) {
                timeframe = "more than 30 days";
            } else if (daysBefore >= 7) {
                timeframe = "7-29 days";
            } else if (daysBefore >= 1) {
                timeframe = "1-6 days";
            } else {
                timeframe = "on arrival day";
            }

            Dialog confirm = new Dialog();
            confirm.setHeaderTitle("Confirm cancellation");
            VerticalLayout cnt = new VerticalLayout();
            cnt.add(new Paragraph("Cancellation window: " + timeframe + " before check-in"));
            cnt.add(new Paragraph("Cancellation fee: " + String.format("%.2f €", penalty)));
            cnt.add(new Paragraph("Refund: " + String.format("%.2f €", b.getTotalPrice().subtract(penalty))));
            cnt.add(new Paragraph("Do you want to confirm the cancellation?"));

            final java.math.BigDecimal penaltyFinal = penalty;
            Button confirmBtn = new Button("Confirm", ev -> {
                try {
                    BookingCancellation bc = new BookingCancellation();
                    bc.setBooking(b);
                    bc.setCancelledAt(java.time.LocalDateTime.now());
                    bc.setReason("Cancelled by management");
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
                    Notification.show("Booking cancelled. Refund: " + String.format("%.2f €", refundedAmount) + " | Fee: " + String.format("%.2f €", penaltyFinal), 4000, Notification.Position.BOTTOM_START);
                } catch (Exception ex) {
                    Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error canceling booking", 5000, Notification.Position.MIDDLE);
                }
            });
            
            Button backBtn = new Button("Back", ev -> confirm.close());
            confirm.add(cnt, new HorizontalLayout(confirmBtn, backBtn));
            confirm.open();
        } catch (Exception ex) {
            Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error canceling booking", 5000, Notification.Position.MIDDLE);
        }
    }

    // Open a details dialog for a booking. Shows tabs for details, payments, history and extras.
    // History tab aggregates BookingModification entries grouped by modification timestamp.
    private void openDetails(Booking b) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Booking Details - " + b.getBookingNumber());
        d.setWidth("800px");

        Tabs tabs = new Tabs(new Tab("Details"), new Tab("History"), new Tab("Extras"));
        
        Div details = new Div();
        details.add(new Paragraph("Guest Name: " + (b.getGuest() != null ? b.getGuest().getFullName() : "N/A")));
        details.add(new Paragraph("Booking Number: " + b.getBookingNumber()));
        details.add(new Paragraph("Total Price: €" + b.getTotalPrice()));
        details.add(new Paragraph("Check-in: " + b.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Check-out: " + b.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Guests: " + b.getAmount()));
        details.add(new Paragraph("Status: " + b.getStatus()));

        // If cancelled: show the most recently saved cancellation fee and reason
        if (b.getStatus() == com.hotel.booking.entity.BookingStatus.CANCELLED && b.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(b.getId()).ifPresent(bc -> {
                    if (bc.getCancellationFee() != null) {
                        details.add(new Paragraph("Cancellation fee: " + String.format("%.2f €", bc.getCancellationFee())));
                    }
                    if (bc.getReason() != null && !bc.getReason().isBlank()) {
                        details.add(new Paragraph("Cancellation reason: " + bc.getReason()));
                    }
                });
            } catch (Exception ex) {
                // ignore
            }
        }

        Div history = new Div();
        // Load modifications for this booking and display them grouped by `modifiedAt` timestamp
        if (b.getId() != null) {
            java.util.List<com.hotel.booking.entity.BookingModification> mods = modificationService.findByBookingId(b.getId());
            if (mods.isEmpty()) {
                history.add(new Paragraph("No modification history available."));
            } else {
                // Group by modifiedAt to produce one batch group per modification timestamp
                java.util.Map<java.time.LocalDateTime, java.util.List<com.hotel.booking.entity.BookingModification>> grouped =
                        mods.stream().collect(java.util.stream.Collectors.groupingBy(com.hotel.booking.entity.BookingModification::getModifiedAt, java.util.LinkedHashMap::new, java.util.stream.Collectors.toList()));

                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

                for (java.util.Map.Entry<java.time.LocalDateTime, java.util.List<com.hotel.booking.entity.BookingModification>> entry : grouped.entrySet()) {
                    java.time.LocalDateTime ts = entry.getKey();
                    java.util.List<com.hotel.booking.entity.BookingModification> group = entry.getValue();

                    VerticalLayout groupBox = new VerticalLayout();
                    groupBox.addClassName("history-group");

                    // Header: timestamp + handler (first non-null handler in the group)
                    String who = "system";
                    for (com.hotel.booking.entity.BookingModification m : group) {
                        if (m.getHandledBy() != null) {
                            who = (m.getHandledBy().getFullName() != null && !m.getHandledBy().getFullName().isBlank()) ? m.getHandledBy().getFullName() : m.getHandledBy().getEmail();
                            break;
                        }
                    }
                    groupBox.add(new Paragraph(ts.format(dtf) + " — " + who));

                    // List of field changes for this group
                    for (com.hotel.booking.entity.BookingModification m : group) {
                        HorizontalLayout row = new HorizontalLayout();
                        row.setWidthFull();
                        Paragraph field = new Paragraph(m.getFieldChanged() + ": ");
                        field.addClassName("history-field");
                        Paragraph values = new Paragraph((m.getOldValue() != null ? m.getOldValue() : "<null>") + " → " + (m.getNewValue() != null ? m.getNewValue() : "<null>"));
                        values.addClassName("history-values");
                        row.add(field, values);
                        groupBox.add(row);
                        if (m.getReason() != null && !m.getReason().isBlank()) {
                            Span note = new Span("Reason: " + m.getReason());
                            note.addClassName("history-note");
                            groupBox.add(note);
                        }
                    }

                    history.add(groupBox);
                }
            }
        } else {
            history.add(new Paragraph("No modification history available."));
        }

        // If a cancellation exists, display it prominently in the history (who, when, reason, fee)
        if (b.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(b.getId()).ifPresent(bc -> {
                    VerticalLayout cancelBox = new VerticalLayout();
                    cancelBox.addClassName("cancel-box");

                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
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

                    // Prepend the cancellation entry to the top of the history list
                    history.addComponentAtIndex(0, cancelBox);
                });
            } catch (Exception ex) {
                // ignore any errors when loading cancellation info
            }
        }

        Div extras = new Div();
        // Show extras the same way as in MyBookings view: each extra with name, price and optional description
        if (b.getExtras() == null || b.getExtras().isEmpty()) {
            extras.add(new Paragraph("No additional services requested"));
        } else {
            for (BookingExtra extra : b.getExtras()) {
                Div extraItem = new Div();
                extraItem.add(new Paragraph(extra.getName() + " - " + String.format("%.2f €", extra.getPrice())));
                if (extra.getDescription() != null && !extra.getDescription().isBlank()) {
                    Paragraph desc = new Paragraph(extra.getDescription());
                    desc.getStyle().set("font-size", "var(--font-size-sm)");
                    desc.getStyle().set("color", "var(--color-text-secondary)");
                    extraItem.add(desc);
                }
                extras.add(extraItem);
            }
        }

        Div pages = new Div(details, history, extras);
        pages.addClassName("booking-details-container");
        history.setVisible(false); 
        extras.setVisible(false);

        tabs.addSelectedChangeListener(ev -> {
            details.setVisible(tabs.getSelectedIndex() == 0);
            history.setVisible(tabs.getSelectedIndex() == 1);
            extras.setVisible(tabs.getSelectedIndex() == 2);
        });

        Button edit = new Button("Edit Booking");
        if (b.getStatus() == null || b.getStatus() != BookingStatus.CONFIRMED) {
            edit.addClickListener(e -> { d.close(); openAddBookingDialog(b); });
        } else {
            edit.setEnabled(false);
            edit.getElement().setProperty("title", "Cannot edit a confirmed booking");
        }
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
}