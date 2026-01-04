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

/**
 * Dialog component for payment entry and processing.
 * <p>
 * Provides a user-friendly interface for collecting payment information and processing payments.
 * Supports two payment methods:
 * </p>
 * <ul>
 *   <li>Credit Card: collects card number, expiry date, and CVC</li>
 *   <li>Bank Transfer: collects IBAN, BIC, and account holder information</li>
 * </ul>
 * <p>
 * Features include dynamic payment method selection with form field switching, comprehensive
 * validation of all payment fields, error display for failed validation, callback support for
 * successful and deferred payments, and binder-based data binding for form handling. The dialog
 * displays the payment amount and allows users to choose their preferred payment method before
 * submitting payment details for processing.
 * </p>
 *
 * @author Arman Özcanli
 * @see PaymentMethodManager
 * @see PaymentValidator
 */
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

    /**
     * Constructs a PaymentDialog for collecting payment information.
     *
     * @param amount the payment amount to be processed
     */
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

    /**
     * Creates a standard TextField for payment form input.
     * <p>
     * Creates a text field with the specified label and placeholder text.
     * The field is set to full width and ready for data entry.
     * </p>
     *
     * @param label the label for the text field
     * @param placeholder the placeholder text to display when field is empty
     * @return a configured TextField for payment input
     */
    private TextField createPaymentField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        return field;
    }

    /**
     * Creates a PasswordField for secure input of sensitive payment data.
     * <p>
     * Creates a password field with the specified label and placeholder text.
     * The field masks input for security and is set to full width.
     * </p>
     *
     * @param label the label for the password field
     * @param placeholder the placeholder text to display when field is empty
     * @return a configured PasswordField for sensitive payment input
     */
    private PasswordField createPasswordField(String label, String placeholder) {
        PasswordField field = new PasswordField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        return field;
    }

    /**
     * Creates a card number input field.
     * <p>
     * Creates a TextField for entering credit card numbers with a relevant placeholder.
     * Stores the field reference for later validation and binder binding.
     * </p>
     *
     * @return a TextField configured for card number input
     */
    private TextField createCardNumberField() {
        cardNumberField = createPaymentField("Card Number", "1234 5678 9012 3456");
        return cardNumberField;
    }

    /**
     * Creates an expiry date input field for credit card expiry.
     * <p>
     * Creates a TextField for entering card expiry dates in MM/YY format.
     * Stores the field reference for later validation and binder binding.
     * </p>
     *
     * @return a TextField configured for expiry date input
     */
    private TextField createExpiryField() {
        expiryField = createPaymentField("Expiry Date", "MM/YY");
        return expiryField;
    }

    /**
     * Creates a CVC security code input field.
     * <p>
     * Creates a PasswordField for entering the 3-4 digit CVC security code on the back of credit cards.
     * Masks the input for security purposes. Stores the field reference for later validation and binding.
     * </p>
     *
     * @return a PasswordField configured for CVC code input
     */
    private PasswordField createCvcField() {
        cvcField = createPasswordField("CVC", "123");
        return cvcField;
    }

    /**
     * Creates an IBAN input field for bank transfer payments.
     * <p>
     * Creates a TextField for entering the International Bank Account Number (IBAN).
     * Stores the field reference for later validation and binder binding.
     * </p>
     *
     * @return a TextField configured for IBAN input
     */
    private TextField createIbanField() {
        ibanField = createPaymentField("IBAN", "DE89 3704 0044 0532 0130 00");
        return ibanField;
    }

    /**
     * Creates a BIC input field for bank transfer payments.
     * <p>
     * Creates a TextField for entering the Bank Identifier Code (BIC), also known as SWIFT code.
     * Stores the field reference for later validation and binder binding.
     * </p>
     *
     * @return a TextField configured for BIC code input
     */
    private TextField createBicField() {
        bicField = createPaymentField("BIC", "COBADEFF");
        return bicField;
    }

    /**
     * Creates an account holder name input field for bank transfer payments.
     * <p>
     * Creates a TextField for entering the name of the account holder for bank transfer verification.
     * Stores the field reference for later validation and binder binding.
     * </p>
     *
     * @return a TextField configured for account holder name input
     */
    private TextField createAccountHolderField() {
        accountHolderField = createPaymentField("Account Holder", "John Doe");
        return accountHolderField;
    }

    // ===== BINDER SETUP WITH VALIDATORS =====

    /**
     * Sets up the data binder with validators for all payment form fields.
     * <p>
     * Configures bidirectional binding between form fields and the PaymentData object.
     * Applies appropriate validators for credit card fields (card number, expiry, CVC) and
     * bank transfer fields (IBAN, BIC, account holder). All validators are required fields.
     * Validation occurs during form submission via processPayment().
     * </p>
     */
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

    /**
     * Processes the payment with validation and callback execution.
     * <p>
     * Validates all form fields based on the selected payment method (credit card or bank transfer).
     * If all validations pass, displays a success notification, executes the onPaymentSuccess callback,
     * and closes the dialog. If validation fails, displays error messages on the invalid fields.
     * </p>
     */
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
    
    /**
     * Resets the error state of all payment form fields.
     * <p>
     * Clears invalid flags on all credit card and bank transfer input fields,
     * removing any previously displayed error messages. Called before new validation
     * to ensure a clean state.
     * </p>
     */
    private void resetFieldErrors() {
        cardNumberField.setInvalid(false);
        expiryField.setInvalid(false);
        cvcField.setInvalid(false);
        ibanField.setInvalid(false);
        bicField.setInvalid(false);
        accountHolderField.setInvalid(false);
    }
    
    /**
     * Validates a single form field using the provided validator function.
     * <p>
     * Generic validation method supporting both TextField and PasswordField inputs.
     * Checks that the field value is not empty and passes the validator predicate test.
     * Sets field error state and message if validation fails. Used during payment processing
     * to validate all fields before submitting the payment.
     * </p>
     *
     * @param field the form field to validate (TextField or PasswordField)
     * @param validator a predicate function that tests the field value
     * @param errorMsg the error message to display if validation fails
     * @return true if validation passes, false if validation fails
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

    /**
     * Sets the error state and message for a form field.
     * <p>
     * Handles both TextField and PasswordField by checking the field type.
     * Sets the invalid flag and displays the error message in the field.
     * </p>
     *
     * @param field the form field to mark as invalid
     * @param errorMsg the error message to display
     */
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

    /**
     * Registers a callback to be executed when payment is successfully processed.
     *
     * @param callback the runnable to execute on successful payment
     */
    public void setOnPaymentSuccess(Runnable callback) {
        this.onPaymentSuccess = callback;
    }

    /**
     * Registers a callback to be executed when payment is deferred to a later time.
     *
     * @param callback the runnable to execute when payment is deferred
     */
    public void setOnPaymentDeferred(Runnable callback) {
        this.onPaymentDeferred = callback;
    }

    /**
     * Gets the currently selected payment method.
     *
     * @return the selected payment method ("Credit Card" or "Bank Transfer")
     */
    public String getSelectedPaymentMethod() {
        return paymentMethodManager.getSelectedPaymentMethod();
    }

    /**
     * Defers payment to a later time.
     * <p>
     * Allows the user to postpone payment, which can be completed later in the "My Bookings" section.
     * Displays a notification message, executes the onPaymentDeferred callback, and closes the dialog.
     * Useful for guests who want to finalize bookings without immediately paying.
     * </p>
     */
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
