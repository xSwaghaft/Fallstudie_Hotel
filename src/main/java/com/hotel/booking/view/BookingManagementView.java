package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.RoomStatus;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.stream.Collectors;

@Route(value = "bookings", layout = MainLayout.class)
@PageTitle("Booking Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/booking-management.css")
@CssImport("./themes/hotel/views/my-bookings.css")
@RolesAllowed({UserRole.RECEPTIONIST_VALUE, UserRole.MANAGER_VALUE})
public class BookingManagementView extends VerticalLayout {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService formService;
    private final BookingModificationService modificationService;
    private final BookingCancellationService bookingCancellationService;
    private final RoomCategoryService roomCategoryService;
    private final RoomService roomService;

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final LocalDate today = LocalDate.now();

    private final Grid<Booking> grid = new Grid<>(Booking.class, false);
    private final Grid<Booking> checkGrid = new Grid<>(Booking.class, false);

    private final List<Booking> bookings = new ArrayList<>();

    private TextField searchField;
    private Select<String> statusFilter;
    private DatePicker dateFilter;
    private Select<String> categoryFilter;
    private List<String> categoryNames;

    private final String ALL_STATUS = "All Status";
    private static final String ALL_ROOMS = "All Rooms";

    private Div checkInOutCard;
    private H3 checkInOutTitle;

    // Simple immutable snapshot holder for previous booking values (your style)
    private static record PrevBooking(LocalDate checkIn, LocalDate checkOut, Integer amount, BigDecimal total, Set<BookingExtra> extras) {}

    public BookingManagementView(SessionService sessionService,
                                 BookingService bookingService,
                                 BookingFormService formService,
                                 BookingModificationService modificationService,
                                 BookingCancellationService bookingCancellationService,
                                 RoomCategoryService roomCategoryService,
                                 RoomService roomService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.formService = formService;
        this.modificationService = modificationService;
        this.bookingCancellationService = bookingCancellationService;
        this.roomCategoryService = roomCategoryService;
        this.roomService = roomService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        initCategories();
        reloadBookings();

        add(createHeader());

        // Check-in/out card (master view feature)
        checkInOutCard = createCheckInOutCard();
        add(checkInOutCard);

        add(createFilters(), createBookingsCard());

        refreshCheckInOutCardVisibility();
    }

    private void initCategories() {
        categoryNames = new ArrayList<>();
        categoryNames.add(ALL_ROOMS);
        roomCategoryService.getAllRoomCategories().forEach(cat -> categoryNames.add(cat.getName()));
    }

    private void reloadBookings() {
        bookings.clear();
        bookings.addAll(bookingService.findAll());
    }

    private void reloadAndRefresh() {
        reloadBookings();
        filterBookings(); // keeps current filter state
        refreshCheckGrid();
        refreshCheckInOutCardVisibility();
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
        categoryFilter.setValue(ALL_ROOMS);
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
                .setFlexGrow(2);

        grid.addColumn(Booking::getAmount)
                .setHeader("People")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(b -> b.getRoom() != null ? b.getRoom().getRoomNumber() : "N/A")
                .setHeader("Room")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(b -> (b.getGuest() != null ? b.getGuest().getFullName() : "N/A"))
                .setHeader("Guest Name")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(b -> formatDate(b.getCheckInDate()))
                .setHeader("Check-in Date")
                .setWidth("140px")
                .setFlexGrow(0);

        grid.addColumn(b -> formatDate(b.getCheckOutDate()))
                .setHeader("Check-out")
                .setWidth("140px")
                .setFlexGrow(0);

        grid.addColumn(b -> "€" + (b.getTotalPrice() != null ? b.getTotalPrice() : BigDecimal.ZERO))
                .setHeader("Amount")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(4);

        grid.setWidthFull();
        grid.setItems(bookings);

        card.add(title, grid);
        return card;
    }

    private Div createCheckInOutCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        checkInOutTitle = new H3("Today: Check-in / Check-out");
        checkInOutTitle.addClassName("booking-section-title");

        checkGrid.removeAllColumns();

        checkGrid.addColumn(Booking::getBookingNumber)
                .setHeader("Booking ID")
                .setAutoWidth(true);

        checkGrid.addColumn(b -> b.getGuest() != null ? b.getGuest().getFullName() : "N/A")
                .setHeader("Guest");

        checkGrid.addColumn(b -> b.getRoom() != null ? b.getRoom().getRoomNumber() : "N/A")
                .setHeader("Room");

        checkGrid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(0);

        checkGrid.addComponentColumn(this::createCheckInOutButtons)
                .setHeader("Action")
                .setAutoWidth(true);

        checkGrid.setWidthFull();
        checkGrid.setMaxHeight("210px");

        card.add(checkInOutTitle, checkGrid);

        refreshCheckGrid();
        return card;
    }

    private void refreshCheckGrid() {
        List<Booking> actionable = bookings.stream()
                .filter(this::isActionableToday)
                .toList();
        checkGrid.setItems(actionable);
    }

    private void refreshCheckInOutCardVisibility() {
        boolean hasActionable = bookings.stream().anyMatch(this::isActionableToday);
        if (checkInOutCard != null) {
            checkInOutCard.setVisible(hasActionable);
        }
    }

    private Component createStatusBadge(Booking booking) {
        BookingStatus st = booking != null ? booking.getStatus() : null;
        Span badge = new Span(st != null ? st.name() : "UNKNOWN");
        badge.addClassName("status-badge");
        badge.addClassName("status-" + (st != null ? st.name().toLowerCase() : "unknown"));
        return badge;
    }

    /**
     * Status-Regeln von dir:
     * Edit nur wenn Status NICHT CONFIRMED/CANCELLED/COMPLETED ist.
     * Cancel-Regeln von dir: PENDING/MODIFIED/CONFIRMED.
     */
    private Component createActionButtons(Booking booking) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.addClassName("booking-actions");

        Button viewBtn = new Button("View", VaadinIcon.EYE.create());
        viewBtn.addClickListener(e -> openDetails(booking));
        actions.add(viewBtn);

        if (isEditableByStatus(booking)) {
            Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
            editBtn.addClickListener(e -> openAddBookingDialog(booking));
            actions.add(editBtn);
        }

        if (isCancellableByStatus(booking)) {
            Button cancelBtn = new Button("Cancel", VaadinIcon.CLOSE.create());
            cancelBtn.addClickListener(e -> confirmAndCancelBooking(booking));
            actions.add(cancelBtn);
        }

        return actions;
    }

    private boolean isEditableByStatus(Booking booking) {
        if (booking == null || booking.getStatus() == null) return true;
        return booking.getStatus() != BookingStatus.CONFIRMED
                && booking.getStatus() != BookingStatus.CANCELLED
                && booking.getStatus() != BookingStatus.COMPLETED;
    }

    private boolean isCancellableByStatus(Booking booking) {
        if (booking == null || booking.getStatus() == null) return false;
        return booking.getStatus() == BookingStatus.PENDING
                || booking.getStatus() == BookingStatus.MODIFIED
                || booking.getStatus() == BookingStatus.CONFIRMED;
    }

    // Check-in/out logic (kept from master view)
    private Component createCheckInOutButtons(Booking booking) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        boolean hasButton = false;

        // Check-in: today is check-in and status is PENDING/CONFIRMED/MODIFIED
        if (booking != null
                && booking.getCheckInDate() != null
                && today.equals(booking.getCheckInDate())
                && (booking.getStatus() == BookingStatus.PENDING
                    || booking.getStatus() == BookingStatus.CONFIRMED
                    || booking.getStatus() == BookingStatus.MODIFIED)) {

            Button checkInBtn = new Button(VaadinIcon.SIGN_IN.create(), e -> {
                try {
                    booking.setStatus(BookingStatus.CHECKED_IN);
                    if (booking.getRoom() != null) {
                        booking.getRoom().setStatus(RoomStatus.OCCUPIED);
                        roomService.save(booking.getRoom());
                    }
                    bookingService.save(booking);
                    reloadAndRefresh();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage() != null ? ex.getMessage() : "Check-in failed", 5000, Notification.Position.MIDDLE);
                }
            });
            checkInBtn.getElement().setAttribute("title", "Check In");
            layout.add(checkInBtn);
            hasButton = true;
        }

        // Check-out: today is check-out and status is CHECKED_IN
        if (booking != null
                && booking.getCheckOutDate() != null
                && today.equals(booking.getCheckOutDate())
                && booking.getStatus() == BookingStatus.CHECKED_IN) {

            Button checkOutBtn = new Button(VaadinIcon.SIGN_OUT.create(), e -> {
                try {
                    booking.setStatus(BookingStatus.COMPLETED);
                    if (booking.getRoom() != null) {
                        booking.getRoom().setStatus(RoomStatus.AVAILABLE);
                        roomService.save(booking.getRoom());
                    }
                    bookingService.save(booking);
                    reloadAndRefresh();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage() != null ? ex.getMessage() : "Check-out failed", 5000, Notification.Position.MIDDLE);
                }
            });
            checkOutBtn.getElement().setAttribute("title", "Check Out");
            layout.add(checkOutBtn);
            hasButton = true;
        }

        return hasButton ? layout : new Span();
    }

    private boolean isActionableToday(Booking b) {
        if (b == null) return false;

        boolean canCheckIn = b.getCheckInDate() != null
                && today.equals(b.getCheckInDate())
                && (b.getStatus() == BookingStatus.PENDING
                    || b.getStatus() == BookingStatus.CONFIRMED
                    || b.getStatus() == BookingStatus.MODIFIED);

        boolean canCheckOut = b.getCheckOutDate() != null
                && today.equals(b.getCheckOutDate())
                && b.getStatus() == BookingStatus.CHECKED_IN;

        return canCheckIn || canCheckOut;
    }

    private void openAddBookingDialog(Booking existingBooking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingBooking != null ? "Edit Booking" : "New Booking");
        dialog.setWidth("600px");

        createNewBookingForm form = new createNewBookingForm(
                sessionService.getCurrentUser(),
                sessionService,
                existingBooking,
                formService
        );

        Button saveButton = new Button("Save", e -> {
            try {
                final PrevBooking prevSnapshot = existingBooking != null
                        ? new PrevBooking(
                            existingBooking.getCheckInDate(),
                            existingBooking.getCheckOutDate(),
                            existingBooking.getAmount(),
                            existingBooking.getTotalPrice(),
                            existingBooking.getExtras())
                        : null;

                form.writeBean();
                Booking updated = form.getBooking();

                bookingService.calculateBookingPrice(updated);

                Dialog preview = new Dialog();
                preview.setHeaderTitle(existingBooking != null ? "Confirm Booking Changes" : "Confirm New Booking");

                VerticalLayout content = new VerticalLayout();
                content.addClassName("booking-edit-preview");

                if (existingBooking != null) {
                    content.add(createPreviewSection(
                            "Before",
                            prevSnapshot != null ? prevSnapshot.checkIn() : null,
                            prevSnapshot != null ? prevSnapshot.checkOut() : null,
                            prevSnapshot != null ? prevSnapshot.amount() : null,
                            prevSnapshot != null ? prevSnapshot.total() : null,
                            prevSnapshot != null ? prevSnapshot.extras() : null
                    ));
                }

                content.add(createPreviewSection(
                        "After",
                        updated.getCheckInDate(),
                        updated.getCheckOutDate(),
                        updated.getAmount(),
                        updated.getTotalPrice(),
                        updated.getExtras()
                ));

                Button confirm = new Button("Confirm", ev -> {
                    try {
                        if (existingBooking != null) {
                            modificationService.recordChangesFromSnapshot(
                                    existingBooking,
                                    (prevSnapshot != null ? prevSnapshot.checkIn() : null),
                                    (prevSnapshot != null ? prevSnapshot.checkOut() : null),
                                    (prevSnapshot != null ? prevSnapshot.amount() : null),
                                    (prevSnapshot != null ? prevSnapshot.total() : null),
                                    (prevSnapshot != null ? prevSnapshot.extras() : null),
                                    updated,
                                    sessionService.getCurrentUser(),
                                    null
                            );
                        }

                        bookingService.save(updated);
                        preview.close();
                        dialog.close();

                        reloadAndRefresh();
                        Notification.show("Booking saved successfully.", 3000, Notification.Position.BOTTOM_START);
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error saving booking.", 6000, Notification.Position.MIDDLE);
                    }
                });
                confirm.addClassName("primary-button");

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

    // Cancellation rules from your file (PENDING/MODIFIED/CONFIRMED)
    private void confirmAndCancelBooking(Booking b) {
        if (b == null || b.getStatus() == null
                || (b.getStatus() != BookingStatus.PENDING
                && b.getStatus() != BookingStatus.MODIFIED
                && b.getStatus() != BookingStatus.CONFIRMED)) {
            Notification.show("Only bookings with status 'Pending', 'Modified' or 'Confirmed' can be cancelled here.", 4000, Notification.Position.MIDDLE);
            return;
        }

        try {
            BigDecimal penalty = bookingCancellationService.calculateCancellationFee(b, b.getTotalPrice());
            long daysBefore = java.time.Duration
                    .between(LocalDateTime.now(), b.getCheckInDate().atStartOfDay())
                    .toDays();

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

            final BigDecimal penaltyFinal = penalty;

            Button confirmBtn = new Button("Confirm", ev -> {
                try {
                    BookingCancellation bc = new BookingCancellation();
                    bc.setBooking(b);
                    bc.setCancelledAt(LocalDateTime.now());
                    bc.setReason("Cancelled by management");
                    bc.setCancellationFee(penaltyFinal);

                    BigDecimal refundedAmount = b.getTotalPrice().subtract(penaltyFinal);
                    bc.setRefundedAmount(refundedAmount);

                    User current = sessionService.getCurrentUser();
                    if (current != null) {
                        bc.setHandledBy(current);
                    }

                    bookingCancellationService.processCancellation(b, bc, refundedAmount);

                    confirm.close();
                    reloadAndRefresh();

                    Notification.show(
                            "Booking cancelled. Refund: " + String.format("%.2f €", refundedAmount) + " | Fee: " + String.format("%.2f €", penaltyFinal),
                            4000,
                            Notification.Position.BOTTOM_START
                    );
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

    private void openDetails(Booking b) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Booking Details - " + (b != null ? b.getBookingNumber() : ""));
        d.setWidth("800px");

        Tabs tabs = new Tabs(new Tab("Details"), new Tab("History"), new Tab("Extras"));

        Div details = new Div();
        details.add(new Paragraph("Guest Name: " + (b.getGuest() != null ? b.getGuest().getFullName() : "N/A")));
        details.add(new Paragraph("Booking Number: " + (b.getBookingNumber() != null ? b.getBookingNumber() : "N/A")));
        details.add(new Paragraph("Total Price: €" + (b.getTotalPrice() != null ? b.getTotalPrice() : BigDecimal.ZERO)));
        details.add(new Paragraph("Check-in: " + formatDate(b.getCheckInDate())));
        details.add(new Paragraph("Check-out: " + formatDate(b.getCheckOutDate())));
        details.add(new Paragraph("Room: " + (b.getRoom() != null ? b.getRoom().getRoomNumber() : "N/A")));
        details.add(new Paragraph("Guests: " + formatValue(b.getAmount())));
        details.add(new Paragraph("Status: " + (b.getStatus() != null ? b.getStatus().name() : "UNKNOWN")));

        // If cancelled: show cancellation fee and reason if available
        if (b.getStatus() == BookingStatus.CANCELLED && b.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(b.getId()).ifPresent(bc -> {
                    if (bc.getCancellationFee() != null) {
                        details.add(new Paragraph("Cancellation fee: " + String.format("%.2f €", bc.getCancellationFee())));
                    }
                    if (bc.getReason() != null && !bc.getReason().isBlank()) {
                        details.add(new Paragraph("Cancellation reason: " + bc.getReason()));
                    }
                });
            } catch (Exception ignored) {
            }
        }

        Div history = buildHistoryTab(b);
        Div extras = buildExtrasTab(b);

        Div pages = new Div(details, history, extras);
        pages.addClassName("booking-details-container");

        history.setVisible(false);
        extras.setVisible(false);

        tabs.addSelectedChangeListener(ev -> {
            details.setVisible(tabs.getSelectedIndex() == 0);
            history.setVisible(tabs.getSelectedIndex() == 1);
            extras.setVisible(tabs.getSelectedIndex() == 2);
        });

        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);

        if (isEditableByStatus(b)) {
            Button edit = new Button("Edit Booking");
            edit.addClickListener(e -> { d.close(); openAddBookingDialog(b); });
            footer.add(edit);
        }

        Button close = new Button("Close", e -> d.close());
        footer.add(close);

        d.add(new VerticalLayout(tabs, pages));
        d.getFooter().add(footer);
        d.open();
    }

    private Div buildHistoryTab(Booking b) {
        Div history = new Div();

        if (b.getId() != null) {
            List<com.hotel.booking.entity.BookingModification> mods = modificationService.findByBookingId(b.getId());
            if (mods.isEmpty()) {
                history.add(new Paragraph("No modification history available."));
            } else {
                Map<LocalDateTime, List<com.hotel.booking.entity.BookingModification>> grouped =
                        mods.stream().collect(Collectors.groupingBy(
                                com.hotel.booking.entity.BookingModification::getModifiedAt,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

                for (Map.Entry<LocalDateTime, List<com.hotel.booking.entity.BookingModification>> entry : grouped.entrySet()) {
                    LocalDateTime ts = entry.getKey();
                    List<com.hotel.booking.entity.BookingModification> group = entry.getValue();

                    VerticalLayout groupBox = new VerticalLayout();
                    groupBox.addClassName("history-group");

                    String who = "system";
                    for (com.hotel.booking.entity.BookingModification m : group) {
                        if (m.getHandledBy() != null) {
                            who = (m.getHandledBy().getFullName() != null && !m.getHandledBy().getFullName().isBlank())
                                    ? m.getHandledBy().getFullName()
                                    : m.getHandledBy().getEmail();
                            break;
                        }
                    }

                    groupBox.add(new Paragraph(ts.format(dtf) + " — " + who));

                    for (com.hotel.booking.entity.BookingModification m : group) {
                        HorizontalLayout row = new HorizontalLayout();
                        row.setWidthFull();

                        Paragraph field = new Paragraph(m.getFieldChanged() + ": ");
                        field.addClassName("history-field");

                        Paragraph values = new Paragraph(
                                (m.getOldValue() != null ? m.getOldValue() : "<null>")
                                        + " → "
                                        + (m.getNewValue() != null ? m.getNewValue() : "<null>")
                        );
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

        // If a cancellation exists, display it prominently at top
        if (b.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(b.getId()).ifPresent(bc -> {
                    VerticalLayout cancelBox = new VerticalLayout();
                    cancelBox.addClassName("cancel-box");

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                    String who = "system";
                    if (bc.getHandledBy() != null) {
                        who = (bc.getHandledBy().getFullName() != null && !bc.getHandledBy().getFullName().isBlank())
                                ? bc.getHandledBy().getFullName()
                                : bc.getHandledBy().getEmail();
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
            } catch (Exception ignored) {
            }
        }

        return history;
    }

    private Div buildExtrasTab(Booking b) {
        Div extras = new Div();

        if (b.getExtras() == null || b.getExtras().isEmpty()) {
            extras.add(new Paragraph("No additional services requested"));
            return extras;
        }

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

        return extras;
    }

    private void filterBookings() {
        String search = searchField != null && searchField.getValue() != null
                ? searchField.getValue().trim().toLowerCase()
                : "";

        String selectedStatus = statusFilter != null ? statusFilter.getValue() : ALL_STATUS;
        LocalDate date = dateFilter != null ? dateFilter.getValue() : null;
        String selectedCategory = categoryFilter != null ? categoryFilter.getValue() : ALL_ROOMS;

        List<Booking> filtered = bookings.stream()
                .filter(b -> {
                    boolean matchesSearch = search.isEmpty()
                            || (b.getBookingNumber() != null && b.getBookingNumber().toLowerCase().contains(search))
                            || (b.getGuest() != null && b.getGuest().getFullName() != null
                                && b.getGuest().getFullName().toLowerCase().contains(search));

                    boolean matchesStatus = ALL_STATUS.equals(selectedStatus)
                            || (b.getStatus() != null && b.getStatus().name().equals(selectedStatus));

                    boolean matchesDate = (date == null)
                            || (b.getCreatedAt() != null && b.getCreatedAt().isAfter(date));

                    boolean matchesCategory = ALL_ROOMS.equals(selectedCategory)
                            || (b.getRoomCategory() != null
                                && b.getRoomCategory().getName() != null
                                && b.getRoomCategory().getName().equalsIgnoreCase(selectedCategory));

                    return matchesSearch && matchesStatus && matchesDate && matchesCategory;
                })
                .toList();

        grid.setItems(filtered);
    }

    private VerticalLayout createPreviewSection(String title,
                                               LocalDate checkIn,
                                               LocalDate checkOut,
                                               Integer amount,
                                               BigDecimal totalPrice,
                                               Set<BookingExtra> extras) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("booking-edit-preview-section");

        Paragraph titlePara = new Paragraph(title);
        titlePara.addClassName("booking-edit-preview-title");
        section.add(titlePara);

        section.add(new Paragraph("Check-in: " + formatDate(checkIn)));
        section.add(new Paragraph("Check-out: " + formatDate(checkOut)));
        section.add(new Paragraph("Guests: " + formatValue(amount)));
        section.add(new Paragraph("Total Price: " + formatPrice(totalPrice)));
        section.add(new Paragraph("Extras: " + formatExtras(extras)));

        return section;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(GERMAN_DATE_FORMAT) : "N/A";
    }

    private String formatValue(Object value) {
        return value != null ? value.toString() : "N/A";
    }

    private String formatPrice(BigDecimal price) {
        return price != null ? String.format("%.2f €", price) : "N/A";
    }

    private String formatExtras(Set<BookingExtra> extras) {
        if (extras == null || extras.isEmpty()) {
            return "none";
        }
        return extras.stream()
                .map(BookingExtra::getName)
                .collect(Collectors.joining(", "));
    }
}
