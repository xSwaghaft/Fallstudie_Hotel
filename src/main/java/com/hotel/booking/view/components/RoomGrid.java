package com.hotel.booking.view.components;

import java.util.List;
import java.util.function.Consumer;

import com.hotel.booking.entity.RoomCategory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;

/**
 * Grid component for displaying room categories as cards.
 * This is a reusable container component that displays RoomCards.
 * Business logic (e.g., booking) should be handled by the parent view.
 */
public class RoomGrid extends Div {

    /**
     * Creates a RoomGrid component.
     */
    public RoomGrid() {
        addClassName("room-grid");
        setWidthFull();
    }

    /**
     * Sets the categories to display and renders them as cards.
     *
     * @param cardConfigurator Optional callback to configure each card (e.g., add buttons)
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
