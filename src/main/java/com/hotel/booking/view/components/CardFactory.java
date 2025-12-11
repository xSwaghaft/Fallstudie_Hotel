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
 * Factory für die Erstellung von wiederverwendbaren Card-Komponenten.
 * Ermöglicht flexible und konsistente Card-Layouts in verschiedenen Views.
 */
public class CardFactory {

    // ==================== STAT CARD (mit Farbe) ====================
    
    /**
     * Erstellt eine Statistik-Card mit Label und Wert
     * @param label Beschriftung (z.B. "Total Rooms")
     * @param value Anzahl/Wert
     * @return Div-Komponente als Card
     */
    public static Div createStatCard(String label, String value, String color) {
        Div card = new Div();
        card.addClassName("stat-card");

        Paragraph labelP = new Paragraph(label);
        labelP.addClassName("stat-card-label");

        H2 valueH = new H2(value);
        valueH.addClassName("stat-card-value");

        card.add(labelP, valueH);
        return card;
    }

    // ==================== STAT CARD MIT ICON ====================

    /**
     * Erstellt eine Statistik-Card mit Label, Wert und Icon (für Views wie UserManagement)
     * @param label Beschriftung
     * @param value Anzahl/Wert
     * @param iconType VaadinIcon-Typ
     * @return Div-Komponente als Card
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

    // ==================== CONTENT CARD (mit Grid und Header) ====================

    /**
     * Erstellt eine Content-Card mit Titel, Untertitel, Action-Button und Grid
     * @param title Haupttitel
     * @param subtitle Untertitel/Beschreibung
     * @param buttonText Text für Add-Button (null wenn nicht gewünscht)
     * @param buttonClickListener Click-Listener für Button (null wenn nicht gewünscht)
     * @param buttonColor Farbe des Buttons
     * @param grid Grid-Komponente mit Daten
     * @return VerticalLayout als Card
     */
    public static VerticalLayout createContentCard(
            String title,
            String subtitle,
            String buttonText,
            Runnable buttonClickListener,
            String buttonColor,
            Grid<?> grid) {

        VerticalLayout card = new VerticalLayout();
        card.addClassName("content-card");
        card.setPadding(true);
        card.setSpacing(true);

        // Header mit Titel und Button
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

        // Button hinzufügen wenn vorhanden
        if (buttonText != null && buttonClickListener != null) {
            Button addBtn = new Button(buttonText, VaadinIcon.PLUS.create());
            addBtn.addClassName("primary-button");
            addBtn.addClassName("content-card-button");
            addBtn.getStyle().set("background", buttonColor).set("color", "white");
            addBtn.addClickListener(e -> buttonClickListener.run());
            headerRow.add(addBtn);
        }

        // Grid konfigurieren
        grid.setWidthFull();
        grid.setHeightFull();

        card.add(headerRow, grid);
        card.setFlexGrow(1, grid);
        return card;
    }

    // ==================== SIMPLE CONTENT CARD (ohne Button) ====================

    /**
     * Erstellt eine einfache Content-Card mit Titel, Untertitel und Grid (ohne Button)
     * @param title Haupttitel
     * @param subtitle Untertitel/Beschreibung
     * @param grid Grid-Komponente mit Daten
     * @return VerticalLayout als Card
     */
    public static VerticalLayout createContentCard(String title, String subtitle, Grid<?> grid) {
        return createContentCard(title, subtitle, null, null, null, grid);
    }

    // ==================== CUSTOM CONTENT CARD ====================

    /**
     * Erstellt eine Card mit benutzerdefinierten Komponenten
     * @param title Haupttitel
     * @param subtitle Untertitel
     * @param buttonText Text für Add-Button (null wenn nicht gewünscht)
     * @param buttonClickListener Click-Listener (null wenn nicht gewünscht)
     * @param buttonColor Farbe des Buttons
     * @param content Beliebige Komponenten/Content
     * @return VerticalLayout als Card
     */
    public static VerticalLayout createCustomCard(
            String title,
            String subtitle,
            String buttonText,
            Runnable buttonClickListener,
            String buttonColor,
            Component... content) {

        VerticalLayout card = new VerticalLayout();
        card.addClassName("content-card");
        card.setPadding(true);
        card.setSpacing(true);

        // Header
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

        if (buttonText != null && buttonClickListener != null) {
            Button addBtn = new Button(buttonText, VaadinIcon.PLUS.create());
            addBtn.addClassName("primary-button");
            addBtn.addClassName("content-card-button");
            addBtn.getStyle().set("background", buttonColor).set("color", "white");
            addBtn.addClickListener(e -> buttonClickListener.run());
            headerRow.add(addBtn);
        }

        card.add(headerRow);
        for (Component c : content) {
            card.add(c);
        }

        return card;
    }

    // ==================== STATS ROW (flexibel) ====================

    /**
     * Erstellt eine horizontale Reihe mit Stat-Cards
     * @param cards Variable Anzahl von Stat-Cards
     * @return HorizontalLayout mit Cards
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

    // ==================== MINIMAL CARD ====================

    /**
     * Erstellt eine minimalistische Card für einfache Inhalte
     * @param content Komponenten die in der Card angezeigt werden
     * @return Div als Card
     */
    public static Div createMinimalCard(Component... content) {
        Div card = new Div();
        card.addClassName("minimal-card");

        for (Component c : content) {
            card.add(c);
        }

        return card;
    }

    //Matthias Lohr
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
