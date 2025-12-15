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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Payment View - Display and manage payments based on user role.
 */
@Route(value = "payment", layout = MainLayout.class)
@PageTitle("Payments")
@CssImport("./themes/hotel/styles.css")
public class PaymentView extends VerticalLayout {

    private final SessionService sessionService;
    private final PaymentService paymentService;
    private Grid<Payment> grid;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
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
        // Titel basierend auf Rolle
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
        search.setPlaceholder("Transaction Ref, Booking ID...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());

        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems("All Status", "Pending", "Completed", "Failed");
        status.setValue("All Status");

        Select<String> method = new Select<>();
        method.setLabel("Payment Method");
        method.setItems("All Methods", "Card", "Bank Transfer", "Cash");
        method.setValue("All Methods");

        DatePicker date = new DatePicker("Date (optional)");
        // Kein Standard-Wert - nur filtern wenn ausgewählt

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
        
        grid.addColumn(Payment::getId).setHeader("ID").setSortable(true).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(p -> p.getAmount()).setHeader("Betrag").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Payment::getMethod).setHeader("Methode").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Payment::getStatus).setHeader("Status").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Payment::getTransactionRef).setHeader("Transaktions-Ref").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(payment -> payment.getPaidAt() != null 
                ? payment.getPaidAt().format(GERMAN_DATETIME_FORMAT) 
                : "")
                .setHeader("Bezahlt am").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.setMultiSort(true, MultiSortPriority.APPEND);
        grid.setWidthFull();

        // Load initial data
        loadPayments("");

        card.add(title, grid);
        return card;
    }

    private void loadPayments(String query) {
        loadPayments(query, "All Status", "All Methods", LocalDate.now());
    }

    private void loadPayments(String query, String statusFilter, String methodFilter, LocalDate dateFilter) {
        List<Payment> items;
        UserRole role = sessionService.getCurrentRole();

        // Basisdaten laden je nach Rolle
        if (role == UserRole.GUEST) {
            // Guests sehen nur ihre eigenen Payments
            Long guestId = sessionService.getCurrentUser().getId();
            items = paymentService.findAll().stream()
                    .filter(p -> p.getBooking() != null && 
                               p.getBooking().getGuest() != null && 
                               p.getBooking().getGuest().getId().equals(guestId))
                    .collect(Collectors.toList());
        } else {
            // Receptionist und Manager sehen alle Payments
            items = paymentService.findAll();
        }

        // Status Filter
        if (statusFilter != null && !statusFilter.equals("All Status")) {
            items = items.stream()
                    .filter(p -> p.getStatus() != null && p.getStatus().toString().equals(statusFilter))
                    .collect(Collectors.toList());
        }

        // Payment Method Filter
        if (methodFilter != null && !methodFilter.equals("All Methods")) {
            items = items.stream()
                    .filter(p -> p.getMethod() != null && p.getMethod().toString().equals(methodFilter))
                    .collect(Collectors.toList());
        }

        // Date Filter - nur anwenden wenn ein Datum ausgewählt wurde
        if (dateFilter != null) {
            items = items.stream()
                    .filter(p -> p.getPaidAt() != null && 
                               p.getPaidAt().toLocalDate().equals(dateFilter))
                    .collect(Collectors.toList());
        }

        // Suchfilter anwenden
        if (query != null && !query.isBlank()) {
            // Try exact match first
            var byTx = paymentService.findByTransactionRef(query);
            if (byTx.isPresent()) {
                Payment payment = byTx.get();
                // Überprüfe ob der User Zugriff auf diesen Payment hat
                if (role != UserRole.GUEST || 
                    (payment.getBooking() != null && 
                     payment.getBooking().getGuest() != null && 
                     payment.getBooking().getGuest().getId().equals(sessionService.getCurrentUser().getId()))) {
                    items = Collections.singletonList(payment);
                } else {
                    items = Collections.emptyList();
                }
            } else {
                // Fallback to contains (case-insensitive)
                items = items.stream()
                        .filter(p -> p.getTransactionRef() != null && 
                                   p.getTransactionRef().toLowerCase().contains(query.toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        grid.setItems(items);
    }
}
