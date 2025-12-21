package com.hotel.booking.view.components;

import java.util.List;
import java.util.function.Consumer;

import com.hotel.booking.entity.RoomCategory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;

/**
 * Grid component for displaying room categories as cards.
 * 
 * <p>
 * Displays a collection of {@link RoomCard} components in a grid layout.
 * Business logic (e.g., booking actions) is handled by the parent view through
 * the optional card configurator callback.
 * </p>
 * 
 * <p>
 * Used primarily in {@link com.hotel.booking.view.GuestPortalView} to display
 * available room categories after a search operation.
 * </p>
 * 
 * @author Viktor Götting
 */
public class RoomGrid extends Div {

    /**
     * Creates a new RoomGrid component with default styling.
     */
    public RoomGrid() {
        addClassName("room-grid");
        setWidthFull();
    }

    /**
     * Sets the categories to display and renders them as cards.
     * 
     * <p>
     * Clears any existing content and creates a new {@link RoomCard} for each
     * category. If a card configurator is provided, it is called for each card
     * to allow customization (e.g., adding booking buttons).
     * </p>
     * 
     * @param categories the list of room categories to display
     * @param cardConfigurator optional callback to configure each card (e.g., add buttons)
     */
    public void setCategories(List<RoomCategory> categories, Consumer<RoomCard> cardConfigurator) {
        removeAll();

        if (categories == null || categories.isEmpty()) {
            add(new Paragraph("No categories available"));
            return;
        }

        for (RoomCategory category : categories) {
            RoomCard categoryCard = new RoomCard(category);
            
            if (cardConfigurator != null) {
                cardConfigurator.accept(categoryCard);
            }
            
            add(categoryCard);
        }
    }
}
