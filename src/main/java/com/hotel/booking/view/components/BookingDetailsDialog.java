package com.hotel.booking.view.components;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.service.BookingCancellationService;
import com.hotel.booking.service.BookingModificationService;
import com.hotel.booking.service.InvoiceService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

/**
 * Dialog component displaying detailed booking information with tabs for Details, Payments, History, and Extras.
 * 
 * @author Arman Özcanli
 */
@Component
public class BookingDetailsDialog {
    
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter GERMAN_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    
    private final BookingModificationService modificationService;
    private final BookingCancellationService bookingCancellationService;
    private final InvoiceService invoiceService;
    
    public BookingDetailsDialog(BookingModificationService modificationService,
                                BookingCancellationService bookingCancellationService,
                                InvoiceService invoiceService) {
        this.modificationService = modificationService;
        this.bookingCancellationService = bookingCancellationService;
        this.invoiceService = invoiceService;
    }
    
    /**
     * Opens a dialog with detailed booking information.
     * 
     * @param booking the booking to display
     */
    public void open(Booking booking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Booking Details - " + booking.getBookingNumber());
        dialog.setWidth("800px");

        Tabs tabs = new Tabs(new Tab("Details"), new Tab("Payments"), new Tab("History"), new Tab("Extras"));

        // Details tab content
        Div details = createDetailsTab(booking);

        // Payments tab
        Div payments = createPaymentsTab(booking);

        // History tab
        Div history = createHistoryTab(booking);

        // Extras tab
        Div extras = createExtrasTab(booking);

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

        Button close = new Button("Close", e -> dialog.close());
        close.addClassName("primary-button");

        dialog.add(new VerticalLayout(tabs, pages));
        dialog.getFooter().add(close);
        dialog.open();
    }
    
    /**
     * Creates the Details tab content.
     */
    private Div createDetailsTab(Booking booking) {
        Div details = new Div();
        details.add(new Paragraph("Guest Name: " + (booking.getGuest() != null ? booking.getGuest().getFullName() : "N/A")));
        details.add(new Paragraph("Booking Number: " + booking.getBookingNumber()));
        details.add(new Paragraph("Check-in: " + booking.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Check-out: " + booking.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Guests: " + booking.getAmount()));
        details.add(new Paragraph("Status: " + booking.getStatus()));

        // Price details
        String pricePerNightText = calculatePricePerNight(booking);
        if (pricePerNightText != null) {
            details.add(new Paragraph("Price per Night: " + pricePerNightText));
        }
        String totalPriceText = booking.getTotalPrice() != null ? String.format("%.2f €", booking.getTotalPrice()) : "-";
        details.add(new Paragraph("Total Price: " + totalPriceText));

        // Cancellation policy
        Div cancellationPolicy = new Div();
        cancellationPolicy.addClassName("booking-detail-section");
        cancellationPolicy.addClassName("booking-cancellation-policy");
        
        Paragraph policyTitle = new Paragraph("Cancellation Policy");
        policyTitle.addClassName("booking-detail-title");
        cancellationPolicy.add(policyTitle);
        
        cancellationPolicy.add(new Paragraph("• More than 30 days before check-in: Free cancellation"));
        cancellationPolicy.add(new Paragraph("• 7-29 days before check-in: 20% cancellation fee"));
        cancellationPolicy.add(new Paragraph("• 1-6 days before check-in: 50% cancellation fee"));
        cancellationPolicy.add(new Paragraph("• On check-in day: 100% cancellation fee (no refund)"));
        
        details.add(cancellationPolicy);

        // Show cancellation info if already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED && booking.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(booking.getId()).ifPresent(bc -> {
                    if (bc.getCancellationFee() != null) {
                        details.add(new Paragraph("Cancellation Fee: " + String.format("%.2f €", bc.getCancellationFee())));
                    }
                    if (bc.getReason() != null && !bc.getReason().isBlank()) {
                        details.add(new Paragraph("Cancellation Reason: " + bc.getReason()));
                    }
                });
            } catch (Exception ex) {
                // ignore read errors to keep dialog usable
            }
        }
        
        return details;
    }
    
    /**
     * Creates the Payments tab content.
     */
    private Div createPaymentsTab(Booking booking) {
        Div payments = new Div();
        // Use InvoiceService to handle inverted relationship (Invoice owns booking_id FK)
        if (booking.getId() != null) {
            invoiceService.findByBookingId(booking.getId()).ifPresentOrElse(
                invoice -> {
                    payments.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()));
                    payments.add(new Paragraph("Amount: " + String.format("%.2f €", invoice.getAmount())));
                    payments.add(new Paragraph("Status: " + invoice.getInvoiceStatus().toString()));
                    payments.add(new Paragraph("Payment Method: " + invoice.getPaymentMethod().toString()));
                    if (invoice.getIssuedAt() != null) {
                        payments.add(new Paragraph("Issued: " + invoice.getIssuedAt().format(
                                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
                    }
                },
                () -> payments.add(new Paragraph("Payment information not available"))
            );
        } else {
            payments.add(new Paragraph("Payment information not available"));
        }
        return payments;
    }
    
    /**
     * Creates the History tab content.
     */
    private Div createHistoryTab(Booking booking) {
        Div history = new Div();
        if (booking.getId() != null) {
            List<BookingModification> mods = modificationService.findByBookingId(booking.getId());
            if (mods.isEmpty()) {
                history.add(new Paragraph("No modification history available."));
            } else {
                Map<java.time.LocalDateTime, List<BookingModification>> grouped =
                        mods.stream().collect(Collectors.groupingBy(
                                BookingModification::getModifiedAt,
                                java.util.LinkedHashMap::new,
                                Collectors.toList()));

                for (Map.Entry<java.time.LocalDateTime, List<BookingModification>> entry : grouped.entrySet()) {
                    java.time.LocalDateTime ts = entry.getKey();
                    List<BookingModification> group = entry.getValue();

                    VerticalLayout groupBox = new VerticalLayout();
                    groupBox.addClassName("booking-history-group");

                    String who = "system";
                    for (BookingModification m : group) {
                        if (m.getHandledBy() != null) {
                            who = (m.getHandledBy().getFullName() != null && !m.getHandledBy().getFullName().isBlank())
                                    ? m.getHandledBy().getFullName()
                                    : m.getHandledBy().getEmail();
                            break;
                        }
                    }
                    groupBox.add(new Paragraph(ts.format(GERMAN_DATETIME_FORMAT) + " — " + who));

                    for (BookingModification m : group) {
                        HorizontalLayout row = new HorizontalLayout();
                        row.setWidthFull();
                        Paragraph field = new Paragraph(m.getFieldChanged() + ": ");
                        field.addClassName("booking-history-field");
                        Paragraph values = new Paragraph(
                                (m.getOldValue() != null ? m.getOldValue() : "<null>") + " → " +
                                (m.getNewValue() != null ? m.getNewValue() : "<null>"));
                        values.addClassName("booking-history-value");
                        row.add(field, values);
                        groupBox.add(row);
                        if (m.getReason() != null && !m.getReason().isBlank()) {
                            Span note = new Span("Reason: " + m.getReason());
                            note.addClassName("booking-history-reason");
                            groupBox.add(note);
                        }
                    }

                    history.add(groupBox);
                }
            }
        } else {
            history.add(new Paragraph("No modification history available."));
        }

        // Add cancellation info to history if exists
        if (booking.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(booking.getId()).ifPresent(bc -> {
                    VerticalLayout cancelBox = new VerticalLayout();
                    cancelBox.addClassName("booking-history-group");
                    cancelBox.addClassName("booking-history-cancellation");

                    String who = "guest";
                    if (bc.getHandledBy() != null) {
                        who = bc.getHandledBy().getFullName() != null && !bc.getHandledBy().getFullName().isBlank()
                                ? bc.getHandledBy().getFullName()
                                : bc.getHandledBy().getEmail();
                    }

                    cancelBox.add(new Paragraph(bc.getCancelledAt().format(GERMAN_DATETIME_FORMAT) + " — " + who + " (cancellation)"));
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
        
        return history;
    }
    
    /**
     * Creates the Extras tab content.
     */
    private Div createExtrasTab(Booking booking) {
        Div extras = new Div();
        if (booking.getExtras() == null || booking.getExtras().isEmpty()) {
            extras.add(new Paragraph("No additional services requested"));
        } else {
            for (BookingExtra extra : booking.getExtras()) {
                Div extraItem = new Div();
                extraItem.add(new Paragraph(extra.getName() + " - " + String.format("%.2f €", extra.getPrice())));
                if (extra.getDescription() != null && !extra.getDescription().isBlank()) {
                    Paragraph desc = new Paragraph(extra.getDescription());
                    desc.addClassName("booking-extra-description");
                    extraItem.add(desc);
                }
                extras.add(extraItem);
            }
        }
        return extras;
    }
    
    /**
     * Calculates price per night for display.
     */
    private String calculatePricePerNight(Booking booking) {
        if (booking.getRoomCategory() != null && booking.getRoomCategory().getPricePerNight() != null) {
            return String.format("%.2f €", booking.getRoomCategory().getPricePerNight());
        } else if (booking.getRoom() != null && booking.getRoom().getCategory() != null 
                && booking.getRoom().getCategory().getPricePerNight() != null) {
            return String.format("%.2f €", booking.getRoom().getCategory().getPricePerNight());
        }
        return null;
    }
}
