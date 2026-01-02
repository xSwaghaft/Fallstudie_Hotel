package com.hotel.booking.view.components;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingModificationService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.view.createNewBookingForm;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * Dialog component for editing a booking.
 * 
 * @author Arman Özcanli
 */
@Component
public class EditBookingDialog {
    
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    /** Default text for no extras */
    private static final String NO_EXTRAS_TEXT = "none";
    
    /** Default text for N/A values */
    private static final String NOT_AVAILABLE_TEXT = "N/A";
    
    /** Dialog width */
    private static final String DIALOG_WIDTH = "600px";
    
    private final SessionService sessionService;
    private final BookingFormService formService;
    private final BookingService bookingService;
    private final BookingModificationService modificationService;
    
    public EditBookingDialog(SessionService sessionService,
                            BookingFormService formService,
                            BookingService bookingService,
                            BookingModificationService modificationService) {
        this.sessionService = sessionService;
        this.formService = formService;
        this.bookingService = bookingService;
        this.modificationService = modificationService;
    }
    
    /**
     * Opens the edit booking dialog.
     * 
     * @param booking the booking to edit
     * @param onSuccess callback when booking is successfully updated
     */
    public void open(Booking booking, Runnable onSuccess) {
        createNewBookingForm form = new createNewBookingForm(
                sessionService.getCurrentUser(), sessionService, booking, formService);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Booking");
        dialog.setWidth(DIALOG_WIDTH);

        AtomicReference<LocalDate> prevCheckInRef = new AtomicReference<>(booking.getCheckInDate());
        AtomicReference<LocalDate> prevCheckOutRef = new AtomicReference<>(booking.getCheckOutDate());
        AtomicReference<Integer> prevAmountRef = new AtomicReference<>(booking.getAmount());
        AtomicReference<java.math.BigDecimal> prevTotalRef = new AtomicReference<>(booking.getTotalPrice());
        AtomicReference<Set<BookingExtra>> prevExtrasRef = new AtomicReference<>(booking.getExtras());

        Button saveBtn = new Button("Save", ev -> {
            try {
                form.writeBean();
                Booking updated = form.getBooking();
                bookingService.calculateBookingPrice(updated);

                Dialog preview = new Dialog();
                preview.setHeaderTitle("Confirm Changes");
                VerticalLayout content = new VerticalLayout();
                content.addClassName("booking-edit-preview");
                
                content.add(createPreviewSection("Before", prevCheckInRef.get(), prevCheckOutRef.get(), 
                        prevAmountRef.get(), prevTotalRef.get(), prevExtrasRef.get()));
                content.add(createPreviewSection("After", updated.getCheckInDate(), updated.getCheckOutDate(), 
                        updated.getAmount(), updated.getTotalPrice(), updated.getExtras()));

                Button confirm = new Button("Confirm", confirmEv -> {
                    try {
                        modificationService.recordChangesFromSnapshot(booking,
                                prevCheckInRef.get(), prevCheckOutRef.get(), prevAmountRef.get(), 
                                prevTotalRef.get(), prevExtrasRef.get(),
                                updated, sessionService.getCurrentUser(), null);

                        bookingService.save(updated);
                        dialog.close();
                        preview.close();
                        onSuccess.run();
                        Notification.show("Booking updated successfully.", 3000, Notification.Position.BOTTOM_START);
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error saving booking", 
                                5000, Notification.Position.MIDDLE);
                    }
                });
                confirm.addClassName("primary-button");
                
                Button back = new Button("Back", backEv -> preview.close());
                preview.add(content, new HorizontalLayout(confirm, back));
                preview.open();

            } catch (ValidationException ex) {
                Notification.show("Please fix validation errors.", 3000, Notification.Position.MIDDLE);
            }
        });
        saveBtn.addClassName("primary-button");

        Button cancelBtn = new Button("Cancel", ev -> dialog.close());
        dialog.add(form, new HorizontalLayout(saveBtn, cancelBtn));
        dialog.open();
    }
    
    /**
     * Creates a preview section showing booking details.
     * 
     * @param title the section title (e.g., "Before" or "After")
     * @param checkIn the check-in date
     * @param checkOut the check-out date
     * @param amount the number of guests
     * @param totalPrice the total price
     * @param extras the set of booking extras
     * @return a VerticalLayout containing the preview section
     */
    private VerticalLayout createPreviewSection(String title, LocalDate checkIn, LocalDate checkOut, 
                                                Integer amount, java.math.BigDecimal totalPrice, Set<BookingExtra> extras) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("booking-edit-preview-section");
        
        Paragraph titlePara = new Paragraph(title);
        titlePara.addClassName("booking-edit-preview-title");
        section.add(titlePara);
        
        section.add(new Paragraph("Check-in: " + formatDate(checkIn)));
        section.add(new Paragraph("Check-out: " + formatDate(checkOut)));
        section.add(new Paragraph("Guests: " + formatValue(amount)));
        section.add(new Paragraph("Price: " + formatPrice(totalPrice)));
        section.add(new Paragraph("Extras: " + formatExtras(extras)));
        
        return section;
    }
    
    /**
     * Formats a date for display.
     */
    private String formatDate(LocalDate date) {
        return date != null ? date.format(GERMAN_DATE_FORMAT) : NOT_AVAILABLE_TEXT;
    }
    
    /**
     * Formats a value for display.
     */
    private String formatValue(Object value) {
        return value != null ? value.toString() : NOT_AVAILABLE_TEXT;
    }
    
    /**
     * Formats a price for display.
     */
    private String formatPrice(java.math.BigDecimal price) {
        return price != null ? String.format("%.2f €", price) : NOT_AVAILABLE_TEXT;
    }
    
    /**
     * Formats extras for display.
     */
    private String formatExtras(Set<BookingExtra> extras) {
        if (extras == null || extras.isEmpty()) {
            return NO_EXTRAS_TEXT;
        }
        return extras.stream()
                .map(BookingExtra::getName)
                .collect(Collectors.joining(", "));
    }
}
