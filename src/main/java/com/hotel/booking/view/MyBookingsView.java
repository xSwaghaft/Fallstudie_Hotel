package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingModificationService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.*;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.service.BookingCancellationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// @Route: registriert die View unter /my-bookings im MainLayout.
// @CssImport: bindet globale und Guest-spezifische Styles ein.
@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My Bookings")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
public class MyBookingsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService formService;
    private final BookingModificationService modificationService;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Tabs tabs;
    private Div contentArea;
    private List<Booking> allBookings;
    // Cancellation data persisted via BookingCancellation entity (use BookingCancellationService)
    private final BookingCancellationService bookingCancellationService;

    @Autowired
    public MyBookingsView(SessionService sessionService, BookingService bookingService, BookingFormService formService, BookingModificationService modificationService, BookingCancellationService bookingCancellationService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.formService = formService;
        this.modificationService = modificationService;
        this.bookingCancellationService = bookingCancellationService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        setWidthFull();
        getStyle().set("overflow-x", "hidden");

        allBookings = loadAllBookingsForCurrentUser();

        add(new H1("Meine Buchungen"));
        
        if (allBookings.isEmpty()) {
            add(new Paragraph("Keine Buchungen gefunden."));
        } else {
            createTabsAndContent();
        }
    }
    
    // Erstellt die Tabs (Bevorstehend/Vergangen/Storniert) und den Content-Bereich.
    private void createTabsAndContent() {
        Tab upcomingTab = new Tab("Bevorstehend");
        Tab pastTab = new Tab("Vergangen");
        Tab cancelledTab = new Tab("Storniert");
        
        tabs = new Tabs(upcomingTab, pastTab, cancelledTab);
        tabs.addClassName("bookings-tabs");
        tabs.addSelectedChangeListener(e -> updateContent());
        
        contentArea = new Div();
        contentArea.addClassName("bookings-content-area");
        contentArea.setWidthFull();
        
        add(tabs, contentArea);
        updateContent();
    }
    
    // Filtert Buchungen je nach gewähltem Tab und rendert die Kartenliste.
    private void updateContent() {
        contentArea.removeAll();
        
        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab == null) {
            return;
        }
        
        String tabLabel = selectedTab.getLabel();
        List<Booking> filteredBookings;
        
        LocalDate today = LocalDate.now();
        
        switch (tabLabel) {
            case "Bevorstehend":
                // Buchungen, die noch nicht begonnen haben ODER gerade laufen
                filteredBookings = allBookings.stream()
                    .filter(b -> b.getCheckInDate().isAfter(today) || 
                                (!b.getCheckInDate().isAfter(today) && !b.getCheckOutDate().isBefore(today)))
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
                break;
            case "Vergangen":
                filteredBookings = allBookings.stream()
                    .filter(b -> b.getCheckOutDate().isBefore(today))
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
                break;
            case "Storniert":
                filteredBookings = allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
                break;
            default:
                filteredBookings = new ArrayList<>();
        }
        
        if (filteredBookings.isEmpty()) {
            Paragraph emptyMessage = new Paragraph("Keine Buchungen in dieser Kategorie.");
            emptyMessage.getStyle().set("padding", "var(--spacing-xl)");
            emptyMessage.getStyle().set("text-align", "center");
            emptyMessage.getStyle().set("color", "var(--color-text-secondary)");
            contentArea.add(emptyMessage);
        } else {
            VerticalLayout bookingsLayout = new VerticalLayout();
            bookingsLayout.setSpacing(true);
            bookingsLayout.setPadding(false);
            
            for (Booking booking : filteredBookings) {
                bookingsLayout.add(createBookingItem(booking, tabLabel));
            }
            
            contentArea.add(bookingsLayout);
        }
    }

    // Baut eine einzelne Buchungskarte inkl. Buttons und klickbarem Detailbereich.
    private Div createBookingItem(Booking booking, String tabLabel) {
        Div card = new Div();
        card.addClassName("booking-item-card");
        
        // Hauptbereich klickbar machen
        Div clickableArea = new Div();
        clickableArea.getStyle().set("cursor", "pointer");
        clickableArea.addClickListener(e -> openBookingDetailsDialog(booking));
        
        String roomType = booking.getRoomCategory() != null
                ? booking.getRoomCategory().getName()
                : "Room";
        String roomNumber = booking.getRoom() != null
                ? booking.getRoom().getRoomNumber()
                : "-";
        
        Div header = new Div();
        header.addClassName("booking-item-header");
        
        H3 bookingNumber = new H3(booking.getBookingNumber());
        bookingNumber.addClassName("booking-item-number");
        
        Span statusBadge = new Span(booking.getStatus().toString());
        statusBadge.addClassName("booking-item-status");
        statusBadge.addClassName(booking.getStatus().toString().toLowerCase());
        
        header.add(bookingNumber, statusBadge);
        
        Div details = new Div();
        details.addClassName("booking-item-details");
        
        details.add(createDetailItem("Zimmer", roomType + " - " + roomNumber));
        details.add(createDetailItem("Check-in", booking.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(createDetailItem("Check-out", booking.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(createDetailItem("Gäste", booking.getAmount() != null ? String.valueOf(booking.getAmount()) : "-"));
        // Wenn bereits storniert: Suche letzte BookingCancellation und zeige die berechnete Strafe an
        if (booking.getStatus() == BookingStatus.CANCELLED && booking.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(booking.getId()).ifPresent(bc -> {
                    if (bc.getCancellationFee() != null) {
                        details.add(createDetailItem("Strafe", String.format("%.2f €", bc.getCancellationFee())));
                    }
                });
            } catch (Exception ex) {
                // Fehler beim Lesen der Storno-Info darf die Karte nicht komplett brechen
            }
        }
        
        // Berechne Preis pro Nacht für Anzeige
        String pricePerNightText = calculatePricePerNight(booking);
        if (pricePerNightText != null) {
            details.add(createDetailItem("Preis pro Nacht", pricePerNightText));
        }
        
        // Gesamtpreis
        String totalPriceText = "-";
        if (booking.getTotalPrice() != null) {
            totalPriceText = String.format("%.2f €", booking.getTotalPrice());
        }
        
        H3 price = new H3("Gesamtpreis: " + totalPriceText);
        price.addClassName("booking-item-price");
        
        // Klickbarer Bereich
        clickableArea.add(header, details, price);
        
        // Buttons basierend auf Tab
        Div buttonsContainer = new Div();
        buttonsContainer.addClassName("booking-item-buttons");
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        // Note: details remain available via clicking the card's main area

            if ("Bevorstehend".equals(tabLabel)) {
            Button modifyButton = new Button("Bearbeiten");
            modifyButton.addClassName("primary-button");
            // Edit-Handler: öffnet das Formular mit der bestehenden Buchung
            modifyButton.addClickListener(e -> {
                // Erstelle das Formular im Edit-Modus (bestehende Buchung übergeben)
                createNewBookingForm form = new createNewBookingForm(sessionService.getCurrentUser(), sessionService, booking, formService);

                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Buchung bearbeiten");
                dialog.setWidth("600px");

                // Snapshot alte Werte bevor Binder schreibt (AtomicReference um Lambda-Capture zu umgehen)
                final java.util.concurrent.atomic.AtomicReference<java.time.LocalDate> prevCheckInRef = new java.util.concurrent.atomic.AtomicReference<>(booking.getCheckInDate());
                final java.util.concurrent.atomic.AtomicReference<java.time.LocalDate> prevCheckOutRef = new java.util.concurrent.atomic.AtomicReference<>(booking.getCheckOutDate());
                final java.util.concurrent.atomic.AtomicReference<Integer> prevAmountRef = new java.util.concurrent.atomic.AtomicReference<>(booking.getAmount());
                final java.util.concurrent.atomic.AtomicReference<java.math.BigDecimal> prevTotalRef = new java.util.concurrent.atomic.AtomicReference<>(booking.getTotalPrice());
                final java.util.concurrent.atomic.AtomicReference<java.util.Set<com.hotel.booking.entity.BookingExtra>> prevExtrasRef = new java.util.concurrent.atomic.AtomicReference<>(booking.getExtras());

                Button saveBtn = new Button("Speichern", ev -> {
                    try {
                        form.writeBean();
                        Booking updated = form.getBooking();
                        bookingService.calculateBookingPrice(updated);

                        // Preview Dialog
                        Dialog preview = new Dialog();
                        preview.setHeaderTitle("Änderungen bestätigen");
                        VerticalLayout content = new VerticalLayout();
                        content.add(new Paragraph("-- Vorher --"));
                        content.add(new Paragraph("Check-in: " + (prevCheckInRef.get() != null ? prevCheckInRef.get().format(GERMAN_DATE_FORMAT) : "N/A")));
                        content.add(new Paragraph("Check-out: " + (prevCheckOutRef.get() != null ? prevCheckOutRef.get().format(GERMAN_DATE_FORMAT) : "N/A")));
                        content.add(new Paragraph("Gäste: " + (prevAmountRef.get() != null ? prevAmountRef.get() : "N/A")));
                        content.add(new Paragraph("Preis: " + (prevTotalRef.get() != null ? prevTotalRef.get().toString() : "N/A")));
                        String prevExtrasStr = "none";
                        if (prevExtrasRef.get() != null && !prevExtrasRef.get().isEmpty()) {
                            prevExtrasStr = prevExtrasRef.get().stream().map(x -> x.getName()).collect(java.util.stream.Collectors.joining(", "));
                        }
                        content.add(new Paragraph("Extras: " + prevExtrasStr));

                        content.add(new Paragraph("-- Nachher --"));
                        content.add(new Paragraph("Check-in: " + (updated.getCheckInDate() != null ? updated.getCheckInDate().format(GERMAN_DATE_FORMAT) : "N/A")));
                        content.add(new Paragraph("Check-out: " + (updated.getCheckOutDate() != null ? updated.getCheckOutDate().format(GERMAN_DATE_FORMAT) : "N/A")));
                        content.add(new Paragraph("Gäste: " + (updated.getAmount() != null ? updated.getAmount() : "N/A")));
                        content.add(new Paragraph("Preis: " + (updated.getTotalPrice() != null ? updated.getTotalPrice().toString() : "N/A")));
                        String newExtrasStr = "none";
                        if (updated.getExtras() != null && !updated.getExtras().isEmpty()) {
                            newExtrasStr = updated.getExtras().stream().map(x -> x.getName()).collect(java.util.stream.Collectors.joining(", "));
                        }
                        content.add(new Paragraph("Extras: " + newExtrasStr));

                        Button confirm = new Button("Bestätigen", confirmEv -> {
                            try {
                                // Protokolliere alte Werte
                                modificationService.recordChangesFromSnapshot(booking,
                                        prevCheckInRef.get(), prevCheckOutRef.get(), prevAmountRef.get(), prevTotalRef.get(), prevExtrasRef.get(),
                                        updated, sessionService.getCurrentUser(), null);

                                bookingService.save(updated);
                                dialog.close();
                                preview.close();
                                // Refresh lokal geladene Liste und UI
                                allBookings = loadAllBookingsForCurrentUser();
                                updateContent();
                                Notification.show("Buchung erfolgreich aktualisiert.", 3000, Notification.Position.BOTTOM_START);
                            } catch (Exception ex) {
                                Notification.show(ex.getMessage() != null ? ex.getMessage() : "Fehler beim Speichern", 5000, Notification.Position.MIDDLE);
                            }
                        });
                        Button back = new Button("Zurück", backEv -> preview.close());
                        preview.add(content, new HorizontalLayout(confirm, back));
                        preview.open();

                    } catch (ValidationException ex) {
                        Notification.show("Bitte korrigiere Validierungsfehler.", 3000, Notification.Position.MIDDLE);
                    }
                });

                Button cancelBtn = new Button("Abbrechen", ev -> dialog.close());
                dialog.add(form, new HorizontalLayout(saveBtn, cancelBtn));
                dialog.open();
            });

                // Cancel button only available to guest when booking is PENDING or MODIFIED and not paid
                if ((booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.MODIFIED)
                    && (booking.getInvoice() == null || booking.getInvoice().getInvoiceStatus() != Invoice.PaymentStatus.PAID)) {
                Button cancelButton = new Button("Stornieren");
                cancelButton.addClassName("secondary-button");
                // Stornierungs-Handler: prüft 48h-Regel, zeigt Bestätigung (inkl. Strafe) und speichert lokal die Strafe
                cancelButton.addClickListener(e -> {
                    // Prevent cancelling if invoice was paid in the meantime (extra safety)
                    if (booking.getInvoice() != null && booking.getInvoice().getInvoiceStatus() == Invoice.PaymentStatus.PAID) {
                        Notification.show("Diese Buchung wurde bereits bezahlt und kann nicht storniert werden.", 4000, Notification.Position.MIDDLE);
                        return;
                    }
                try {
                    // Prüfe, ob Storno innerhalb von 48 Stunden vor Check-in liegt
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    java.time.LocalDateTime checkInAtStart = booking.getCheckInDate().atStartOfDay();
                    long hoursBefore = java.time.Duration.between(now, checkInAtStart).toHours();

                    java.math.BigDecimal penalty = java.math.BigDecimal.ZERO;
                    boolean hasPenalty = false;
                    if (booking.getTotalPrice() != null && hoursBefore < 48) {
                        penalty = booking.getTotalPrice().multiply(new java.math.BigDecimal("0.5"));
                        // Runde auf 2 Nachkommastellen
                        penalty = penalty.setScale(2, java.math.RoundingMode.HALF_UP);
                        hasPenalty = true;
                    }

                    if (hasPenalty) {
                        final java.math.BigDecimal penaltyFinal = penalty;
                        Dialog confirm = new Dialog();
                        confirm.setHeaderTitle("Stornierung bestätigen");
                        VerticalLayout cnt = new VerticalLayout();
                        cnt.add(new Paragraph("Du stornierst weniger als 48 Stunden vor Check-in."));
                        cnt.add(new Paragraph("Es fällt eine Strafe in Höhe von 50% des Gesamtpreises an: " + String.format("%.2f €", penaltyFinal)));
                        cnt.add(new Paragraph("Möchtest du die Stornierung mit der Strafe bestätigen?"));

                        Button confirmBtn = new Button("Bestätigen", ev -> {
                            try {
                                booking.setStatus(BookingStatus.CANCELLED);
                                bookingService.save(booking);

                                // Persistiere die Stornierungsinformation über BookingCancellation
                                BookingCancellation bc = new BookingCancellation();
                                bc.setBooking(booking);
                                bc.setCancelledAt(java.time.LocalDateTime.now());
                                bc.setReason("Storniert vom Gast innerhalb 48 Stunden");
                                bc.setCancellationFee(penaltyFinal);
                                // set handledBy to current user if available
                                User current = sessionService.getCurrentUser();
                                if (current != null) {
                                    bc.setHandledBy(current);
                                }
                                bookingCancellationService.save(bc);

                                confirm.close();
                                // Refresh lokal geladene Liste und UI
                                allBookings = loadAllBookingsForCurrentUser();
                                updateContent();
                                Notification.show("Buchung storniert. Strafe: " + String.format("%.2f €", penaltyFinal), 4000, Notification.Position.BOTTOM_START);
                            } catch (Exception ex) {
                                Notification.show(ex.getMessage() != null ? ex.getMessage() : "Fehler beim Stornieren", 5000, Notification.Position.MIDDLE);
                            }
                        });

                        Button backBtn = new Button("Zurück", ev -> confirm.close());
                        confirm.add(cnt, new HorizontalLayout(confirmBtn, backBtn));
                        confirm.open();
                    } else {
                        // Kein Straf-Fall -> normale Bestätigung
                        Dialog confirm = new Dialog();
                        confirm.setHeaderTitle("Stornierung bestätigen");
                        confirm.add(new Paragraph("Möchtest du die Buchung wirklich stornieren?"));
                        Button confirmBtn = new Button("Ja, stornieren", ev -> {
                            try {
                                booking.setStatus(BookingStatus.CANCELLED);
                                bookingService.save(booking);

                                // Persistiere den Cancellation-Eintrag (ohne Strafe)
                                BookingCancellation bc = new BookingCancellation();
                                bc.setBooking(booking);
                                bc.setCancelledAt(java.time.LocalDateTime.now());
                                bc.setReason("Storniert vom Gast");
                                bc.setCancellationFee(java.math.BigDecimal.ZERO);
                                User current = sessionService.getCurrentUser();
                                if (current != null) {
                                    bc.setHandledBy(current);
                                }
                                bookingCancellationService.save(bc);

                                allBookings = loadAllBookingsForCurrentUser();
                                updateContent();
                                Notification.show("Buchung wurde storniert.", 3000, Notification.Position.BOTTOM_START);
                                confirm.close();
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
            });

                buttonsLayout.add(modifyButton, cancelButton);
            } else {
                // If cancel button not available, only show modify (if applicable)
                buttonsLayout.add(modifyButton);
            }
        } else if ("Vergangen".equals(tabLabel)) {
            RouterLink reviewLink = new RouterLink("Review schreiben", MyReviewsView.class);
            reviewLink.addClassName("primary-button");
            buttonsLayout.add(reviewLink);
        }
        
        buttonsContainer.add(buttonsLayout);
        
        card.add(clickableArea, buttonsContainer);
        return card;
    }
    
    // Hilfsmethode: Label/Value-Paar für Details innerhalb der Karte.
    private Div createDetailItem(String label, String value) {
        Div item = new Div();
        item.addClassName("booking-item-detail");
        
        Span labelSpan = new Span(label);
        labelSpan.addClassName("booking-item-detail-label");
        
        Span valueSpan = new Span(value);
        valueSpan.addClassName("booking-item-detail-value");
        
        item.add(labelSpan, valueSpan);
        return item;
    }
    
    // Ermittelt den Anzeigenwert für Preis pro Nacht aus Kategorie oder Zimmer.
    private String calculatePricePerNight(Booking booking) {
        if (booking.getRoomCategory() != null && booking.getRoomCategory().getPricePerNight() != null) {
            return String.format("%.2f €", booking.getRoomCategory().getPricePerNight());
        } else if (booking.getRoom() != null && booking.getRoom().getCategory() != null 
                && booking.getRoom().getCategory().getPricePerNight() != null) {
            return String.format("%.2f €", booking.getRoom().getCategory().getPricePerNight());
        }
        return null;
    }

    // Lädt alle Buchungen des aktuellen Nutzers.
    private List<Booking> loadAllBookingsForCurrentUser() {
        User user = sessionService.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        return bookingService.findAllBookingsForGuest(user.getId());
    }

    // Öffnet ein Dialogfenster mit allen Details, Extras und Rechnung zur Buchung.
    private void openBookingDetailsDialog(Booking booking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Buchungsdetails - " + booking.getBookingNumber());
        dialog.setWidth("800px");

        Tabs tabs = new Tabs(new Tab("Details"), new Tab("Payments"), new Tab("History"), new Tab("Extras"));

        // Details tab content
        Div details = new Div();
        details.add(new Paragraph("Guest Name: " + (booking.getGuest() != null ? booking.getGuest().getFullName() : "N/A")));
        details.add(new Paragraph("Booking Number: " + booking.getBookingNumber()));
        details.add(new Paragraph("Check-in: " + booking.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Check-out: " + booking.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Guests: " + booking.getAmount()));
        details.add(new Paragraph("Status: " + booking.getStatus()));

        // Price details included in Details tab
        String pricePerNightText = calculatePricePerNight(booking);
        if (pricePerNightText != null) {
            details.add(new Paragraph("Preis pro Nacht: " + pricePerNightText));
        }
        String totalPriceText = booking.getTotalPrice() != null ? String.format("%.2f €", booking.getTotalPrice()) : "-";
        details.add(new Paragraph("Gesamtpreis: " + totalPriceText));

        // Cancellation policy shown in Details as before
        details.add(new Paragraph("Stornobedingungen: Stornierungen bis 48 Stunden vor Check-in sind kostenfrei. Bei späteren Stornierungen werden 50% des Gesamtpreises berechnet."));

        // Wenn die Buchung bereits storniert wurde, zeige die gespeicherte Stornogebühr und Grund an (falls vorhanden)
        if (booking.getStatus() == BookingStatus.CANCELLED && booking.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(booking.getId()).ifPresent(bc -> {
                    if (bc.getCancellationFee() != null) {
                        details.add(new Paragraph("Stornogebühr: " + String.format("%.2f €", bc.getCancellationFee())));
                    }
                    if (bc.getReason() != null && !bc.getReason().isBlank()) {
                        details.add(new Paragraph("Storno-Grund: " + bc.getReason()));
                    }
                });
            } catch (Exception ex) {
                // ignore read errors to keep dialog usable
            }
        }

        // Payments tab
        Div payments = new Div();
        if (booking.getInvoice() != null) {
            Invoice invoice = booking.getInvoice();
            payments.add(new Paragraph("Rechnungsnummer: " + invoice.getInvoiceNumber()));
            payments.add(new Paragraph("Betrag: " + String.format("%.2f €", invoice.getAmount())));
            payments.add(new Paragraph("Status: " + invoice.getInvoiceStatus().toString()));
            payments.add(new Paragraph("Zahlungsmethode: " + invoice.getPaymentMethod().toString()));
            if (invoice.getIssuedAt() != null) {
                payments.add(new Paragraph("Ausgestellt: " + invoice.getIssuedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
            }
        } else {
            payments.add(new Paragraph("Payment information not available"));
        }

        // History tab
        Div history = new Div();
        if (booking.getId() != null) {
            java.util.List<com.hotel.booking.entity.BookingModification> mods = modificationService.findByBookingId(booking.getId());
            if (mods.isEmpty()) {
                history.add(new Paragraph("No modification history available."));
            } else {
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

                    String who = "system";
                    for (com.hotel.booking.entity.BookingModification m : group) {
                        if (m.getHandledBy() != null) {
                            who = (m.getHandledBy().getFullName() != null && !m.getHandledBy().getFullName().isBlank()) ? m.getHandledBy().getFullName() : m.getHandledBy().getEmail();
                            break;
                        }
                    }
                    groupBox.add(new Paragraph(ts.format(dtf) + " — " + who));

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
        if (booking.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(booking.getId()).ifPresent(bc -> {
                    VerticalLayout cancelBox = new VerticalLayout();
                    cancelBox.getStyle().set("padding", "8px");
                    cancelBox.getStyle().set("margin-bottom", "6px");
                    cancelBox.getStyle().set("border", "1px solid #f5c6cb");

                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                    String who = "guest";
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

        // Extras tab
        Div extras = new Div();
        if (booking.getExtras() == null || booking.getExtras().isEmpty()) {
            extras.add(new Paragraph("No additional services requested"));
        } else {
            for (BookingExtra extra : booking.getExtras()) {
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

        Button close = new Button("Schließen", e -> dialog.close());
        close.addClassName("primary-button");

        dialog.add(new VerticalLayout(tabs, pages));
        dialog.getFooter().add(close);
        dialog.open();
    }

    // Zugriffsschutz: Nur eingeloggte Gäste dürfen die Seite sehen, sonst Login-Redirect.
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.GUEST)) {
            event.rerouteTo(LoginView.class);
        }
    }
}
