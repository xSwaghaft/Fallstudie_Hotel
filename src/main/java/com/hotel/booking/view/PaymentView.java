package com.hotel.booking.view;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.entity.Payment;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


// @author: Arman Özcanli

@Route(value = "payment", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class PaymentView extends VerticalLayout implements HasDynamicTitle{
    private final SessionService sessionService;
    private final PaymentService paymentService;

    public PaymentView(SessionService sessionService, PaymentService paymentService) {
        this.sessionService = sessionService;
        this.paymentService = paymentService;

        VerticalLayout layout = new VerticalLayout();

        TextField searchField = new TextField("Search Payments (transactionRef)");
        Button searchButton = new Button("Search");

        Grid<Payment> grid = new Grid<>(Payment.class, false);
        grid.addColumn(Payment::getId).setHeader("ID").setSortable(true);
        grid.addColumn(p -> p.getAmount()).setHeader("Amount").setSortable(true);
        grid.addColumn(Payment::getMethod).setHeader("Method").setSortable(true);
        grid.addColumn(Payment::getStatus).setHeader("Status").setSortable(true);
        grid.addColumn(Payment::getTransactionRef).setHeader("Transaction Ref").setSortable(true);
        grid.addColumn(Payment::getPaidAt).setHeader("Paid At").setSortable(true);
        grid.setMultiSort(true, MultiSortPriority.APPEND);

        // Load initial data (all)
        loadPayments("", grid);

        searchButton.addClickListener(e -> {
            String query = searchField.getValue();
            loadPayments(query, grid);
        });
        searchButton.addClickShortcut(Key.ENTER);

        layout.add(searchField, searchButton, grid);
        var link = new RouterLink("Home", MainLayout.class);
        add(link, layout);

        add(new H1("Payment Management"), new Span("Manage and search payments"), new HorizontalLayout());
    }

    @Override
    public String getPageTitle() {
        return "Payment Management";
    }

    private void loadPayments(String query, Grid<Payment> grid) {
        List<Payment> items;
        if (query == null || query.isBlank()) {
            items = paymentService.findAll();
        } else {
            // Try exact match first
            var byTx = paymentService.findByTransactionRef(query);
            if (byTx.isPresent()) {
                items = Collections.singletonList(byTx.get());
            } else {
                // Fallback to contains (case-insensitive) over all payments
                items = paymentService.findAll().stream()
                        .filter(p -> p.getTransactionRef() != null && p.getTransactionRef().toLowerCase().contains(query.toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        grid.setItems(items);
    }
}
