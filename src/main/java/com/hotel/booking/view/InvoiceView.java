package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

import java.time.LocalDate;
import java.util.List;

@Route(value = "invoices", layout = MainLayout.class)
@PageTitle("Invoice Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/invoice.css")
public class InvoiceView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    record Invoice(String id, String guest, String bookingId, String issueDate,
                  String dueDate, int amount, int paid, int balance, String status) {}

    public InvoiceView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createStatsRow(), createSearchCard(), createInvoicesCard());
    }

    private Component createHeader() {
        H1 title = new H1("Invoice Management");
        
        Paragraph subtitle = new Paragraph("Track and manage guest invoices");
        
        Div headerLeft = new Div(title, subtitle);
        
        Button generateBtn = new Button("Generate Invoice", VaadinIcon.FILE_ADD.create());
        generateBtn.addClassName("primary-button");
        
        HorizontalLayout header = new HorizontalLayout(headerLeft, generateBtn);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    private Component createStatsRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        
        row.add(
            createStatCard("Total Invoices", "5", null),
            createStatCard("Paid", "3", "#10b981"),
            createStatCard("Pending", "1", "#f59e0b"),
            createStatCard("Total Amount", "€3552", "#D4AF37")
        );
        
        row.expand(
            row.getComponentAt(0),
            row.getComponentAt(1),
            row.getComponentAt(2),
            row.getComponentAt(3)
        );
        
        return row;
    }

    private Component createStatCard(String title, String value, String color) {
        Div card = new Div();
        card.addClassName("kpi-card");
        
        Span titleSpan = new Span(title);
        titleSpan.addClassName("kpi-card-title");
        
        H2 valueHeading = new H2(value);
        if (color != null) {
            valueHeading.getStyle().set("color", color);
        }
        
        card.add(titleSpan, valueHeading);
        return card;
    }

    private Component createSearchCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull(); // WICHTIG: Card nutzt volle Breite
        
        H3 title = new H3("Search & Filter");
        
        TextField search = new TextField("Search");
        search.setPlaceholder("Invoice ID, Guest name...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        
        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems("All Status", "paid", "pending", "partial");
        status.setValue("All Status");
        
        DatePicker dateRange = new DatePicker("Date Range");
        dateRange.setValue(LocalDate.of(2025, 10, 1));
        
        FormLayout form = new FormLayout(search, status, dateRange);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 3)
        );
        
        card.add(title, form);
        return card;
    }

    private Component createInvoicesCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull(); // WICHTIG: Card nutzt volle Breite
        
        H3 title = new H3("All Invoices");
        
        Paragraph subtitle = new Paragraph("Complete list of invoices and their payment status");
        
        Grid<Invoice> grid = new Grid<>(Invoice.class, false);
        grid.addColumn(Invoice::id).setHeader("Invoice ID").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(inv -> {
            Div container = new Div();
            Div name = new Div(new Span(inv.guest()));
            name.addClassName("invoice-guest-name");
            Div bookingId = new Div(new Span(inv.bookingId()));
            bookingId.addClassName("invoice-booking-id");
            container.add(name, bookingId);
            return container;
        }).setHeader("Guest Name").setFlexGrow(1);
        grid.addColumn(Invoice::issueDate).setHeader("Issue Date").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Invoice::dueDate).setHeader("Due Date").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(inv -> "€" + inv.amount()).setHeader("Amount").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(inv -> {
            Span span = new Span("€" + inv.paid());
            span.addClassName("invoice-paid-amount");
            return span;
        }).setHeader("Paid").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(inv -> {
            Span span = new Span("€" + inv.balance());
            span.addClassName(inv.balance() > 0 ? "invoice-balance-negative" : "invoice-balance-positive");
            return span;
        }).setHeader("Balance").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Status").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(inv -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button view = new Button(VaadinIcon.EYE.create());
            Button download = new Button(VaadinIcon.DOWNLOAD.create());
            
            if ("pending".equals(inv.status()) || "partial".equals(inv.status())) {
                Button confirm = new Button(VaadinIcon.CHECK.create());
                confirm.addClassName("invoice-button-success");
                actions.add(view, download, confirm);
            } else {
                actions.add(view, download);
            }
            
            actions.setSpacing(true);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        
        grid.setItems(getMockInvoices());
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        
        card.add(title, subtitle, grid);
        return card;
    }

    private Component createStatusBadge(Invoice invoice) {
        Span badge = new Span(invoice.status());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + invoice.status());
        return badge;
    }

    private List<Invoice> getMockInvoices() {
        return List.of(
            new Invoice("INV-2025-001", "Emma Wilson", "BK001", "2025-10-28", "2025-11-04", 447, 447, 0, "paid"),
            new Invoice("INV-2025-002", "Michael Brown", "BK002", "2025-10-30", "2025-11-07", 1196, 0, 1196, "pending"),
            new Invoice("INV-2025-003", "Sarah Davis", "BK003", "2025-10-25", "2025-11-02", 267, 267, 0, "paid"),
            new Invoice("INV-2025-004", "James Miller", "BK004", "2025-11-01", "2025-11-09", 745, 300, 445, "partial"),
            new Invoice("INV-2025-005", "Lisa Anderson", "BK005", "2025-10-29", "2025-11-06", 897, 897, 0, "paid")
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}