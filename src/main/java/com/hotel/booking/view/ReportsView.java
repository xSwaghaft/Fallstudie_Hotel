package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
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

/**
 * View for displaying reports and analytics KPIs.
 * <p>
 * This Vaadin view provides a dashboard-style overview of key performance
 * indicators (KPIs) such as revenue, bookings, average stay duration and trends.
 * Users can select a reporting period and compare the current period with the
 * previous month.
 * </p>
 * <p>
 * Access to this view is restricted to logged-in users with the MANAGER role.
 * </p>
 *
 * @author Matthias Lohr
 */
@Route(value = "reports", layout = MainLayout.class)
@PageTitle("Reports & Analytics")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/reports.css")
public class ReportsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final ReportService reportService;

    private CardFactory cardFactory = new CardFactory();

    DatePicker startDate = new DatePicker("Start Date");
    DatePicker endDate = new DatePicker("End Date");

    private final VerticalLayout kpiArea = new VerticalLayout();

    public ReportsView(SessionService sessionService, 
                        BookingService bookingService, 
                        ReportService reportService) {
        this.sessionService = sessionService;

        this.bookingService = bookingService;
        this.reportService = reportService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createFilters(), kpiArea);
        kpiArea.add(createKpiArea());
    }

    /**
     * Creates the header section of the view, including title and subtitle.
     */
    private Component createHeader() {
        H1 title = new H1("Reports & Analytics");
        title.addClassName("reports-header-title");

        Paragraph subtitle = new Paragraph("Month-over-Month KPI Insights");
        subtitle.getStyle().set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(new Div(title, subtitle));
        header.setWidthFull();

        return header;
    }

      /**
     * Creates the filter section allowing the user to define
     * the reporting period for the KPIs.
     * <p>
     * The selected date range is used to compare the current period
     * against the previous month.
     * </p>
     */
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

    /**
     * Builds the KPI area containing multiple KPI cards arranged in rows.
     * <p>
     * Each card visualizes a specific KPI together with its trend
     * compared to the previous period.
     * </p>
     */
    private Component createKpiArea() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSpacing(true);
        wrapper.setPadding(false);
        wrapper.setWidthFull();

        // first row
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

        // second row
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

    /**
     * Refreshes the KPI area by rebuilding all KPI cards
     * based on the currently selected date range.
     */
    private void refreshKpiArea() {
        kpiArea.removeAll();
        kpiArea.add(createKpiArea());
    }

     /**
     * Navigation guard to restrict access to authorized users.
     * <p>
     * Users who are not logged in or do not have the MANAGER role
     * are redirected to the login view.
     * </p>
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}