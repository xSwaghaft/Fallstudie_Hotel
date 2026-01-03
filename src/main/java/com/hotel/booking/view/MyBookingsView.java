package com.hotel.booking.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.BookingCancellationService;
import com.hotel.booking.service.InvoiceService;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.view.components.BookingCard;
import com.hotel.booking.view.components.BookingDetailsDialog;
import com.hotel.booking.view.components.CancellationDialog;
import com.hotel.booking.view.components.EditBookingDialog;
import com.hotel.booking.view.components.PaymentDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import jakarta.annotation.security.RolesAllowed;

// @Route: registriert die View unter /my-bookings im MainLayout.
// @CssImport: bindet globale und Guest-spezifische Styles ein.
@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My Bookings")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
@RolesAllowed(UserRole.GUEST_VALUE)
public class MyBookingsView extends VerticalLayout {

    // Tab labels
    private static final String TAB_UPCOMING = "Upcoming";
    private static final String TAB_PAST = "Past";
    private static final String TAB_CANCELLED = "Cancelled";

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final BookingCancellationService bookingCancellationService;
    private final InvoiceService invoiceService;

    // Components
    private final BookingDetailsDialog bookingDetailsDialog;
    private final BookingCard bookingCard;
    private final EditBookingDialog editBookingDialog;
    private final CancellationDialog cancellationDialog;

    private Tabs tabs;
    private Div contentArea;
    private List<Booking> allBookings = List.of();

    public MyBookingsView(SessionService sessionService,
                          BookingService bookingService,
                          PaymentService paymentService,
                          BookingCancellationService bookingCancellationService,
                          InvoiceService invoiceService,
                          BookingDetailsDialog bookingDetailsDialog,
                          BookingCard bookingCard,
                          EditBookingDialog editBookingDialog,
                          CancellationDialog cancellationDialog) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.bookingCancellationService = bookingCancellationService;
        this.invoiceService = invoiceService;
        this.bookingDetailsDialog = bookingDetailsDialog;
        this.bookingCard = bookingCard;
        this.editBookingDialog = editBookingDialog;
        this.cancellationDialog = cancellationDialog;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        getStyle().set("overflow-x", "hidden");

        reloadBookings();

        add(createHeader(), createTabsBar(), createBookingsCard());
        updateContent();
    }

    private Component createHeader() {
        return new H1("My Bookings");
    }

    // Erstellt die Tabs (Bevorstehend/Vergangen/Storniert) und den Content-Bereich.
    private Component createTabsBar() {
        Tab upcomingTab = new Tab(TAB_UPCOMING);
        Tab pastTab = new Tab(TAB_PAST);
        Tab cancelledTab = new Tab(TAB_CANCELLED);

        tabs = new Tabs(upcomingTab, pastTab, cancelledTab);
        tabs.addClassName("bookings-tabs");
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(e -> updateContent());

        return tabs;
    }

    private Component createBookingsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        contentArea = new Div();
        contentArea.addClassName("bookings-content-area");
        contentArea.setWidthFull();

        card.add(contentArea);
        return card;
    }

    // Lädt alle Buchungen des aktuellen Nutzers (inkl. Preisberechnung).
    private void reloadBookings() {
        User currentUser = sessionService.getCurrentUser();
        if (currentUser == null) {
            allBookings = List.of();
            return;
        }

        allBookings = bookingService.findAllBookingsForGuest(currentUser.getId());
        allBookings.forEach(bookingService::calculateBookingPrice);
    }

    // Filtert Buchungen je nach gewähltem Tab und rendert die Kartenliste.
    private void updateContent() {
        contentArea.removeAll();

        if (tabs == null || tabs.getSelectedTab() == null) {
            contentArea.add(createEmptyMessage("No tab selected."));
            return;
        }

        User user = sessionService.getCurrentUser();
        if (user == null) {
            contentArea.add(createEmptyMessage("No user session."));
            return;
        }

        // IMPORTANT: Reload all bookings from database to get fresh payment data
        reloadBookings();

        String tabLabel = tabs.getSelectedTab().getLabel();
        List<Booking> filteredBookings = filterBookingsByTabType(tabLabel);
        
        // Sort bookings by check-in date
        filteredBookings = filteredBookings.stream()
                .sorted((b1, b2) -> {
                    if (b1.getCheckInDate() == null || b2.getCheckInDate() == null) {
                        return 0;
                    }
                    return b1.getCheckInDate().compareTo(b2.getCheckInDate());
                })
                .collect(Collectors.toList());

        if (filteredBookings.isEmpty()) {
            contentArea.add(createEmptyMessage("No bookings in this category."));
            return;
        }

        Div bookingsContainer = new Div();
        bookingsContainer.addClassName("bookings-container");

        for (Booking booking : filteredBookings) {
            HorizontalLayout actionButtons = createActionButtons(booking, tabLabel);
            Div card = bookingCard.create(
                    booking,
                    () -> bookingDetailsDialog.open(booking),
                    actionButtons
            );
            bookingsContainer.add(card);
        }

        contentArea.add(bookingsContainer);
    }

    // Hilfsmethode: Filtert Buchungen basierend auf dem gewählten Tab-Type
    private List<Booking> filterBookingsByTabType(String tabLabel) {
        LocalDate today = LocalDate.now();

        switch (tabLabel) {
            case TAB_UPCOMING:
                // Buchungen, die noch nicht begonnen haben ODER gerade laufen
                return allBookings.stream()
                        .filter(b -> b.getCheckInDate().isAfter(today)
                                || (!b.getCheckInDate().isAfter(today) && !b.getCheckOutDate().isBefore(today)))
                        .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                        .collect(Collectors.toList());
            case TAB_PAST:
                return allBookings.stream()
                        .filter(b -> b.getCheckOutDate().isBefore(today))
                        .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                        .collect(Collectors.toList());
            case TAB_CANCELLED:
                return allBookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    /**
     * Creates a "Pay Now" button ONLY if booking status is PENDING or MODIFIED.
     *
     * Rules:
     * - Pay Now is only relevant while booking is unpaid: PENDING / MODIFIED
     * - It is shown if there is a pending payment OR no payment exists yet
     * - It is NOT shown for CONFIRMED / COMPLETED / CANCELLED etc.
     */
    private Button createPayButtonIfNeeded(Booking booking) {
        if (booking.getId() == null || booking.getTotalPrice() == null) {
            return null;
        }

        // Only allow payment while booking is in PENDING or MODIFIED state
        BookingStatus status = booking.getStatus();
        boolean isPayableStatus = (status == BookingStatus.PENDING || status == BookingStatus.MODIFIED);
        if (!isPayableStatus) {
            return null;
        }

        List<Payment> allPayments = paymentService.findByBookingId(booking.getId());
        List<Payment> pendingPayments = allPayments.stream()
                .filter(p -> p.getStatus() == Invoice.PaymentStatus.PENDING)
                .toList();

        // Show button if:
        // 1) There is at least one pending payment OR
        // 2) No payment exists yet (e.g., booking created by manager/receptionist or after edits)
        boolean shouldShowButton = !pendingPayments.isEmpty() || allPayments.isEmpty();
        if (!shouldShowButton) {
            return null;
        }

        Button payBtn = new Button("Pay Now", e -> openPaymentDialog(booking));
        payBtn.addClassName("primary-button");
        return payBtn;
    }

    /**
     * Creates a "Pay Fees" button for cancelled bookings.
     *
     * Rules:
     * - Only shown for CANCELLED bookings
     * - Only shown if the booking was previously PENDING (no payment made)
     * - Only shown if there's a cancellation fee to pay (unpaid)
     */
    private Button createPayFeesButtonIfNeeded(Booking booking) {
        if (booking.getId() == null || booking.getStatus() != BookingStatus.CANCELLED) {
            return null;
        }

        try {
            // Get all payments once
            List<Payment> payments = paymentService.findByBookingId(booking.getId());
            
            // Check if booking was previously PENDING (no PAID/REFUNDED payments)
            boolean wasPreviouslyPending = payments.stream()
                    .allMatch(p -> p.getStatus() != Invoice.PaymentStatus.PAID 
                               && p.getStatus() != Invoice.PaymentStatus.REFUNDED);
            
            // Check if fee already paid (PARTIAL status) or not previously pending
            if (!wasPreviouslyPending || payments.stream().anyMatch(p -> p.getStatus() == Invoice.PaymentStatus.PARTIAL)) {
                return null;
            }
            
            // Check if there's a cancellation fee to pay
            java.util.Optional<BookingCancellation> cancellation = bookingCancellationService.findLatestByBookingId(booking.getId());
            BigDecimal fee = cancellation.isPresent() ? cancellation.get().getCancellationFee() : null;
            
            if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }

            Button payFeesBtn = new Button("Pay Fees", e -> openPaymentDialogForFees(booking));
            payFeesBtn.addClassName("primary-button");
            return payFeesBtn;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Opens payment dialog for the booking
     */
    private void openPaymentDialog(Booking booking) {
        try {
            if (booking.getTotalPrice() == null) {
                Notification.show("Error: Total price is missing", 5000, Notification.Position.TOP_CENTER);
                return;
            }

            Long bookingId = booking.getId(); // Store booking ID
            PaymentDialog paymentDialog = new PaymentDialog(booking.getTotalPrice());

            paymentDialog.setOnPaymentSuccess(() -> {
                try {
                    // Update existing PENDING payment to PAID, or create new PAID payment
                    paymentService.processPaymentForBooking(
                            bookingId,
                            null,
                            paymentDialog.getSelectedPaymentMethod(),
                            Invoice.PaymentStatus.PAID
                    );

                    Notification.show("Payment completed! Thank you.", 3000, Notification.Position.TOP_CENTER);
                    updateContent();
                } catch (Exception ex) {
                    Notification.show("Error processing payment. Please try again.", 5000, Notification.Position.TOP_CENTER);
                }
            });

            paymentDialog.setOnPaymentDeferred(() -> {
                try {
                    // Create new PENDING payment if none exists
                    paymentService.processPaymentForBooking(
                            bookingId,
                            null,
                            paymentDialog.getSelectedPaymentMethod(),
                            Invoice.PaymentStatus.PENDING
                    );

                    Notification.show("Payment postponed. You can pay later in 'My Bookings'.", 3000, Notification.Position.TOP_CENTER);
                    updateContent();
                } catch (Exception ex) {
                    Notification.show("Error processing payment. Please try again.", 5000, Notification.Position.TOP_CENTER);
                }
            });

            paymentDialog.open();
        } catch (Exception ex) {
            Notification.show("Error opening payment dialog", 5000, Notification.Position.TOP_CENTER);
        }
    }

    /**
     * Opens payment dialog for cancellation fees on cancelled bookings.
     * Treats the fee payment as PARTIAL status (refunded booking with unpaid fee).
     */
    private void openPaymentDialogForFees(Booking booking) {
        try {
            if (booking.getId() == null) {
                Notification.show("Error: Booking ID is missing", 5000, Notification.Position.TOP_CENTER);
                return;
            }

            // Get the cancellation fee from BookingCancellation entity
            java.util.Optional<BookingCancellation> cancellation = bookingCancellationService.findLatestByBookingId(booking.getId());

            if (cancellation.isEmpty()) {
                Notification.show("No cancellation record found.", 3000, Notification.Position.TOP_CENTER);
                updateContent();
                return;
            }

            BigDecimal totalFee = cancellation.get().getCancellationFee();

            if (totalFee == null || totalFee.compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show("No fees to pay.", 3000, Notification.Position.TOP_CENTER);
                updateContent();
                return;
            }

            PaymentDialog paymentDialog = new PaymentDialog(totalFee);

            paymentDialog.setOnPaymentSuccess(() -> {
                try {
                    // Update the existing PENDING payment to PARTIAL status
                    // Keep the original amount and store the fee as refundedAmount
                    List<Payment> payments = paymentService.findByBookingId(booking.getId());
                    for (Payment p : payments) {
                        if (p.getStatus() == Invoice.PaymentStatus.PENDING) {
                            // Change status to PARTIAL and set refundedAmount to the fee paid
                            p.setStatus(Invoice.PaymentStatus.PARTIAL);
                            p.setRefundedAmount(totalFee);
                            p.setPaidAt(java.time.LocalDateTime.now());
                            p.setMethod(paymentService.mapPaymentMethod(paymentDialog.getSelectedPaymentMethod()));
                            paymentService.save(p);
                            
                            // Create an invoice for the fee payment
                            com.hotel.booking.entity.Invoice invoice = new com.hotel.booking.entity.Invoice();
                            invoice.setBooking(booking);
                            invoice.setAmount(booking.getTotalPrice());
                            invoice.setInvoiceStatus(Invoice.PaymentStatus.PARTIAL);
                            invoice.setPaymentMethod(paymentService.mapPaymentMethod(paymentDialog.getSelectedPaymentMethod()));
                            invoice.setIssuedAt(java.time.LocalDateTime.now());
                            invoice.setInvoiceNumber(invoiceService.generateInvoiceNumber());
                            invoiceService.save(invoice);
                            
                            break;
                        }
                    }

                    Notification.show("Cancellation fee paid! Thank you.", 3000, Notification.Position.TOP_CENTER);
                    updateContent();
                } catch (Exception ex) {
                    Notification.show("Error processing payment. Please try again.", 5000, Notification.Position.TOP_CENTER);
                }
            });

            paymentDialog.setOnPaymentDeferred(() -> {
                Notification.show("Payment postponed. You can pay the fee later.", 3000, Notification.Position.TOP_CENTER);
                paymentDialog.close();
            });

            paymentDialog.open();
        } catch (Exception ex) {
            Notification.show("Error opening payment dialog: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }

    private HorizontalLayout createActionButtons(Booking booking, String tab) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        BookingStatus status = booking.getStatus();
        boolean isPast = booking.getCheckOutDate().isBefore(LocalDate.now());

        boolean canEdit = status == BookingStatus.PENDING || status == BookingStatus.MODIFIED;
        boolean canCancel = !isPast && (status == BookingStatus.PENDING
                || status == BookingStatus.CONFIRMED
                || status == BookingStatus.MODIFIED);

        if (TAB_UPCOMING.equals(tab)) {
            Button payBtn = createPayButtonIfNeeded(booking);
            if (payBtn != null) {
                layout.add(payBtn);
            }

            if (canEdit) {
                Button editBtn = new Button("Edit", e -> editBookingDialog.open(booking, () -> {
                    reloadBookings();
                    updateContent();
                }));
                editBtn.addClassName("primary-button");
                layout.add(editBtn);
            }

            if (canCancel) {
                Button cancelBtn = new Button("Cancel", e -> cancellationDialog.open(booking, () -> {
                    reloadBookings();
                    updateContent();
                }));
                cancelBtn.addClassName("secondary-button");
                layout.add(cancelBtn);
            }
        }

        if (TAB_CANCELLED.equals(tab)) {
            Button payFeesBtn = createPayFeesButtonIfNeeded(booking);
            if (payFeesBtn != null) {
                layout.add(payFeesBtn);
            }
        }

        if (TAB_PAST.equals(tab)) {
            RouterLink reviewLink = new RouterLink("Write Review", MyReviewsView.class);
            reviewLink.addClassName("primary-button");
            layout.add(reviewLink);
        }

        return layout;
    }

    private Component createEmptyMessage(String message) {
        Paragraph emptyMessage = new Paragraph(message);
        emptyMessage.getStyle().set("padding", "var(--spacing-xl)");
        emptyMessage.getStyle().set("text-align", "center");
        emptyMessage.getStyle().set("color", "var(--color-text-secondary)");
        return emptyMessage;
    }
}
