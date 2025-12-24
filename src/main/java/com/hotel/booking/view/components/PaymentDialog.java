package com.hotel.booking.view.components;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

public class PaymentDialog extends Dialog {

    private Binder<PaymentData> binder;
    private RadioButtonGroup<String> paymentMethodGroup;
    private TextField cardNumberField;
    private TextField expiryField;
    private PasswordField cvcField;
    private TextField ibanField;
    private TextField bicField;
    private TextField accountHolderField;
    private VerticalLayout cardSection;
    private VerticalLayout bankSection;
    private Runnable onPaymentSuccess;
    private Runnable onPaymentDeferred; // Callback for "Pay Later"
    private String selectedPaymentMethod; // Store selected payment method

    public PaymentDialog(BigDecimal amount) {
        setHeaderTitle("Payment");
        setWidth("500px");
        setMaxWidth("90%");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // Amount display
        content.add(new Paragraph("Amount to pay: €" + String.format("%.2f", amount)));
        content.add(new Div()); // Spacer

        // Payment method selection
        paymentMethodGroup = new RadioButtonGroup<>();
        paymentMethodGroup.setLabel("Payment Method");
        paymentMethodGroup.setItems("Credit Card", "Bank Transfer");
        paymentMethodGroup.setValue("Credit Card");
        paymentMethodGroup.addValueChangeListener(e -> {
            selectedPaymentMethod = e.getValue();
            togglePaymentMethod(e.getValue());
        });
        content.add(paymentMethodGroup);

        // Credit Card Section
        cardSection = new VerticalLayout();
        cardSection.setPadding(false);
        cardSection.setSpacing(true);
        cardSection.add(
                createCardNumberField(),
                createExpiryField(),
                createCvcField()
        );
        content.add(cardSection);

        // Bank Transfer Section
        bankSection = new VerticalLayout();
        bankSection.setPadding(false);
        bankSection.setSpacing(true);
        bankSection.add(
                createIbanField(),
                createBicField(),
                createAccountHolderField()
        );
        bankSection.setVisible(false);
        content.add(bankSection);

        // Binder setup
        setupBinder();

        add(content);

        // Buttons
        Button pay = new Button("Pay", e -> processPayment());
        pay.addClassName("primary-button");
        Button payLater = new Button("Pay Later", e -> deferPayment());
        payLater.addClassName("secondary-button");
        Button cancel = new Button("Cancel", e -> close());

        getFooter().add(new HorizontalLayout(pay, payLater, cancel));
    }

    // ===== FIELD CREATORS =====

    private TextField createCardNumberField() {
        cardNumberField = new TextField("Card Number");
        cardNumberField.setPlaceholder("1234 5678 9012 3456");
        cardNumberField.setWidthFull();
        return cardNumberField;
    }

    private TextField createExpiryField() {
        expiryField = new TextField("Expiry Date");
        expiryField.setPlaceholder("MM/YY");
        expiryField.setWidthFull();
        return expiryField;
    }

    private PasswordField createCvcField() {
        PasswordField cvc = new PasswordField();
        cvc.setLabel("CVC");
        cvc.setPlaceholder("123");
        cvc.setWidthFull();
        cvcField = cvc;
        return cvc;
    }

    private TextField createIbanField() {
        ibanField = new TextField("IBAN");
        ibanField.setPlaceholder("DE89 3704 0044 0532 0130 00");
        ibanField.setWidthFull();
        return ibanField;
    }

    private TextField createBicField() {
        bicField = new TextField("BIC");
        bicField.setPlaceholder("COBADEFF");
        bicField.setWidthFull();
        return bicField;
    }

    private TextField createAccountHolderField() {
        accountHolderField = new TextField("Account Holder");
        accountHolderField.setPlaceholder("John Doe");
        accountHolderField.setWidthFull();
        return accountHolderField;
    }

    // ===== BINDER SETUP WITH VALIDATORS =====

    private void setupBinder() {
        binder = new Binder<>(PaymentData.class);

        // Credit Card Validators
        binder.forField(cardNumberField)
                .asRequired("Card number is required")
                .withValidator(value -> isValidCardNumber(value), "Invalid card number format")
                .bind(PaymentData::getCardNumber, PaymentData::setCardNumber);

        binder.forField(expiryField)
                .asRequired("Expiry date is required")
                .withValidator(value -> isValidExpiryDate(value), "Use MM/YY format")
                .bind(PaymentData::getExpiry, PaymentData::setExpiry);

        binder.forField(cvcField)
                .asRequired("CVC is required")
                .withValidator(value -> isValidCvc(value), "CVC must be 3-4 digits")
                .bind(PaymentData::getCvc, PaymentData::setCvc);

        // Bank Transfer Validators
        binder.forField(ibanField)
                .asRequired("IBAN is required")
                .withValidator(value -> isValidIban(value), "Invalid IBAN format")
                .bind(PaymentData::getIban, PaymentData::setIban);

        binder.forField(bicField)
                .asRequired("BIC is required")
                .withValidator(value -> isValidBic(value), "BIC must be 8 or 11 characters")
                .bind(PaymentData::getBic, PaymentData::setBic);

        binder.forField(accountHolderField)
                .asRequired("Account holder is required")
                .bind(PaymentData::getAccountHolder, PaymentData::setAccountHolder);
    }

    // ===== VALIDATORS =====

    private boolean isValidCardNumber(String value) {
        if (value == null || value.isEmpty()) return false;
        // Remove spaces and check if it's 16 digits
        String cleaned = value.replaceAll("\\s", "");
        return cleaned.matches("\\d{16}");
    }

    private boolean isValidExpiryDate(String value) {
        if (value == null || value.isEmpty()) return false;
        // Check MM/YY format
        return value.matches("^(0[1-9]|1[0-2])/\\d{2}$");
    }

    private boolean isValidCvc(String value) {
        if (value == null || value.isEmpty()) return false;
        // CVC must be 3 or 4 digits
        return value.matches("\\d{3,4}");
    }

    private boolean isValidIban(String value) {
        if (value == null || value.isEmpty()) return false;
        // Simple IBAN validation: starts with 2 letters, followed by 2 digits, rest alphanumeric
        String cleaned = value.replaceAll("\\s", "").toUpperCase();
        return cleaned.matches("^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$") && cleaned.length() >= 15 && cleaned.length() <= 34;
    }

    private boolean isValidBic(String value) {
        if (value == null || value.isEmpty()) return false;
        // BIC must be 8 or 11 characters, alphanumeric
        String cleaned = value.replaceAll("\\s", "").toUpperCase();
        return cleaned.matches("^[A-Z0-9]{8}([A-Z0-9]{3})?$");
    }

    // ===== PAYMENT PROCESSING =====

    private void togglePaymentMethod(String method) {
        if ("Credit Card".equals(method)) {
            cardSection.setVisible(true);
            bankSection.setVisible(false);
        } else {
            cardSection.setVisible(false);
            bankSection.setVisible(true);
        }
    }

    private void processPayment() {
        String method = paymentMethodGroup.getValue();
        boolean isValid = true;
        
        // Reset all field errors
        resetFieldErrors();
        
        if ("Credit Card".equals(method)) {
            // Validate ONLY credit card fields
            if (!validateCardNumber()) isValid = false;
            if (!validateExpiry()) isValid = false;
            if (!validateCvc()) isValid = false;
        } else {
            // Validate ONLY bank transfer fields
            if (!validateIban()) isValid = false;
            if (!validateBic()) isValid = false;
            if (!validateAccountHolder()) isValid = false;
        }
        
        if (!isValid) {
            return;
        }

        // Simulate payment processing
        Notification.show("Payment processed successfully!", 3000, Notification.Position.TOP_CENTER);
        
        if (onPaymentSuccess != null) {
            onPaymentSuccess.run();
        }
        
        close();
    }
    
    private void resetFieldErrors() {
        cardNumberField.setInvalid(false);
        expiryField.setInvalid(false);
        cvcField.setInvalid(false);
        ibanField.setInvalid(false);
        bicField.setInvalid(false);
        accountHolderField.setInvalid(false);
    }
    
    private boolean validateCardNumber() {
        String value = cardNumberField.getValue();
        if (value == null || value.isEmpty()) {
            cardNumberField.setInvalid(true);
            cardNumberField.setErrorMessage("Card number is required");
            return false;
        }
        if (!isValidCardNumber(value)) {
            cardNumberField.setInvalid(true);
            cardNumberField.setErrorMessage("Invalid card number format (must be 16 digits)");
            return false;
        }
        return true;
    }
    
    private boolean validateExpiry() {
        String value = expiryField.getValue();
        if (value == null || value.isEmpty()) {
            expiryField.setInvalid(true);
            expiryField.setErrorMessage("Expiry date is required");
            return false;
        }
        if (!isValidExpiryDate(value)) {
            expiryField.setInvalid(true);
            expiryField.setErrorMessage("Use MM/YY format");
            return false;
        }
        return true;
    }
    
    private boolean validateCvc() {
        String value = cvcField.getValue();
        if (value == null || value.isEmpty()) {
            cvcField.setInvalid(true);
            cvcField.setErrorMessage("CVC is required");
            return false;
        }
        if (!isValidCvc(value)) {
            cvcField.setInvalid(true);
            cvcField.setErrorMessage("CVC must be 3-4 digits");
            return false;
        }
        return true;
    }
    
    private boolean validateIban() {
        String value = ibanField.getValue();
        if (value == null || value.isEmpty()) {
            ibanField.setInvalid(true);
            ibanField.setErrorMessage("IBAN is required");
            return false;
        }
        if (!isValidIban(value)) {
            ibanField.setInvalid(true);
            ibanField.setErrorMessage("Invalid IBAN format");
            return false;
        }
        return true;
    }
    
    private boolean validateBic() {
        String value = bicField.getValue();
        if (value == null || value.isEmpty()) {
            bicField.setInvalid(true);
            bicField.setErrorMessage("BIC is required");
            return false;
        }
        if (!isValidBic(value)) {
            bicField.setInvalid(true);
            bicField.setErrorMessage("BIC must be 8 or 11 characters");
            return false;
        }
        return true;
    }
    
    private boolean validateAccountHolder() {
        String value = accountHolderField.getValue();
        if (value == null || value.trim().isEmpty()) {
            accountHolderField.setInvalid(true);
            accountHolderField.setErrorMessage("Account holder is required");
            return false;
        }
        return true;
    }

    public void setOnPaymentSuccess(Runnable callback) {
        this.onPaymentSuccess = callback;
    }

    public void setOnPaymentDeferred(Runnable callback) {
        this.onPaymentDeferred = callback;
    }

    public String getSelectedPaymentMethod() {
        return selectedPaymentMethod != null ? selectedPaymentMethod : "Credit Card";
    }

    private void deferPayment() {
        System.out.println("DEBUG: Payment deferred!");
        Notification.show("Payment postponed. You can pay later in 'My Bookings'.", 3000, Notification.Position.TOP_CENTER);
        
        if (onPaymentDeferred != null) {
            onPaymentDeferred.run();
        }
        
        close();
    }

    // ===== DATA CLASS =====

    public static class PaymentData {
        private String cardNumber;
        private String expiry;
        private String cvc;
        private String iban;
        private String bic;
        private String accountHolder;

        public String getCardNumber() {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public String getExpiry() {
            return expiry;
        }

        public void setExpiry(String expiry) {
            this.expiry = expiry;
        }

        public String getCvc() {
            return cvc;
        }

        public void setCvc(String cvc) {
            this.cvc = cvc;
        }

        public String getIban() {
            return iban;
        }

        public void setIban(String iban) {
            this.iban = iban;
        }

        public String getBic() {
            return bic;
        }

        public void setBic(String bic) {
            this.bic = bic;
        }

        public String getAccountHolder() {
            return accountHolder;
        }

        public void setAccountHolder(String accountHolder) {
            this.accountHolder = accountHolder;
        }
    }
}
