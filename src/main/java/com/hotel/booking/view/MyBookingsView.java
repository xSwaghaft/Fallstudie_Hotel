package com.hotel.booking.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My Bookings")
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

        // Live berechnen (nur Anzeige). Wenn du DB-Werte anzeigen willst -> diese Zeile löschen.
        allBookings.forEach(bookingService::calculateBookingPrice);

        add(new H1("Meine Buchungen"));

        if (allBookings.isEmpty()) {
            add(new Paragraph("Keine Buchungen gefunden."));
        } else {
            createTabsAndContent();
        }
    }

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

    private void updateContent() {
        contentArea.removeAll();

        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab == null) return;

        String tabLabel = selectedTab.getLabel();
        LocalDate today = LocalDate.now();

        List<Booking> filteredBookings;

        switch (tabLabel) {
            case "Bevorstehend":
                filteredBookings = allBookings.stream()
                        .filter(b -> b.getCheckInDate() != null && b.getCheckOutDate() != null)
                        .filter(b -> b.getCheckInDate().isAfter(today)
                                || (!b.getCheckInDate().isAfter(today) && !b.getCheckOutDate().isBefore(today)))
                        .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                        .collect(Collectors.toList());
                break;

            case "Vergangen":
                filteredBookings = allBookings.stream()
                        .filter(b -> b.getCheckOutDate() != null)
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
            return;
        }

        VerticalLayout bookingsLayout = new VerticalLayout();
        bookingsLayout.setSpacing(true);
        bookingsLayout.setPadding(false);

        for (Booking booking : filteredBookings) {
            bookingsLayout.add(createBookingItem(booking, tabLabel));
        }

        contentArea.add(bookingsLayout);
    }

    private Div createBookingItem(Booking booking, String tabLabel) {
        Div card = new Div();
        card.addClassName("booking-item-card");

        Div clickableArea = new Div();
        clickableArea.getStyle().set("cursor", "pointer");
        clickableArea.addClickListener(e -> openBookingDetailsDialog(booking));

        String roomType = (booking.getRoomCategory() != null) ? booking.getRoomCategory().getName() : "Room";
        String roomNumber = (booking.getRoom() != null) ? booking.getRoom().getRoomNumber() : "-";

        Div header = new Div();
        header.addClassName("booking-item-header");

        H3 bookingNumber = new H3(booking.getBookingNumber() != null ? booking.getBookingNumber() : "-");
        bookingNumber.addClassName("booking-item-number");

        String statusText = String.valueOf(booking.getStatus());
        Span statusBadge = new Span(statusText);
        statusBadge.addClassName("booking-item-status");
        statusBadge.addClassName(statusText.toLowerCase());

        header.add(bookingNumber, statusBadge);

        Div details = new Div();
        details.addClassName("booking-item-details");

        details.add(createDetailItem("Zimmer", roomType + " - " + roomNumber));
        details.add(createDetailItem("Check-in", booking.getCheckInDate() != null ? booking.getCheckInDate().format(GERMAN_DATE_FORMAT) : "-"));
        details.add(createDetailItem("Check-out", booking.getCheckOutDate() != null ? booking.getCheckOutDate().format(GERMAN_DATE_FORMAT) : "-"));
        details.add(createDetailItem("Gäste", booking.getAmount() != null ? String.valueOf(booking.getAmount()) : "-"));

        BigDecimal ppn = (booking.getRoomCategory() != null) ? booking.getRoomCategory().getPricePerNight() : null;
        if (ppn != null) {
            details.add(createDetailItem("Preis pro Nacht", formatMoney(ppn)));
        }

        H3 price = new H3("Gesamtpreis: " + formatMoney(booking.getTotalPrice()));
        price.addClassName("booking-item-price");

        clickableArea.add(header, details, price);

        Div buttonsContainer = new Div();
        buttonsContainer.addClassName("booking-item-buttons");

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        if ("Bevorstehend".equals(tabLabel)) {
            Button modifyButton = new Button("Bearbeiten");
            modifyButton.addClassName("primary-button");

            Button cancelButton = new Button("Stornieren");
            cancelButton.addClassName("secondary-button");

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

    private List<Booking> loadAllBookingsForCurrentUser() {
        User user = sessionService.getCurrentUser();
        if (user == null) return List.of();
        return bookingService.findAllBookingsForGuest(user.getId());
    }

    private void openBookingDetailsDialog(Booking booking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Buchungsdetails");
        dialog.setWidth("600px");
        dialog.setMaxWidth("90%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        // Header
        Div headerSection = new Div();
        headerSection.addClassName("booking-detail-section");

        H2 bookingNumber = new H2(booking.getBookingNumber() != null ? booking.getBookingNumber() : "-");

        String statusText = String.valueOf(booking.getStatus());
        Span statusBadge = new Span(statusText);
        statusBadge.addClassName("booking-item-status");
        statusBadge.addClassName(statusText.toLowerCase());

        headerSection.add(bookingNumber, statusBadge);
        content.add(headerSection);

        // Room
        Div roomSection = new Div();
        roomSection.addClassName("booking-detail-section");
        H4 roomTitle = new H4("Zimmer");

        String roomType = (booking.getRoomCategory() != null) ? booking.getRoomCategory().getName() : "Room";
        String roomNumberTxt = (booking.getRoom() != null) ? booking.getRoom().getRoomNumber() : "-";

        roomSection.add(roomTitle, new Paragraph(roomType + " #" + roomNumberTxt));
        content.add(roomSection);

        // Dates
        Div dateSection = new Div();
        dateSection.addClassName("booking-detail-section");
        H4 dateTitle = new H4("Zeitraum & Gäste");
        dateSection.add(dateTitle);

        dateSection.add(new Paragraph("Check-in: " + (booking.getCheckInDate() != null ? booking.getCheckInDate().format(GERMAN_DATE_FORMAT) : "-")));
        dateSection.add(new Paragraph("Check-out: " + (booking.getCheckOutDate() != null ? booking.getCheckOutDate().format(GERMAN_DATE_FORMAT) : "-")));
        dateSection.add(new Paragraph("Gäste: " + (booking.getAmount() != null ? booking.getAmount() : "-")));

        content.add(dateSection);

        // Price
        Div priceSection = new Div();
        priceSection.addClassName("booking-detail-section");
        H4 priceTitle = new H4("Preis");
        priceSection.add(priceTitle);

        BigDecimal ppn = (booking.getRoomCategory() != null) ? booking.getRoomCategory().getPricePerNight() : null;
        if (ppn != null) {
            priceSection.add(new Paragraph("Preis pro Nacht: " + formatMoney(ppn)));
        }
        priceSection.add(new Paragraph("Gesamtpreis: " + formatMoney(booking.getTotalPrice())));
        content.add(priceSection);

        // Extras
        if (booking.getExtras() != null && !booking.getExtras().isEmpty()) {
            Div extrasSection = new Div();
            extrasSection.addClassName("booking-detail-section");
            H4 extrasTitle = new H4("Extras");
            extrasSection.add(extrasTitle);

            for (BookingExtra extra : booking.getExtras()) {
                if (extra == null) continue;

                Div extraItem = new Div();
                extraItem.addClassName("booking-extra-item");

                extraItem.add(new Paragraph(extra.getName() + " - " + formatMoney(extra.getPrice())));

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

        // Invoice
        if (booking.getInvoice() != null) {
            Div invoiceSection = new Div();
            invoiceSection.addClassName("booking-detail-section");
            H4 invoiceTitle = new H4("Rechnung");
            invoiceSection.add(invoiceTitle);

            Invoice invoice = booking.getInvoice();
            invoiceSection.add(new Paragraph("Rechnungsnummer: " + invoice.getInvoiceNumber()));
            invoiceSection.add(new Paragraph("Betrag: " + formatMoney(invoice.getAmount())));
            invoiceSection.add(new Paragraph("Status: " + invoice.getInvoiceStatus()));
            invoiceSection.add(new Paragraph("Zahlungsmethode: " + invoice.getPaymentMethod()));

            if (invoice.getIssuedAt() != null) {
                invoiceSection.add(new Paragraph("Ausgestellt: " + invoice.getIssuedAt().format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                )));
            }

            content.add(invoiceSection);
        }

        // Cancellation
        Div cancellationSection = new Div();
        cancellationSection.addClassName("booking-detail-section");
        H4 cancellationTitle = new H4("Stornobedingungen");
        cancellationSection.add(cancellationTitle);
        cancellationSection.add(new Paragraph(
                "Stornierungen bis 48 Stunden vor Check-in sind kostenfrei. " +
                        "Bei späteren Stornierungen werden 50% des Gesamtpreises berechnet."
        ));
        content.add(cancellationSection);

        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addClassName("primary-button");

        dialog.add(content);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "-";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €";
    }

    private String formatMoney(Double value) {
        if (value == null) return "-";
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString() + " €";
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.GUEST)) {
            event.rerouteTo(LoginView.class);
        }
    }
}
