package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.List;

@Route(value = "guest-portal", layout = MainLayout.class)
@PageTitle("Guest Portal")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/guest-portal.css")
public class GuestPortalView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    private record Room(int id, String name, String desc, int price, int available, double rating, String image) {}

    public GuestPortalView(SessionService sessionService) {
        this.sessionService = sessionService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createSearchCard(), createRoomsGrid());
    }

    private Component createHeader() {
        H1 title = new H1("Search Available Rooms");
        
        Paragraph subtitle = new Paragraph("Find your perfect accommodation");
        
        return new Div(title, subtitle);
    }

    private Component createSearchCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull(); // WICHTIG: Card nutzt volle Breite
        
        H3 title = new H3("Search & Filters");
        
        Paragraph subtitle = new Paragraph("Customize your search criteria");

        FormLayout layout = new FormLayout();
        
        DatePicker checkIn = new DatePicker("Check-in Date");
        checkIn.setValue(LocalDate.now().plusDays(2));

        DatePicker checkOut = new DatePicker("Check-out Date");
        checkOut.setValue(LocalDate.now().plusDays(5));

        NumberField guests = new NumberField("Number of Guests");
        guests.setValue(2d);
        guests.setStep(1);

        Select<String> type = new Select<>();
        type.setLabel("Room Type");
        type.setItems("All Types", "Standard", "Deluxe", "Suite");
        type.setValue("All Types");

        layout.add(checkIn, checkOut, guests, type);
        layout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2),
            new FormLayout.ResponsiveStep("900px", 4)
        );

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("guest-portal-actions");
        
        Button searchBtn = new Button("Search Rooms", VaadinIcon.SEARCH.create());
        searchBtn.addClassName("primary-button");
        searchBtn.addClickListener(e -> Notification.show("Search executed"));
        
        Button clearBtn = new Button("Clear Filters");
        clearBtn.addClickListener(e -> {
            checkIn.clear();
            checkOut.clear();
            guests.setValue(1d);
            type.setValue("All Types");
        });
        
        actions.add(searchBtn, clearBtn);

        card.add(title, subtitle, layout, actions);
        return card;
    }

    private Component createRoomsGrid() {
        List<Room> rooms = List.of(
                new Room(1, "Standard Room",
                        "Comfortable and cozy room perfect for budget travelers", 
                        89, 8, 4.2,
                        "https://images.unsplash.com/photo-1566665797739-1674de7a421a"),
                new Room(2, "Deluxe Room",
                        "Spacious room with premium amenities and city view", 
                        149, 5, 4.6,
                        "https://images.unsplash.com/photo-1618773928121-c32242e63f39"),
                new Room(3, "Luxury Suite",
                        "Ultimate luxury experience with separate living area", 
                        299, 2, 4.9,
                        "https://images.unsplash.com/photo-1631049307264-da0ec9d70304")
        );

        Div wrapper = new Div();
        wrapper.setWidthFull(); // WICHTIG: Wrapper nutzt volle Breite
        wrapper.addClassName("guest-portal-rooms-wrapper");

        rooms.forEach(room -> wrapper.add(createRoomCard(room)));

        return wrapper;
    }

    private Component createRoomCard(Room room) {
        Div card = new Div();
        card.addClassName("card");
        card.addClassName("guest-portal-room-card");

        // Room Image
        Div imageContainer = new Div();
        imageContainer.addClassName("guest-portal-room-image");
        imageContainer.getStyle()
            .set("background-image", "url('" + room.image() + "')");
        
        // Available badge on image
        Span availableBadge = new Span(room.available() + " available");
        availableBadge.addClassName("guest-portal-available-badge");
        
        imageContainer.add(availableBadge);

        // Content section
        Div content = new Div();
        content.addClassName("guest-portal-room-content");

        // Header with name and price
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("guest-portal-room-header");
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.BASELINE);
        
        H4 name = new H4(room.name());
        
        Div priceContainer = new Div();
        Span price = new Span("€" + room.price());
        price.addClassName("guest-portal-price-large");
        
        Paragraph perNight = new Paragraph("per night");
        
        priceContainer.add(price, perNight);
        
        header.add(name, priceContainer);

        // Rating
        HorizontalLayout rating = new HorizontalLayout();
        rating.addClassName("guest-portal-rating");
        rating.setSpacing(false);
        
        Span star = new Span("⭐");
        Span ratingValue = new Span(String.valueOf(room.rating()));
        ratingValue.addClassName("guest-portal-rating-icon");
        
        rating.add(star, ratingValue);

        // Description
        Paragraph desc = new Paragraph(room.desc());

        // Amenities icons (simplified)
        HorizontalLayout amenities = new HorizontalLayout();
        amenities.addClassName("guest-portal-amenities");
        
        Span wifi = new Span("📶 WiFi");
        Span tv = new Span("📺 TV");
        Span ac = new Span("❄️ AC");
        
        wifi.addClassName("guest-portal-amenity");
        tv.addClassName("guest-portal-amenity");
        ac.addClassName("guest-portal-amenity");
        
        amenities.add(wifi, tv, ac);
        
        if (room.id() > 1) {
            Span miniBar = new Span("🍷 Mini Bar");
            miniBar.addClassName("guest-portal-amenity");
            amenities.add(miniBar);
        }

        // Book button
        Button bookBtn = new Button("Book Now");
        bookBtn.addClassName("primary-button");
        bookBtn.setWidthFull();
        bookBtn.addClickListener(e -> openBookingDialog(room));

        content.add(header, rating, desc, amenities, bookBtn);

        card.add(imageContainer, content);
        return card;
    }

    private void openBookingDialog(Room room) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Confirm Booking");
        d.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.add(new Paragraph("Complete your reservation for " + room.name()));

        Div summary = new Div();
        summary.addClassName("card");
        summary.addClassName("guest-portal-booking-summary");
        
        summary.add(new Paragraph("Room Type: " + room.name()));
        summary.add(new Paragraph("Check-in: 07.11.2025"));
        summary.add(new Paragraph("Check-out: 10.11.2025"));
        summary.add(new Paragraph("Nights: 3"));
        summary.add(new Paragraph("Total: €" + (room.price() * 3)));

        TextField guestName = new TextField("Guest Name");
        guestName.setWidthFull();
        
        EmailField email = new EmailField("Email");
        email.setWidthFull();
        
        TextField phone = new TextField("Phone Number");
        phone.setWidthFull();

        content.add(summary, guestName, email, phone);

        Button confirm = new Button("Confirm Booking");
        confirm.addClassName("primary-button");
        confirm.addClickListener(e -> {
            d.close();
            Notification.show("Booking confirmed! A confirmation email will be sent.");
        });
        
        Button cancel = new Button("Cancel");
        cancel.addClickListener(e -> d.close());

        d.add(content);
        d.getFooter().add(new HorizontalLayout(confirm, cancel));
        d.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.GUEST)) {
            event.rerouteTo(LoginView.class);
        }
    }
}