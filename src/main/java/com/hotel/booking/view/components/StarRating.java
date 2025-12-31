package com.hotel.booking.view.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

/**
 * Component for displaying a star rating with half-star support.
 * Stars can be clickable and trigger a callback.
 * 
 * @author Viktor Götting
 */
public class StarRating extends HorizontalLayout {
    
    /** Maximum rating value */
    private static final int MAX_RATING = 5;
    
    /** Half-star threshold values */
    private static final double HALF_STAR_MIN = 0.25;
    private static final double HALF_STAR_MAX = 0.75;
    
    /** Rating format */
    private static final String RATING_FORMAT = "%.1f";
    
    /** CSS class for the rating container */
    private static final String RATING_CLASS = "room-card__rating";
    
    /** CSS class for clickable wrapper */
    private static final String RATING_CLICKABLE_CLASS = "room-card__rating-clickable";
    
    /** CSS class for rating text */
    private static final String RATING_TEXT_CLASS = "room-card__rating-text";
    
    private final Div clickableStarsWrapper;
    private Runnable onClickCallback;
    
    /**
     * Creates a new StarRating component.
     * 
     * @param average the average rating value (0.0 to 5.0)
     * @param onClick callback to execute when stars are clicked (can be null)
     */
    public StarRating(double average, Runnable onClick) {
        this.onClickCallback = onClick;
        
        addClassName(RATING_CLASS);
        setSpacing(false);
        setPadding(false);
        setAlignItems(Alignment.CENTER);
        
        clickableStarsWrapper = new Div();
        clickableStarsWrapper.addClassName(RATING_CLICKABLE_CLASS);
        
        if (onClick != null) {
            clickableStarsWrapper.addClickListener(e -> onClick.run());
        }
        
        renderStars(average);
        
        Span text = new Span(String.format(java.util.Locale.US, RATING_FORMAT, average));
        text.addClassName(RATING_TEXT_CLASS);
        clickableStarsWrapper.add(text);
        
        add(clickableStarsWrapper);
    }
    
    /**
     * Creates a new StarRating component without click handler.
     * 
     * @param average the average rating value (0.0 to 5.0)
     */
    public StarRating(double average) {
        this(average, null);
    }
    
    /**
     * Sets a callback to be executed when the rating stars are clicked.
     * 
     * @param callback Runnable to execute on click
     */
    public void setOnClick(Runnable callback) {
        this.onClickCallback = callback;
        if (callback != null) {
            clickableStarsWrapper.addClickListener(e -> callback.run());
        }
    }
    
    /**
     * Renders the star rating display.
     * 
     * @param average the average rating value
     */
    private void renderStars(double average) {
        int full = (int) average;
        double remainder = average - full;
        boolean hasHalf = remainder >= HALF_STAR_MIN && remainder < HALF_STAR_MAX;
        
        for (int i = 1; i <= MAX_RATING; i++) {
            if (i <= full) {
                Span star = new Span("★");
                star.addClassName("filled");
                clickableStarsWrapper.add(star);
            } else if (hasHalf && i == full + 1) {
                Div halfStarContainer = new Div();
                halfStarContainer.addClassName("half-star-container");
                
                Span emptyStar = new Span("★");
                emptyStar.addClassName("half-star-empty");
                
                Span filledHalf = new Span("★");
                filledHalf.addClassName("half-star-filled");
                
                halfStarContainer.add(emptyStar, filledHalf);
                clickableStarsWrapper.add(halfStarContainer);
            } else {
                Span star = new Span("★");
                star.addClassName("empty");
                clickableStarsWrapper.add(star);
            }
        }
    }
}
