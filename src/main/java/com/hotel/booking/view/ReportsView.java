package com.hotel.booking.view;

import com.hotel.booking.security.SessionService;
import com.hotel.booking.security.UserRole;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "reports", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class ReportsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    @Autowired
    public ReportsView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createFilters(), createKpiRow(), createChartsPlaceholder());
    }

    private Component createHeader() {
        H1 title = new H1("Reports & Analytics");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Comprehensive insights and performance metrics");
        subtitle.getStyle().set("margin", "0");
        
        Div headerLeft = new Div(title, subtitle);
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        
        Button exportPdf = new Button("Export PDF", VaadinIcon.DOWNLOAD.create());
        Button exportCsv = new Button("Export CSV", VaadinIcon.DOWNLOAD.create());
        
        buttons.add(exportPdf, exportCsv);
        
        HorizontalLayout header = new HorizontalLayout(headerLeft, buttons);
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

        DatePicker startDate = new DatePicker("Start Date");
        startDate.setValue(LocalDate.of(2025, 1, 1));
        
        DatePicker endDate = new DatePicker("End Date");
        endDate.setValue(LocalDate.of(2025, 10, 31));
        
        Select<String> reportType = new Select<>();
        reportType.setLabel("Report Type");
        reportType.setItems("Overview", "Revenue", "Occupancy", "Bookings");
        reportType.setValue("Overview");

        FormLayout form = new FormLayout(startDate, endDate, reportType);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 3)
        );

        Button generateBtn = new Button("Generate Report");
        generateBtn.addClassName("primary-button");
        generateBtn.getStyle().set("margin-top", "1rem");

        card.add(title, subtitle, form, generateBtn);
        return card;
    }

    private Component createKpiRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        Component card1 = createKpiCard("Total Revenue", "€589,000", 
            VaadinIcon.DOLLAR, "#D4AF37", "+12.5% from last period", true);
        Component card2 = createKpiCard("Avg Occupancy", "78.3%", 
            VaadinIcon.TRENDING_UP, "#3b82f6", "+5.2% from last period", true);
        Component card3 = createKpiCard("Total Bookings", "1,507", 
            VaadinIcon.CALENDAR, "#10b981", "+8.7% from last period", true);
        Component card4 = createKpiCard("Avg Stay Duration", "3.2 days", 
            VaadinIcon.CLOCK, "#8b5cf6", "Consistent with last period", false);

        row.add(card1, card2, card3, card4);
        row.expand(card1, card2, card3, card4);

        return row;
    }

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

    private Component createChartsPlaceholder() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        section.setWidthFull();

        // Chart placeholders with sample data visualization
        section.add(createRevenueChartPlaceholder());

        HorizontalLayout twoColumnRow = new HorizontalLayout();
        twoColumnRow.setWidthFull();
        twoColumnRow.setSpacing(true);
        
        Component occupancyChart = createOccupancyChartPlaceholder();
        Component roomTypeChart = createRoomTypeChartPlaceholder();
        
        twoColumnRow.add(occupancyChart, roomTypeChart);
        twoColumnRow.expand(occupancyChart, roomTypeChart);
        
        section.add(twoColumnRow);

        section.add(createWeeklyBookingChartPlaceholder());

        return section;
    }

    private Component createRevenueChartPlaceholder() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Revenue & Bookings Trend");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Monthly revenue and booking count over time");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        Div chartPlaceholder = new Div();
        chartPlaceholder.getStyle()
            .set("height", "350px")
            .set("background", "linear-gradient(180deg, #f9fafb 0%, #ffffff 100%)")
            .set("border-radius", "0.5rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("border", "2px dashed #e5e7eb");
        
        Div content = new Div();
        content.getStyle().set("text-align", "center");
        
        Icon chartIcon = VaadinIcon.LINE_CHART.create();
        chartIcon.setSize("48px");
        chartIcon.getStyle().set("color", "#9ca3af");
        
        Paragraph text = new Paragraph("Revenue & Bookings Trend Chart");
        text.getStyle()
            .set("margin", "1rem 0 0 0")
            .set("color", "#6b7280")
            .set("font-weight", "500");
        
        Paragraph info = new Paragraph("Monthly data visualization showing revenue (€) and booking trends");
        info.getStyle()
            .set("margin", "0.5rem 0 0 0")
            .set("color", "#9ca3af")
            .set("font-size", "0.875rem");
        
        content.add(chartIcon, text, info);
        chartPlaceholder.add(content);
        
        card.add(title, subtitle, chartPlaceholder);
        return card;
    }

    private Component createOccupancyChartPlaceholder() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Room Occupancy (Last 7 Days)");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Daily breakdown of occupied vs available rooms");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        Div chartPlaceholder = new Div();
        chartPlaceholder.getStyle()
            .set("height", "300px")
            .set("background", "linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%)")
            .set("border-radius", "0.5rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("border", "2px dashed #d1fae5");
        
        Div content = new Div();
        content.getStyle().set("text-align", "center");
        
        Icon chartIcon = VaadinIcon.BAR_CHART.create();
        chartIcon.setSize("48px");
        chartIcon.getStyle().set("color", "#10b981");
        
        Paragraph text = new Paragraph("Room Occupancy Chart");
        text.getStyle()
            .set("margin", "1rem 0 0 0")
            .set("color", "#6b7280")
            .set("font-weight", "500");
        
        content.add(chartIcon, text);
        chartPlaceholder.add(content);
        
        card.add(title, subtitle, chartPlaceholder);
        return card;
    }

    private Component createRoomTypeChartPlaceholder() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Bookings by Room Type");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Distribution of bookings across room categories");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        Div chartPlaceholder = new Div();
        chartPlaceholder.getStyle()
            .set("height", "300px")
            .set("background", "linear-gradient(180deg, #fef3c7 0%, #ffffff 100%)")
            .set("border-radius", "0.5rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("border", "2px dashed #fde68a");
        
        Div content = new Div();
        content.getStyle().set("text-align", "center");
        
        Icon chartIcon = VaadinIcon.PIE_CHART.create();
        chartIcon.setSize("48px");
        chartIcon.getStyle().set("color", "#D4AF37");
        
        Paragraph text = new Paragraph("Room Type Distribution");
        text.getStyle()
            .set("margin", "1rem 0 0 0")
            .set("color", "#6b7280")
            .set("font-weight", "500");
        
        content.add(chartIcon, text);
        chartPlaceholder.add(content);
        
        card.add(title, subtitle, chartPlaceholder);
        return card;
    }

    private Component createWeeklyBookingChartPlaceholder() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Weekly Booking Trends");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("New bookings vs cancellations by day of week");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        Div chartPlaceholder = new Div();
        chartPlaceholder.getStyle()
            .set("height", "350px")
            .set("background", "linear-gradient(180deg, #dbeafe 0%, #ffffff 100%)")
            .set("border-radius", "0.5rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("border", "2px dashed #bfdbfe");
        
        Div content = new Div();
        content.getStyle().set("text-align", "center");
        
        Icon chartIcon = VaadinIcon.BAR_CHART_V.create();
        chartIcon.setSize("48px");
        chartIcon.getStyle().set("color", "#3b82f6");
        
        Paragraph text = new Paragraph("Weekly Booking Trends");
        text.getStyle()
            .set("margin", "1rem 0 0 0")
            .set("color", "#6b7280")
            .set("font-weight", "500");
        
        Paragraph info = new Paragraph("Comparison of new bookings and cancellations");
        info.getStyle()
            .set("margin", "0.5rem 0 0 0")
            .set("color", "#9ca3af")
            .set("font-size", "0.875rem");
        
        content.add(chartIcon, text, info);
        chartPlaceholder.add(content);
        
        card.add(title, subtitle, chartPlaceholder);
        return card;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}