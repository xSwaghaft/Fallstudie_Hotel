package com.hotel.booking.view;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.service.InvoiceService;
import com.hotel.booking.service.InvoicePdfService;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.security.RolesAllowed;

/**
 * Invoice View - Manage invoices (Receptionist and Manager only).
 */
@Route(value = "invoices", layout = MainLayout.class)
@PageTitle("Invoices")
@CssImport("./themes/hotel/styles.css")
@RolesAllowed({UserRole.RECEPTIONIST_VALUE, UserRole.MANAGER_VALUE, UserRole.GUEST_VALUE})
public class InvoiceView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceView.class);
    private final SessionService sessionService;
    private final InvoiceService invoiceService;
    @SuppressWarnings("unused")
    private final InvoicePdfService invoicePdfService;
    private Grid<Invoice> grid;
    private static final DateTimeFormatter GERMAN_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public InvoiceView(SessionService sessionService, InvoiceService invoiceService, InvoicePdfService invoicePdfService) {
        this.sessionService = sessionService;
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;

        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setHeight(null);

        initializeUI();
    }

    private void initializeUI() {
        add(createHeader());
        add(createFilters());
        add(createInvoicesCard());
    }

    private Component createHeader() {
        String title = sessionService.getCurrentRole() == UserRole.GUEST 
            ? "My Invoices" 
            : "Invoice Management";
        String subtitle = sessionService.getCurrentRole() == UserRole.GUEST 
            ? "View your invoices" 
            : "Manage and search invoices";
        
        H1 h1 = new H1(title);
        Paragraph p = new Paragraph(subtitle);
        p.addClassName("invoice-subtitle");
        
        HorizontalLayout header = new HorizontalLayout(h1, p);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    private Component createFilters() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Search & Filter");
        title.addClassName("invoice-section-title");
        
        Paragraph subtitle = new Paragraph("Find specific invoices quickly");
        subtitle.addClassName("invoice-subtitle");

        TextField search = new TextField("Search");
        search.setPlaceholder("Invoice Number...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());

        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems("All Status", "PENDING", "PAID", "FAILED", "REFUNDED", "PARTIAL");
        status.setValue("All Status");

        Select<String> method = new Select<>();
        method.setLabel("Payment Method");
        method.setItems("All Methods", "CARD", "TRANSFER", "CASH", "INVOICE");
        method.setValue("All Methods");

        DatePicker date = new DatePicker("Date (optional)");
        // No default value - only filter when selected

        FormLayout form = new FormLayout(search, status, method, date);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 4)
        );

        // Search Button
        Button searchButton = new Button("Search", VaadinIcon.SEARCH.create());
        searchButton.addClassName("primary-button");
        searchButton.addClickListener(e -> loadInvoices(search.getValue(), status.getValue(), method.getValue(), date.getValue()));
        searchButton.addClickShortcut(Key.ENTER);

        HorizontalLayout buttonLayout = new HorizontalLayout(searchButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        card.add(title, subtitle, form, buttonLayout);
        return card;
    }

    private Component createInvoicesCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Invoices");
        title.addClassName("invoice-section-title");

        // Grid setup
        grid = new Grid<>(Invoice.class, false);
        
        grid.addColumn(Invoice::getId).setHeader("ID").setSortable(true).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Invoice No.").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(invoice -> {
            NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            return nf.format(invoice.getAmount()) + " €";
        }).setHeader("Amount").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Invoice::getPaymentMethod).setHeader("Payment Method").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Invoice::getInvoiceStatus).setHeader("Status").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(invoice -> invoice.getIssuedAt() != null 
                ? invoice.getIssuedAt().format(GERMAN_DATETIME_FORMAT) 
                : "")
                .setHeader("Issued At").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        
        // PDF Download Column
        grid.addComponentColumn(invoice -> {
            Button downloadButton = new Button(VaadinIcon.DOWNLOAD.create());
            downloadButton.setTooltipText("Download as PDF");
            downloadButton.addClassName("primary-button");
            downloadButton.addClickListener(e -> downloadInvoicePdf(invoice));
            return downloadButton;
        }).setHeader("PDF").setAutoWidth(true).setFlexGrow(0);
        
        grid.setWidthFull();

        // Load initial data
        loadInvoices("");

        card.add(title, grid);
        return card;
    }

    // Simple search method
    private void loadInvoices(String query) {
        loadInvoices(query, "All Status", "All Methods", null);
    }

    private void loadInvoices(String query, String statusFilter, String methodFilter, LocalDate dateFilter) {
        List<Invoice> items = getBaseInvoices();
        items = applySearchFilter(items, query);
        items = applyStatusFilter(items, statusFilter);
        items = applyMethodFilter(items, methodFilter);
        items = applyDateFilter(items, dateFilter);
        grid.setItems(items);
    }

    // ===== FILTER HELPER METHODS =====

    private List<Invoice> getBaseInvoices() {
        List<Invoice> items = invoiceService.findAll();
        if (sessionService.getCurrentRole() == UserRole.GUEST) {
            Long guestId = sessionService.getCurrentUser().getId();
            return items.stream()
                    .filter(inv -> inv.getBooking() != null && 
                               inv.getBooking().getGuest() != null && 
                               inv.getBooking().getGuest().getId().equals(guestId))
                    .collect(Collectors.toList());
        }
        return items;
    }

    private List<Invoice> applySearchFilter(List<Invoice> items, String query) {
        if (query == null || query.isBlank()) {
            return items;
        }
        var byNumber = invoiceService.findByInvoiceNumber(query);
        if (byNumber.isPresent()) {
            return Collections.singletonList(byNumber.get());
        }
        return items.stream()
                .filter(inv -> inv.getInvoiceNumber() != null && 
                        inv.getInvoiceNumber().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<Invoice> applyStatusFilter(List<Invoice> items, String statusFilter) {
        if (statusFilter != null && !statusFilter.equals("All Status")) {
            return items.stream()
                    .filter(i -> i.getInvoiceStatus() != null && i.getInvoiceStatus().name().equals(statusFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    private List<Invoice> applyMethodFilter(List<Invoice> items, String methodFilter) {
        if (methodFilter != null && !methodFilter.equals("All Methods")) {
            return items.stream()
                    .filter(i -> i.getPaymentMethod() != null && i.getPaymentMethod().name().equals(methodFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    private List<Invoice> applyDateFilter(List<Invoice> items, LocalDate dateFilter) {
        if (dateFilter != null) {
            return items.stream()
                    .filter(i -> i.getIssuedAt() != null && 
                               i.getIssuedAt().toLocalDate().equals(dateFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    private void downloadInvoicePdf(Invoice invoice) {
        try {
            // Simple approach: open the API endpoint directly
            String url = "/api/invoice/" + invoice.getId() + "/pdf";
            logger.info("Opening PDF download URL: {}", url);
            com.vaadin.flow.component.UI.getCurrent().getPage().open(url);
        } catch (Exception e) {
            logger.error("Error initiating PDF download", e);
            Notification.show("Error downloading invoice: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }
}