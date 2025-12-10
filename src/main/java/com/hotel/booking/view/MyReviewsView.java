package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "my-reviews", layout = MainLayout.class)
@PageTitle("My Reviews")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/my-reviews.css")
public class MyReviewsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    record Stay(String bookingId, String roomType, String roomNumber, 
                LocalDate checkIn, LocalDate checkOut, boolean hasReview,
                Integer rating, String reviewText, LocalDate reviewDate) {}

    public MyReviewsView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createStatsRow(), createStaysGrid());
    }

    private Component createHeader() {
        H1 title = new H1("My Reviews");
        
        Paragraph subtitle = new Paragraph("Rate your stays and share your experience");
        
        return new Div(title, subtitle);
    }

    private Component createStatsRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.addClassName("stats-row");

        Div card1 = createStatCard("Total Stays", "5", VaadinIcon.BED);
        Div card2 = createStatCard("Reviews Written", "3", VaadinIcon.COMMENT);
        Div card3 = createStatCard("Pending Reviews", "2", VaadinIcon.CLOCK);
        Div card4 = createStatCard("Average Rating", "4.7", VaadinIcon.STAR);
        
        row.add(card1, card2, card3, card4);
        row.expand(card1, card2, card3, card4);

        return row;
    }

    private Div createStatCard(String title, String value, VaadinIcon iconType) {
        Div card = new Div();
        card.addClassName("kpi-card");
        
        Span titleSpan = new Span(title);
        titleSpan.addClassName("kpi-card-title");
        
        Icon icon = iconType.create();
        icon.addClassName("kpi-card-icon");
        
        HorizontalLayout cardHeader = new HorizontalLayout(titleSpan, icon);
        cardHeader.addClassName("kpi-card-header");
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H2 valueHeading = new H2(value);
        
        card.add(cardHeader, valueHeading);
        return card;
    }

    private Component createStaysGrid() {
        Div container = new Div();
        container.setWidthFull();
        container.addClassName("stays-container");

        List<Stay> stays = getMockStays();
        
        for (Stay stay : stays) {
            container.add(createStayCard(stay));
        }

        return container;
    }

    private Component createStayCard(Stay stay) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.addClassName("stay-card-main-layout");
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(FlexComponent.Alignment.START);

        // Left side - Stay Info
        VerticalLayout leftSide = new VerticalLayout();
        leftSide.addClassName("stay-card-left");
        leftSide.setSpacing(false);
        leftSide.setPadding(false);

        H4 roomTitle = new H4(stay.roomType() + " - Room " + stay.roomNumber());

        Paragraph bookingId = new Paragraph("Booking ID: " + stay.bookingId());
        bookingId.addClassName("stay-card-booking-id");

        HorizontalLayout dates = new HorizontalLayout();
        dates.addClassName("stay-card-dates");
        dates.setSpacing(true);

        VerticalLayout checkInBox = new VerticalLayout();
        checkInBox.addClassName("stay-date-box");
        checkInBox.setSpacing(false);
        checkInBox.setPadding(false);
        Span checkInLabel = new Span("Check-in");
        checkInLabel.addClassName("stay-date-label");
        Span checkInDate = new Span(stay.checkIn().format(GERMAN_DATE_FORMAT));
        checkInDate.addClassName("stay-date-value");
        checkInBox.add(checkInLabel, checkInDate);

        VerticalLayout checkOutBox = new VerticalLayout();
        checkOutBox.addClassName("stay-date-box");
        checkOutBox.setSpacing(false);
        checkOutBox.setPadding(false);
        Span checkOutLabel = new Span("Check-out");
        checkOutLabel.addClassName("stay-date-label");
        Span checkOutDate = new Span(stay.checkOut().format(GERMAN_DATE_FORMAT));
        checkOutDate.addClassName("stay-date-value");
        checkOutBox.add(checkOutLabel, checkOutDate);

        dates.add(checkInBox, checkOutBox);

        leftSide.add(roomTitle, bookingId, dates);

        // Right side - Review Section
        VerticalLayout rightSide = new VerticalLayout();
        rightSide.addClassName("stay-card-right");
        rightSide.setSpacing(false);
        rightSide.setPadding(false);

        if (stay.hasReview()) {
            // Show existing review
            Div reviewHeader = new Div();
            reviewHeader.addClassName("review-header");

            HorizontalLayout stars = createStarRating(stay.rating());
            
            Span reviewDateSpan = new Span("Reviewed on " + stay.reviewDate().format(GERMAN_DATE_FORMAT));
            reviewDateSpan.addClassName("review-date");

            reviewHeader.add(stars, reviewDateSpan);

            Paragraph reviewText = new Paragraph(stay.reviewText());
            reviewText.addClassName("review-text");

            Button editBtn = new Button("Edit Review", VaadinIcon.EDIT.create());
            editBtn.addClickListener(e -> openReviewDialog(stay, true));

            rightSide.add(reviewHeader, reviewText, editBtn);
        } else {
            // Show "Leave Review" button
            Paragraph noReview = new Paragraph("You haven't reviewed this stay yet");
            noReview.addClassName("no-review-text");

            Button leaveReviewBtn = new Button("Leave a Review", VaadinIcon.COMMENT.create());
            leaveReviewBtn.addClassName("primary-button");
            leaveReviewBtn.addClickListener(e -> openReviewDialog(stay, false));

            rightSide.add(noReview, leaveReviewBtn);
        }

        mainLayout.add(leftSide, rightSide);
        card.add(mainLayout);

        return card;
    }

    private HorizontalLayout createStarRating(Integer rating) {
        HorizontalLayout stars = new HorizontalLayout();
        stars.addClassName("star-rating");
        stars.setSpacing(false);

        if (rating != null) {
            for (int i = 1; i <= 5; i++) {
                Span star = new Span(i <= rating ? "⭐" : "☆");
                star.getStyle().set("font-size", "1.25rem");
                stars.add(star);
            }
        }

        return stars;
    }

    private void openReviewDialog(Stay stay, boolean isEdit) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isEdit ? "Edit Your Review" : "Leave a Review");
        dialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.addClassName("review-dialog-content");

        H4 stayInfo = new H4(stay.roomType() + " - " + stay.bookingId());
        stayInfo.addClassName("review-dialog-stay-info");

        // Star Rating Selection
        Paragraph ratingLabel = new Paragraph("Your Rating");
        ratingLabel.addClassName("review-dialog-rating-label");

        HorizontalLayout starSelection = new HorizontalLayout();
        starSelection.addClassName("review-dialog-star-selection");
        starSelection.setSpacing(true);

        int[] selectedRating = {isEdit ? stay.rating() : 0};

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button starBtn = new Button(rating <= selectedRating[0] ? "⭐" : "☆");
            starBtn.addClassName("star-button");
            
            starBtn.addClickListener(e -> {
                selectedRating[0] = rating;
                // Update all stars
                for (int j = 0; j < 5; j++) {
                    Button btn = (Button) starSelection.getComponentAt(j);
                    btn.setText(j < rating ? "⭐" : "☆");
                }
            });
            
            starSelection.add(starBtn);
        }

        // Review Text
        TextArea reviewTextArea = new TextArea("Your Review");
        reviewTextArea.addClassName("review-textarea");
        reviewTextArea.setPlaceholder("Share your experience with this stay...");
        reviewTextArea.setWidthFull();
        reviewTextArea.setHeight("150px");
        
        if (isEdit && stay.reviewText() != null) {
            reviewTextArea.setValue(stay.reviewText());
        }

        content.add(stayInfo, ratingLabel, starSelection, reviewTextArea);

        Button submitBtn = new Button(isEdit ? "Update Review" : "Submit Review");
        submitBtn.addClassName("primary-button");
        submitBtn.addClickListener(e -> {
            if (selectedRating[0] == 0) {
                Notification.show("Please select a rating", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (reviewTextArea.isEmpty()) {
                Notification.show("Please write a review", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            dialog.close();
            Notification.show(isEdit ? "Review updated successfully!" : "Thank you for your review!", 
                            3000, Notification.Position.MIDDLE);
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        content.add(stayInfo, ratingLabel, starSelection, reviewTextArea);

        dialog.add(content);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, submitBtn));
        dialog.open();
    }

    private List<Stay> getMockStays() {
        return List.of(
            new Stay("BK001", "Deluxe Room", "302", 
                    LocalDate.of(2025, 11, 5), LocalDate.of(2025, 11, 8),
                    false, null, null, null),
            new Stay("BK006", "Suite", "501", 
                    LocalDate.of(2024, 12, 20), LocalDate.of(2024, 12, 25),
                    true, 5, "Amazing stay! The suite was luxurious and the service was impeccable. Will definitely return!", 
                    LocalDate.of(2024, 12, 26)),
            new Stay("BK008", "Deluxe Room", "310", 
                    LocalDate.of(2024, 8, 10), LocalDate.of(2024, 8, 14),
                    true, 4, "Great room with beautiful city views. Staff was friendly and helpful.", 
                    LocalDate.of(2024, 8, 15)),
            new Stay("BK012", "Standard Room", "205", 
                    LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 18),
                    true, 5, "Perfect for a budget-friendly stay. Clean, comfortable, and convenient location.", 
                    LocalDate.of(2024, 6, 19)),
            new Stay("BK015", "Suite", "402", 
                    LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 13),
                    false, null, null, null)
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.GUEST)) {
            event.rerouteTo(LoginView.class);
        }
    }
}