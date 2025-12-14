package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Invoice;
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
@PageTitle("Booking Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/booking-management.css")
public class BookingManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService formService;
    private final com.hotel.booking.service.BookingModificationService modificationService;

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    Grid<Booking> grid = new Grid<>(Booking.class, false);

    public BookingManagementView(SessionService sessionService, BookingService bookingService, BookingFormService formService, com.hotel.booking.service.BookingModificationService modificationService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.formService = formService;
        this.modificationService = modificationService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

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
                // Wenn wir eine bestehende Buchung bearbeiten, legen wir Snapshot-Container an.
                // Erklärung: Binder schreibt möglicherweise direkt in dasselbe Objekt. Deshalb
                // speichern wir die alten Werte in AtomicReference-Containern, bevor wir überschreiben.
                final java.util.concurrent.atomic.AtomicReference<java.time.LocalDate> prevCheckInRef = new java.util.concurrent.atomic.AtomicReference<>();
                // Vorheriges Check-In-Datum
                final java.util.concurrent.atomic.AtomicReference<java.time.LocalDate> prevCheckOutRef = new java.util.concurrent.atomic.AtomicReference<>();
                // Vorheriges Check-Out-Datum
                final java.util.concurrent.atomic.AtomicReference<Integer> prevAmountRef = new java.util.concurrent.atomic.AtomicReference<>();
                // Vorherige Gästezahl
                final java.util.concurrent.atomic.AtomicReference<java.math.BigDecimal> prevTotalRef = new java.util.concurrent.atomic.AtomicReference<>();
                // Vorheriger Gesamtpreis
                final java.util.concurrent.atomic.AtomicReference<java.util.Set<com.hotel.booking.entity.BookingExtra>> prevExtrasRef = new java.util.concurrent.atomic.AtomicReference<>();
                // Vorherige Extras-Menge
                if (existingBooking != null) {
                    prevCheckInRef.set(existingBooking.getCheckInDate());
                    prevCheckOutRef.set(existingBooking.getCheckOutDate());
                    prevAmountRef.set(existingBooking.getAmount());
                    prevTotalRef.set(existingBooking.getTotalPrice());
                    prevExtrasRef.set(existingBooking.getExtras());
                }

                // Schreibe die Benutzer-Eingaben vom Formular in das Booking-Objekt.
                // Erklärung: `writeBean()` validiert und überträgt alle gebundenen Felder in `formBooking`.
                form.writeBean(); // Überträgt die Formulardaten in das Booking-Objekt
                Booking updated = form.getBooking();

                // Preis neu berechnen (nutze vorhandene Methode)
                // Erklärung: Die Methode berücksichtigt Anzahl Nächte, Kategoriepreis und Extras.
                bookingService.calculateBookingPrice(updated);

                // Erzeuge ein Vorschau-Dialogfenster, das „Vorher / Nachher“ zeigt.
                // Erklärung: Dies ist der Zweitschritt vor der finalen Bestätigung.
                Dialog preview = new Dialog();
                // Setze den passenden Titel je nachdem ob es sich um Edit oder Create handelt.
                preview.setHeaderTitle(existingBooking != null ? "Confirm Booking Changes" : "Confirm New Booking");
                // Inhaltliche Container für die Vorschau
                VerticalLayout content = new VerticalLayout();
                if (existingBooking != null) {
                    // Kopf: Kennzeichnung des „Vorher“-Blocks
                    content.add(new Paragraph("-- Before --"));
                    // Lese die Snapshot-Werte aus den AtomicReferences
                    java.time.LocalDate prevCheckIn = prevCheckInRef.get();
                    java.time.LocalDate prevCheckOut = prevCheckOutRef.get();
                    Integer prevAmount = prevAmountRef.get();
                    java.math.BigDecimal prevTotal = prevTotalRef.get();
                    java.util.Set<com.hotel.booking.entity.BookingExtra> prevExtras = prevExtrasRef.get();
                    // Füge die vorherigen Werte als Paragraphen hinzu (lesbares Format)
                    content.add(new Paragraph("Check-in: " + (prevCheckIn != null ? prevCheckIn.format(GERMAN_DATE_FORMAT) : "N/A")));
                    content.add(new Paragraph("Check-out: " + (prevCheckOut != null ? prevCheckOut.format(GERMAN_DATE_FORMAT) : "N/A")));
                    content.add(new Paragraph("Guests: " + (prevAmount != null ? prevAmount : "N/A")));
                    content.add(new Paragraph("Total Price: " + (prevTotal != null ? prevTotal.toString() : "N/A")));
                    // Baue eine lesbare String-Repräsentation der vorherigen Extras
                    String prevExtrasStr = "none";
                    if (prevExtras != null && !prevExtras.isEmpty()) {
                        // Mappe jedes Extra auf dessen Namen und join mit Komma
                        prevExtrasStr = prevExtras.stream().map(x -> x.getName()).collect(java.util.stream.Collectors.joining(", "));
                    }
                    content.add(new Paragraph("Extras: " + prevExtrasStr));
                }

                // After-Block: Zeigt die aktuell eingegebenen/neu berechneten Werte
                content.add(new Paragraph("-- After --"));
                content.add(new Paragraph("Check-in: " + (updated.getCheckInDate() != null ? updated.getCheckInDate().format(GERMAN_DATE_FORMAT) : "N/A")));
                content.add(new Paragraph("Check-out: " + (updated.getCheckOutDate() != null ? updated.getCheckOutDate().format(GERMAN_DATE_FORMAT) : "N/A")));
                content.add(new Paragraph("Guests: " + (updated.getAmount() != null ? updated.getAmount() : "N/A")));
                content.add(new Paragraph("Total Price: " + (updated.getTotalPrice() != null ? updated.getTotalPrice().toString() : "N/A")));
                // Baue die Darstellung der neuen Extras
                String newExtrasStr = "none";
                if (updated.getExtras() != null && !updated.getExtras().isEmpty()) {
                    // Mappe auf Namen und füge zu einem String zusammen
                    newExtrasStr = updated.getExtras().stream().map(x -> x.getName()).collect(java.util.stream.Collectors.joining(", "));
                }
                content.add(new Paragraph("Extras: " + newExtrasStr));

                Button confirm = new Button("Confirm", ev -> {
                    try {
                        // Wenn es eine bestehende Buchung war, zeichne die alten Werte in der Audit-Tabelle auf.
                        // Erklärung: `recordChangesFromSnapshot` nimmt die Snapshot-Werte und das nachher-Objekt,
                        // erstellt für jede Änderung einen BookingModification-Eintrag und persistiert ihn.
                        if (existingBooking != null) {
                                modificationService.recordChangesFromSnapshot(existingBooking,
                                    prevCheckInRef.get(), prevCheckOutRef.get(), prevAmountRef.get(), prevTotalRef.get(), prevExtrasRef.get(),
                                    updated, sessionService.getCurrentUser(), null);
                        }

                        // Speichere die geänderte/neue Buchung via Service
                        bookingService.save(updated);
                        // Schließe Dialoge nach erfolgreichem Speichern
                        dialog.close();
                        preview.close();
                        // Aktualisiere das Grid mit aktuellen Daten
                        grid.setItems(bookingService.findAll());
                        // Kurze Benachrichtigung über Erfolg
                        Notification.show("Booking saved successfully.", 3000, Notification.Position.BOTTOM_START);
                    } catch (Exception ex) {
                        // Falls beim Speichern ein Fehler auftritt, zeige eine aussagekräftige Nachricht
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
        card.setWidthFull(); // WICHTIG: Card nutzt volle Breite
        
        H3 title = new H3("Search & Filter");
        title.addClassName("booking-section-title");
        
        Paragraph subtitle = new Paragraph("Find specific bookings quickly");
        subtitle.addClassName("booking-subtitle");

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
        title.addClassName("booking-section-title");

        //Verwende das Feld-Grid (nicht lokal), damit Aktualisierungen sichtbar werden
        grid = new Grid<>(Booking.class, false);
        
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
        
        if (booking.getStatus() != null && "CONFIRMED".equals(booking.getStatus().name())) {
            Button checkInBtn = new Button("Check In", VaadinIcon.SIGN_IN.create());
            checkInBtn.addClickListener(e -> Notification.show("Checked in " + booking.getBookingNumber()));
            actions.add(checkInBtn);
        }
        
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
        details.add(new Paragraph("Check-in: " + b.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Check-out: " + b.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Guests: " + b.getAmount()));
        details.add(new Paragraph("Status: " + b.getStatus()));

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

        Button checkIn = new Button("Check In", e -> { d.close(); Notification.show("Checked in"); });
        Button edit = new Button("Edit Booking", e -> { d.close(); openAddBookingDialog(b); });
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