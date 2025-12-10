package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.*;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.Invoice;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// @Route: registriert die View unter /my-bookings im MainLayout.
// @CssImport: bindet globale und Guest-spezifische Styles ein.
@Route(value = "my-bookings", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
public class MyBookingsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Tabs tabs;
    private Div contentArea;
    private List<Booking> allBookings;

    @Autowired
    public MyBookingsView(SessionService sessionService, BookingService bookingService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
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
        
        if ("Bevorstehend".equals(tabLabel)) {
            Button modifyButton = new Button("Bearbeiten");
            modifyButton.addClassName("primary-button");
            // TODO: Logik für Bearbeitung
            
            Button cancelButton = new Button("Stornieren");
            cancelButton.addClassName("secondary-button");
            // TODO: Logik für Stornierung
            
            buttonsLayout.add(modifyButton, cancelButton);
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
        } else if (booking.getRoom() != null && booking.getRoom().getPrice() != null) {
            return String.format("%.2f €", booking.getRoom().getPrice());
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
        dialog.setHeaderTitle("Buchungsdetails");
        dialog.setWidth("600px");
        dialog.setMaxWidth("90%");
        
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        
        // Buchungsnummer und Status
        Div headerSection = new Div();
        headerSection.addClassName("booking-detail-section");
        H2 bookingNumber = new H2(booking.getBookingNumber());
        Span statusBadge = new Span(booking.getStatus().toString());
        statusBadge.addClassName("booking-item-status");
        statusBadge.addClassName(booking.getStatus().toString().toLowerCase());
        headerSection.add(bookingNumber, statusBadge);
        content.add(headerSection);
        
        // Zimmer-Informationen
        Div roomSection = new Div();
        roomSection.addClassName("booking-detail-section");
        H4 roomTitle = new H4("Zimmer");
        String roomType = booking.getRoomCategory() != null
                ? booking.getRoomCategory().getName()
                : "Room";
        String roomNumber = booking.getRoom() != null
                ? booking.getRoom().getRoomNumber()
                : "-";
        roomSection.add(roomTitle, new Paragraph(roomType + " #" + roomNumber));
        content.add(roomSection);
        
        // Datum und Gäste
        Div dateSection = new Div();
        dateSection.addClassName("booking-detail-section");
        H4 dateTitle = new H4("Zeitraum & Gäste");
        dateSection.add(dateTitle);
        dateSection.add(new Paragraph("Check-in: " + booking.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        dateSection.add(new Paragraph("Check-out: " + booking.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        dateSection.add(new Paragraph("Gäste: " + (booking.getAmount() != null ? booking.getAmount() : "-")));
        content.add(dateSection);
        
        // Preis
        Div priceSection = new Div();
        priceSection.addClassName("booking-detail-section");
        H4 priceTitle = new H4("Preis");
        priceSection.add(priceTitle);
        
        String pricePerNightText = calculatePricePerNight(booking);
        if (pricePerNightText != null) {
            priceSection.add(new Paragraph("Preis pro Nacht: " + pricePerNightText));
        }
        
        String totalPriceText = "-";
        if (booking.getTotalPrice() != null) {
            totalPriceText = String.format("%.2f €", booking.getTotalPrice());
        }
        priceSection.add(new Paragraph("Gesamtpreis: " + totalPriceText));
        content.add(priceSection);
        
        // Extras
        if (booking.getExtras() != null && !booking.getExtras().isEmpty()) {
            Div extrasSection = new Div();
            extrasSection.addClassName("booking-detail-section");
            H4 extrasTitle = new H4("Extras");
            extrasSection.add(extrasTitle);
            
            for (BookingExtra extra : booking.getExtras()) {
                Div extraItem = new Div();
                extraItem.addClassName("booking-extra-item");
                extraItem.add(new Paragraph(extra.getName() + " - " + String.format("%.2f €", extra.getPrice())));
                if (extra.getDescription() != null && !extra.getDescription().isEmpty()) {
                    Paragraph desc = new Paragraph(extra.getDescription());
                    desc.getStyle().set("font-size", "var(--font-size-sm)");
                    desc.getStyle().set("color", "var(--color-text-secondary)");
                    extraItem.add(desc);
                }
                extrasSection.add(extraItem);
            }
            content.add(extrasSection);
        }
        
        // Rechnung
        if (booking.getInvoice() != null) {
            Div invoiceSection = new Div();
            invoiceSection.addClassName("booking-detail-section");
            H4 invoiceTitle = new H4("Rechnung");
            invoiceSection.add(invoiceTitle);
            
            Invoice invoice = booking.getInvoice();
            invoiceSection.add(new Paragraph("Rechnungsnummer: " + invoice.getInvoiceNumber()));
            invoiceSection.add(new Paragraph("Betrag: " + String.format("%.2f €", invoice.getAmount())));
            invoiceSection.add(new Paragraph("Status: " + invoice.getInvoiceStatus().toString()));
            invoiceSection.add(new Paragraph("Zahlungsmethode: " + invoice.getPaymentMethod().toString()));
            if (invoice.getIssuedAt() != null) {
                invoiceSection.add(new Paragraph("Ausgestellt: " + invoice.getIssuedAt().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
            }
            content.add(invoiceSection);
        }
        
        // Stornobedingungen
        Div cancellationSection = new Div();
        cancellationSection.addClassName("booking-detail-section");
        H4 cancellationTitle = new H4("Stornobedingungen");
        cancellationSection.add(cancellationTitle);
        cancellationSection.add(new Paragraph("Stornierungen bis 48 Stunden vor Check-in sind kostenfrei. " +
            "Bei späteren Stornierungen werden 50% des Gesamtpreises berechnet."));
        content.add(cancellationSection);
        
        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addClassName("primary-button");
        
        dialog.add(content);
        dialog.getFooter().add(closeButton);
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
