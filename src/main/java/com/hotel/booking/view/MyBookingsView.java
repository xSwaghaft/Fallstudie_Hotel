package com.hotel.booking.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingCancellationService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingModificationService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.InvoiceService;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.view.components.PaymentDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import jakarta.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My Bookings")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
@RolesAllowed(UserRole.GUEST_VALUE)
public class MyBookingsView extends VerticalLayout {

    private static final String TAB_UPCOMING = "Upcoming";
    private static final String TAB_PAST = "Past";
    private static final String TAB_CANCELLED = "Cancelled";

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final BookingFormService formService;
    private final BookingModificationService modificationService;
    private final BookingCancellationService bookingCancellationService;

    private Tabs tabs;
    private Div contentArea;
    private Div bookingsCardBody;

    private List<Booking> allBookings = new ArrayList<>();

    private static record PrevBooking(
            LocalDate checkIn,
            LocalDate checkOut,
            Integer amount,
            java.math.BigDecimal total,
            java.util.Set<BookingExtra> extras
    ) {}

    @Autowired
    public MyBookingsView(
            SessionService sessionService,
            BookingService bookingService,
            PaymentService paymentService,
            InvoiceService invoiceService,
            BookingFormService formService,
            BookingModificationService modificationService,
            BookingCancellationService bookingCancellationService
    ) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
        this.formService = formService;
        this.modificationService = modificationService;
        this.bookingCancellationService = bookingCancellationService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();
        setWidthFull();
        getStyle().set("overflow-x", "hidden");

        reloadBookings();

        add(
                createHeader(),
                createTabsBar(),
                createBookingsCard()
        );

        updateContent();
    }

    // =========================================================
    // TOP-LEVEL LAYOUT
    // =========================================================

    private Component createHeader() {
        return new H1("My Bookings");
    }

    private Component createTabsBar() {
        Tab upcomingTab = new Tab(TAB_UPCOMING);
        Tab pastTab = new Tab(TAB_PAST);
        Tab cancelledTab = new Tab(TAB_CANCELLED);

        tabs = new Tabs(upcomingTab, pastTab, cancelledTab);
        tabs.addClassName("bookings-tabs");
        tabs.addSelectedChangeListener(e -> updateContent());
        tabs.setWidthFull();

        return tabs;
    }

    private Component createBookingsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        bookingsCardBody = new Div();
        bookingsCardBody.addClassName("bookings-content-area");
        bookingsCardBody.setWidthFull();

        contentArea = bookingsCardBody;

        card.add(bookingsCardBody);
        return card;
    }

    // =========================================================
    // DATA FLOW (reload -> filter -> render)
    // =========================================================

    private void reloadBookings() {
        User currentUser = sessionService.getCurrentUser();
        if (currentUser == null) {
            allBookings = List.of();
            return;
        }
        allBookings = bookingService.findAllBookingsForGuest(currentUser.getId());
        allBookings.forEach(bookingService::calculateBookingPrice);
    }

    private void updateContent() {
        contentArea.removeAll();

        if (sessionService.getCurrentUser() == null) {
            contentArea.add(createEmptyMessage("No user session."));
            return;
        }

        if (tabs == null || tabs.getSelectedTab() == null) {
            contentArea.add(createEmptyMessage("No tab selected."));
            return;
        }

        reloadBookings();

        if (allBookings.isEmpty()) {
            contentArea.add(createEmptyMessage("No bookings found."));
            return;
        }

        String tabLabel = tabs.getSelectedTab().getLabel();
        List<Booking> filtered = getBookingsForSelectedTab(tabLabel);

        contentArea.add(renderBookingsList(filtered, tabLabel));
    }

    private List<Booking> getBookingsForSelectedTab(String tabLabel) {
        LocalDate today = LocalDate.now();

        switch (tabLabel) {
            case TAB_UPCOMING:
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

    private Component renderBookingsList(List<Booking> bookings, String tabLabel) {
        if (bookings == null || bookings.isEmpty()) {
            return createEmptyMessage("No bookings in this category.");
        }

        VerticalLayout list = new VerticalLayout();
        list.setSpacing(true);
        list.setPadding(false);

        for (Booking b : bookings) {
            list.add(createBookingCard(b, tabLabel));
        }

        return list;
    }

    private Component createEmptyMessage(String text) {
        Paragraph emptyMessage = new Paragraph(text);
        emptyMessage.getStyle().set("padding", "var(--spacing-xl)");
        emptyMessage.getStyle().set("text-align", "center");
        emptyMessage.getStyle().set("color", "var(--color-text-secondary)");
        return emptyMessage;
    }

    // =========================================================
    // BOOKING CARD RENDERING
    // =========================================================

    private Div createBookingCard(Booking booking, String tabLabel) {
        Div card = new Div();
        card.addClassName("booking-item-card");

        Div clickableArea = new Div();
        clickableArea.getStyle().set("cursor", "pointer");
        clickableArea.addClickListener(e -> openBookingDetailsDialog(booking));

        clickableArea.add(
                createCardHeader(booking),
                createCardDetails(booking),
                createCardTotalPrice(booking)
        );

        Div buttonsContainer = new Div();
        buttonsContainer.addClassName("booking-item-buttons");
        buttonsContainer.add(createCardActions(booking, tabLabel));

        card.add(clickableArea, buttonsContainer);
        return card;
    }

    private Component createCardHeader(Booking booking) {
        Div header = new Div();
        header.addClassName("booking-item-header");

        H3 bookingNumber = new H3(booking.getBookingNumber());
        bookingNumber.addClassName("booking-item-number");

        Span statusBadge = createStatusBadge(booking);

        header.add(bookingNumber, statusBadge);
        return header;
    }

    private Component createCardDetails(Booking booking) {
        Div details = new Div();
        details.addClassName("booking-item-details");

        String roomType = booking.getRoomCategory() != null ? booking.getRoomCategory().getName() : "Room";
        String roomNumber = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "-";

        details.add(createDetailItem("Room", roomType + " - " + roomNumber));
        details.add(createDetailItem("Check-in", booking.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(createDetailItem("Check-out", booking.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(createDetailItem("Guests", booking.getAmount() != null ? String.valueOf(booking.getAmount()) : "-"));

        if (booking.getStatus() == BookingStatus.CANCELLED && booking.getId() != null) {
            try {
                bookingCancellationService.findLatestByBookingId(booking.getId()).ifPresent(bc -> {
                    if (bc.getCancellationFee() != null) {
                        details.add(createDetailItem("Fee", String.format("%.2f €", bc.getCancellationFee())));
                    }
                });
            } catch (Exception ignored) {}
        }

        String pricePerNightText = calculatePricePerNight(booking);
        if (pricePerNightText != null) {
            details.add(createDetailItem("Price per Night", pricePerNightText));
        }

        return details;
    }

    private Component createCardTotalPrice(Booking booking) {
        String totalPriceText = "-";
        if (booking.getTotalPrice() != null) {
            totalPriceText = String.format("%.2f €", booking.getTotalPrice());
        }

        H3 price = new H3("Total Price: " + totalPriceText);
        price.addClassName("booking-item-price");
        return price;
    }

    /**
     * Pay + Edit should be available for PENDING and MODIFIED.
     * CONFIRMED should not show Pay or Edit.
     */
    private boolean isEditableOrPayable(Booking booking) {
        if (booking == null || booking.getStatus() == null) return false;
        return booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.MODIFIED;
    }

    private Component createCardActions(Booking booking, String tabLabel) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        if (TAB_UPCOMING.equals(tabLabel)) {

            // Pay Now only for PENDING or MODIFIED bookings (and only if a pending payment exists)
            if (isEditableOrPayable(booking)) {
                Button payBtn = createPayButtonIfNeeded(booking);
                if (payBtn != null) {
                    buttonsLayout.add(payBtn);
                }
            }

            // Edit only for PENDING or MODIFIED bookings
            if (isEditableOrPayable(booking)) {
                Button editBtn = new Button("Edit");
                editBtn.addClassName("primary-button");
                editBtn.addClickListener(e -> openEditBookingDialog(booking));
                buttonsLayout.add(editBtn);
            }

            // Cancel kept as-is for upcoming bookings
            Button cancelBtn = new Button("Cancel");
            cancelBtn.addClassName("secondary-button");
            cancelBtn.addClickListener(e -> openCancellationDialog(booking));
            buttonsLayout.add(cancelBtn);

        } else if (TAB_PAST.equals(tabLabel)) {
            RouterLink reviewLink = new RouterLink("Write Review", MyReviewsView.class);
            reviewLink.addClassName("primary-button");
            buttonsLayout.add(reviewLink);
        }

        return buttonsLayout;
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

    private Span createStatusBadge(Booking booking) {
        String statusText = booking.getStatus() != null ? booking.getStatus().toString() : "UNKNOWN";
        Span badge = new Span(statusText);
        badge.addClassName("booking-item-status");
        badge.addClassName(statusText.toLowerCase());
        return badge;
    }

    private String calculatePricePerNight(Booking booking) {
        if (booking.getRoomCategory() != null && booking.getRoomCategory().getPricePerNight() != null) {
            return String.format("%.2f €", booking.getRoomCategory().getPricePerNight());
        } else if (booking.getRoom() != null
                && booking.getRoom().getCategory() != null
                && booking.getRoom().getCategory().getPricePerNight() != null) {
            return String.format("%.2f €", booking.getRoom().getCategory().getPricePerNight());
        }
        return null;
    }

    // =========================================================
    // PAYMENT FLOW
    // =========================================================

    private Button createPayButtonIfNeeded(Booking booking) {
        if (booking == null || booking.getId() == null) return null;

        // Only allow payment for PENDING or MODIFIED bookings
        if (!isEditableOrPayable(booking)) {
            return null;
        }

        List<Payment> pendingPayments = paymentService.findByBookingId(booking.getId()).stream()
                .filter(p -> p.getStatus() == Invoice.PaymentStatus.PENDING)
                .toList();

        if (pendingPayments.isEmpty()) return null;

        Button payBtn = new Button("Pay Now", e -> openPaymentDialog(booking));
        payBtn.addClassName("primary-button");
        return payBtn;
    }

    private void openPaymentDialog(Booking booking) {
        try {
            if (booking.getTotalPrice() == null) {
                Notification.show("Error: Total price is missing", 5000, Notification.Position.TOP_CENTER);
                return;
            }

            Long bookingId = booking.getId();
            PaymentDialog paymentDialog = new PaymentDialog(booking.getTotalPrice());

            paymentDialog.setOnPaymentSuccess(() -> {
                updatePendingPaymentToPaid(bookingId, paymentDialog.getSelectedPaymentMethod());
                Notification.show("Payment completed! Thank you.", 3000, Notification.Position.TOP_CENTER);
                updateContent();
            });

            paymentDialog.setOnPaymentDeferred(() -> {
                Notification.show("Payment postponed.", 3000, Notification.Position.TOP_CENTER);
            });

            paymentDialog.open();
        } catch (Exception ex) {
            Notification.show("Error opening payment dialog", 5000, Notification.Position.TOP_CENTER);
        }
    }

    private void updatePendingPaymentToPaid(Long bookingId, String selectedMethod) {
        try {
            List<Payment> payments = paymentService.findByBookingId(bookingId);
            Payment paidPayment = null;

            for (Payment p : payments) {
                if (p.getStatus() == Invoice.PaymentStatus.PENDING) {
                    p.setStatus(Invoice.PaymentStatus.PAID);
                    p.setPaidAt(LocalDateTime.now());
                    p.setMethod(mapPaymentMethod(selectedMethod));
                    paymentService.save(p);
                    paidPayment = p;
                    break;
                }
            }

            var bookingOpt = bookingService.findById(bookingId);
            if (bookingOpt.isPresent()) {
                Booking b = bookingOpt.get();
                // If booking is payable/editable it should become CONFIRMED after payment
                if (b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.MODIFIED) {
                    b.setStatus(BookingStatus.CONFIRMED);
                    bookingService.save(b);

                    if (b.getInvoice() == null && paidPayment != null) {
                        Invoice invoice = new Invoice();
                        invoice.setBooking(b);
                        invoice.setAmount(paidPayment.getAmount());
                        invoice.setInvoiceStatus(Invoice.PaymentStatus.PAID);
                        invoice.setPaymentMethod(paidPayment.getMethod());
                        invoice.setIssuedAt(LocalDateTime.now());
                        invoice.setInvoiceNumber(generateInvoiceNumber());
                        invoiceService.save(invoice);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private Invoice.PaymentMethod mapPaymentMethod(String uiMethod) {
        if ("Bank Transfer".equals(uiMethod) || "Banküberweisung".equals(uiMethod)) {
            return Invoice.PaymentMethod.TRANSFER;
        }
        return Invoice.PaymentMethod.CARD;
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis();
    }

    // =========================================================
    // EDIT FLOW
    // =========================================================

    private void openEditBookingDialog(Booking booking) {
        createNewBookingForm form = new createNewBookingForm(sessionService.getCurrentUser(), sessionService, booking, formService);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Booking");
        dialog.setWidth("600px");

        final PrevBooking prevSnapshot = booking != null
                ? new PrevBooking(
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getAmount(),
                        booking.getTotalPrice(),
                        booking.getExtras()
                )
                : null;

        Button saveBtn = new Button("Save", ev -> {
            try {
                form.writeBean();
                Booking updated = form.getBooking();
                bookingService.calculateBookingPrice(updated);

                Dialog preview = new Dialog();
                preview.setHeaderTitle("Confirm Changes");
                VerticalLayout content = new VerticalLayout();

                content.add(new Paragraph("-- Before --"));
                content.add(new Paragraph("Check-in: " + (prevSnapshot != null && prevSnapshot.checkIn() != null ? prevSnapshot.checkIn().format(GERMAN_DATE_FORMAT) : "N/A")));
                content.add(new Paragraph("Check-out: " + (prevSnapshot != null && prevSnapshot.checkOut() != null ? prevSnapshot.checkOut().format(GERMAN_DATE_FORMAT) : "N/A")));
                content.add(new Paragraph("Guests: " + (prevSnapshot != null && prevSnapshot.amount() != null ? prevSnapshot.amount() : "N/A")));
                content.add(new Paragraph("Price: " + (prevSnapshot != null && prevSnapshot.total() != null ? prevSnapshot.total().toString() : "N/A")));
                String prevExtrasStr = "none";
                if (prevSnapshot != null && prevSnapshot.extras() != null && !prevSnapshot.extras().isEmpty()) {
                    prevExtrasStr = prevSnapshot.extras().stream().map(BookingExtra::getName).collect(Collectors.joining(", "));
                }
                content.add(new Paragraph("Extras: " + prevExtrasStr));

                content.add(new Paragraph("-- After --"));
                content.add(new Paragraph("Check-in: " + (updated.getCheckInDate() != null ? updated.getCheckInDate().format(GERMAN_DATE_FORMAT) : "N/A")));
                content.add(new Paragraph("Check-out: " + (updated.getCheckOutDate() != null ? updated.getCheckOutDate().format(GERMAN_DATE_FORMAT) : "N/A")));
                content.add(new Paragraph("Guests: " + (updated.getAmount() != null ? updated.getAmount() : "N/A")));
                content.add(new Paragraph("Price: " + (updated.getTotalPrice() != null ? updated.getTotalPrice().toString() : "N/A")));
                String newExtrasStr = "none";
                if (updated.getExtras() != null && !updated.getExtras().isEmpty()) {
                    newExtrasStr = updated.getExtras().stream().map(BookingExtra::getName).collect(Collectors.joining(", "));
                }
                content.add(new Paragraph("Extras: " + newExtrasStr));

                Button confirm = new Button("Confirm", confirmEv -> {
                    try {
                        modificationService.recordChangesFromSnapshot(
                                booking,
                                prevSnapshot != null ? prevSnapshot.checkIn() : null,
                                prevSnapshot != null ? prevSnapshot.checkOut() : null,
                                prevSnapshot != null ? prevSnapshot.amount() : null,
                                prevSnapshot != null ? prevSnapshot.total() : null,
                                prevSnapshot != null ? prevSnapshot.extras() : null,
                                updated,
                                sessionService.getCurrentUser(),
                                null
                        );

                        bookingService.save(updated);

                        preview.close();
                        dialog.close();
                        updateContent();

                        Notification.show("Booking updated successfully.", 3000, Notification.Position.BOTTOM_START);
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error saving booking", 5000, Notification.Position.MIDDLE);
                    }
                });

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

    // =========================================================
    // CANCELLATION FLOW
    // =========================================================

    private void openCancellationDialog(Booking booking) {
        try {
            if (booking.getInvoice() != null && booking.getInvoice().getInvoiceStatus() == Invoice.PaymentStatus.PAID) {
                Notification.show("This booking has already been paid and cannot be cancelled.", 4000, Notification.Position.MIDDLE);
                return;
            }

            java.math.BigDecimal penalty = bookingCancellationService.calculateCancellationFee(booking, booking.getTotalPrice());
            long daysBefore = java.time.Duration.between(java.time.LocalDateTime.now(), booking.getCheckInDate().atStartOfDay()).toDays();

            Dialog confirm = new Dialog();
            confirm.setHeaderTitle("Confirm Cancellation");
            VerticalLayout cnt = new VerticalLayout();

            if (penalty.compareTo(java.math.BigDecimal.ZERO) > 0) {
                String timeframe;
                if (daysBefore >= 7) {
                    timeframe = "more than 7 days";
                } else if (daysBefore >= 1) {
                    timeframe = "1-6 days";
                } else {
                    timeframe = "on check-in day";
                }
                cnt.add(new Paragraph("You are cancelling " + timeframe + " before check-in."));
                cnt.add(new Paragraph("A fee will be charged: " + String.format("%.2f €", penalty)));
                cnt.add(new Paragraph("Refund: " + String.format("%.2f €", booking.getTotalPrice().subtract(penalty))));
                cnt.add(new Paragraph("Do you want to confirm the cancellation?"));
            } else {
                cnt.add(new Paragraph("You are cancelling more than 30 days before check-in."));
                cnt.add(new Paragraph("Free cancellation. Full refund!"));
                cnt.add(new Paragraph("Do you really want to cancel this booking?"));
            }

            final java.math.BigDecimal penaltyFinal = penalty;

            Button confirmBtn = new Button("Yes, cancel", ev -> {
                try {
                    BookingCancellation bc = new BookingCancellation();
                    bc.setBooking(booking);
                    bc.setCancelledAt(java.time.LocalDateTime.now());
                    bc.setReason("Cancelled by guest");
                    bc.setCancellationFee(penaltyFinal);

                    java.math.BigDecimal refundedAmount = booking.getTotalPrice().subtract(penaltyFinal);
                    bc.setRefundedAmount(refundedAmount);

                    User current = sessionService.getCurrentUser();
                    if (current != null) {
                        bc.setHandledBy(current);
                    }

                    bookingCancellationService.processCancellation(booking, bc, refundedAmount);

                    updateContent();

                    String msg = penaltyFinal.compareTo(java.math.BigDecimal.ZERO) > 0
                            ? "Booking cancelled. Refund: " + String.format("%.2f €", refundedAmount) + " | Fee: " + String.format("%.2f €", penaltyFinal)
                            : "Booking cancelled. Full amount will be refunded.";
                    Notification.show(msg, 3000, Notification.Position.BOTTOM_START);

                    confirm.close();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error cancelling booking", 5000, Notification.Position.MIDDLE);
                }
            });

            Button backBtn = new Button("Cancel", ev -> confirm.close());
            confirm.add(cnt, new HorizontalLayout(confirmBtn, backBtn));
            confirm.open();

        } catch (Exception ex) {
            Notification.show(ex.getMessage() != null ? ex.getMessage() : "Error cancelling booking", 5000, Notification.Position.MIDDLE);
        }
    }

    // =========================================================
    // DETAILS DIALOG
    // =========================================================

    private void openBookingDetailsDialog(Booking booking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Booking Details - " + booking.getBookingNumber());
        dialog.setWidth("800px");

        Tabs detailsTabs = new Tabs(new Tab("Details"), new Tab("Payments"), new Tab("History"), new Tab("Extras"));

        Div details = new Div();
        details.add(new Paragraph("Guest Name: " + (booking.getGuest() != null ? booking.getGuest().getFullName() : "N/A")));
        details.add(new Paragraph("Booking Number: " + booking.getBookingNumber()));
        details.add(new Paragraph("Check-in: " + booking.getCheckInDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Check-out: " + booking.getCheckOutDate().format(GERMAN_DATE_FORMAT)));
        details.add(new Paragraph("Guests: " + booking.getAmount()));
        details.add(new Paragraph("Status: " + booking.getStatus()));

        String pricePerNightText = calculatePricePerNight(booking);
        if (pricePerNightText != null) {
            details.add(new Paragraph("Price per Night: " + pricePerNightText));
        }
        String totalPriceText = booking.getTotalPrice() != null ? String.format("%.2f €", booking.getTotalPrice()) : "-";
        details.add(new Paragraph("Total Price: " + totalPriceText));

        Div cancellationPolicy = new Div();
        cancellationPolicy.getStyle().set("margin-top", "var(--spacing-md)");
        cancellationPolicy.getStyle().set("padding", "var(--spacing-md)");
        cancellationPolicy.getStyle().set("background", "var(--color-bg-light)");
        cancellationPolicy.getStyle().set("border-radius", "var(--radius-md)");

        Paragraph policyTitle = new Paragraph("Cancellation Policy");
        policyTitle.getStyle().set("font-weight", "600");
        policyTitle.getStyle().set("margin-bottom", "var(--spacing-sm)");
        cancellationPolicy.add(policyTitle);

        cancellationPolicy.add(new Paragraph("• More than 30 days before check-in: Free cancellation"));
        cancellationPolicy.add(new Paragraph("• 7-29 days before check-in: 20% cancellation fee"));
        cancellationPolicy.add(new Paragraph("• 1-6 days before check-in: 50% cancellation fee"));
        cancellationPolicy.add(new Paragraph("• On check-in day: 100% cancellation fee (no refund)"));

        details.add(cancellationPolicy);

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
            } catch (Exception ignored) {}
        }

        Div payments = new Div();
        if (booking.getInvoice() != null) {
            Invoice invoice = booking.getInvoice();
            payments.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()));
            payments.add(new Paragraph("Amount: " + String.format("%.2f €", invoice.getAmount())));
            payments.add(new Paragraph("Status: " + invoice.getInvoiceStatus().toString()));
            payments.add(new Paragraph("Payment Method: " + invoice.getPaymentMethod().toString()));
            if (invoice.getIssuedAt() != null) {
                payments.add(new Paragraph("Issued: " + invoice.getIssuedAt()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
            }
        } else {
            payments.add(new Paragraph("Payment information not available"));
        }

        Div history = new Div();
        if (booking.getId() != null) {
            java.util.List<com.hotel.booking.entity.BookingModification> mods = modificationService.findByBookingId(booking.getId());
            if (mods.isEmpty()) {
                history.add(new Paragraph("No modification history available."));
            } else {
                java.util.Map<java.time.LocalDateTime, java.util.List<com.hotel.booking.entity.BookingModification>> grouped =
                        mods.stream().collect(java.util.stream.Collectors.groupingBy(
                                com.hotel.booking.entity.BookingModification::getModifiedAt,
                                java.util.LinkedHashMap::new,
                                java.util.stream.Collectors.toList()
                        ));

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
                            who = (m.getHandledBy().getFullName() != null && !m.getHandledBy().getFullName().isBlank())
                                    ? m.getHandledBy().getFullName()
                                    : m.getHandledBy().getEmail();
                            break;
                        }
                    }
                    groupBox.add(new Paragraph(ts.format(dtf) + " — " + who));

                    for (com.hotel.booking.entity.BookingModification m : group) {
                        HorizontalLayout row = new HorizontalLayout();
                        row.setWidthFull();

                        Paragraph field = new Paragraph(m.getFieldChanged() + ": ");
                        field.getStyle().set("font-weight", "600");

                        Paragraph values = new Paragraph((m.getOldValue() != null ? m.getOldValue() : "<null>")
                                + " → "
                                + (m.getNewValue() != null ? m.getNewValue() : "<null>"));
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
                        who = bc.getHandledBy().getFullName() != null && !bc.getHandledBy().getFullName().isBlank()
                                ? bc.getHandledBy().getFullName()
                                : bc.getHandledBy().getEmail();
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
            } catch (Exception ignored) {}
        }

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

        detailsTabs.addSelectedChangeListener(ev -> {
            details.setVisible(detailsTabs.getSelectedIndex() == 0);
            payments.setVisible(detailsTabs.getSelectedIndex() == 1);
            history.setVisible(detailsTabs.getSelectedIndex() == 2);
            extras.setVisible(detailsTabs.getSelectedIndex() == 3);
        });

        Button close = new Button("Close", e -> dialog.close());
        close.addClassName("primary-button");

        dialog.add(new VerticalLayout(detailsTabs, pages));
        dialog.getFooter().add(close);
        dialog.open();
    }
}
