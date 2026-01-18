package com.hotel.booking.view.components;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.service.FeedbackService;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

/**
 * Component for displaying reviews/feedback for room categories.
 * Handles review card creation, star rating display, and average rating calculation.
 * 
 * @author Viktor Götting
 */
@Component
public class ReviewsSection {
    
    /** Maximum rating value */
    private static final int MAX_RATING = 5;
    
    /** Default category name when category is not available */
    private static final String DEFAULT_CATEGORY_NAME = "Room";
    
    /** Default text for N/A values */
    private static final String NOT_AVAILABLE_TEXT = "N/A";
    
    /** Date format for review dates */
    private static final java.time.format.DateTimeFormatter DATE_FORMAT = 
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    /** Empty state messages */
    private static final String NO_CATEGORY_MESSAGE = "No category specified.";
    private static final String NO_REVIEWS_MESSAGE = "No reviews yet for this room category.";
    private static final String REVIEWED_ON_PREFIX = "Reviewed on: ";
    private static final String AVERAGE_RATING_FORMAT = "Average Rating: %.1f / 5.0";
    
    private final FeedbackService feedbackService;
    
    public ReviewsSection(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }
    
    /**
     * Populates the reviews content div with reviews for the specified category.
     * 
     * @param reviewsDiv the container div to populate
     * @param category the room category to load reviews for
     */
    public void populateReviews(Div reviewsDiv, RoomCategory category) {
        if (category == null || category.getCategory_id() == null) {
            Paragraph empty = new Paragraph(NO_CATEGORY_MESSAGE);
            empty.addClassName("reviews-empty");
            reviewsDiv.add(empty);
            return;
        }

        // Load all reviews for this category
        List<Feedback> reviews = feedbackService.findByRoomCategoryId(category.getCategory_id());

        if (reviews.isEmpty()) {
            Paragraph empty = new Paragraph(NO_REVIEWS_MESSAGE);
            empty.addClassName("reviews-empty");
            reviewsDiv.add(empty);
        } else {
            // Calculate average rating
            double avgRating = calculateAverageRating(reviews);

            // Display average rating
            Div avgDiv = new Div();
            avgDiv.addClassName("reviews-avg-container");
            
            Paragraph avgText = new Paragraph(String.format(java.util.Locale.US, AVERAGE_RATING_FORMAT, avgRating));
            avgText.addClassName("reviews-avg-text");
            avgDiv.add(avgText);
            reviewsDiv.add(avgDiv);

            // Display all reviews
            Div reviewsContainer = new Div();
            reviewsContainer.addClassName("reviews-container");

            reviews.forEach(review -> reviewsContainer.add(createReviewCard(review)));

            reviewsDiv.add(reviewsContainer);
        }
    }

    /**
     * Creates a card displaying a single review.
     * Note: Booking numbers and room numbers are not displayed for privacy reasons.
     * 
     * @param feedback the feedback to display
     * @return a div containing the review card
     */
    private Div createReviewCard(Feedback feedback) {
        Div card = new Div();
        card.addClassName("review-card");

        var booking = feedback.getBooking();
        // Only show category, no room number (privacy)
        String categoryName = booking != null && booking.getRoomCategory() != null 
                ? booking.getRoomCategory().getName() 
                : DEFAULT_CATEGORY_NAME;

        Div header = new Div();
        header.addClassName("review-card__header");

        // Nur Kategorie anzeigen, keine identifizierenden Informationen
        Paragraph category = new Paragraph(categoryName);
        category.addClassName("review-card__category");
        header.add(category);

        Div stars = createStars(feedback.getRating());
        stars.addClassName("review-card__stars");

        Paragraph comment = new Paragraph(feedback.getComment() != null ? feedback.getComment() : "");
        comment.addClassName("review-card__comment");

        Paragraph date = new Paragraph(formatReviewDate(feedback.getCreatedAt()));
        date.addClassName("review-card__date");

        card.add(header, stars, comment, date);
        return card;
    }

    /**
     * Renders rating stars.
     * 
     * @param rating the rating value (1-5)
     * @return a div containing the star rating display
     */
    private Div createStars(Integer rating) {
        Div starsDiv = new Div();
        starsDiv.addClassName("review-stars");
        if (rating != null) {
            for (int i = 1; i <= MAX_RATING; i++) {
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
     * Calculates the average rating from a list of feedback.
     * 
     * @param reviews the list of feedback reviews
     * @return the average rating, or 0.0 if no valid ratings exist
     */
    private double calculateAverageRating(List<Feedback> reviews) {
        return reviews.stream()
                .filter(f -> f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Formats the review date for display.
     * 
     * @param createdAt the creation date of the review
     * @return formatted date string
     */
    private String formatReviewDate(java.time.LocalDateTime createdAt) {
        String dateStr = createdAt != null 
                ? createdAt.format(DATE_FORMAT)
                : NOT_AVAILABLE_TEXT;
        return REVIEWED_ON_PREFIX + dateStr;
    }
}
