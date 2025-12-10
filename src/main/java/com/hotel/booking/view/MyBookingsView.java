package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;

import java.util.List;

@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My Bookings")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/my-bookings.css")
public class MyBookingsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    record Booking(String id, String roomType, String roomNumber, String checkIn, 
                  String checkOut, int guests, int amount, String status, String image) {}

    private VerticalLayout contentArea;

    public MyBookingsView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createTabsAndContent());
    }

    private Component createHeader() {
        H1 title = new H1("My Bookings");
        
        Paragraph subtitle = new Paragraph("Manage your reservations and leave reviews");
        
        return new Div(title, subtitle);
    }

    private Component createTabsAndContent() {
        Tab upcomingTab = new Tab("Upcoming (2)");
        Tab pastTab = new Tab("Past (2)");
        
        Tabs tabs = new Tabs(upcomingTab, pastTab);
        tabs.setWidthFull();
        
        contentArea = new VerticalLayout();
        contentArea.setSpacing(true);
        contentArea.setPadding(false);
        
        // Initial content
        updateContent(true);
        
        tabs.addSelectedChangeListener(e -> {
            updateContent(tabs.getSelectedTab() == upcomingTab);
        });
        
        VerticalLayout container = new VerticalLayout(tabs, contentArea);
        container.setSpacing(false);
        container.setPadding(false);
        
        return container;
    }

    private void updateContent(boolean upcoming) {
        contentArea.removeAll();
        
        List<Booking> bookings = upcoming ? getUpcomingBookings() : getPastBookings();
        
        for (Booking booking : bookings) {
            contentArea.add(createBookingCard(booking, upcoming));
        }
    }

    private Component createBookingCard(Booking booking, boolean isUpcoming) {
        Div card = new Div();
        card.addClassName("card");
        card.addClassName("my-bookings-card");
        
        // Status Badge
        Span statusBadge = new Span(booking.status());
        statusBadge.addClassName("status-badge");
        statusBadge.addClassName("status-" + booking.status());
        statusBadge.addClassName("my-bookings-status-badge");
        
        // Image
        Div imageContainer = new Div();
        imageContainer.addClassName("my-bookings-image");
        imageContainer.getStyle()
            .set("background-image", "url('" + booking.image() + "')");
        
        imageContainer.add(statusBadge);
        
        // Content
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.addClassName("my-bookings-content");
        
        H4 roomType = new H4(booking.roomType());
        roomType.addClassName("my-bookings-room-type");
        
        Paragraph roomNumber = new Paragraph("Room " + booking.roomNumber());
        roomNumber.addClassName("my-bookings-room-number");
        
        Paragraph bookingId = new Paragraph("Booking ID: " + booking.id());
        
        // Dates
        HorizontalLayout dates = new HorizontalLayout();
        dates.setSpacing(true);
        
        VerticalLayout checkInBox = new VerticalLayout();
        checkInBox.setSpacing(false);
        checkInBox.setPadding(false);
        Span checkInLabel = new Span("Check-in");
        checkInLabel.addClassName("my-bookings-date-label");
        Span checkInDate = new Span(booking.checkIn());
        checkInDate.addClassName("my-bookings-date-value");
        checkInBox.add(checkInLabel, checkInDate);
        
        VerticalLayout checkOutBox = new VerticalLayout();
        checkOutBox.setSpacing(false);
        checkOutBox.setPadding(false);
        Span checkOutLabel = new Span("Check-out");
        checkOutLabel.addClassName("my-bookings-date-label");
        Span checkOutDate = new Span(booking.checkOut());
        checkOutDate.addClassName("my-bookings-date-value");
        checkOutBox.add(checkOutLabel, checkOutDate);
        
        VerticalLayout guestsBox = new VerticalLayout();
        guestsBox.setSpacing(false);
        guestsBox.setPadding(false);
        Span guestsLabel = new Span("Guests");
        guestsLabel.addClassName("my-bookings-date-label");
        Span guestsValue = new Span(booking.guests() + " guests");
        guestsValue.addClassName("my-bookings-date-value");
        guestsBox.add(guestsLabel, guestsValue);
        
        dates.add(checkInBox, checkOutBox, guestsBox);
        
        content.add(roomType, roomNumber, bookingId, dates);
        
        // Right side - Price and Actions
        VerticalLayout rightSide = new VerticalLayout();
        rightSide.setSpacing(false);
        rightSide.setPadding(false);
        rightSide.setAlignItems(FlexComponent.Alignment.END);
        
        H3 price = new H3("$" + booking.amount());
        price.addClassName("my-bookings-price");
        
        Paragraph nights = new Paragraph("3 nights");
        
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "auto");
        
        Button modifyBtn = new Button("Modify", VaadinIcon.EDIT.create());
        Button cancelBtn = new Button("Cancel", VaadinIcon.TRASH.create());
        cancelBtn.addClassName("my-bookings-cancel-button");
        
        if (isUpcoming) {
            actions.add(modifyBtn, cancelBtn);
        }
        
        rightSide.add(price, nights, actions);
        
        card.add(imageContainer, content, rightSide);
        return card;
    }

    private List<Booking> getUpcomingBookings() {
        return List.of(
            new Booking("BK001", "Deluxe Room", "302", "2025-11-05", "2025-11-08", 2, 447, "confirmed", 
                       "https://images.unsplash.com/photo-1618773928121-c32242e63f39"),
            new Booking("BK006", "Suite", "501", "2025-12-20", "2025-12-25", 3, 1495, "confirmed",
                       "https://images.unsplash.com/photo-1631049307264-da0ec9d70304")
        );
    }

    private List<Booking> getPastBookings() {
        return List.of(
            new Booking("BK008", "Deluxe Room", "310", "2025-08-10", "2025-08-14", 2, 596, "confirmed",
                       "https://images.unsplash.com/photo-1618773928121-c32242e63f39"),
            new Booking("BK007", "Standard Room", "205", "2025-09-15", "2025-09-18", 1, 267, "confirmed",
                       "https://images.unsplash.com/photo-1566665797739-1674de7a421a")
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.GUEST)) {
            event.rerouteTo(LoginView.class);
        }
    }
}