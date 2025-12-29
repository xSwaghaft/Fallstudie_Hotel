package com.hotel.booking.view.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Factory for creating reusable card components.
 * Provides flexible and consistent card layouts for various views.
 * 
 * @author Matthias Lohr
 */
public class CardFactory {

    // ==================== STAT CARD (with color) ====================

    /**
     * Creates a statistics card with a label and value.
     * @param label Label (e.g., "Total Rooms")
     * @param value Value or count
     * @param color Color of the card
     * @return Div component representing the card
     */
    public static Div createStatCard(String label, String value) {
        Div card = new Div();
        card.addClassName("stat-card");

        Paragraph labelP = new Paragraph(label);
        labelP.addClassName("stat-card-label");

        H2 valueH = new H2(value);
        valueH.addClassName("stat-card-value");

        card.add(labelP, valueH);
        return card;
    }

    // ==================== STAT CARD WITH ICON ====================

    /**
     * Creates a statistics card with a label, value, and icon (for views like UserManagement).
     * @param label Label of the card
     * @param value Value or count
     * @param iconType VaadinIcon type
     * @return Div component representing the card
     */
    public static Div createStatCard(String label, String value, VaadinIcon iconType) {
        Div card = new Div();
        card.addClassName("kpi-card");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("kpi-card-title");

        Icon icon = iconType.create();
        icon.addClassName("kpi-card-icon");

        HorizontalLayout cardHeader = new HorizontalLayout(labelSpan, icon);
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        cardHeader.getStyle().set("margin-bottom", "0.5rem");

        H2 valueHeading = new H2(value);
        valueHeading.getStyle().set("margin", "0");

        card.add(cardHeader, valueHeading);
        return card;
    }

    // ==================== CONTENT CARD (with Grid and header) ====================

    /**
     * Creates a content card with title, subtitle, action button, and grid.
     * @param title Main title
     * @param subtitle Subtitle/description
     * @param buttonText Text for the add button (null if not needed)
     * @param buttonClickListener Click listener for the button (null if not needed)
     * @param buttonColor Color of the button
     * @param grid Grid component with data
     * @return VerticalLayout representing the content card
     */
    public static VerticalLayout createContentCard(
            String title,
            String subtitle,
            String buttonText,
            Runnable buttonClickListener,
            Grid<?> grid) {

        VerticalLayout card = new VerticalLayout();
        card.addClassName("content-card");
        card.setPadding(true);
        card.setSpacing(true);

        // Header with title and button
        H3 titleH3 = new H3(title);
        titleH3.addClassName("content-card-title");

        Paragraph subtitleP = new Paragraph(subtitle);
        subtitleP.addClassName("content-card-subtitle");

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Div titleBox = new Div(titleH3, subtitleP);
        headerRow.add(titleBox);

        // Add button if provided
        if (buttonText != null && buttonClickListener != null) {
            Button addBtn = new Button(buttonText, VaadinIcon.PLUS.create());
            addBtn.addClassName("primary-button");
            addBtn.addClassName("content-card-button");
            addBtn.addClickListener(e -> buttonClickListener.run());
            headerRow.add(addBtn);
        }

        // Configure grid
        grid.setWidthFull();
        grid.setHeightFull();

        card.add(headerRow, grid);
        card.setFlexGrow(1, grid);
        return card;
    }

    // ==================== SIMPLE CONTENT CARD (without button) ====================

    /**
     * Creates a simple content card with title, subtitle, and grid (without a button).
     * @param title Main title
     * @param subtitle Subtitle/description
     * @param grid Grid component with data
     * @return VerticalLayout representing the content card
     */
    public static VerticalLayout createContentCard(String title, String subtitle, Grid<?> grid) {
        return createContentCard(title, subtitle, null, null, grid);
    }

    // ==================== STATS ROW (flexible) ====================

    /**
     * Creates a horizontal row with stat cards.
     * @param cards Variable number of stat cards
     * @return HorizontalLayout containing the cards
     */
    public static HorizontalLayout createStatsRow(Component... cards) {
        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.setWidthFull();
        statsRow.setSpacing(true);

        for (Component card : cards) {
            statsRow.add(card);
            statsRow.expand(card);
        }

        return statsRow;
    }

    // ==================== KPI CARD (with trend display) ====================

    /**
     * Creates a KPI card with title, value, icon, color, and trend display.
     * @param title Title of the KPI
     * @param value Value of the KPI
     * @param iconType VaadinIcon type
     * @param color Color of the icon
     * @param trend Trend description (e.g., "+5% since last month")
     * @param isPositive true if trend is positive, false otherwise
     * @return Div component representing the KPI card
     */
    public Div createKpiCard(String title, String value, VaadinIcon iconType, 
                                   String color, String trend, boolean isPositive) {
        Div card = new Div();
        card.addClassName("kpi-card");

        // Header
        Span titleSpan = new Span(title);
        titleSpan.addClassName("kpi-card-title");

        Icon icon = iconType.create();
        icon.getStyle().set("color", color);

        HorizontalLayout cardHeader = new HorizontalLayout(titleSpan, icon);
        cardHeader.addClassName("kpi-card-header");
        cardHeader.setWidthFull();

        // Value
        H2 valueHeading = new H2(value);
        valueHeading.addClassName("kpi-card-value");

        // Trend
        Span trendSpan = new Span(trend);
        trendSpan.addClassName("kpi-card-trend");
        if (isPositive) {
            trendSpan.addClassName("positive");
        } else {
            trendSpan.addClassName("neutral");
        }

        card.add(cardHeader, valueHeading, trendSpan);
        return card;
    }
}

