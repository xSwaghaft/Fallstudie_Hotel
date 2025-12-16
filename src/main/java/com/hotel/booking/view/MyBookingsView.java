package com.hotel.booking.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.view.components.BookingDetailsDialog;
import com.hotel.booking.view.components.PaymentDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.UI;

/**
 * View for guest bookings with tabs (Upcoming, Past, Cancelled).
 */
@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My Bookings")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/my-bookings.css")
public class MyBookingsView extends VerticalLayout implements BeforeEnterObserver {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String TAB_UPCOMING = "Upcoming";
    private static final String TAB_PAST = "Past";
    private static final String TAB_CANCELLED = "Cancelled";

    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    // =========================================================
    // UI COMPONENTS
    // =========================================================

    private Tabs tabs;
    private Div contentArea;
    private List<Booking> allBookings;

    // =========================================================
    // CONSTRUCTOR & INITIALIZATION
    // =========================================================

    @Autowired
    public MyBookingsView(SessionService sessionService, BookingService bookingService, PaymentService paymentService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;

        configureLayout();
        initializeContent();
    }

    private void configureLayout() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setHeight(null);
        addClassName("guest-portal-view");
    }

    private void initializeContent() {
        add(new H1("My Bookings"));

        User currentUser = sessionService.getCurrentUser();
        if (currentUser == null) {
            add(new Paragraph("No bookings found."));
            return;
        }

        // Load all bookings for current user
        allBookings = bookingService.findAllBookingsForGuest(currentUser.getId());
        if (allBookings.isEmpty()) {
            add(new Paragraph("No bookings found."));
        } else {
            // Calculate live (display only). If you want to show DB values -> delete this line.
            allBookings.forEach(bookingService::calculateBookingPrice);
            createTabsAndContent();
        }
    }

    // =========================================================
    // CONTENT MANAGEMENT (TABS & FILTERING)
    // =========================================================

    private void createTabsAndContent() {
        Tab upcomingTab = new Tab(TAB_UPCOMING);
        Tab pastTab = new Tab(TAB_PAST);
        Tab cancelledTab = new Tab(TAB_CANCELLED);

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
        
        // IMPORTANT: Reload all bookings from database to get fresh payment data
        User currentUser = sessionService.getCurrentUser();
        if (currentUser != null) {
            allBookings = bookingService.findAllBookingsForGuest(currentUser.getId());
            allBookings.forEach(bookingService::calculateBookingPrice);
        }
        
        List<Booking> filteredBookings = filterBookingsByTabType(tabLabel);

        if (filteredBookings.isEmpty()) {
            Paragraph emptyMessage = new Paragraph("No bookings in this category.");
            emptyMessage.addClassName("bookings-empty-message");
            contentArea.add(emptyMessage);
            return;
        }

        VerticalLayout bookingsLayout = new VerticalLayout();
        bookingsLayout.setSpacing(true);
        bookingsLayout.setPadding(false);
        filteredBookings.forEach(booking -> bookingsLayout.add(createBookingItem(booking, tabLabel)));
        contentArea.add(bookingsLayout);
    }

    private List<Booking> filterBookingsByTabType(String tabLabel) {
        if (allBookings == null || allBookings.isEmpty()) {
            return List.of();
        }

        LocalDate today = LocalDate.now();

        return switch (tabLabel) {
            case TAB_UPCOMING -> allBookings.stream()
                    .filter(b -> b.getCheckInDate() != null && b.getCheckOutDate() != null)
                    .filter(b -> b.getCheckOutDate().isAfter(today)
                            || (!b.getCheckInDate().isAfter(today) && !b.getCheckOutDate().isBefore(today)))
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .toList();

            case TAB_PAST -> allBookings.stream()
                    .filter(b -> b.getCheckOutDate() != null)
                    .filter(b -> b.getCheckOutDate().isBefore(today))
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .toList();

            case TAB_CANCELLED -> allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                    .toList();

            default -> List.of();
        };
    }

    // =========================================================
    // BOOKING ITEM CREATION
    // =========================================================

    private Div createBookingItem(Booking booking, String tabLabel) {
        Div card = new Div();
        card.addClassName("booking-item-card");

        Div clickableArea = new Div();
        clickableArea.addClassName("booking-item-clickable");
        clickableArea.addClickListener(e -> openBookingDetailsDialog(booking));

        Div header = new Div();
        header.addClassName("booking-item-header");
        H3 bookingNumber = new H3(booking.getBookingNumber() != null ? booking.getBookingNumber() : "-");
        bookingNumber.addClassName("booking-item-number");
        header.add(bookingNumber, createStatusBadge(booking));

        Div details = new Div();
        details.addClassName("booking-item-details");
        String roomType = booking.getRoomCategory() != null ? booking.getRoomCategory().getName() : "Room";
        String roomNumber = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "-";
        details.add(createDetailItem("Room", roomType + " - " + roomNumber),
                   createDetailItem("Check-in", booking.getCheckInDate() != null ? booking.getCheckInDate().format(GERMAN_DATE_FORMAT) : "-"),
                   createDetailItem("Check-out", booking.getCheckOutDate() != null ? booking.getCheckOutDate().format(GERMAN_DATE_FORMAT) : "-"),
                   createDetailItem("Guests", booking.getAmount() != null ? String.valueOf(booking.getAmount()) : "-"));

        if (booking.getRoomCategory() != null && booking.getRoomCategory().getPricePerNight() != null) {
            BigDecimal ppn = booking.getRoomCategory().getPricePerNight();
            String ppnFormatted = ppn.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " €";
            details.add(createDetailItem("Price per night", ppnFormatted));
        }

        BigDecimal totalPrice = booking.getTotalPrice();
        String totalFormatted = totalPrice != null ? totalPrice.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " €" : "-";
        H3 price = new H3("Total price: " + totalFormatted);
        price.addClassName("booking-item-price");
        clickableArea.add(header, details, price);

        Div buttonsContainer = new Div();
        buttonsContainer.addClassName("booking-item-buttons");
        buttonsContainer.add(createActionButtons(booking, tabLabel));
        card.add(clickableArea, buttonsContainer);
        return card;
    }

    // =========================================================
    // HELPER METHODS (UI COMPONENTS)
    // =========================================================

    private Span createStatusBadge(Booking booking) {
        String statusText = String.valueOf(booking.getStatus());
        Span badge = new Span(statusText);
        badge.addClassName("booking-item-status");
        badge.addClassName(statusText.toLowerCase());
        return badge;
    }

    private HorizontalLayout createActionButtons(Booking booking, String tabLabel) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        if (TAB_UPCOMING.equals(tabLabel)) {
            Button editBtn = new Button("Edit");
            editBtn.addClassName("primary-button");
            Button cancelBtn = new Button("Cancel");
            cancelBtn.addClassName("secondary-button");
            
            // Add Pay button if payment is pending
            Button payBtn = createPayButtonIfNeeded(booking);
            if (payBtn != null) {
                buttonsLayout.add(payBtn, editBtn, cancelBtn);
            } else {
                buttonsLayout.add(editBtn, cancelBtn);
            }
        } else if (TAB_PAST.equals(tabLabel)) {
            RouterLink reviewLink = new RouterLink("Write Review", MyReviewsView.class);
            reviewLink.addClassName("primary-button");
            buttonsLayout.add(reviewLink);
        }
        return buttonsLayout;
    }
    
    /**
     * Creates a "Pay" button if booking has a pending payment
     */
    private Button createPayButtonIfNeeded(Booking booking) {
        // Check if booking has a pending payment
        List<Payment> pendingPayments = paymentService.findByBookingId(booking.getId()).stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING)
                .toList();
        
        if (pendingPayments.isEmpty()) {
            return null; // No pending payment, no button needed
        }
        
        Button payBtn = new Button("Pay Now", e -> openPaymentDialog(booking));
        payBtn.addClassName("primary-button");
        return payBtn;
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
                // Update existing PENDING payment to PAID
                updatePendingPaymentToPaid(bookingId, paymentDialog.getSelectedPaymentMethod());
                
                Notification.show("Payment completed! Thank you.", 3000, Notification.Position.TOP_CENTER);
                
                // Refresh only the current tab content to update badge status
                updateContent();
            });
            
            paymentDialog.setOnPaymentDeferred(() -> {
                System.out.println("DEBUG: Payment deferred!");
                Notification.show("Payment postponed.", 3000, Notification.Position.TOP_CENTER);
            });
            
            paymentDialog.open();
        } catch (Exception ex) {
            System.err.println("DEBUG: Error opening payment dialog: " + ex.getMessage());
            ex.printStackTrace();
            Notification.show("Error opening payment dialog", 5000, Notification.Position.TOP_CENTER);
        }
    }
    
    /**
     * Updates the PENDING payment for a booking to PAID status
     * and updates the booking status to CONFIRMED
     */
    private void updatePendingPaymentToPaid(Long bookingId, String selectedMethod) {
        try {
            // Update payment status
            List<Payment> payments = paymentService.findByBookingId(bookingId);
            
            for (Payment p : payments) {
                if (p.getStatus() == Payment.PaymentStatus.PENDING) {
                    p.setStatus(Payment.PaymentStatus.PAID);
                    p.setPaidAt(LocalDateTime.now());
                    p.setMethod(mapPaymentMethod(selectedMethod));
                    paymentService.save(p);
                    System.out.println("DEBUG: Updated payment " + p.getId() + " to PAID");
                    break;
                }
            }
            
            // Load booking fresh from database and update status to CONFIRMED
            var bookingOpt = bookingService.findById(bookingId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                if (booking.getStatus() == BookingStatus.PENDING) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingService.save(booking);
                    System.out.println("DEBUG: Updated booking " + booking.getId() + " status to CONFIRMED");
                }
            }
        } catch (Exception ex) {
            System.err.println("DEBUG: Error updating payment/booking: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Maps UI payment method string to Payment.PaymentMethod enum
     */
    private Payment.PaymentMethod mapPaymentMethod(String uiMethod) {
        if ("Bank Transfer".equals(uiMethod)) {
            return Payment.PaymentMethod.TRANSFER;
        }
        return Payment.PaymentMethod.CARD;
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

    // =========================================================
    // BOOKING DETAILS DIALOG
    // =========================================================

    private void openBookingDetailsDialog(Booking booking) {
        BookingDetailsDialog dialog = new BookingDetailsDialog(booking);
        dialog.open();
    }


    // =========================================================
    // SECURITY
    // =========================================================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.GUEST)) {
            event.rerouteTo(LoginView.class);
        }
    }
}
