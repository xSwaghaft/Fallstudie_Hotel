package com.hotel.booking.view;

import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.PaymentService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View for displaying and managing payment transactions.
 * <p>
 * Provides payment tracking and management functionality with role-based access:
 * </p>
 * <ul>
 *   <li>GUEST: view only their own payments</li>
 *   <li>RECEPTIONIST & MANAGER: view and manage all payments</li>
 * </ul>
 * <p>
 * Features include displaying payments in a sortable grid with columns for amount, method,
 * status, transaction reference, and payment date; searching payments by transaction reference;
 * filtering payments by status, payment method, and date paid; visual status badges with color
 * coding; formatted currency amounts in German locale; and multi-level sorting support. The view
 * automatically filters data based on the current user's role, ensuring guests see only their own
 * payment transactions while staff see all payments. Payment statuses (PENDING, PAID, FAILED,
 * REFUNDED, PARTIAL) are displayed with visual indicators to quickly identify payment states.
 * </p>
 *
 * @author Arman Özcanli
 * @see Payment
 * @see PaymentService
 * @see SessionService
 * @see Invoice
 */
@Route(value = "payment", layout = MainLayout.class)
@PageTitle("Payments")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/my-bookings.css")
@RolesAllowed({UserRole.RECEPTIONIST_VALUE, UserRole.MANAGER_VALUE, UserRole.GUEST_VALUE})
public class PaymentView extends VerticalLayout {

    private final SessionService sessionService;
    private final PaymentService paymentService;
    private Grid<Payment> grid;
    private static final DateTimeFormatter GERMAN_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public PaymentView(SessionService sessionService, PaymentService paymentService) {
        this.sessionService = sessionService;
        this.paymentService = paymentService;

        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setHeight(null);

        initializeUI();
    }

    /**
     * Initializes the user interface with header, filters, and payments card.
     * <p>
     * The content adapts based on the current user's role:
     * </p>
     * <ul>
     *   <li>GUEST: displays "My Payments" with personal payment history</li>
     *   <li>STAFF: displays "Payment Management" with all system payments</li>
     * </ul>
     */
    private void initializeUI() {
        // Title based on role
        String title = sessionService.getCurrentRole() == UserRole.GUEST 
            ? "My Payments" 
            : "Payment Management";
        String subtitle = sessionService.getCurrentRole() == UserRole.GUEST 
            ? "View your payment history" 
            : "Manage all payments in the system";

        add(createHeader(title, subtitle));
        add(createFilters());
        add(createPaymentsCard());
    }

    /**
     * Creates the header component for the payment view.
     *
     * @param title the header title
     * @param subtitle the header subtitle
     * @return a Component containing the header
     */
    private Component createHeader(String title, String subtitle) {
        H1 h1 = new H1(title);
        Paragraph p = new Paragraph(subtitle);
        p.addClassName("payment-subtitle");
        
        HorizontalLayout header = new HorizontalLayout(h1, p);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    /**
     * Creates the search and filter component for payments.
     * <p>
     * Provides filtering capabilities by booking number/ID, payment status, payment method, and payment date.
     * Allows users to search for specific payments and apply multiple filter criteria simultaneously.
     * The search button triggers the loadPayments() method with all selected filter parameters.
     * </p>
     *
     * @return a Component containing search and filter controls
     */
    private Component createFilters() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Search & Filter");
        title.addClassName("payment-section-title");
        
        Paragraph subtitle = new Paragraph("Find specific payments quickly");
        subtitle.addClassName("payment-subtitle");

        TextField search = new TextField("Search");
        search.setPlaceholder("Booking number, Booking ID...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());

        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems("All Status", "PENDING", "PAID", "FAILED", "REFUNDED", "PARTIAL");
        status.setValue("All Status");

        Select<String> method = new Select<>();
        method.setLabel("Payment Method");
        method.setItems("All Methods", "Card", "Bank Transfer", "Cash");
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
        searchButton.addClickListener(e -> loadPayments(search.getValue(), status.getValue(), method.getValue(), date.getValue()));
        searchButton.addClickShortcut(Key.ENTER);

        HorizontalLayout buttonLayout = new HorizontalLayout(searchButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        card.add(title, subtitle, form, buttonLayout);
        return card;
    }

    /**
     * Creates the main payment display card with sortable grid.
     * <p>
     * Displays payments in a sortable grid with columns for booking number, amount (with refund notation),
     * payment method, payment status, and paid date. Each row shows formatted currency amounts in German
     * locale (€) and color-coded status badges. The grid supports multi-level sorting and shows all rows
     * on the page without internal scrolling. Initial data is loaded on view creation and respects the
     * current user's role and permissions.
     * </p>
     *
     * @return a Component containing the payment grid and title
     */
    private Component createPaymentsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Payments");
        title.addClassName("payment-section-title");

        // Grid setup
        grid = new Grid<>(Payment.class, false);

        grid.addColumn(this::getBookingNumber).setHeader("Booking No.").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(this::formatPaymentAmount).setHeader("Amount").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Payment::getMethod).setHeader("Method").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addComponentColumn(payment -> createStatusBadge(payment.getStatus()))
            .setHeader("Status").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(payment -> payment.getPaidAt() != null 
                ? payment.getPaidAt().format(GERMAN_DATETIME_FORMAT) 
                : "")
                .setHeader("Paid at").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.setMultiSort(true, MultiSortPriority.APPEND);
        grid.setWidthFull();
        // Let the page scroll, not the grid
        grid.setAllRowsVisible(true);

        // Load initial data
        loadPayments("");

        card.add(title, grid);
        return card;
    }

    /**
     * Creates a styled status badge for a payment status.
     * <p>
     * Returns a Span component with CSS class styling applied based on the status value.
     * The badge displays the status text and applies color-coded styling (e.g., pending, paid, failed).
     * </p>
     *
     * @param status the payment status
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
     * Loads payments with the specified search query.
     * <p>
     * Convenience method that applies only a search query filter while using default values
     * for status, method, and date filters. Delegates to the full loadPayments() method
     * with "All Status" and "All Methods" filters.
     * </p>
     *
     * @param query the search query to filter payments by booking number or ID
     */
    private void loadPayments(String query) {
        // No default date filter: show payments immediately like InvoiceView
        loadPayments(query, "All Status", "All Methods", null);
    }

    /**
     * Loads and filters payments based on multiple criteria.
     * <p>
     * Retrieves the base payment list (filtered by user role), then applies status filter, method filter,
     * date filter, and finally search query filter in sequence. Updates the grid with the filtered results.
     * The search filter supports both exact booking ID matching and booking number matching. All filters
     * are optional and can be null or "All".
     * </p>
     *
     * @param query the search query to filter by booking number or ID (null or empty to skip)
     * @param statusFilter the payment status to filter by ("All Status" to skip)
     * @param methodFilter the payment method to filter by ("All Methods" to skip)
     * @param dateFilter the payment date to filter by (null to skip)
     */
    private void loadPayments(String query, String statusFilter, String methodFilter, LocalDate dateFilter) {
        List<Payment> items = getBasePayments();
        items = applyStatusFilter(items, statusFilter);
        items = applyMethodFilter(items, methodFilter);
        items = applyDateFilter(items, dateFilter);
        items = applySearchFilter(items, query);
        grid.setItems(items);
    }

    // ===== FILTER AND FORMATTING HELPER METHODS =====
    
    /**
     * Formats a payment amount for display with currency symbol and refund notation.
     * <p>
     * If the payment has been partially or fully refunded, displays the refunded amount with
     * "(refunded)" notation. Otherwise displays the full payment amount. All amounts are formatted
     * to 2 decimal places and suffixed with the Euro currency symbol (€).
     * </p>
     *
     * @param payment the payment to format
     * @return a formatted string representing the payment amount with currency symbol
     */
    private String formatPaymentAmount(Payment payment) {
        if (payment.getRefundedAmount() != null && 
            payment.getRefundedAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return String.format("%.2f € (refunded)", payment.getRefundedAmount());
        }
        return String.format("%.2f €", payment.getAmount());
    }

    /**
     * Retrieves the base list of payments based on the current user's role.
     * <p>
     * For guests, returns only payments associated with their own bookings.
     * For staff (receptionist/manager), returns all payments in the system.
     * This filtering ensures role-based access control at the data level.
     * </p>
     *
     * @return a List of Payment objects filtered by user role
     */
    private List<Payment> getBasePayments() {
        if (sessionService.getCurrentRole() == UserRole.GUEST) {
            Long guestId = sessionService.getCurrentUser().getId();
            return paymentService.findAll().stream()
                    .filter(p -> p.getBooking() != null && 
                               p.getBooking().getGuest() != null && 
                               p.getBooking().getGuest().getId().equals(guestId))
                    .collect(Collectors.toList());
        }
        return paymentService.findAll();
    }

    /**
     * Applies a payment status filter to payments.
     * <p>
     * Filters payments by their status (PENDING, PAID, FAILED, REFUNDED, PARTIAL).
     * The filter "All Status" or null returns all payments unchanged.
     * </p>
     *
     * @param items the list of payments to filter
     * @param statusFilter the status filter value ("All Status" to skip filtering)
     * @return a filtered List of payments matching the selected status
     */
    private List<Payment> applyStatusFilter(List<Payment> items, String statusFilter) {
        if (statusFilter != null && !statusFilter.equals("All Status")) {
            return items.stream()
                    .filter(p -> p.getStatus() != null && p.getStatus().name().equals(statusFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    /**
     * Applies a payment method filter to payments.
     * <p>
     * Filters payments by their method (Card, Bank Transfer, Cash, etc.).
     * The filter "All Methods" or null returns all payments unchanged.
     * </p>
     *
     * @param items the list of payments to filter
     * @param methodFilter the payment method filter value ("All Methods" to skip filtering)
     * @return a filtered List of payments matching the selected payment method
     */
    private List<Payment> applyMethodFilter(List<Payment> items, String methodFilter) {
        if (methodFilter != null && !methodFilter.equals("All Methods")) {
            return items.stream()
                    .filter(p -> p.getMethod() != null && p.getMethod().toString().equals(methodFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    /**
     * Applies a date filter to payments by payment date.
     * <p>
     * Filters payments to only include those paid on the specified date.
     * Compares the payment's paid date (ignoring time component) with the filter date.
     * A null date filter returns all payments unchanged.
     * </p>
     *
     * @param items the list of payments to filter
     * @param dateFilter the date to filter by (null to skip filtering)
     * @return a filtered List of payments matching the paid date
     */
    private List<Payment> applyDateFilter(List<Payment> items, LocalDate dateFilter) {
        if (dateFilter != null) {
            return items.stream()
                    .filter(p -> p.getPaidAt() != null && 
                               p.getPaidAt().toLocalDate().equals(dateFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    /**
     * Applies a multi-level search filter to payments by booking number or ID.
     * <p>
     * Uses a three-level search strategy:
     * </p>
     * <ol>
     *   <li>Attempts to parse the query as a numeric Booking ID and searches all payments for matching ID</li>
     *   <li>Searches for exact booking number match (case-insensitive) across all payments</li>
     *   <li>Falls back to substring search within booking number, booking ID, and transaction reference</li>
     * </ol>
     * <p>
     * All results respect the current user's role-based access permissions. Empty or null queries
     * return all payments unchanged.
     * </p>
     *
     * @param items the list of payments to filter
     * @param query the search query for booking number or ID (null or empty to skip filtering)
     * @return a filtered List of payments matching the search criteria
     */
    private List<Payment> applySearchFilter(List<Payment> items, String query) {
        if (query != null && !query.isBlank()) {
            String normalized = query.trim();

            // 1) If the user enters a number, treat it as Booking ID and search across all payments.
            Long bookingId = tryParseLong(normalized);
            if (bookingId != null) {
                List<Payment> byBookingId = paymentService.findByBookingId(bookingId).stream()
                        .filter(this::hasAccessToPayment)
                        .collect(Collectors.toList());
                if (!byBookingId.isEmpty()) {
                    return byBookingId;
                }
            }

            // 2) Exact match by Booking Number (business reference), search across all payments.
            List<Payment> byBookingNumber = paymentService.findAll().stream()
                    .filter(p -> {
                        String bookingNumber = getBookingNumber(p);
                        return bookingNumber != null && bookingNumber.equalsIgnoreCase(normalized);
                    })
                    .filter(this::hasAccessToPayment)
                    .collect(Collectors.toList());
            if (!byBookingNumber.isEmpty()) {
                return byBookingNumber;
            }

            // 3) Fallback: substring search within the currently filtered set.
            String qLower = normalized.toLowerCase();
            return items.stream()
                    .filter(p -> {
                        String bookingNumber = getBookingNumber(p);
                        boolean matchesBookingNumber = bookingNumber != null && bookingNumber.toLowerCase().contains(qLower);
                        boolean matchesBookingId = p.getBooking() != null && p.getBooking().getId() != null
                                && p.getBooking().getId().toString().contains(normalized);
                        // Keep Transaction-Ref as a technical fallback (optional field).
                        boolean matchesTxRef = p.getTransactionRef() != null && p.getTransactionRef().toLowerCase().contains(qLower);
                        return matchesBookingNumber || matchesBookingId || matchesTxRef;
                    })
                    .collect(Collectors.toList());
        }
        return items;
    }

    /**
     * Retrieves the booking number from a payment.
     * <p>
     * Extracts the booking number from the payment's associated booking object.
     * Returns an empty string if the payment or booking is null.
     * </p>
     *
     * @param payment the payment to extract the booking number from
     * @return the booking number string, or empty string if not available
     */
    private String getBookingNumber(Payment payment) {
        if (payment == null || payment.getBooking() == null) {
            return "";
        }
        String bookingNumber = payment.getBooking().getBookingNumber();
        return bookingNumber != null ? bookingNumber : "";
    }

    /**
     * Attempts to parse a string value as a Long.
     * <p>
     * Returns the parsed Long value if successful, or null if the string cannot be
     * parsed as a valid Long number. Trims the input string before attempting to parse.
     * </p>
     *
     * @param value the string value to parse
     * @return the parsed Long value, or null if parsing fails
     */
    private Long tryParseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Checks if the current user has access to view a specific payment.
     * <p>
     * For guests, returns true only if the payment belongs to their own booking.
     * For staff (receptionist/manager), always returns true.
     * This method enforces role-based access control at the individual payment level.
     * </p>
     *
     * @param payment the payment to check access for
     * @return true if the current user has permission to view this payment, false otherwise
     */
    private boolean hasAccessToPayment(Payment payment) {
        UserRole role = sessionService.getCurrentRole();
        if (role == UserRole.GUEST) {
            return payment.getBooking() != null && 
                   payment.getBooking().getGuest() != null && 
                   payment.getBooking().getGuest().getId().equals(sessionService.getCurrentUser().getId());
        }
        return true;
    }
}
