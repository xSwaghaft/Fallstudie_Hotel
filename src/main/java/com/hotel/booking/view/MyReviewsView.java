package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.FeedbackService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// @Route: registriert die View unter /my-reviews im MainLayout.
// @CssImport: bindet globale und Guest-spezifische Styles ein.
@Route(value = "my-reviews", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
public class MyReviewsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final FeedbackService feedbackService;

    private List<Booking> bookings;

    @Autowired
    public MyReviewsView(SessionService sessionService,
                         BookingService bookingService,
                         FeedbackService feedbackService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.feedbackService = feedbackService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();
        setWidthFull();
        getStyle().set("overflow-x", "hidden");

        bookings = loadBookings();

        add(new H1("Meine Reviews"));
        add(createStatsRow(bookings));
        add(createReviewsList());
        add(createBookingsWithoutReview());
    }

    // Baut die KPI-Leiste mit Anzahl/Status/Ø-Bewertung der Reviews.
    private Component createStatsRow(List<Booking> bookings) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.addClassName("reviews-stats-row");

        int total = bookings.size();
        int written = (int) bookings.stream().filter(b -> b.getFeedback() != null).count();
        int pending = total - written;

        var avgOpt = bookings.stream()
                .map(Booking::getFeedback)
                .filter(f -> f != null && f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average();

        Double average = avgOpt.isPresent() ? avgOpt.getAsDouble() : null;

        row.add(createStatCard("Total", String.valueOf(total)));
        row.add(createStatCard("Geschrieben", String.valueOf(written)));
        row.add(createStatCard("Ausstehend", String.valueOf(pending)));
        row.add(createStatCard("Durchschnitt", average != null ? String.format(Locale.GERMANY, "%.1f", average) : "—"));

        return row;
    }

    // Erzeugt eine einzelne KPI-Kachel mit Icon, Label und Wert.
    private Div createStatCard(String label, String value) {
        Div card = new Div();
        card.addClassName("review-stat-card");
        
        Icon icon = getIconForLabel(label);
        if (icon != null) {
            icon.addClassName("review-stat-icon");
            card.add(icon);
        }
        
        Paragraph labelP = new Paragraph(label);
        labelP.addClassName("review-stat-label");
        Paragraph valueP = new Paragraph(value);
        valueP.addClassName("review-stat-value");
        card.add(labelP, valueP);
        return card;
    }
    
    // Wählt das passende Icon je nach KPI-Bezeichnung.
    private Icon getIconForLabel(String label) {
        return switch (label) {
            case "Total" -> VaadinIcon.CLIPBOARD_TEXT.create();
            case "Geschrieben" -> VaadinIcon.CHECK_CIRCLE.create();
            case "Ausstehend" -> VaadinIcon.CLOCK.create();
            case "Durchschnitt" -> VaadinIcon.STAR.create();
            default -> null;
        };
    }

    // Listet alle Buchungen mit bereits vorhandenem Feedback als Karten auf.
    private Component createReviewsList() {
        Div container = new Div();
        container.addClassName("reviews-list-container");

        List<Booking> bookingsWithReview = bookings.stream()
                .filter(b -> b.getFeedback() != null)
                .collect(Collectors.toList());

        if (bookingsWithReview.isEmpty()) {
            Paragraph empty = new Paragraph("Noch keine Reviews vorhanden.");
            empty.addClassName("reviews-empty-message");
            container.add(empty);
            return container;
        }

        bookingsWithReview.forEach(booking -> {
            if (booking.getFeedback() != null) {
                container.add(createReviewCard(booking));
            }
        });

        return container;
    }

    // Baut eine einzelne Review-Karte mit Buchungsinfo, Sternen und Kommentar.
    private Div createReviewCard(Booking booking) {
        Div card = new Div();
        card.addClassName("review-card");

        Feedback feedback = booking.getFeedback();
        if (feedback == null) {
            return card;
        }

        String roomInfo = booking.getRoomCategory() != null
                ? booking.getRoomCategory().getName()
                : "Zimmer";
        if (booking.getRoom() != null) {
            roomInfo += " #" + booking.getRoom().getRoomNumber();
        }

        Div header = new Div();
        header.addClassName("review-card-header");
        
        H3 bookingNumber = new H3(booking.getBookingNumber());
        bookingNumber.addClassName("review-booking-number");

        Paragraph room = new Paragraph(roomInfo);
        room.addClassName("review-room");
        
        header.add(bookingNumber, room);

        Div stars = createStars(feedback.getRating());
        stars.addClassName("review-stars");

        Paragraph comment = new Paragraph(feedback.getComment() != null ? feedback.getComment() : "");
        comment.addClassName("review-comment");

        Button editButton = new Button("Bearbeiten");
        editButton.addClassName("review-edit-button");
        editButton.addClickListener(e -> openReviewDialog(booking, feedback));

        card.add(header, stars, comment, editButton);
        return card;
    }

    // Rendert Sternsymbole basierend auf der übergebenen Bewertung (1-5).
    private Div createStars(Integer rating) {
        Div starsDiv = new Div();
        starsDiv.addClassName("review-stars-container");
        if (rating != null) {
            for (int i = 1; i <= 5; i++) {
                Span star = new Span("★");
                star.addClassName("review-star");
                if (i > rating) {
                    star.addClassName("empty");
                } else {
                    star.addClassName("filled");
                }
                starsDiv.add(star);
            }
        }
        return starsDiv;
    }

    // Zeigt Buchungen ohne Feedback und bietet Buttons zum Hinzufügen einer Review.
    private Component createBookingsWithoutReview() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        List<Booking> bookingsWithoutReview = bookings.stream()
                .filter(b -> b.getFeedback() == null)
                .collect(Collectors.toList());

        if (bookingsWithoutReview.isEmpty()) {
            return new Div();
        }

        Div card = new Div();
        card.addClassName("reviews-pending-card");
        H3 title = new H3("Buchungen ohne Review");
        card.add(title);

        bookingsWithoutReview.forEach(booking -> {
            String roomInfo = booking.getRoomCategory() != null
                    ? booking.getRoomCategory().getName()
                    : "Zimmer";
            if (booking.getRoom() != null) {
                roomInfo += " #" + booking.getRoom().getRoomNumber();
            }

            Div item = new Div();
            item.addClassName("reviews-pending-item");
            Paragraph info = new Paragraph("Buchung " + booking.getBookingNumber() + " - " + roomInfo);
            Button addButton = new Button("Review hinzufügen");
            addButton.addClassName("primary-button");
            addButton.addClickListener(e -> openReviewDialog(booking, null));
            item.add(info, addButton);
            card.add(item);
        });

        return card;
    }

    // Öffnet den Dialog zum Erstellen oder Bearbeiten einer Review.
    private void openReviewDialog(Booking booking, Feedback existingFeedback) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingFeedback != null ? "Review bearbeiten" : "Review hinzufügen");

        boolean isNew = (existingFeedback == null);
        Feedback feedback = isNew ? new Feedback() : existingFeedback;

        if (feedback == null) {
            return;
        }

        if (isNew) {
            feedback.setBooking(booking);
        }

        Binder<Feedback> binder = new Binder<>(Feedback.class);

        Select<Integer> ratingSelect = new Select<>();
        ratingSelect.setLabel("Bewertung");
        ratingSelect.setItems(1, 2, 3, 4, 5);

        TextArea commentArea = new TextArea("Kommentar");
        commentArea.setMaxLength(1000);

        binder.forField(ratingSelect)
                .asRequired("Bewertung erforderlich")
                .bind(Feedback::getRating, Feedback::setRating);

        binder.forField(commentArea)
                .bind(f -> f.getComment() != null ? f.getComment() : "",
                      (f, value) -> f.setComment(value != null ? value : ""));

        binder.readBean(feedback);

        Button saveButton = new Button("Speichern");
        Button cancelButton = new Button("Abbrechen");

        saveButton.addClickListener(e -> {
            try {
                binder.writeBean(feedback);
                if (feedback.getCreatedAt() == null) {
                    feedback.setCreatedAt(LocalDateTime.now());
                }
                feedbackService.save(feedback);
                Notification.show(isNew ? "Review hinzugefügt!" : "Review aktualisiert!");
                bookings = loadBookings();
                removeAll();
                add(new H1("Meine Reviews"));
                add(createStatsRow(bookings));
                add(createReviewsList());
                add(createBookingsWithoutReview());
                dialog.close();
            } catch (ValidationException ex) {
                Notification.show("Bitte Eingaben prüfen");
            }
        });

        cancelButton.addClickListener(e -> dialog.close());
        FormLayout formLayout = new FormLayout(ratingSelect, commentArea);
        dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    // Lädt vergangene Buchungen des aktuellen Nutzers, um Reviews anzuzeigen.
    private List<Booking> loadBookings() {
        User user = sessionService.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        return bookingService.findPastBookingsForGuest(user.getId());
    }

    // Zugriffsschutz: erlaubt nur eingeloggte Gäste, sonst Redirect zum Login.
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.GUEST)) {
            event.rerouteTo(LoginView.class);
        }
    }
}
