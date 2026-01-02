package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My Bookings")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
@RolesAllowed(UserRole.GUEST_VALUE)
public class MyBookingsView extends VerticalLayout {

    private static final String TAB_UPCOMING = "Upcoming";
    private static final String TAB_PAST = "Past";
    private static final String TAB_CANCELLED = "Cancelled";

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    private final BookingDetailsDialog bookingDetailsDialog;
    private final BookingCard bookingCard;
    private final EditBookingDialog editBookingDialog;
    private final CancellationDialog cancellationDialog;

    private Tabs tabs;
    private Div contentArea;
    private List<Booking> allBookings;

    public MyBookingsView(SessionService sessionService,
                          BookingService bookingService,
                          PaymentService paymentService,
                          BookingDetailsDialog bookingDetailsDialog,
                          BookingCard bookingCard,
                          EditBookingDialog editBookingDialog,
                          CancellationDialog cancellationDialog) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.bookingDetailsDialog = bookingDetailsDialog;
        this.bookingCard = bookingCard;
        this.editBookingDialog = editBookingDialog;
        this.cancellationDialog = cancellationDialog;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        reloadBookings();

        add(createHeader(), createTabsBar(), createBookingsCard());
        updateContent();
    }

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

        contentArea = new Div();
        contentArea.addClassName("bookings-content-area");
        contentArea.setWidthFull();

        card.add(contentArea);
        return card;
    }

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

        if (tabs == null || tabs.getSelectedTab() == null) {
            contentArea.add(createEmptyMessage("No tab selected."));
            return;
        }

        User user = sessionService.getCurrentUser();
        if (user == null) {
            contentArea.add(createEmptyMessage("No user session."));
            return;
        }

        allBookings = bookingService.findAllBookingsForGuest(user.getId());
        allBookings.forEach(bookingService::calculateBookingPrice);

        String tabLabel = tabs.getSelectedTab().getLabel();
        List<Booking> filteredBookings = filterBookingsByTabType(tabLabel);

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

    private List<Booking> filterBookingsByTabType(String tabLabel) {
        LocalDate today = LocalDate.now();

        return switch (tabLabel) {
            case TAB_UPCOMING -> allBookings.stream()
                    .filter(b -> b.getCheckInDate().isAfter(today)
                            || (!b.getCheckInDate().isAfter(today) && !b.getCheckOutDate().isBefore(today)))
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
            case TAB_PAST -> allBookings.stream()
                    .filter(b -> b.getCheckOutDate().isBefore(today))
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
            case TAB_CANCELLED -> allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
            default -> new ArrayList<>();
        };
    }

    private Button createPayButtonIfNeeded(Booking booking) {
        if (booking.getId() == null || booking.getTotalPrice() == null) {
            return null;
        }

        List<Payment> allPayments = paymentService.findByBookingId(booking.getId());
        List<Payment> pendingPayments = allPayments.stream()
                .filter(p -> p.getStatus() == Invoice.PaymentStatus.PENDING)
                .toList();

        boolean shouldShowButton = !pendingPayments.isEmpty()
                || (booking.getStatus() == BookingStatus.PENDING && allPayments.isEmpty());

        if (!shouldShowButton) {
            return null;
        }

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
                try {
                    paymentService.processPaymentForBooking(bookingId, null, paymentDialog.getSelectedPaymentMethod(), Invoice.PaymentStatus.PAID);
                    Notification.show("Payment completed! Thank you.", 3000, Notification.Position.TOP_CENTER);
                    updateContent();
                } catch (Exception ex) {
                    Notification.show("Error processing payment. Please try again.", 5000, Notification.Position.TOP_CENTER);
                }
            });

            paymentDialog.setOnPaymentDeferred(() -> {
                try {
                    paymentService.processPaymentForBooking(bookingId, null, paymentDialog.getSelectedPaymentMethod(), Invoice.PaymentStatus.PENDING);
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

    private HorizontalLayout createActionButtons(Booking booking, String tab) {
        HorizontalLayout layout = new HorizontalLayout();

        BookingStatus status = booking.getStatus();
        boolean isPast = booking.getCheckOutDate().isBefore(LocalDate.now());

        boolean canEdit = status == BookingStatus.PENDING || status == BookingStatus.MODIFIED;
        boolean canCancel = !isPast && (status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED || status == BookingStatus.MODIFIED);

        if (TAB_UPCOMING.equals(tab)) {
            Button pay = createPayButtonIfNeeded(booking);
            if (pay != null) {
                layout.add(pay);
            }

            if (canEdit) {
                Button edit = new Button("Edit", e -> editBookingDialog.open(booking, () -> {
                    reloadBookings();
                    updateContent();
                }));
                edit.addClassName("primary-button");
                layout.add(edit);
            }

            if (canCancel) {
                Button cancel = new Button("Cancel", e -> cancellationDialog.open(booking, () -> {
                    reloadBookings();
                    updateContent();
                }));
                cancel.addClassName("secondary-button");
                layout.add(cancel);
            }
        }

        if (TAB_PAST.equals(tab)) {
            RouterLink review = new RouterLink("Write Review", MyReviewsView.class);
            review.addClassName("primary-button");
            layout.add(review);
        }

        return layout;
    }

    private Component createEmptyMessage(String message) {
        Paragraph empty = new Paragraph(message);
        empty.getStyle().set("padding", "var(--spacing-xl)");
        empty.getStyle().set("text-align", "center");
        empty.getStyle().set("color", "var(--color-text-secondary)");
        return empty;
    }
}
