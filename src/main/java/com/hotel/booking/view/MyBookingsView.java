package com.hotel.booking.view;

import com.hotel.booking.security.SessionService;
import com.hotel.booking.security.UserRole;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "my-bookings", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class MyBookingsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    record Booking(String id, String roomType, String roomNumber, String checkIn, 
                  String checkOut, int guests, int amount, String status, String image) {}

    private VerticalLayout contentArea;

    @Autowired
    public MyBookingsView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createTabsAndContent());
    }

    private Component createHeader() {
        H1 title = new H1("My Bookings");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Manage your reservations and leave reviews");
        subtitle.getStyle().set("margin", "0");
        
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
        card.getStyle()
            .set("display", "flex")
            .set("gap", "1.5rem")
            .set("position", "relative");
        
        // Status Badge
        Span statusBadge = new Span(booking.status());
        statusBadge.addClassName("status-badge");
        statusBadge.addClassName("status-" + booking.status());
        statusBadge.getStyle()
            .set("position", "absolute")
            .set("top", "1rem")
            .set("left", "1rem")
            .set("z-index", "10");
        
        // Image
        Div imageContainer = new Div();
        imageContainer.getStyle()
            .set("width", "200px")
            .set("height", "150px")
            .set("border-radius", "0.75rem")
            .set("background-image", "url('" + booking.image() + "')")
            .set("background-size", "cover")
            .set("background-position", "center")
            .set("flex-shrink", "0");
        
        imageContainer.add(statusBadge);
        
        // Content
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.getStyle().set("flex", "1");
        
        H4 roomType = new H4(booking.roomType());
        roomType.getStyle().set("margin", "0");
        
        Paragraph roomNumber = new Paragraph("Room " + booking.roomNumber());
        roomNumber.getStyle()
            .set("margin", "0")
            .set("color", "var(--color-text-secondary)");
        
        Paragraph bookingId = new Paragraph("Booking ID: " + booking.id());
        bookingId.getStyle()
            .set("margin", "0.5rem 0")
            .set("font-size", "0.875rem")
            .set("color", "var(--color-text-secondary)");
        
        // Dates
        HorizontalLayout dates = new HorizontalLayout();
        dates.setSpacing(true);
        
        VerticalLayout checkInBox = new VerticalLayout();
        checkInBox.setSpacing(false);
        checkInBox.setPadding(false);
        Span checkInLabel = new Span("Check-in");
        checkInLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--color-text-secondary)");
        Span checkInDate = new Span(booking.checkIn());
        checkInDate.getStyle().set("font-weight", "600");
        checkInBox.add(checkInLabel, checkInDate);
        
        VerticalLayout checkOutBox = new VerticalLayout();
        checkOutBox.setSpacing(false);
        checkOutBox.setPadding(false);
        Span checkOutLabel = new Span("Check-out");
        checkOutLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--color-text-secondary)");
        Span checkOutDate = new Span(booking.checkOut());
        checkOutDate.getStyle().set("font-weight", "600");
        checkOutBox.add(checkOutLabel, checkOutDate);
        
        VerticalLayout guestsBox = new VerticalLayout();
        guestsBox.setSpacing(false);
        guestsBox.setPadding(false);
        Span guestsLabel = new Span("Guests");
        guestsLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--color-text-secondary)");
        Span guestsValue = new Span(booking.guests() + " guests");
        guestsValue.getStyle().set("font-weight", "600");
        guestsBox.add(guestsLabel, guestsValue);
        
        dates.add(checkInBox, checkOutBox, guestsBox);
        
        content.add(roomType, roomNumber, bookingId, dates);
        
        // Right side - Price and Actions
        VerticalLayout rightSide = new VerticalLayout();
        rightSide.setSpacing(false);
        rightSide.setPadding(false);
        rightSide.setAlignItems(FlexComponent.Alignment.END);
        
        H3 price = new H3("$" + booking.amount());
        price.getStyle()
            .set("margin", "0")
            .set("color", "var(--color-primary)");
        
        Paragraph nights = new Paragraph("3 nights");
        nights.getStyle()
            .set("margin", "0")
            .set("font-size", "0.875rem")
            .set("color", "var(--color-text-secondary)");
        
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "auto");
        
        Button modifyBtn = new Button("Modify", VaadinIcon.EDIT.create());
        Button cancelBtn = new Button("Cancel", VaadinIcon.TRASH.create());
        cancelBtn.getStyle().set("color", "#ef4444");
        
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