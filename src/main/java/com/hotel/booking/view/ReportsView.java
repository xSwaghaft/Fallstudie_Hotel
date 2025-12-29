package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.ReportService;
import com.hotel.booking.view.components.CardFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;

import java.time.LocalDate;

import jakarta.annotation.security.RolesAllowed;

//Matthias Lohr
@Route(value = "reports", layout = MainLayout.class)
@PageTitle("Reports & Analytics")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/reports.css")
@RolesAllowed(UserRole.MANAGER_VALUE)
public class ReportsView extends VerticalLayout {

    private final BookingService bookingService;
    private final ReportService reportService;

    private CardFactory cardFactory = new CardFactory();

    DatePicker startDate = new DatePicker("Start Date");
    DatePicker endDate = new DatePicker("End Date");

    private final VerticalLayout kpiArea = new VerticalLayout();

    public ReportsView(
                        BookingService bookingService, 
                        ReportService reportService) {

        this.bookingService = bookingService;
        this.reportService = reportService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createFilters(), kpiArea);
        kpiArea.add(createKpiArea());
    }

    private Component createHeader() {
        H1 title = new H1("Reports & Analytics");
        title.addClassName("reports-header-title");

        Paragraph subtitle = new Paragraph("Month-over-Month KPI Insights");
        subtitle.getStyle().set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(new Div(title, subtitle));
        header.setWidthFull();

        return header;
    }

    private Component createFilters() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Reporting Period");
        title.addClassName("reports-filters-title");
        
        Paragraph subtitle = new Paragraph("Select date range - Current Period vs. Previous Month");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        startDate.setValue(LocalDate.now());
        
        endDate.setValue(LocalDate.now().plusDays(7));

        FormLayout form = new FormLayout(startDate, endDate);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 3)
        );

        Button generateBtn = new Button("Generate Report");
        generateBtn.addClickListener(e -> refreshKpiArea());
        generateBtn.addClassName("primary-button");
        generateBtn.addClassName("reports-filter-button");

        card.add(title, subtitle, form, generateBtn);
        return card;
    }

    //Matthias Lohr
    private Component createKpiArea() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSpacing(true);
        wrapper.setPadding(false);
        wrapper.setWidthFull();

        // erste Reihe
        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.setSpacing(true);

        Div card1 = cardFactory.createKpiCard("Total Revenue", 
            reportService.getTotalRevenueInPeriod(startDate.getValue(), endDate.getValue()),
            VaadinIcon.DOLLAR, "#D4AF37",
            reportService.getTotalRevenueTrendString(startDate.getValue(), endDate.getValue()),
            reportService.getTotalRevenueTrendPositive(startDate.getValue(), endDate.getValue()));
        card1.setWidthFull();

        Div card2 = cardFactory.createKpiCard("Top performing Category",
         reportService.getTopCategoryInPeriod(startDate.getValue(), endDate.getValue()),
            VaadinIcon.STAR, "#3b82f6",
            reportService.getMostPopularCategoryLastPeriod(startDate.getValue(), endDate.getValue()),
             false);
        card2.setWidthFull();

        Div card3 = cardFactory.createKpiCard("Total Bookings",
            Integer.toString(bookingService.getNumberOfBookingsInPeriod(
                startDate.getValue(), endDate.getValue())),
            VaadinIcon.CALENDAR, "#10b981",
            reportService.getBookingTrendString(startDate.getValue(), endDate.getValue()),
            reportService.getBookingTrendPositive(startDate.getValue(), endDate.getValue()));
        card3.setWidthFull();

        row1.add(card1, card2, card3);
        row1.expand(card1, card2, card3);

        // Zweite Reihe
        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.setSpacing(true);

        Div card4 = cardFactory.createKpiCard("Avg Stay Duration", 
            reportService.getAvgStayDurationInPeriod(startDate.getValue(), endDate.getValue()),
            VaadinIcon.CLOCK, "#8b5cf6",
            reportService.getAvgStayTrendString(startDate.getValue(), endDate.getValue()), 
            reportService.getAvgStayTrendPositive(startDate.getValue(), endDate.getValue()));
        card4.setWidthFull();

        Div card5 = cardFactory.createKpiCard("Most popular Extra",
            reportService.getMostPopularExtraInPeriod(startDate.getValue(), endDate.getValue()),
            VaadinIcon.STAR, "#8b5cf6",
            reportService.getMostPopularExtraLastPeriod(startDate.getValue(), endDate.getValue()),
             false);
        card5.setWidthFull();

        Div card6 = cardFactory.createKpiCard("Revenue per Booking", 
            reportService.getAvgRevenuePerBookingInPeriod(startDate.getValue(), endDate.getValue()),
            VaadinIcon.DOLLAR, "#6366f1",
            reportService.getAvgRevenueTrendString(startDate.getValue(), endDate.getValue()),
            reportService.getAvgRevenueTrendPositive(startDate.getValue(), endDate.getValue()));
        card6.setWidthFull();

        row2.add(card4, card5, card6);
        row2.expand(card4, card5, card6);

        wrapper.add(row1, row2);

        return wrapper;
    }

    private void refreshKpiArea() {
        kpiArea.removeAll();
        kpiArea.add(createKpiArea());
    }
}