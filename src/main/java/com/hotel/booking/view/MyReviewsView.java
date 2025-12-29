package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.FeedbackService;
import com.hotel.booking.view.components.CardFactory;
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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.annotation.security.RolesAllowed;

/**
 * View for displaying and managing guest reviews.
 * 
 * @author Viktor Götting
 */
@Route(value = "my-reviews", layout = MainLayout.class)
@PageTitle("My Reviews")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/my-reviews.css")
@RolesAllowed(UserRole.GUEST_VALUE)
public class MyReviewsView extends VerticalLayout {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final FeedbackService feedbackService;

    private List<Booking> bookings;

    public MyReviewsView(SessionService sessionService,
                         BookingService bookingService,
                         FeedbackService feedbackService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.feedbackService = feedbackService;

        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setHeight(null);
        addClassName("guest-portal-view");

        bookings = loadBookings();
        refreshView();
    }

    /**
     * Refreshes the entire view.
     */
    private void refreshView() {
        removeAll();
        add(new H1("My Reviews"));
        add(createStatsRow(bookings));
        add(createReviewsList());
        add(createBookingsWithoutReview());
    }

    /**
     * Creates a statistics row.
     */
    private Component createStatsRow(List<Booking> bookings) {
        int total = bookings.size();
        int written = (int) bookings.stream().filter(b -> b.getFeedback() != null).count();
        int pending = total - written;

        // Calculate average rating from all reviews
        var avgOpt = bookings.stream()
                .map(Booking::getFeedback)
                .filter(f -> f != null && f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average();

        Double average = avgOpt.isPresent() ? avgOpt.getAsDouble() : null;

        Component statsRow = CardFactory.createStatsRow(
            CardFactory.createStatCard("Total", String.valueOf(total), VaadinIcon.CLIPBOARD_TEXT),
            CardFactory.createStatCard("Written", String.valueOf(written), VaadinIcon.CHECK_CIRCLE),
            CardFactory.createStatCard("Pending", String.valueOf(pending), VaadinIcon.CLOCK),
            CardFactory.createStatCard("Average", average != null ? String.format(Locale.US, "%.1f", average) : "—", VaadinIcon.STAR)
        );
        statsRow.addClassName("reviews-stats-row");
        return statsRow;
    }

    /**
     * Creates a list of all reviews.
     */
    private Component createReviewsList() {
        Div container = new Div();
        container.addClassName("reviews-list-container");

        List<Booking> bookingsWithReview = bookings.stream()
                .filter(b -> b.getFeedback() != null)
                .collect(Collectors.toList());

        if (bookingsWithReview.isEmpty()) {
            Paragraph empty = new Paragraph("No reviews yet.");
            empty.addClassName("reviews-empty-message");
            container.add(empty);
            return container;
        }

        bookingsWithReview.forEach(booking -> container.add(createReviewCard(booking)));

        return container;
    }

    /**
     * Creates a card displaying a single review.
     */
    private Div createReviewCard(Booking booking) {
        Div card = new Div();
        card.addClassName("review-card");

        Feedback feedback = booking.getFeedback();
        String roomInfo = getRoomInfo(booking);

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

        Button editButton = new Button("Edit");
        editButton.addClassName("review-edit-button");
        editButton.addClickListener(e -> openReviewDialog(booking, feedback));

        card.add(header, stars, comment, editButton);
        return card;
    }

    /**
     * Renders rating stars.
     */
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

    /**
     * Creates a section listing all bookings without reviews.
     */
    private Component createBookingsWithoutReview() {
        List<Booking> bookingsWithoutReview = bookings.stream()
                .filter(b -> b.getFeedback() == null)
                .collect(Collectors.toList());

        if (bookingsWithoutReview.isEmpty()) {
            return new Div();
        }

        Div card = new Div();
        card.addClassName("reviews-pending-card");
        H3 title = new H3("Bookings without Review");
        card.add(title);

        bookingsWithoutReview.forEach(booking -> {
            Div item = new Div();
            item.addClassName("reviews-pending-item");
            Paragraph info = new Paragraph("Booking " + booking.getBookingNumber() + " - " + getRoomInfo(booking));
            Button addButton = new Button("Add Review");
            addButton.addClassName("primary-button");
            addButton.addClickListener(e -> openReviewDialog(booking, null));
            item.add(info, addButton);
            card.add(item);
        });

        return card;
    }

    /**
     * Opens a dialog for adding or editing a review.
     */
    private void openReviewDialog(Booking booking, Feedback existingFeedback) {
        Dialog dialog = new Dialog();
        
        // Check if booking already has feedback (even if existingFeedback is null)
        final Feedback feedback;
        final boolean isNew;
        
        if (existingFeedback != null) {
            feedback = existingFeedback;
            isNew = false;
        } else if (booking != null && booking.getId() != null) {
            // Check if booking already has feedback using findByBookingId
            List<Feedback> existingFeedbacks = feedbackService.findByBookingId(booking.getId());
            if (!existingFeedbacks.isEmpty()) {
                feedback = existingFeedbacks.get(0);
                isNew = false;
            } else {
                feedback = new Feedback();
                feedback.setBooking(booking);
                isNew = true;
            }
        } else {
            return; // Cannot create feedback without booking
        }
        
        dialog.setHeaderTitle(isNew ? "Add Review" : "Edit Review");

        Binder<Feedback> binder = new Binder<>(Feedback.class);

        Select<Integer> ratingSelect = new Select<>();
        ratingSelect.setLabel("Rating");
        ratingSelect.setItems(1, 2, 3, 4, 5);

        TextArea commentArea = new TextArea("Comment");
        commentArea.setMaxLength(1000);

        binder.forField(ratingSelect)
                .asRequired("Rating required")
                .bind(Feedback::getRating, Feedback::setRating);

        binder.forField(commentArea)
                .bind(f -> f.getComment() != null ? f.getComment() : "",
                      (f, value) -> f.setComment(value != null ? value : ""));

        binder.readBean(feedback);

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.addClickListener(e -> {
            try {
                binder.writeBean(feedback);
                if (feedback.getCreatedAt() == null) {
                    feedback.setCreatedAt(LocalDateTime.now());
                }
                
                // Set guest (User) if not already set
                if (feedback.getGuest() == null && booking != null && booking.getGuest() != null) {
                    feedback.setGuest(booking.getGuest());
                }
                
                feedbackService.save(feedback);
                Notification.show(isNew ? "Review added!" : "Review updated!");
                bookings = loadBookings();
                refreshView();
                dialog.close();
            } catch (ValidationException ex) {
                Notification.show("Please check your inputs");
            } catch (Exception ex) {
                Notification.show("Error saving review: " + ex.getMessage());
            }
        });
        saveButton.addClassName("primary-button");

        cancelButton.addClickListener(e -> dialog.close());
        FormLayout formLayout = new FormLayout(ratingSelect, commentArea);
        dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    /**
     * Returns room info.
     */
    private String getRoomInfo(Booking booking) {
        String type = booking.getRoomCategory() != null ? booking.getRoomCategory().getName() : "Room";
        String number = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : null;
        return number != null ? type + " #" + number : type;
    }

    /**
     * Loads all past completed bookings for the current guest.
     */
    private List<Booking> loadBookings() {
        User user = sessionService.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        return bookingService.findPastBookingsForGuest(user.getId());
    }
}
