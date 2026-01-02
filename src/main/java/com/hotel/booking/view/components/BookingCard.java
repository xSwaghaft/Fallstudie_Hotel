package com.hotel.booking.view.components;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.service.BookingCancellationService;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Component for displaying a booking card in the bookings list.
 * 
 * @author Viktor Götting
 */
@Component
public class BookingCard {
    
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    private final BookingCancellationService bookingCancellationService;
    
    public BookingCard(BookingCancellationService bookingCancellationService) {
        this.bookingCancellationService = bookingCancellationService;
    }
    
    /**
     * Creates a booking card with details and action buttons.
     * 
     * @param booking the booking to display
     * @param onDetailsClick callback when the card is clicked
     * @param actionButtons the action buttons to display
     * @return the booking card div
     */
    public Div create(Booking booking, Runnable onDetailsClick, HorizontalLayout actionButtons) {
        Div card = new Div();
        card.addClassName("booking-item-card");
        
        // Hauptbereich klickbar machen
        Div clickableArea = new Div();
        clickableArea.addClassName("booking-item-clickable");
        clickableArea.addClickListener(e -> onDetailsClick.run());
        
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
        
        Span statusBadge = createStatusBadge(booking);
        
        header.add(bookingNumber, statusBadge);
        
        Div details = new Div();
        details.addClassName("booking-item-details");
        
        details.add(createDetailItem("Room", roomType + " - " + roomNumber));
        details.add(createDetailItem("Check-in", booking.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(createDetailItem("Check-out", booking.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(createDetailItem("Guests", booking.getAmount() != null ? String.valueOf(booking.getAmount()) : "-"));
        
        // Wenn bereits storniert: Suche letzte BookingCancellation und zeige die berechnete Strafe an
        if (booking.getStatus() == BookingStatus.CANCELLED && booking.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(booking.getId()).ifPresent(bc -> {
                    if (bc.getCancellationFee() != null) {
                        details.add(createDetailItem("Fee", String.format("%.2f €", bc.getCancellationFee())));
                    }
                });
            } catch (Exception ex) {
                // Fehler beim Lesen der Storno-Info darf die Karte nicht komplett brechen
            }
        }
        
        // Calculate price per night for display
        String pricePerNightText = calculatePricePerNight(booking);
        if (pricePerNightText != null) {
            details.add(createDetailItem("Price per Night", pricePerNightText));
        }
        
        // Total price
        String totalPriceText = "-";
        if (booking.getTotalPrice() != null) {
            totalPriceText = String.format("%.2f €", booking.getTotalPrice());
        }
        
        H3 price = new H3("Total Price: " + totalPriceText);
        price.addClassName("booking-item-price");
        
        // Klickbarer Bereich
        clickableArea.add(header, details, price);
        
        // Buttons basierend auf Tab
        Div buttonsContainer = new Div();
        buttonsContainer.addClassName("booking-item-buttons");
        buttonsContainer.add(actionButtons);
        
        card.add(clickableArea, buttonsContainer);
        return card;
    }
    
    /**
     * Creates a detail item (label/value pair).
     */
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
    
    /**
     * Creates a status badge for the booking.
     */
    private Span createStatusBadge(Booking booking) {
        String statusText = booking.getStatus().toString();
        Span badge = new Span(statusText);
        badge.addClassName("booking-item-status");
        badge.addClassName(statusText.toLowerCase());
        return badge;
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
