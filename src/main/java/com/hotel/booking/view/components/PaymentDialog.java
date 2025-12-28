package com.hotel.booking.view.components;

import java.math.BigDecimal;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
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
    private PaymentMethodManager paymentMethodManager;
    private TextField cardNumberField;
    private TextField expiryField;
    private PasswordField cvcField;
    private TextField ibanField;
    private TextField bicField;
    private TextField accountHolderField;
    private Runnable onPaymentSuccess;
    private Runnable onPaymentDeferred;

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

        // Initialize payment method manager
        paymentMethodManager = new PaymentMethodManager();

        // Payment method selection
        RadioButtonGroup<String> paymentMethodGroup = paymentMethodManager.getPaymentMethodGroup();
        paymentMethodGroup.addValueChangeListener(e -> paymentMethodManager.togglePaymentMethod(e.getValue()));
        content.add(paymentMethodGroup);

        // Credit Card Section
        VerticalLayout cardSection = paymentMethodManager.buildCardSection(
                createCardNumberField(),
                createExpiryField(),
                createCvcField()
        );
        content.add(cardSection);

        // Bank Transfer Section
        VerticalLayout bankSection = paymentMethodManager.buildBankSection(
                createIbanField(),
                createBicField(),
                createAccountHolderField()
        );
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

    private TextField createPaymentField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        return field;
    }

    private PasswordField createPasswordField(String label, String placeholder) {
        PasswordField field = new PasswordField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        return field;
    }

    private TextField createCardNumberField() {
        cardNumberField = createPaymentField("Card Number", "1234 5678 9012 3456");
        return cardNumberField;
    }

    private TextField createExpiryField() {
        expiryField = createPaymentField("Expiry Date", "MM/YY");
        return expiryField;
    }

    private PasswordField createCvcField() {
        cvcField = createPasswordField("CVC", "123");
        return cvcField;
    }

    private TextField createIbanField() {
        ibanField = createPaymentField("IBAN", "DE89 3704 0044 0532 0130 00");
        return ibanField;
    }

    private TextField createBicField() {
        bicField = createPaymentField("BIC", "COBADEFF");
        return bicField;
    }

    private TextField createAccountHolderField() {
        accountHolderField = createPaymentField("Account Holder", "John Doe");
        return accountHolderField;
    }

    // ===== BINDER SETUP WITH VALIDATORS =====

    private void setupBinder() {
        binder = new Binder<>(PaymentData.class);

        // Credit Card Validators
        binder.forField(cardNumberField)
                .asRequired("Card number is required")
                .withValidator(PaymentValidator::isValidCardNumber, "Invalid card number format")
                .bind(PaymentData::getCardNumber, PaymentData::setCardNumber);

        binder.forField(expiryField)
                .asRequired("Expiry date is required")
                .withValidator(PaymentValidator::isValidExpiryDate, "Use MM/YY format")
                .bind(PaymentData::getExpiry, PaymentData::setExpiry);

        binder.forField(cvcField)
                .asRequired("CVC is required")
                .withValidator(PaymentValidator::isValidCvc, "CVC must be 3-4 digits")
                .bind(PaymentData::getCvc, PaymentData::setCvc);

        // Bank Transfer Validators
        binder.forField(ibanField)
                .asRequired("IBAN is required")
                .withValidator(PaymentValidator::isValidIban, "Invalid IBAN format")
                .bind(PaymentData::getIban, PaymentData::setIban);

        binder.forField(bicField)
                .asRequired("BIC is required")
                .withValidator(PaymentValidator::isValidBic, "BIC must be 8 or 11 characters")
                .bind(PaymentData::getBic, PaymentData::setBic);

        binder.forField(accountHolderField)
                .asRequired("Account holder is required")
                .bind(PaymentData::getAccountHolder, PaymentData::setAccountHolder);
    }



    // ===== PAYMENT PROCESSING =====

    private void processPayment() {
        String method = paymentMethodManager.getSelectedPaymentMethod();
        resetFieldErrors();
        
        if ("Credit Card".equals(method)) {
            if (!validate(cardNumberField, PaymentValidator::isValidCardNumber, "Invalid card number format (must be 16 digits)")) return;
            if (!validate(expiryField, PaymentValidator::isValidExpiryDate, "Use MM/YY format")) return;
            if (!validate(cvcField, PaymentValidator::isValidCvc, "CVC must be 3-4 digits")) return;
        } else {
            if (!validate(ibanField, PaymentValidator::isValidIban, "Invalid IBAN format")) return;
            if (!validate(bicField, PaymentValidator::isValidBic, "BIC must be 8 or 11 characters")) return;
            if (!validate(accountHolderField, v -> !v.trim().isEmpty(), "Account holder is required")) return;
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
    
    /**
     * Generic validation method - works with both TextField and PasswordField via interface
     */
    private boolean validate(com.vaadin.flow.component.HasValue<?, String> field, 
                             java.util.function.Predicate<String> validator, 
                             String errorMsg) {
        String value = field.getValue();
        if (value == null || value.isEmpty()) {
            setFieldError(field, "This field is required");
            return false;
        }
        if (!validator.test(value)) {
            setFieldError(field, errorMsg);
            return false;
        }
        return true;
    }

    private void setFieldError(com.vaadin.flow.component.HasValue<?, String> field, String errorMsg) {
        if (field instanceof TextField) {
            TextField textField = (TextField) field;
            textField.setInvalid(true);
            textField.setErrorMessage(errorMsg);
        } else if (field instanceof PasswordField) {
            PasswordField passwordField = (PasswordField) field;
            passwordField.setInvalid(true);
            passwordField.setErrorMessage(errorMsg);
        }
    }

    public void setOnPaymentSuccess(Runnable callback) {
        this.onPaymentSuccess = callback;
    }

    public void setOnPaymentDeferred(Runnable callback) {
        this.onPaymentDeferred = callback;
    }

    public String getSelectedPaymentMethod() {
        return paymentMethodManager.getSelectedPaymentMethod();
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
