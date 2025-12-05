package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;

@Route(value = "reports", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class ReportsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;

    DatePicker startDate = new DatePicker("Start Date");
    DatePicker endDate = new DatePicker("End Date");

    public ReportsView(SessionService sessionService, BookingService bookingService) {
        this.sessionService = sessionService;

        this.bookingService = bookingService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createFilters(), createKpiRow());
    }

    private Component createHeader() {
        H1 title = new H1("Reports & Analytics");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Comprehensive insights and performance metrics");
        subtitle.getStyle().set("margin", "0");
        
        Div headerLeft = new Div(title, subtitle);
        
        
        HorizontalLayout header = new HorizontalLayout(headerLeft);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    private Component createFilters() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Report Filters");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Select date range and report parameters");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        startDate.setValue(LocalDate.now());
        
        endDate.setValue(LocalDate.now().plusDays(7));

        FormLayout form = new FormLayout(startDate, endDate);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 3)
        );

        Button generateBtn = new Button("Generate Report");
        generateBtn.addClickListener(e -> createKpiRow());
        generateBtn.addClassName("primary-button");
        generateBtn.getStyle().set("margin-top", "1rem");

        card.add(title, subtitle, form, generateBtn);
        return card;
    }

    //Matthias Lohr
    private Component createKpiRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        boolean bookingTrend = getBookingTrend() > 0;

        Component card1 = createKpiCard("Total Revenue", "€589,000", 
            VaadinIcon.DOLLAR, "#D4AF37", "+12.5% from last period", true);
        Component card2 = createKpiCard("Avg Occupancy", "78.3%", 
            VaadinIcon.TRENDING_UP, "#3b82f6", "+5.2% from last period", true);
        Component card3 = createKpiCard("Total Bookings", Integer.toString(
            bookingService.getNumberOfBookingsInPeriod(endDate.getValue(), startDate.getValue())),
            VaadinIcon.CALENDAR, "#10b981", getBookingTrendString(), bookingTrend);
        Component card4 = createKpiCard("Avg Stay Duration", "3.2 days", 
            VaadinIcon.CLOCK, "#8b5cf6", "Consistent with last period", false);
        Component card5 = createKpiCard("Most popular Extra", "Wifi", 
            VaadinIcon.STAR, "#8b5cf6", "Consistent with last period", true);

        row.add(card1, card2, card3, card4);
        row.expand(card1, card2, card3, card4);

        return row;
    }

    //Matthias Lohr
    private Component createKpiCard(String title, String value, VaadinIcon iconType, 
                                   String color, String trend, boolean isPositive) {
        Div card = new Div();
        card.addClassName("kpi-card");
        
        // Header
        Span titleSpan = new Span(title);
        titleSpan.addClassName("kpi-card-title");
        
        Icon icon = iconType.create();
        icon.getStyle().set("color", color);
        
        HorizontalLayout cardHeader = new HorizontalLayout(titleSpan, icon);
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        // Value
        H2 valueHeading = new H2(value);
        valueHeading.getStyle().set("margin", "0.5rem 0");
        
        // Trend
        Span trendSpan = new Span(trend);
        trendSpan.getStyle()
            .set("font-size", "0.875rem")
            .set("color", isPositive ? "#10b981" : "#6b7280")
            .set("font-weight", "500");
        
        card.add(cardHeader, valueHeading, trendSpan);
        return card;
    }

    //Matthias Lohr
    private double getBookingTrend() {
        int thisPeriod = bookingService.getNumberOfBookingsInPeriod(endDate.getValue(), startDate.getValue());
        int comparisonPeriod = bookingService.getNumberOfBookingsInPeriod(
            endDate.getValue().minusMonths(1), startDate.getValue().minusMonths(1));
        double trendPercent = ((thisPeriod - comparisonPeriod)/ comparisonPeriod) * 100;

        return trendPercent;
    }

    //Matthias Lohr
    private String getBookingTrendString() {
        int thisPeriod = bookingService.getNumberOfBookingsInPeriod(endDate.getValue(), startDate.getValue());
        int comparisonPeriod = bookingService.getNumberOfBookingsInPeriod(
            endDate.getValue().minusMonths(1), startDate.getValue().minusMonths(1));
        String trendString;
    
        // Keine Buchungen in dieser, oder Vergleichsperiode
        if (comparisonPeriod == 0) {
            if (thisPeriod > 0) {
                trendString = "No Bookings in comparison period";
            } else {
                trendString = "0% from last period"; 
            }
        } else {
            // Berechne die prozentuale Veränderung (TypeCast zu double - sollte unproblematisch sein)
            double difference = thisPeriod - comparisonPeriod;
            double percentage = (difference / comparisonPeriod) * 100;
            
            // DecimalFormat für die Formatierung
            // Setze DecimalFormatSymbols auf US, für Punkt als Dezimaltrennzeichen
            DecimalFormatSymbols symbol = new DecimalFormatSymbols(Locale.US);
            // Definiert die Vorzeichen, '0.0' für eine Nachkommastelle
            DecimalFormat df = new DecimalFormat("+#0.0;-#0.0", symbol);
            
            // Formatiere den Wert und füge den Rest des Strings hinzu
            String formattedPercentage = df.format(percentage);
            trendString = formattedPercentage + "% from last period";
        }

        return trendString;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}