package com.hotel.booking.view;

import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.PaymentService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

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
            : "Manage and search all payments";

        add(new H1(title));
        add(new Span(subtitle));

        // Suchfeld
        TextField searchField = new TextField("Search Payments (transactionRef)");
        Button searchButton = new Button("Search");

        // Grid setup
        grid = new Grid<>(Payment.class, false);
        DateTimeFormatter germanFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(Locale.GERMANY);
        
        grid.addColumn(Payment::getId).setHeader("ID").setSortable(true).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(p -> p.getAmount()).setHeader("Betrag").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Payment::getMethod).setHeader("Methode").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Payment::getStatus).setHeader("Status").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Payment::getTransactionRef).setHeader("Transaktions-Ref").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(payment -> payment.getPaidAt() != null 
                ? payment.getPaidAt().format(germanFormatter) 
                : "")
                .setHeader("Bezahlt am").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.setMultiSort(true, MultiSortPriority.APPEND);
        grid.setWidthFull();

        // Load initial data
        loadPayments("");

        searchButton.addClickListener(e -> {
            String query = searchField.getValue();
            loadPayments(query);
        });
        searchButton.addClickShortcut(Key.ENTER);

        add(searchField, searchButton, grid);
    }

    private void loadPayments(String query) {
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
