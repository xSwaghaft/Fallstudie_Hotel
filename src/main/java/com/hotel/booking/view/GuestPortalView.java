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
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@Route(value = "guest-portal", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class GuestPortalView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    private record Room(int id, String name, String desc, int price, int available, double rating, String image) {}

    @Autowired
    public GuestPortalView(SessionService sessionService) {
        this.sessionService = sessionService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createSearchCard(), createRoomsGrid());
    }

    private Component createHeader() {
        H1 title = new H1("Search Available Rooms");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Find your perfect accommodation");
        subtitle.getStyle().set("margin", "0");
        
        return new Div(title, subtitle);
    }

    private Component createSearchCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull(); // WICHTIG: Card nutzt volle Breite
        
        H3 title = new H3("Search & Filters");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Customize your search criteria");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

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
        actions.getStyle().set("gap", "1rem").set("margin-top", "1rem");
        
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
        wrapper.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(320px, 1fr))")
            .set("gap", "1.5rem")
            .set("margin-top", "1rem");

        rooms.forEach(room -> wrapper.add(createRoomCard(room)));

        return wrapper;
    }

    private Component createRoomCard(Room room) {
        Div card = new Div();
        card.addClassName("card");
        card.getStyle()
            .set("padding", "0")
            .set("overflow", "hidden");

        // Room Image
        Div imageContainer = new Div();
        imageContainer.getStyle()
            .set("width", "100%")
            .set("height", "200px")
            .set("background-image", "url('" + room.image() + "')")
            .set("background-size", "cover")
            .set("background-position", "center")
            .set("position", "relative");
        
        // Available badge on image
        Span availableBadge = new Span(room.available() + " available");
        availableBadge.getStyle()
            .set("position", "absolute")
            .set("top", "1rem")
            .set("right", "1rem")
            .set("background", "white")
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "0.5rem")
            .set("font-size", "0.875rem")
            .set("font-weight", "600")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        
        imageContainer.add(availableBadge);

        // Content section
        Div content = new Div();
        content.getStyle().set("padding", "1.25rem");

        // Header with name and price
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.BASELINE);
        
        H4 name = new H4(room.name());
        name.getStyle().set("margin", "0");
        
        Div priceContainer = new Div();
        Span price = new Span("€" + room.price());
        price.getStyle()
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("color", "var(--color-primary)");
        
        Paragraph perNight = new Paragraph("per night");
        perNight.getStyle()
            .set("margin", "0")
            .set("font-size", "0.875rem")
            .set("color", "var(--color-text-secondary)");
        
        priceContainer.add(price, perNight);
        
        header.add(name, priceContainer);

        // Rating
        HorizontalLayout rating = new HorizontalLayout();
        rating.setSpacing(false);
        rating.getStyle().set("margin", "0.5rem 0");
        
        Span star = new Span("⭐");
        Span ratingValue = new Span(String.valueOf(room.rating()));
        ratingValue.getStyle()
            .set("margin-left", "0.25rem")
            .set("font-weight", "600");
        
        rating.add(star, ratingValue);

        // Description
        Paragraph desc = new Paragraph(room.desc());
        desc.getStyle()
            .set("margin", "0.75rem 0")
            .set("color", "var(--color-text-secondary)");

        // Amenities icons (simplified)
        HorizontalLayout amenities = new HorizontalLayout();
        amenities.getStyle().set("gap", "1rem").set("margin", "1rem 0");
        
        Span wifi = new Span("📶 WiFi");
        Span tv = new Span("📺 TV");
        Span ac = new Span("❄️ AC");
        
        wifi.getStyle().set("font-size", "0.875rem");
        tv.getStyle().set("font-size", "0.875rem");
        ac.getStyle().set("font-size", "0.875rem");
        
        amenities.add(wifi, tv, ac);
        
        if (room.id() > 1) {
            Span miniBar = new Span("🍷 Mini Bar");
            miniBar.getStyle().set("font-size", "0.875rem");
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
        summary.getStyle().set("background", "#f9fafb").set("margin-bottom", "1rem");
        
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