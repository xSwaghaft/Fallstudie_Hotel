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
 * View for displaying and managing invoices.
 * <p>
 * Provides invoice management functionality with role-based access:
 * </p>
 * <ul>
 *   <li>GUEST: view only their own invoices</li>
 *   <li>RECEPTIONIST & MANAGER: view and manage all invoices</li>
 * </ul>
 * <p>
 * Features include displaying invoices in a sortable grid with columns for invoice number,
 * booking number, amount, payment method, status, and issued date; searching invoices by
 * invoice number; filtering invoices by payment status, payment method, and date issued;
 * downloading invoices as PDF documents; visual status badges with color coding; and formatted
 * currency amounts in German locale. The view automatically filters data based on the current
 * user's role, ensuring guests see only their own invoices while staff see all invoices. All
 * data is loaded and filtered asynchronously to provide a responsive user experience.
 * </p>
 *
 * @author Arman Özcanli
 * @see Invoice
 * @see InvoiceService
 * @see InvoicePdfService
 * @see SessionService
 */
@Route(value = "invoices", layout = MainLayout.class)
@PageTitle("Invoices")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/my-bookings.css")
@RolesAllowed({UserRole.RECEPTIONIST_VALUE, UserRole.MANAGER_VALUE, UserRole.GUEST_VALUE})
public class InvoiceView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceView.class);
    private final SessionService sessionService;
    private final InvoiceService invoiceService;
    @SuppressWarnings("unused")
    private final InvoicePdfService invoicePdfService;
    private Grid<Invoice> grid;
    private static final DateTimeFormatter GERMAN_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Constructs an InvoiceView with required service dependencies.
     *
     * @param sessionService service for managing user sessions
     * @param invoiceService service for managing invoices
     * @param invoicePdfService service for generating invoice PDFs
     */
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

    /**
     * Initializes the user interface with header, filters, and invoice card.
     */
    private void initializeUI() {
        add(createHeader());
        add(createFilters());
        add(createInvoicesCard());
    }

    /**
     * Creates the header component for the invoice view.
     * <p>
     * The header title adapts based on the current user's role: "My Invoices" for guests,
     * "Invoice Management" for staff.
     * </p>
     *
     * @return a Component containing the header
     */
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

    /**
     * Creates the search and filter component for invoices.
     * <p>
     * Provides filtering capabilities by invoice number, payment status, payment method, and issued date.
     * Allows users to search for specific invoices and apply multiple filter criteria simultaneously.
     * The search button triggers the loadInvoices() method with all selected filter parameters.
     * </p>
     *
     * @return a Component containing search and filter controls
     */
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

    /**
     * Creates the main invoice display card with sortable grid.
     * <p>
     * Displays invoices in a sortable grid with columns for invoice number, booking number, amount,
     * payment method, payment status, and issued date. Each invoice row includes a PDF download button.
     * The grid shows formatted currency amounts in German locale (€) and color-coded status badges.
     * Initial data is loaded on view creation and respects the current user's role and permissions.
     * </p>
     *
     * @return a Component containing the invoice grid and title
     */
    private Component createInvoicesCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Invoices");
        title.addClassName("invoice-section-title");

        // Grid setup
        grid = new Grid<>(Invoice.class, false);

        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Invoice No.").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(invoice -> invoice.getBooking() != null ? invoice.getBooking().getBookingNumber() : "").setHeader("Booking No.").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(invoice -> {
            NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            return nf.format(invoice.getAmount()) + " €";
        }).setHeader("Amount").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Invoice::getPaymentMethod).setHeader("Payment Method").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addComponentColumn(invoice -> createStatusBadge(invoice.getInvoiceStatus()))
            .setHeader("Status").setSortable(true).setAutoWidth(true).setFlexGrow(1);
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
        // Let the page scroll, not the grid
        grid.setAllRowsVisible(true);

        // Load initial data
        loadInvoices("");

        card.add(title, grid);
        return card;
    }

    /**
     * Creates a styled status badge for an invoice payment status.
     * <p>
     * Returns a Span component with CSS class styling applied based on the status value.
     * The badge displays the status text and applies color-coded styling (e.g., pending, paid, failed).
     * </p>
     *
     * @param status the payment status of the invoice
     * @return a Span component displaying the status badge
     */
    private Span createStatusBadge(Object status) {
        String statusText = status != null ? String.valueOf(status) : "";
        Span badge = new Span(statusText);
        badge.addClassName("booking-item-status");
        if (!statusText.isBlank()) {
            badge.addClassName(statusText.toLowerCase());
        }
        return badge;
    }

    /**
     * Loads invoices with the specified search query.
     * <p>
     * Convenience method that applies only a search query filter while using default values
     * for status, method, and date filters. Delegates to the full loadInvoices() method
     * with "All Status" and "All Methods" filters.
     * </p>
     *
     * @param query the search query to filter invoices by invoice number
     */
    private void loadInvoices(String query) {
        loadInvoices(query, "All Status", "All Methods", null);
    }

    /**
     * Loads and filters invoices based on multiple criteria.
     * <p>
     * Retrieves the base invoice list (filtered by user role), then applies search query filter,
     * payment status filter, payment method filter, and date filter in sequence.
     * Updates the grid with the filtered results. All filters are optional and can be null or "All".
     * </p>
     *
     * @param query the search query to filter by invoice number (null or empty to skip)
     * @param statusFilter the payment status to filter by ("All Status" to skip)
     * @param methodFilter the payment method to filter by ("All Methods" to skip)
     * @param dateFilter the issued date to filter by (null to skip)
     */
    private void loadInvoices(String query, String statusFilter, String methodFilter, LocalDate dateFilter) {
        List<Invoice> items = getBaseInvoices();
        items = applySearchFilter(items, query);
        items = applyStatusFilter(items, statusFilter);
        items = applyMethodFilter(items, methodFilter);
        items = applyDateFilter(items, dateFilter);
        grid.setItems(items);
    }

    // ===== FILTER HELPER METHODS =====

    /**
     * Retrieves the base list of invoices based on the current user's role.
     * <p>
     * For guests, returns only invoices associated with their own bookings.
     * For staff (receptionist/manager), returns all invoices in the system.
     * This filtering ensures role-based access control at the data level.
     * </p>
     *
     * @return a List of Invoice objects filtered by user role
     */
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

    /**
     * Applies a search query filter to invoices by invoice number.
     * <p>
     * If a query is provided, attempts to find an exact invoice number match first.
     * If found, returns only that invoice. If no exact match, filters invoices by
     * case-insensitive partial invoice number matching. Empty or null queries return
     * all invoices unchanged.
     * </p>
     *
     * @param items the list of invoices to filter
     * @param query the search query string (null or empty to skip filtering)
     * @return a filtered List of invoices matching the search query
     */
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

    /**
     * Applies a payment status filter to invoices.
     * <p>
     * Filters invoices by their payment status (PENDING, PAID, FAILED, REFUNDED, PARTIAL).
     * The filter "All Status" or null returns all invoices unchanged.
     * </p>
     *
     * @param items the list of invoices to filter
     * @param statusFilter the status filter value ("All Status" to skip filtering)
     * @return a filtered List of invoices matching the selected status
     */
    private List<Invoice> applyStatusFilter(List<Invoice> items, String statusFilter) {
        if (statusFilter != null && !statusFilter.equals("All Status")) {
            return items.stream()
                    .filter(i -> i.getInvoiceStatus() != null && i.getInvoiceStatus().name().equals(statusFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    /**
     * Applies a payment method filter to invoices.
     * <p>
     * Filters invoices by their payment method (CARD, TRANSFER, CASH, INVOICE).
     * The filter "All Methods" or null returns all invoices unchanged.
     * </p>
     *
     * @param items the list of invoices to filter
     * @param methodFilter the payment method filter value ("All Methods" to skip filtering)
     * @return a filtered List of invoices matching the selected payment method
     */
    private List<Invoice> applyMethodFilter(List<Invoice> items, String methodFilter) {
        if (methodFilter != null && !methodFilter.equals("All Methods")) {
            return items.stream()
                    .filter(i -> i.getPaymentMethod() != null && i.getPaymentMethod().name().equals(methodFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    /**
     * Applies a date filter to invoices by issued date.
     * <p>
     * Filters invoices to only include those issued on the specified date.
     * Compares the invoice's issued date (ignoring time component) with the filter date.
     * A null date filter returns all invoices unchanged.
     * </p>
     *
     * @param items the list of invoices to filter
     * @param dateFilter the date to filter by (null to skip filtering)
     * @return a filtered List of invoices matching the issued date
     */
    private List<Invoice> applyDateFilter(List<Invoice> items, LocalDate dateFilter) {
        if (dateFilter != null) {
            return items.stream()
                    .filter(i -> i.getIssuedAt() != null && 
                               i.getIssuedAt().toLocalDate().equals(dateFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    /**
     * Initiates the download of an invoice as a PDF document.
     * <p>
     * Opens the PDF download endpoint in the browser, which triggers a file download.
     * The endpoint URL is constructed using the invoice ID (/api/invoice/{id}/pdf).
     * If an error occurs, displays a notification message to the user.
     * </p>
     *
     * @param invoice the invoice to download as PDF
     */
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