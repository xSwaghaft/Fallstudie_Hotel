package com.hotel.booking.view.components;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingCancellationService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Dialog component for cancelling a booking.
 * 
 * @author Arman Özcanli
 */
@Component
public class CancellationDialog {
    
    /** Default cancellation reason when cancelled by guest */
    private static final String DEFAULT_CANCELLATION_REASON = "Cancelled by guest";
    
    /** Threshold days for cancellation policy */
    private static final long FREE_CANCELLATION_DAYS = 30L;
    private static final long MEDIUM_FEE_DAYS = 7L;
    private static final long HIGH_FEE_DAYS = 1L;
    
    private final SessionService sessionService;
    private final BookingCancellationService bookingCancellationService;
    
    public CancellationDialog(SessionService sessionService,
                              BookingCancellationService bookingCancellationService) {
        this.sessionService = sessionService;
        this.bookingCancellationService = bookingCancellationService;
    }
    
    /**
     * Opens the cancellation dialog.
     * 
     * @param booking the booking to cancel
     * @param onSuccess callback when cancellation is successful
     */
    public void open(Booking booking, Runnable onSuccess) {
        try {
            // Calculate cancellation fee based on days before check-in
            BigDecimal penalty = bookingCancellationService.calculateCancellationFee(booking, booking.getTotalPrice());
            long daysBefore = Duration.between(LocalDateTime.now(), booking.getCheckInDate().atStartOfDay()).toDays();

            Dialog confirm = new Dialog();
            confirm.setHeaderTitle("Confirm Cancellation");
            VerticalLayout cnt = new VerticalLayout();

            if (penalty.compareTo(BigDecimal.ZERO) > 0) {
                String timeframe = formatTimeframe(daysBefore);
                cnt.add(new Paragraph("You are cancelling " + timeframe + " before check-in."));
                cnt.add(new Paragraph("A fee will be charged: " + String.format("%.2f €", penalty)));
                cnt.add(new Paragraph("Refund: " + String.format("%.2f €", booking.getTotalPrice().subtract(penalty))));
                cnt.add(new Paragraph("Do you want to confirm the cancellation?"));
            } else {
                cnt.add(new Paragraph("You are cancelling more than " + FREE_CANCELLATION_DAYS + " days before check-in."));
                cnt.add(new Paragraph("Free cancellation. Full refund!"));
                cnt.add(new Paragraph("Do you really want to cancel this booking?"));
            }

            BigDecimal penaltyFinal = penalty;
            Button confirmBtn = new Button("Yes, cancel", ev -> {
                try {
                    BookingCancellation bc = new BookingCancellation();
                    bc.setBooking(booking);
                    bc.setCancelledAt(LocalDateTime.now());
                    bc.setReason(DEFAULT_CANCELLATION_REASON);
                    bc.setCancellationFee(penaltyFinal);
                    
                    // Calculate refunded amount (total - penalty)
                    BigDecimal refundedAmount = booking.getTotalPrice().subtract(penaltyFinal);
                    bc.setRefundedAmount(refundedAmount);
                    
                    if (sessionService.getCurrentUser() != null) {
                        bc.setHandledBy(sessionService.getCurrentUser());
                    }
                    
                    // Use centralized cancellation logic
                    bookingCancellationService.processCancellation(booking, bc, refundedAmount);

                    onSuccess.run();
                    
                    String msg = penaltyFinal.compareTo(BigDecimal.ZERO) > 0 
                        ? "Booking cancelled. Refund: " + String.format("%.2f €", refundedAmount) + 
                          " | Fee: " + String.format("%.2f €", penaltyFinal)
                        : "Booking cancelled. Full amount will be refunded.";
                    Notification.show(msg, 3000, Notification.Position.BOTTOM_START);
                    confirm.close();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error cancelling booking", 
                            5000, Notification.Position.MIDDLE);
                }
            });
            confirmBtn.addClassName("primary-button");
            
            Button backBtn = new Button("Cancel", ev -> confirm.close());
            confirm.add(cnt, new HorizontalLayout(confirmBtn, backBtn));
            confirm.open();
        } catch (Exception ex) {
            Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error cancelling booking", 
                    5000, Notification.Position.MIDDLE);
        }
    }
    
    /**
     * Formats the timeframe string based on days before check-in.
     * 
     * @param daysBefore number of days before check-in
     * @return formatted timeframe string
     */
    private String formatTimeframe(long daysBefore) {
        if (daysBefore >= MEDIUM_FEE_DAYS) {
            return "more than " + MEDIUM_FEE_DAYS + " days";
        } else if (daysBefore >= HIGH_FEE_DAYS) {
            return HIGH_FEE_DAYS + "-" + (MEDIUM_FEE_DAYS - 1) + " days";
        } else {
            return "on check-in day";
        }
    }
}
