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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Route(value = "bookings", layout = MainLayout.class)
@PageTitle("Booking Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/booking-management.css")
public class BookingManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService formService;
    private final com.hotel.booking.service.BookingModificationService modificationService;
    private final BookingCancellationService bookingCancellationService;

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Grid<Booking> grid = new Grid<>(Booking.class, false);
    private final List<Booking> bookings = new ArrayList<>();
    private TextField searchField;
    private Select<String> statusFilter;
    private DatePicker dateFilter;
    private Select<String> categoryFilter;
    private List<String> categoryNames;
    private final String ALL_STATUS = "All Status";

    public BookingManagementView(SessionService sessionService, BookingService bookingService, BookingFormService formService, com.hotel.booking.service.BookingModificationService modificationService, RoomCategoryService roomCategoryService, BookingCancellationService bookingCancellationService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.formService = formService;
        this.modificationService = modificationService;
        this.bookingCancellationService = bookingCancellationService;

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

    //Ruslan
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

        /**
         * Öffnet das Booking-Formular (Neu/ Edit).
         *
         * Verhalten:
         * - Fügt eine zweistufige Speicherung hinzu: Formular -> Preview (Vorher/Nachher) -> Confirm.
         * - Bei Bestätigung werden alte Werte protokolliert (über `BookingModificationService`)
         *   und die Änderung gespeichert. Fehler beim Speichern werden in einer
         *   Notification mit der konkreten Fehlermeldung angezeigt.
         */
        createNewBookingForm form = new createNewBookingForm(sessionService.getCurrentUser(), sessionService, existingBooking, formService);

        Button saveButton = new Button("Save", e -> {
            try {
                // Bei bestehenden Buchungen vorherige Werte als Snapshot merken
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

                form.writeBean(); // Überträgt die Formulardaten in das Booking-Objekt
                Booking updated = form.getBooking();

                // Preis neu berechnen (nutze vorhandene Methode)
                bookingService.calculateBookingPrice(updated);

                // Preview Dialog (Vorher / Nachher)
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
                        // Falls vorhanden, protokolliere alte Werte
                        if (existingBooking != null) {
                                modificationService.recordChangesFromSnapshot(existingBooking,
                                    prevCheckInRef.get(), prevCheckOutRef.get(), prevAmountRef.get(), prevTotalRef.get(), prevExtrasRef.get(),
                                    updated, sessionService.getCurrentUser(), null);
                        }

                        bookingService.save(updated);
                        dialog.close();
                        preview.close();
                        // Refresh Grid
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
        
        if (booking.getStatus() != null && "CONFIRMED".equals(booking.getStatus().name())) {
            Button checkInBtn = new Button("Check In", VaadinIcon.SIGN_IN.create());
            checkInBtn.addClickListener(e -> Notification.show("Checked in " + booking.getBookingNumber()));
            actions.add(checkInBtn);
        }

        // Cancel button: visible/usable for PENDING or MODIFIED bookings
        if (booking.getStatus() != null && (booking.getStatus() == com.hotel.booking.entity.BookingStatus.PENDING || booking.getStatus() == com.hotel.booking.entity.BookingStatus.MODIFIED)) {
            Button cancelBtn = new Button("Cancel", VaadinIcon.CLOSE.create());
            cancelBtn.addClickListener(e -> confirmAndCancelBooking(booking));
            actions.add(cancelBtn);
        }
        
        return actions;
    }

    // Führt die Stornierung mit Bestätigungsdialog, Berechnung der 48h/50% Regel
    private void confirmAndCancelBooking(Booking b) {
        // Only allow cancellation via this action for bookings with PENDING or MODIFIED status
        if (b.getStatus() == null || (b.getStatus() != com.hotel.booking.entity.BookingStatus.PENDING && b.getStatus() != com.hotel.booking.entity.BookingStatus.MODIFIED)) {
            Notification.show("Nur Buchungen mit Status 'Pending' oder 'Modified' können hier storniert werden.", 4000, Notification.Position.MIDDLE);
            return;
        }

        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime checkInAtStart = b.getCheckInDate().atStartOfDay();
            long hoursBefore = java.time.Duration.between(now, checkInAtStart).toHours();

            java.math.BigDecimal penalty = java.math.BigDecimal.ZERO;
            boolean hasPenalty = false;
            if (b.getTotalPrice() != null && hoursBefore < 48) {
                penalty = b.getTotalPrice().multiply(new java.math.BigDecimal("0.5")).setScale(2, java.math.RoundingMode.HALF_UP);
                hasPenalty = true;
            }

            if (hasPenalty) {
                final java.math.BigDecimal penaltyFinal = penalty;
                Dialog confirm = new Dialog();
                confirm.setHeaderTitle("Stornierung bestätigen");
                VerticalLayout cnt = new VerticalLayout();
                cnt.add(new Paragraph("Sie stornieren weniger als 48 Stunden vor Check-in."));
                cnt.add(new Paragraph("Es fällt eine Strafe in Höhe von 50% des Gesamtpreises an: " + String.format("%.2f €", penaltyFinal)));
                cnt.add(new Paragraph("Möchten Sie die Stornierung mit der Strafe bestätigen?"));

                Button confirmBtn = new Button("Bestätigen", ev -> {
                    try {
                        b.setStatus(com.hotel.booking.entity.BookingStatus.CANCELLED);
                        bookingService.save(b);

                        BookingCancellation bc = new BookingCancellation();
                        bc.setBooking(b);
                        bc.setCancelledAt(java.time.LocalDateTime.now());
                        bc.setReason("Storniert vom Management innerhalb 48 Stunden");
                        bc.setCancellationFee(penaltyFinal);
                        User current = sessionService.getCurrentUser();
                        if (current != null) {
                            bc.setHandledBy(current);
                        }
                        bookingCancellationService.save(bc);

                        confirm.close();
                        grid.setItems(bookingService.findAll());
                        Notification.show("Buchung storniert. Strafe: " + String.format("%.2f €", penaltyFinal), 4000, Notification.Position.BOTTOM_START);
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage() != null ? ex.getMessage() : "Fehler beim Stornieren", 5000, Notification.Position.MIDDLE);
                    }
                });

                Button backBtn = new Button("Zurück", ev -> confirm.close());
                confirm.add(cnt, new HorizontalLayout(confirmBtn, backBtn));
                confirm.open();
            } else {
                Dialog confirm = new Dialog();
                confirm.setHeaderTitle("Stornierung bestätigen");
                confirm.add(new Paragraph("Möchten Sie die Buchung wirklich stornieren?"));
                Button confirmBtn = new Button("Ja, stornieren", ev -> {
                    try {
                        b.setStatus(com.hotel.booking.entity.BookingStatus.CANCELLED);
                        bookingService.save(b);

                        BookingCancellation bc = new BookingCancellation();
                        bc.setBooking(b);
                        bc.setCancelledAt(java.time.LocalDateTime.now());
                        bc.setReason("Storniert vom Management");
                        bc.setCancellationFee(java.math.BigDecimal.ZERO);
                        User current = sessionService.getCurrentUser();
                        if (current != null) {
                            bc.setHandledBy(current);
                        }
                        bookingCancellationService.save(bc);

                        confirm.close();
                        grid.setItems(bookingService.findAll());
                        Notification.show("Buchung wurde storniert.", 3000, Notification.Position.BOTTOM_START);
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage() != null ? ex.getMessage() : "Fehler beim Stornieren", 5000, Notification.Position.MIDDLE);
                    }
                });
                Button backBtn = new Button("Abbrechen", ev -> confirm.close());
                confirm.add(new VerticalLayout(new Paragraph("Keine Strafe fällig."), new HorizontalLayout(confirmBtn, backBtn)));
                confirm.open();
            }
        } catch (Exception ex) {
            Notification.show(ex.getMessage() != null ? ex.getMessage() : "Fehler beim Stornieren", 5000, Notification.Position.MIDDLE);
        }
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

        // Wenn storniert: zeige die zuletzt gespeicherte Stornogebühr und Grund an
        if (b.getStatus() == com.hotel.booking.entity.BookingStatus.CANCELLED && b.getId() != null) {
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
        // Lade Modifikationen für diese Buchung und zeige sie gruppiert an (nach modifiedAt)
        if (b.getId() != null) {
            java.util.List<com.hotel.booking.entity.BookingModification> mods = modificationService.findByBookingId(b.getId());
            if (mods.isEmpty()) {
                history.add(new Paragraph("No modification history available."));
            } else {
                // Gruppiere nach modifiedAt (erzeugt pro Batch eine Gruppe)
                java.util.Map<java.time.LocalDateTime, java.util.List<com.hotel.booking.entity.BookingModification>> grouped =
                        mods.stream().collect(java.util.stream.Collectors.groupingBy(com.hotel.booking.entity.BookingModification::getModifiedAt, java.util.LinkedHashMap::new, java.util.stream.Collectors.toList()));

                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

                for (java.util.Map.Entry<java.time.LocalDateTime, java.util.List<com.hotel.booking.entity.BookingModification>> entry : grouped.entrySet()) {
                    java.time.LocalDateTime ts = entry.getKey();
                    java.util.List<com.hotel.booking.entity.BookingModification> group = entry.getValue();

                    VerticalLayout groupBox = new VerticalLayout();
                    groupBox.getStyle().set("padding", "8px");
                    groupBox.getStyle().set("margin-bottom", "6px");
                    groupBox.getStyle().set("border", "1px solid #eee");

                    // Kopfzeile: Zeitpunkt + Bearbeiter (erster nicht-null)
                    String who = "system";
                    for (com.hotel.booking.entity.BookingModification m : group) {
                        if (m.getHandledBy() != null) {
                            who = (m.getHandledBy().getFullName() != null && !m.getHandledBy().getFullName().isBlank()) ? m.getHandledBy().getFullName() : m.getHandledBy().getEmail();
                            break;
                        }
                    }
                    groupBox.add(new Paragraph(ts.format(dtf) + " — " + who));

                    // Liste der Feld-Änderungen in der Gruppe
                    for (com.hotel.booking.entity.BookingModification m : group) {
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

        // Wenn es eine Stornierung gab, zeige diese prominent in der History (wer, wann, Grund, Gebühr)
        if (b.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(b.getId()).ifPresent(bc -> {
                    VerticalLayout cancelBox = new VerticalLayout();
                    cancelBox.getStyle().set("padding", "8px");
                    cancelBox.getStyle().set("margin-bottom", "6px");
                    cancelBox.getStyle().set("border", "1px solid #f5c6cb");

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

                    // Füge die Storno-Eintragung an den Anfang der History ein
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