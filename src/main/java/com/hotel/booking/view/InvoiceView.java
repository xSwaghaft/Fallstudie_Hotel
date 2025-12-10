package com.hotel.booking.view;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.InvoiceService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.router.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Uses InvoiceFDO for form binding
@Route(value = "invoices", layout = MainLayout.class)
@PageTitle("Invoice Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/invoice.css")
public class InvoiceView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final InvoiceService invoiceService;
    private Grid<Invoice> grid;

    // FDO for Invoice form - simple JavaBean for UI binding
    public static class InvoiceFDO {
        private String invoiceNumber = "";
        private BigDecimal amount = BigDecimal.ZERO;
        private Invoice.PaymentMethod paymentMethod = Invoice.PaymentMethod.CARD;
        private Invoice.PaymentStatus status = Invoice.PaymentStatus.PENDING;

        // Getters and setters
        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Invoice.PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(Invoice.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
        public Invoice.PaymentStatus getStatus() { return status; }
        public void setStatus(Invoice.PaymentStatus status) { this.status = status; }
    }

    public InvoiceView(SessionService sessionService, InvoiceService invoiceService) {
        this.sessionService = sessionService;
        this.invoiceService = invoiceService;

        //simple layout
        VerticalLayout layout = new VerticalLayout();

        TextField searchField = new TextField("Search Invoices (Invoice Number)");
        Button searchButton = new Button("Search");
        Button addButton = new Button("Add Invoice");

        HorizontalLayout buttonLayout = new HorizontalLayout(searchButton, addButton);

        //grid
        grid = new Grid<>(Invoice.class, false);
        grid.addColumn(Invoice::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Invoice Number").setSortable(true);
        grid.addColumn(invoice -> invoice.getAmount()).setHeader("Amount").setSortable(true);
        grid.addColumn(Invoice::getPaymentMethod).setHeader("Payment Method").setSortable(true);
        grid.addColumn(Invoice::getInvoiceStatus).setHeader("Status").setSortable(true);
        grid.addColumn(Invoice::getIssuedAt).setHeader("Issued At").setSortable(true);

        // Load initial data (all invoices)
        loadInvoices("");

        // Search button click
        searchButton.addClickListener(e -> {
            String query = searchField.getValue();
            loadInvoices(query);
        });
        searchButton.addClickShortcut(Key.ENTER);

        // Add button click
        addButton.addClickListener(e -> openAddInvoiceDialog());

        layout.add(searchField, buttonLayout, grid);
        var link = new RouterLink("Home", MainLayout.class);
        add(link, layout);

        add(new H1("Invoice Management"), new Span("Manage and search invoices"));
    }

    // Simple search method
    private void loadInvoices(String query) {
        List<Invoice> items;
        if (query == null || query.isBlank()) {
            items = invoiceService.findAll();
        } else {
            // Try exact match first by invoice number
            var byNumber = invoiceService.findByInvoiceNumber(query);
            if (byNumber.isPresent()) {
                items = Collections.singletonList(byNumber.get());
            } else {
                // Fallback to contains search (groß-/kleinschreibung wichtig)
                items = invoiceService.findAll().stream()
                        .filter(inv -> inv.getInvoiceNumber() != null && 
                                inv.getInvoiceNumber().toLowerCase().contains(query.toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        grid.setItems(items);
    }

    // Simple dialog for adding new invoices with Binder
    private void openAddInvoiceDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Invoice");

        // Create FDO and Binder
        InvoiceFDO formData = new InvoiceFDO();
        Binder<InvoiceFDO> binder = new Binder<>(InvoiceFDO.class);

        // Form fields
        TextField invoiceNumberField = new TextField("Invoice Number");
        NumberField amountField = new NumberField("Amount");
        Select<Invoice.PaymentMethod> methodSelect = new Select<>();
        methodSelect.setLabel("Payment Method");
        methodSelect.setItems(Invoice.PaymentMethod.values());
        
        Select<Invoice.PaymentStatus> statusSelect = new Select<>();
        statusSelect.setLabel("Status");
        statusSelect.setItems(Invoice.PaymentStatus.values());

        // Bind fields to FDO using Binder
        binder.forField(invoiceNumberField)
                .asRequired("Invoice number is required")
                .bind(InvoiceFDO::getInvoiceNumber, InvoiceFDO::setInvoiceNumber);
        
        binder.forField(amountField)
                .asRequired("Amount is required")
                .withConverter(
                    value -> value != null ? BigDecimal.valueOf(value) : null, 
                    value -> value != null ? value.doubleValue() : null,
                    "Please enter a valid amount")
                .bind(InvoiceFDO::getAmount, InvoiceFDO::setAmount);
        
        binder.forField(methodSelect)
                .asRequired("Payment method is required")
                .bind(InvoiceFDO::getPaymentMethod, InvoiceFDO::setPaymentMethod);
        
        binder.forField(statusSelect)
                .asRequired("Status is required")
                .bind(InvoiceFDO::getStatus, InvoiceFDO::setStatus);

        // Set default values
        binder.readBean(formData);

        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(invoiceNumberField, amountField, methodSelect, statusSelect);
        
        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        
        saveButton.addClickListener(e -> {
            try {
                binder.writeBean(formData);
                
                // Create new Invoice from FDO
                Invoice newInvoice = new Invoice();
                newInvoice.setInvoiceNumber(formData.getInvoiceNumber());
                newInvoice.setAmount(formData.getAmount());
                newInvoice.setPaymentMethod(formData.getPaymentMethod());
                newInvoice.setInvoiceStatus(formData.getStatus());
                
                // Save through service
                invoiceService.save(newInvoice);
                
                // Refresh grid and close dialog
                loadInvoices("");
                dialog.close();
                Notification.show("Invoice saved successfully!");
                
            } catch (ValidationException ex) {
                Notification.show("Please fix the errors in the form");
            }
        });
        
        cancelButton.addClickListener(e -> dialog.close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(formLayout, buttonLayout);
        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}