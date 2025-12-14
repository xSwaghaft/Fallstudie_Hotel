package com.hotel.booking.view.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.Invoice;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BookingDetailsDialog extends Dialog {

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public BookingDetailsDialog(Booking booking) {
        setHeaderTitle("Booking Details");
        setWidth("600px");
        setMaxWidth("90%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        Div headerSection = createDetailSection("Header");
        headerSection.add(new H2(getOrElse(booking.getBookingNumber(), "-")), createStatusBadge(booking));
        content.add(headerSection);

        content.add(createRoomSection(booking));
        content.add(createDatesSection(booking));
        content.add(createPriceSection(booking));

        if (booking.getExtras() != null && !booking.getExtras().isEmpty()) {
            content.add(createExtrasSection(booking));
        }

        if (booking.getInvoice() != null) {
            content.add(createInvoiceSection(booking.getInvoice()));
        }

        content.add(createCancellationSection());

        Button closeButton = new Button("Close", e -> close());
        closeButton.addClassName("primary-button");

        add(content);
        getFooter().add(closeButton);
    }

    // =========================================================
    // SECTION CREATORS
    // =========================================================

    private Div createDetailSection(String title) {
        Div section = createStyledDiv("booking-detail-section");
        if (!title.equals("Header")) {
            section.add(new H4(title));
        }
        return section;
    }

    private Div createRoomSection(Booking booking) {
        Div section = createDetailSection("Room");
        String roomType = booking.getRoomCategory() != null ? booking.getRoomCategory().getName() : "Room";
        String roomNumber = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "-";
        section.add(new Paragraph(roomType + " #" + roomNumber));
        return section;
    }

    private Div createDatesSection(Booking booking) {
        Div section = createDetailSection("Period & Guests");
        section.add(new Paragraph("Check-in: " + formatDate(booking.getCheckInDate())),
                   new Paragraph("Check-out: " + formatDate(booking.getCheckOutDate())),
                   new Paragraph("Guests: " + getOrElse(booking.getAmount(), "-")));
        return section;
    }

    private Div createPriceSection(Booking booking) {
        Div section = createDetailSection("Price");
        if (booking.getRoomCategory() != null && booking.getRoomCategory().getPricePerNight() != null) {
            section.add(new Paragraph("Price per night: " + formatMoney(booking.getRoomCategory().getPricePerNight())));
        }
        section.add(new Paragraph("Total price: " + formatMoney(booking.getTotalPrice())));
        return section;
    }

    private Div createExtrasSection(Booking booking) {
        Div section = createDetailSection("Extras");
        booking.getExtras().stream()
                .filter(extra -> extra != null)
                .forEach(extra -> {
                    Div extraItem = createStyledDiv("booking-extra-item");
                    extraItem.add(new Paragraph(extra.getName() + " - " + formatMoney(extra.getPrice())));
                    if (extra.getDescription() != null && !extra.getDescription().isEmpty()) {
                        Paragraph desc = new Paragraph(extra.getDescription());
                        desc.addClassName("booking-extra-description");
                        extraItem.add(desc);
                    }
                    section.add(extraItem);
                });
        return section;
    }

    private Div createInvoiceSection(Invoice invoice) {
        Div section = createDetailSection("Invoice");
        section.add(new Paragraph("Invoice number: " + invoice.getInvoiceNumber()),
                   new Paragraph("Amount: " + formatMoney(invoice.getAmount())),
                   new Paragraph("Status: " + invoice.getInvoiceStatus()),
                   new Paragraph("Payment method: " + invoice.getPaymentMethod()));
        if (invoice.getIssuedAt() != null) {
            section.add(new Paragraph("Issued: " + invoice.getIssuedAt().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
        }
        return section;
    }

    private Div createCancellationSection() {
        Div section = createDetailSection("Cancellation Policy");
        section.add(new Paragraph(
                "Cancellations up to 48 hours before check-in are free of charge. " +
                        "Later cancellations will be charged 50% of the total price."));
        return section;
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private Span createStatusBadge(Booking booking) {
        String statusText = String.valueOf(booking.getStatus());
        Span badge = new Span(statusText);
        badge.addClassName("booking-item-status");
        badge.addClassName(statusText.toLowerCase());
        return badge;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "-";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €";
    }

    private String formatMoney(Double value) {
        if (value == null) return "-";
        return formatMoney(BigDecimal.valueOf(value));
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(GERMAN_DATE_FORMAT) : "-";
    }

    private String getOrElse(Object value, String defaultValue) {
        return value != null ? String.valueOf(value) : defaultValue;
    }

    private Div createStyledDiv(String className) {
        Div div = new Div();
        div.addClassName(className);
        return div;
    }
}

