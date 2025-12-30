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
 * Payment View - Display and manage payments based on user role.
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

    private Component createHeader(String title, String subtitle) {
        H1 h1 = new H1(title);
        Paragraph p = new Paragraph(subtitle);
        p.addClassName("payment-subtitle");
        
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

    private Span createStatusBadge(Object status) {
        String statusText = status != null ? String.valueOf(status) : "";
        Span badge = new Span(statusText);
        badge.addClassName("booking-item-status");
        if (!statusText.isBlank()) {
            badge.addClassName(statusText.toLowerCase());
        }
        return badge;
    }

    private void loadPayments(String query) {
        // No default date filter: show payments immediately like InvoiceView
        loadPayments(query, "All Status", "All Methods", null);
    }

    private void loadPayments(String query, String statusFilter, String methodFilter, LocalDate dateFilter) {
        List<Payment> items = getBasePayments();
        items = applyStatusFilter(items, statusFilter);
        items = applyMethodFilter(items, methodFilter);
        items = applyDateFilter(items, dateFilter);
        items = applySearchFilter(items, query);
        grid.setItems(items);
    }

    // ===== FILTER AND FORMATTING HELPER METHODS =====
    
    private String formatPaymentAmount(Payment payment) {
        if (payment.getRefundedAmount() != null && 
            payment.getRefundedAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return String.format("%.2f € (refunded)", payment.getRefundedAmount());
        }
        return String.format("%.2f €", payment.getAmount());
    }

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

    private List<Payment> applyStatusFilter(List<Payment> items, String statusFilter) {
        if (statusFilter != null && !statusFilter.equals("All Status")) {
            return items.stream()
                    .filter(p -> p.getStatus() != null && p.getStatus().name().equals(statusFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    private List<Payment> applyMethodFilter(List<Payment> items, String methodFilter) {
        if (methodFilter != null && !methodFilter.equals("All Methods")) {
            return items.stream()
                    .filter(p -> p.getMethod() != null && p.getMethod().toString().equals(methodFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

    private List<Payment> applyDateFilter(List<Payment> items, LocalDate dateFilter) {
        if (dateFilter != null) {
            return items.stream()
                    .filter(p -> p.getPaidAt() != null && 
                               p.getPaidAt().toLocalDate().equals(dateFilter))
                    .collect(Collectors.toList());
        }
        return items;
    }

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

    private String getBookingNumber(Payment payment) {
        if (payment == null || payment.getBooking() == null) {
            return "";
        }
        String bookingNumber = payment.getBooking().getBookingNumber();
        return bookingNumber != null ? bookingNumber : "";
    }

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
