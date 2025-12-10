package com.hotel.booking.view.components;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

/**
 * RegistrationForm Component - Reusable registration form with password validation.
 * Includes live password strength checking and address form fields.
 */
public class RegistrationForm extends VerticalLayout {

    private final Binder<RegistrationFormData> binder = new Binder<>(RegistrationFormData.class);
    private final RegistrationFormData formData = new RegistrationFormData();

    // Form fields
    private final TextField usernameField = new TextField("Username");
    private final TextField emailField = new TextField("Email");
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final TextField streetField = new TextField("Street");
    private final TextField houseNumberField = new TextField("No.");
    private final TextField postalCodeField = new TextField("Postal Code");
    private final TextField cityField = new TextField("City");
    private final TextField countryField = new TextField("Country");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField passwordRepeatField = new PasswordField("Confirm Password");

    // Password criteria container
    private Div passwordCriteriaContainer;
    private Div minLengthCriteria;
    private Div uppercaseCriteria;
    private Div lowercaseCriteria;
    private Div digitCriteria;
    private Div specialCharCriteria;

    // Buttons
    private final Button registerBtn = new Button("Register");
    private final Button cancelBtn = new Button("Back to Login");

    private Runnable onRegisterClick;
    private Runnable onCancelClick;

    public RegistrationForm() {
        setSpacing(false);
        setMargin(false);
        setPadding(false);
        setWidthFull();
        
        initializeFields();
        createPasswordCriteria();
        setupBinder();
        layoutForm();
    }

    private void initializeFields() {
        usernameField.setPlaceholder("Choose a unique username");
        usernameField.setWidthFull();
        usernameField.addClassName("login-input");
        usernameField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        emailField.setPlaceholder("your@email.com");
        emailField.setWidthFull();
        emailField.addClassName("login-input");
        emailField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        firstNameField.setPlaceholder("Max");
        firstNameField.setWidth("50%");
        firstNameField.addClassName("login-input");
        firstNameField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        lastNameField.setPlaceholder("Smith");
        lastNameField.setWidth("50%");
        lastNameField.addClassName("login-input");
        lastNameField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        streetField.setPlaceholder("Main Street");
        streetField.setWidth("75%");
        streetField.addClassName("login-input");
        streetField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        houseNumberField.setPlaceholder("42");
        houseNumberField.setWidth("25%");
        houseNumberField.addClassName("login-input");
        houseNumberField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        postalCodeField.setPlaceholder("12345");
        postalCodeField.setWidth("24%");
        postalCodeField.addClassName("login-input");
        postalCodeField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        cityField.setPlaceholder("Berlin");
        cityField.setWidth("34%");
        cityField.addClassName("login-input");
        cityField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        countryField.setPlaceholder("Germany");
        countryField.setWidth("38%");
        countryField.addClassName("login-input");
        countryField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        passwordField.setPlaceholder("Secure password");
        passwordField.setWidthFull();
        passwordField.addClassName("login-input");
        passwordField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");
        passwordField.addValueChangeListener(e -> updatePasswordCriteria(e.getValue()));

        passwordRepeatField.setPlaceholder("Confirm password");
        passwordRepeatField.setWidthFull();
        passwordRepeatField.addClassName("login-input");
        passwordRepeatField.getStyle().set("--vaadin-input-field-background", "#f3f4f6");

        registerBtn.addClassName("gold-button");
        registerBtn.setWidth("50%");
        registerBtn.addClickListener(e -> handleRegistration());
        registerBtn.addClickShortcut(Key.ENTER);

        cancelBtn.addClassName("back-login-button");
        cancelBtn.setWidth("50%");
        cancelBtn.addClickListener(e -> {
            if (onCancelClick != null) onCancelClick.run();
        });
    }

    private void createPasswordCriteria() {
        passwordCriteriaContainer = new Div();
        passwordCriteriaContainer.getStyle()
            .set("margin-top", "0.25rem")
            .set("margin-bottom", "0.15rem");

        minLengthCriteria = createCriteriaElement("at least 8 characters");
        uppercaseCriteria = createCriteriaElement("at least 1 uppercase letter");
        lowercaseCriteria = createCriteriaElement("at least 1 lowercase letter");
        digitCriteria = createCriteriaElement("at least 1 number");
        specialCharCriteria = createCriteriaElement("at least 1 special character");

        passwordCriteriaContainer.add(
                minLengthCriteria,
                uppercaseCriteria,
                lowercaseCriteria,
                digitCriteria,
                specialCharCriteria
        );
    }

    private Div createCriteriaElement(String text) {
        Div criteriaElement = new Div();
        criteriaElement.getStyle()
            .set("color", "#ef4444")
            .set("font-size", "0.8125rem")
            .set("line-height", "1.4")
            .set("margin", "0")
            .set("padding", "0");
        criteriaElement.add(new Span(text));
        return criteriaElement;
    }

    private void setupBinder() {
        binder.forField(usernameField)
            .withValidator((value, context) -> validateUsername(value, context))
            .bind(RegistrationFormData::getUsername, RegistrationFormData::setUsername);

        binder.forField(emailField)
            .withValidator((value, context) -> validateEmail(value, context))
            .bind(RegistrationFormData::getEmail, RegistrationFormData::setEmail);

        binder.forField(firstNameField)
            .withValidator((value, context) -> validateNotEmpty(value, "First name", context))
            .bind(RegistrationFormData::getFirstName, RegistrationFormData::setFirstName);

        binder.forField(lastNameField)
            .withValidator((value, context) -> validateNotEmpty(value, "Last name", context))
            .bind(RegistrationFormData::getLastName, RegistrationFormData::setLastName);

        binder.forField(streetField)
            .withValidator((value, context) -> validateNotEmpty(value, "Street", context))
            .bind(RegistrationFormData::getStreet, RegistrationFormData::setStreet);

        binder.forField(houseNumberField)
            .withValidator((value, context) -> validateNotEmpty(value, "House number", context))
            .bind(RegistrationFormData::getHouseNumber, RegistrationFormData::setHouseNumber);

        binder.forField(postalCodeField)
            .withValidator((value, context) -> validateNotEmpty(value, "Postal code", context))
            .bind(RegistrationFormData::getPostalCode, RegistrationFormData::setPostalCode);

        binder.forField(cityField)
            .withValidator((value, context) -> validateNotEmpty(value, "City", context))
            .bind(RegistrationFormData::getCity, RegistrationFormData::setCity);

        binder.forField(countryField)
            .withValidator((value, context) -> validateNotEmpty(value, "Country", context))
            .bind(RegistrationFormData::getCountry, RegistrationFormData::setCountry);

        binder.forField(passwordField)
            .withValidator((value, context) -> validatePasswordStrength(value, context))
            .bind(RegistrationFormData::getPassword, RegistrationFormData::setPassword);

        binder.forField(passwordRepeatField)
            .withValidator((value, context) -> validatePasswordMatch(value, context))
            .bind(RegistrationFormData::getPasswordRepeat, RegistrationFormData::setPasswordRepeat);

        binder.setBean(formData);
    }

    private ValidationResult validateUsername(String value, ValueContext context) {
        if (value == null || value.isBlank()) {
            return ValidationResult.error("Username is required");
        }
        if (value.length() < 3) {
            return ValidationResult.error("Username must be at least 3 characters");
        }
        return ValidationResult.ok();
    }

    private ValidationResult validateEmail(String value, ValueContext context) {
        if (value == null || value.isBlank()) {
            return ValidationResult.error("Email is required");
        }
        // RFC 5322 simplified regex pattern für Email-Validierung
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!value.matches(emailRegex)) {
            return ValidationResult.error("Please enter a valid email address");
        }
        return ValidationResult.ok();
    }

    private ValidationResult validateNotEmpty(String value, String fieldName, ValueContext context) {
        if (value == null || value.isBlank()) {
            return ValidationResult.error(fieldName + " is required");
        }
        return ValidationResult.ok();
    }

    private ValidationResult validatePasswordStrength(String value, ValueContext context) {
        if (value == null || value.isBlank()) {
            return ValidationResult.error("Password is required");
        }
        PasswordValidationResult result = validatePasswordStrengthLogic(value);
        if (!result.isValid()) {
            return ValidationResult.error("Password does not meet all criteria");
        }
        return ValidationResult.ok();
    }

    private ValidationResult validatePasswordMatch(String value, ValueContext context) {
        String password = passwordField.getValue();
        if (value == null || value.isBlank()) {
            return ValidationResult.error("Password confirmation is required");
        }
        if (!password.equals(value)) {
            return ValidationResult.error("Passwords do not match");
        }
        return ValidationResult.ok();
    }

    private void updatePasswordCriteria(String password) {
        PasswordValidationResult result = validatePasswordStrengthLogic(password);
        updateCriteriaElement(minLengthCriteria, result.isMinLength());
        updateCriteriaElement(uppercaseCriteria, result.isHasUppercase());
        updateCriteriaElement(lowercaseCriteria, result.isHasLowercase());
        updateCriteriaElement(digitCriteria, result.isHasDigit());
        updateCriteriaElement(specialCharCriteria, result.isHasSpecialChar());
    }

    private void updateCriteriaElement(Div criteriaElement, boolean fulfilled) {
        if (fulfilled) {
            criteriaElement.getStyle().set("color", "#10b981");
        } else {
            criteriaElement.getStyle().set("color", "#ef4444");
        }
    }

    private PasswordValidationResult validatePasswordStrengthLogic(String password) {
        if (password == null) {
            return new PasswordValidationResult(false, false, false, false, false, false);
        }

        boolean hasMinLength = password.length() >= 8;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        boolean isValid = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar;

        return new PasswordValidationResult(isValid, hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecialChar);
    }

    private void layoutForm() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);
        formLayout.setPadding(false);
        formLayout.getStyle().set("gap", "0.5rem").set("row-gap", "0.5rem");

        // Name row
        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setWidthFull();
        nameRow.setSpacing(false);
        nameRow.getStyle().set("gap", "0.5rem");
        nameRow.add(firstNameField, lastNameField);

        // Address row 1
        HorizontalLayout addressRow1 = new HorizontalLayout();
        addressRow1.setWidthFull();
        addressRow1.setSpacing(false);
        addressRow1.getStyle().set("gap", "0.5rem");
        addressRow1.add(streetField, houseNumberField);

        // Address row 2
        HorizontalLayout addressRow2 = new HorizontalLayout();
        addressRow2.setWidthFull();
        addressRow2.setSpacing(false);
        addressRow2.getStyle().set("gap", "0.5rem");
        addressRow2.add(postalCodeField, cityField, countryField);

        // Buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(false);
        buttonLayout.getStyle().set("gap", "0.5rem");
        buttonLayout.add(registerBtn, cancelBtn);

        formLayout.add(
                usernameField,
                emailField,
                nameRow,
                addressRow1,
                addressRow2,
                passwordField,
                passwordCriteriaContainer,
                passwordRepeatField
        );

        add(formLayout, new Hr(), buttonLayout);
    }

    private void handleRegistration() {
        if (binder.writeBeanIfValid(formData)) {
            if (onRegisterClick != null) {
                onRegisterClick.run();
            }
        }
    }

    public RegistrationFormData getFormData() {
        return formData;
    }

    public void setOnRegisterClick(Runnable callback) {
        this.onRegisterClick = callback;
    }

    public void setOnCancelClick(Runnable callback) {
        this.onCancelClick = callback;
    }

    public static class RegistrationFormData {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String street;
        private String houseNumber;
        private String postalCode;
        private String city;
        private String country;
        private String password;
        private String passwordRepeat;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getHouseNumber() { return houseNumber; }
        public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getPasswordRepeat() { return passwordRepeat; }
        public void setPasswordRepeat(String passwordRepeat) { this.passwordRepeat = passwordRepeat; }
    }

    private static class PasswordValidationResult {
        private final boolean isValid;
        private final boolean minLength;
        private final boolean hasUppercase;
        private final boolean hasLowercase;
        private final boolean hasDigit;
        private final boolean hasSpecialChar;

        public PasswordValidationResult(boolean isValid, boolean minLength, boolean hasUppercase,
                                       boolean hasLowercase, boolean hasDigit, boolean hasSpecialChar) {
            this.isValid = isValid;
            this.minLength = minLength;
            this.hasUppercase = hasUppercase;
            this.hasLowercase = hasLowercase;
            this.hasDigit = hasDigit;
            this.hasSpecialChar = hasSpecialChar;
        }

        public boolean isValid() { return isValid; }
        public boolean isMinLength() { return minLength; }
        public boolean isHasUppercase() { return hasUppercase; }
        public boolean isHasLowercase() { return hasLowercase; }
        public boolean isHasDigit() { return hasDigit; }
        public boolean isHasSpecialChar() { return hasSpecialChar; }
    }
}
