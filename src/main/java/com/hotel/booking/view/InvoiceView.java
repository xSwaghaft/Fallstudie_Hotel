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
import com.hotel.booking.service.InvoiceService;

import java.time.LocalDate;
import java.util.List;

@Route(value = "invoices", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class InvoiceView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final InvoiceService invoiceService;
    record InvoiceRow(String id, String guest, String bookingId, String issueDate, 
                  String dueDate, int amount, int paid, int balance, String status) {}

    public InvoiceView(SessionService sessionService, InvoiceService invoiceService) {
        this.sessionService = sessionService;
        this.invoiceService = invoiceService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createStatsRow(), createSearchCard(), createInvoicesCard());
    }

    private Component createHeader() {
        H1 title = new H1("Invoice Management");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Track and manage guest invoices");
        subtitle.getStyle().set("margin", "0");
        
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
        valueHeading.getStyle().set("margin", "0");
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
        title.getStyle().set("margin", "0 0 1rem 0");
        
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
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Complete list of invoices and their payment status");
        subtitle.getStyle().set("margin", "0 0 1rem 0");
        
        Grid<InvoiceRow> grid = new Grid<>(InvoiceRow.class, false);
        grid.addColumn(inv -> inv.id()).setHeader("Invoice ID").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(inv -> {
            Div container = new Div();
            Div name = new Div(new Span(inv.guest()));
            name.getStyle().set("font-weight", "600");
            Div bookingId = new Div(new Span(inv.bookingId()));
            bookingId.getStyle().set("font-size", "0.85rem").set("color", "var(--color-text-secondary)");
            container.add(name, bookingId);
            return container;
        }).setHeader("Guest Name").setFlexGrow(1);
        grid.addColumn(inv -> inv.issueDate()).setHeader("Issue Date").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(inv -> inv.dueDate()).setHeader("Due Date").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(inv -> "€" + inv.amount()).setHeader("Amount").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(inv -> {
            Span span = new Span("€" + inv.paid());
            span.getStyle().set("color", "#10b981").set("font-weight", "600");
            return span;
        }).setHeader("Paid").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(inv -> {
            Span span = new Span("€" + inv.balance());
            span.getStyle().set("color", inv.balance() > 0 ? "#ef4444" : "#10b981")
                          .set("font-weight", "600");
            return span;
        }).setHeader("Balance").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Status").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(inv -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button view = new Button(VaadinIcon.EYE.create());
            Button download = new Button(VaadinIcon.DOWNLOAD.create());
            
            if ("pending".equals(inv.status()) || "partial".equals(inv.status())) {
                Button confirm = new Button(VaadinIcon.CHECK.create());
                confirm.getStyle().set("color", "#10b981");
                actions.add(view, download, confirm);
            } else {
                actions.add(view, download);
            }
            
            actions.setSpacing(true);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        
        grid.setItems(getInvoices());
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        
        card.add(title, subtitle, grid);
        return card;
    }

    private Component createStatusBadge(InvoiceRow invoice) {
        Span badge = new Span(invoice.status());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + invoice.status());
        return badge;
    }

    // kept for compatibility; views use service-backed data via getInvoices()

    private List<InvoiceRow> getInvoices() {
        return invoiceService.findAll().stream().map(inv -> {
            String id = inv.getInvoiceNumber();
            String guest = inv.getBooking() != null ? inv.getBooking().toString() : "-";
            String bookingId = inv.getBooking() != null ? String.valueOf(inv.getBooking().getId()) : "-";
            String issueDate = inv.getIssuedAt() != null ? inv.getIssuedAt().toLocalDate().toString() : "-";
            String dueDate = inv.getIssuedAt() != null ? inv.getIssuedAt().toLocalDate().plusDays(7).toString() : "-";
            int amount = inv.getAmount() != null ? inv.getAmount().intValue() : 0;
            int paid = (inv.getPaidAt() != null) ? amount : 0;
            int balance = amount - paid;
            String status = inv.getInvoiceStatus() != null ? inv.getInvoiceStatus().name().toLowerCase() : "pending";
            return new InvoiceRow(id, guest, bookingId, issueDate, dueDate, amount, paid, balance, status);
        }).toList();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}