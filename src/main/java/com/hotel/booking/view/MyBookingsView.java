package com.hotel.booking.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.view.components.*;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.button.Button;
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

    @Autowired
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

        allBookings = loadAllBookingsForCurrentUser();

        add(new H1("My Bookings"));

        if (allBookings.isEmpty()) {
            add(new Paragraph("No bookings found."));
        } else {
            createTabsAndContent();
        }
    }

    private void createTabsAndContent() {
        tabs = new Tabs(
                new Tab(TAB_UPCOMING),
                new Tab(TAB_PAST),
                new Tab(TAB_CANCELLED)
        );
        tabs.addSelectedChangeListener(e -> updateContent());

        contentArea = new Div();
        contentArea.setWidthFull();
        contentArea.addClassName("bookings-content-area");

        add(tabs, contentArea);
        updateContent();
    }

    private void updateContent() {
        contentArea.removeAll();

        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab == null) return;

        User user = sessionService.getCurrentUser();
        if (user != null) {
            allBookings = bookingService.findAllBookingsForGuest(user.getId());
            allBookings.forEach(bookingService::calculateBookingPrice);
        }

        List<Booking> filtered = filterBookingsByTabType(selectedTab.getLabel());

        if (filtered.isEmpty()) {
            contentArea.add(new Paragraph("No bookings in this category."));
            return;
        }

        Div container = new Div();
        container.addClassName("bookings-container");

        for (Booking booking : filtered) {
            HorizontalLayout actions = createActionButtons(booking, selectedTab.getLabel());
            container.add(
                    bookingCard.create(
                            booking,
                            () -> bookingDetailsDialog.open(booking),
                            actions
                    )
            );
        }

        contentArea.add(container);
    }

    private List<Booking> filterBookingsByTabType(String tab) {
        LocalDate today = LocalDate.now();

        switch (tab) {
            case TAB_UPCOMING:
                return allBookings.stream()
                        .filter(b -> !b.getCheckOutDate().isBefore(today))
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

    private Button createPayButtonIfNeeded(Booking booking) {
        List<Payment> payments = paymentService.findByBookingId(booking.getId());

        boolean hasPendingPayment = payments.stream()
                .anyMatch(p -> p.getStatus() == Invoice.PaymentStatus.PENDING);

        boolean noPaymentsYet = payments.isEmpty();
        boolean statusAllowsPay = booking.getStatus() == BookingStatus.PENDING
                || booking.getStatus() == BookingStatus.MODIFIED;

        if (!hasPendingPayment && !(statusAllowsPay && noPaymentsYet)) {
            return null;
        }

        Button pay = new Button("Pay Now", e -> openPaymentDialog(booking));
        pay.addClassName("primary-button");
        return pay;
    }

    private void openPaymentDialog(Booking booking) {
        PaymentDialog dialog = new PaymentDialog(booking.getTotalPrice());

        dialog.setOnPaymentSuccess(() -> {
            paymentService.processPaymentForBooking(
                    booking.getId(),
                    null,
                    dialog.getSelectedPaymentMethod(),
                    Invoice.PaymentStatus.PAID
            );
            updateContent();
        });

        dialog.setOnPaymentDeferred(() -> {
            paymentService.processPaymentForBooking(
                    booking.getId(),
                    null,
                    dialog.getSelectedPaymentMethod(),
                    Invoice.PaymentStatus.PENDING
            );
            updateContent();
        });

        dialog.open();
    }

    private HorizontalLayout createActionButtons(Booking booking, String tab) {
        HorizontalLayout layout = new HorizontalLayout();

        BookingStatus status = booking.getStatus();
        boolean isPast = booking.getCheckOutDate().isBefore(LocalDate.now());

        boolean canEdit = status == BookingStatus.PENDING || status == BookingStatus.MODIFIED;
        boolean canPay = canEdit;
        boolean canCancel = !isPast &&
                (status == BookingStatus.PENDING
                        || status == BookingStatus.CONFIRMED
                        || status == BookingStatus.MODIFIED);

        if (TAB_UPCOMING.equals(tab)) {

            if (canPay) {
                Button pay = createPayButtonIfNeeded(booking);
                if (pay != null) layout.add(pay);
            }

            if (canEdit) {
                Button edit = new Button("Edit", e ->
                        editBookingDialog.open(booking, this::updateContent));
                edit.addClassName("primary-button");
                layout.add(edit);
            }

            if (canCancel) {
                Button cancel = new Button("Cancel", e ->
                        cancellationDialog.open(booking, this::updateContent));
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

    private List<Booking> loadAllBookingsForCurrentUser() {
        User user = sessionService.getCurrentUser();
        return user == null
                ? List.of()
                : bookingService.findAllBookingsForGuest(user.getId());
    }
}
