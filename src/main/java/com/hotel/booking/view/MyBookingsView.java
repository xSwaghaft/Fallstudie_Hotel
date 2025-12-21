package com.hotel.booking.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.view.components.BookingDetailsDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * View for guest bookings with tabs (Upcoming, Past, Cancelled).
 * Clicking booking opens BookingDetailsDialog. "Write Review" navigates to MyReviewsView.
 * Related: GuestPortalView (creates bookings), LoginView (redirect if not guest).
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
    public MyBookingsView(SessionService sessionService, BookingService bookingService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;

        configureLayout();
        initializeContent();
    }

    /** Configures the layout styling for the view. */
    private void configureLayout() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setHeight(null);
        addClassName("guest-portal-view");
    }

    /** Initializes view content: loads bookings, recalculates prices, and creates tabs. */
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

    /** Creates tab navigation (Upcoming, Past, Cancelled) and content area. Tab selection triggers updateContent(). */
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

    /** Updates content area based on selected tab, filters bookings, and displays them. */
    private void updateContent() {
        contentArea.removeAll();

        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab == null) return;

        String tabLabel = selectedTab.getLabel();
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

    /** Filters bookings by tab type: Upcoming (future/active), Past (completed), or Cancelled. */
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

    /** Creates a booking card item with details and action buttons. Clicking opens BookingDetailsDialog. */
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
        buttonsContainer.add(createActionButtons(tabLabel));
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

    private HorizontalLayout createActionButtons(String tabLabel) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        if (TAB_UPCOMING.equals(tabLabel)) {
            Button editBtn = new Button("Edit");
            editBtn.addClassName("primary-button");
            Button cancelBtn = new Button("Cancel");
            cancelBtn.addClassName("secondary-button");
            buttonsLayout.add(editBtn, cancelBtn);
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

    // =========================================================
    // BOOKING DETAILS DIALOG
    // =========================================================

    /** Opens BookingDetailsDialog to show full booking information. */
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
