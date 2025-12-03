package com.hotel.booking.view;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.AdressEmbeddable;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.service.UserService;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RegistrationView - Registration form following Vaadin and Spring Security best practices.
 */
@Route("register")
@AnonymousAllowed
@CssImport("./themes/hotel/styles.css")
public class RegistrationView extends Div implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(RegistrationView.class);

    private final UserService userService;
    private final SessionService sessionService;
    
    private final Binder<RegistrationFormData> binder = new Binder<>(RegistrationFormData.class);
    private final RegistrationFormData formData = new RegistrationFormData();
    
    private boolean darkMode = false;

    private Div passwordCriteriaContainer;
    private Div minLengthCriteria;
    private Div uppercaseCriteria;
    private Div lowercaseCriteria;
    private Div digitCriteria;
    private Div specialCharCriteria;

    @Autowired
    public RegistrationView(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;

        addClassName("registration-view");
        setSizeFull();

        add(createLeftSection());
        add(createRightSection());
        add(createThemeToggle());
    }

    private Div createLeftSection() {
        Div left = new Div();
        left.addClassName("login-left");
        
        Div overlay = new Div();
        overlay.addClassName("login-overlay");
        overlay.add(new H1("Hotelium"),
                    new Paragraph("Experience luxury and comfort with our comprehensive booking management system"));
        left.add(overlay);
        
        return left;
    }

    private VerticalLayout createRightSection() {
        VerticalLayout right = new VerticalLayout();
        right.addClassName("login-right");
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div card = new Div();
        card.addClassName("registration-card");

        H2 title = new H2("Create Your Account");
        title.addClassName("registration-title");
            
        Paragraph info = new Paragraph("Register to get started with our platform");
        info.addClassName("registration-subtitle");

        // Form layout
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);
        formLayout.setPadding(false);
        formLayout.getStyle().set("gap", "0.5rem").set("row-gap", "0.5rem");

        // Create form fields
        TextField usernameField = createInputField("Username", "Choose a unique username");
        TextField emailField = createInputField("Email", "your@email.com");

        // First and last name in one row
        HorizontalLayout nameRow = createNameRow();

        // Address rows
        HorizontalLayout addressRow1 = createAddressRow1();
        HorizontalLayout addressRow2 = createAddressRow2();

        // Get address fields from rows
        TextField streetField = (TextField) addressRow1.getComponentAt(0);
        TextField houseNumberField = (TextField) addressRow1.getComponentAt(1);

        TextField postalCodeField = (TextField) addressRow2.getComponentAt(0);
        TextField cityField = (TextField) addressRow2.getComponentAt(1);
        TextField countryField = (TextField) addressRow2.getComponentAt(2);

        TextField firstNameField = (TextField) nameRow.getComponentAt(0);
        TextField lastNameField = (TextField) nameRow.getComponentAt(1);

        // Password field
        PasswordField passwordField = createPasswordField("Password", "Secure password");

        // Password criteria display
        createPasswordCriteria();

        // Confirm password
        PasswordField passwordRepeatField = createPasswordField("Confirm Password", "Confirm password");

        // Setup Binder with validation
        setupBinder(usernameField, emailField, firstNameField, lastNameField,
                   streetField, houseNumberField, postalCodeField, cityField, countryField,
                   passwordField, passwordRepeatField);

        // Live password validation
        passwordField.addValueChangeListener(e -> updatePasswordCriteria(e.getValue()));

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

        Hr separator = new Hr();
        separator.addClassName("registration-separator");

        // Buttons
        HorizontalLayout buttonLayout = createButtonLayout();

        card.add(title, info, formLayout, separator, buttonLayout);
        right.add(card);
        
        return right;
    }

    private TextField createInputField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.addClassName("login-input");
        field.setRequiredIndicatorVisible(false);
        field.getStyle().set("--vaadin-input-field-background", "#f3f4f6");
        return field;
    }

    private PasswordField createPasswordField(String label, String placeholder) {
        PasswordField field = new PasswordField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.addClassName("login-input");
        field.setRequiredIndicatorVisible(false);
        field.getStyle().set("--vaadin-input-field-background", "#f3f4f6");
        return field;
    }

    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(false);
        buttonLayout.getStyle().set("gap", "0.5rem");

        Button registerBtn = new Button("Register");
        registerBtn.addClassName("gold-button");
        registerBtn.setWidth("50%");
        registerBtn.addClickListener(e -> handleRegistration());
        registerBtn.addClickShortcut(Key.ENTER);

        Button cancelBtn = new Button("Back to Login");
        cancelBtn.addClassName("back-login-button");
        cancelBtn.setWidth("50%");
        cancelBtn.addClickListener(e -> UI.getCurrent().navigate(LoginView.class));

        buttonLayout.add(registerBtn, cancelBtn);
        return buttonLayout;
    }

    private HorizontalLayout createNameRow() {
        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setWidthFull();
        nameRow.setSpacing(false);
        nameRow.setMargin(false);
        nameRow.setPadding(false);
        nameRow.getStyle().set("gap", "0.5rem");

        TextField firstNameField = createInputField("First Name", "Max");
        firstNameField.setWidth("50%");

        TextField lastNameField = createInputField("Last Name", "Smith");
        lastNameField.setWidth("50%");

        nameRow.add(firstNameField, lastNameField);
        return nameRow;
    }

    private HorizontalLayout createAddressRow1() {
        HorizontalLayout addressRow = new HorizontalLayout();
        addressRow.setWidthFull();
        addressRow.setSpacing(false);
        addressRow.setMargin(false);
        addressRow.setPadding(false);
        addressRow.getStyle().set("gap", "0.5rem");

        TextField streetField = createInputField("Street", "Main Street");
        streetField.setWidth("75%");

        TextField houseNumberField = createInputField("No.", "42");
        houseNumberField.setWidth("25%");

        addressRow.add(streetField, houseNumberField);
        return addressRow;
    }

    private HorizontalLayout createAddressRow2() {
        HorizontalLayout addressRow = new HorizontalLayout();
        addressRow.setWidthFull();
        addressRow.setSpacing(false);
        addressRow.setMargin(false);
        addressRow.setPadding(false);
        addressRow.getStyle().set("gap", "0.5rem");

        TextField postalCodeField = createInputField("Postal Code", "12345");
        postalCodeField.setWidth("24%");

        TextField cityField = createInputField("City", "Berlin");
        cityField.setWidth("34%");

        TextField countryField = createInputField("Country", "Germany");
        countryField.setWidth("38%");

        addressRow.add(postalCodeField, cityField, countryField);
        return addressRow;
    }

    private void setupBinder(TextField usernameField, TextField emailField,
                            TextField firstNameField, TextField lastNameField,
                            TextField streetField, TextField houseNumberField,
                            TextField postalCodeField, TextField cityField, TextField countryField,
                            PasswordField passwordField, PasswordField passwordRepeatField) {
        
        // Username binding
        binder.forField(usernameField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("Username is required");
                }
                if (value.length() < 3) {
                    return ValidationResult.error("Username must be at least 3 characters");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getUsername, RegistrationFormData::setUsername);

        // Email binding
        binder.forField(emailField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("Email is required");
                }
                // Regex für einfache Email-Validierung: test@example.com Format
                String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
                if (!value.matches(emailRegex)) {
                    return ValidationResult.error("Valid email required (e.g., test@example.com)");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getEmail, RegistrationFormData::setEmail);

        // First name binding
        binder.forField(firstNameField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("First name is required");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getFirstName, RegistrationFormData::setFirstName);

        // Last name binding
        binder.forField(lastNameField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("Last name is required");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getLastName, RegistrationFormData::setLastName);

        // Street binding
        binder.forField(streetField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("Street is required");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getStreet, RegistrationFormData::setStreet);

        // House number binding
        binder.forField(houseNumberField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("House number is required");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getHouseNumber, RegistrationFormData::setHouseNumber);

        // Postal code binding
        binder.forField(postalCodeField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("Postal code is required");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getPostalCode, RegistrationFormData::setPostalCode);

        // City binding
        binder.forField(cityField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("City is required");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getCity, RegistrationFormData::setCity);

        // Country binding
        binder.forField(countryField)
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.error("Country is required");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getCountry, RegistrationFormData::setCountry);

        // Password binding with custom validator
        binder.forField(passwordField)
            .withValidator((value, context) -> {
                if (value == null || value.isEmpty()) {
                    return ValidationResult.error("Password is required");
                }
                PasswordValidationResult validation = validatePasswordStrength(value);
                if (!validation.isValid()) {
                    return ValidationResult.error("Password does not meet all criteria");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getPassword, RegistrationFormData::setPassword);

        // Password repeat binding with cross-field validator
        binder.forField(passwordRepeatField)
            .withValidator((value, context) -> {
                if (value == null || value.isEmpty()) {
                    return ValidationResult.error("Password confirmation is required");
                }
                if (!value.equals(passwordField.getValue())) {
                    return ValidationResult.error("Passwords do not match");
                }
                return ValidationResult.ok();
            })
            .bind(RegistrationFormData::getPasswordRepeat, RegistrationFormData::setPasswordRepeat);

        // Bind to the DTO
        binder.setBean(formData);
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

    private void updatePasswordCriteria(String password) {
        PasswordValidationResult result = validatePasswordStrength(password);

        updateCriteriaElement(minLengthCriteria, result.isMinLength());
        updateCriteriaElement(uppercaseCriteria, result.isHasUppercase());
        updateCriteriaElement(lowercaseCriteria, result.isHasLowercase());
        updateCriteriaElement(digitCriteria, result.isHasDigit());
        updateCriteriaElement(specialCharCriteria, result.isHasSpecialChar());
    }

    private void updateCriteriaElement(Div criteriaElement, boolean fulfilled) {
        if (fulfilled) {
            criteriaElement.getStyle().set("color", "#10b981"); // Green
        } else {
            criteriaElement.getStyle().set("color", "#ef4444"); // Red
        }
    }

    private PasswordValidationResult validatePasswordStrength(String password) {
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

    private void handleRegistration() {
        if (binder.writeBeanIfValid(formData)) {
            registerUser();
        } else {
            Notification.show("Please check your input", 3000, Notification.Position.MIDDLE);
        }
    }

    private void registerUser() {
        try {
            log.info("Registration attempt for username: {}, email: {}", formData.getUsername(), formData.getEmail());

            User newUser = new User(
                    formData.getUsername(),
                    formData.getFirstName(),
                    formData.getLastName(),
                    new AdressEmbeddable(
                            formData.getStreet(),
                            formData.getHouseNumber(),
                            formData.getPostalCode(),
                            formData.getCity(),
                            formData.getCountry()
                    ),
                    formData.getEmail(),
                    formData.getPassword(),
                    UserRole.GUEST,
                    true
            );

            User registeredUser = userService.registerUser(newUser);
            log.info("User registered successfully: {}", registeredUser.getUsername());
            
            showRegistrationSuccessDialog();
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed with validation error: {}", e.getMessage());
            Notification.show(e.getMessage(), 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            log.error("Registration failed with exception", e);
            Notification.show("Registration failed: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void showRegistrationSuccessDialog() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);

        Icon successIcon = VaadinIcon.CHECK_CIRCLE_O.create();
        successIcon.setSize("48px");
        successIcon.setColor("green");

        Paragraph title = new Paragraph("Registration successful!");
        title.getStyle().set("font-size", "1.2rem").set("font-weight", "600");

        Paragraph message = new Paragraph(
                "Your account has been created successfully. " +
                "You can now log in with your credentials."
        );

        layout.add(successIcon, title, message);

        Button closeBtn = new Button("Back to Login", e -> {
            dialog.close();
            UI.getCurrent().navigate(LoginView.class);
        });
        closeBtn.addClassName("gold-button");

        layout.add(closeBtn);
        dialog.add(layout);
        dialog.open();
    }

    private Div createThemeToggle() {
        Icon icon = VaadinIcon.ADJUST.create();
        icon.setSize("20px");
        Div themeToggle = new Div(icon);
        themeToggle.addClassName("theme-toggle");
        themeToggle.addClickListener(e -> toggleTheme());
        return themeToggle;
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        UI ui = UI.getCurrent();
        if (darkMode) {
            ui.getElement().getClassList().add("dark-theme");
            ui.getPage().executeJs("document.documentElement.classList.add('dark-theme')");
        } else {
            ui.getElement().getClassList().remove("dark-theme");
            ui.getPage().executeJs("document.documentElement.classList.remove('dark-theme')");
        }
        VaadinSession.getCurrent().setAttribute("darkMode", darkMode);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionService != null && sessionService.getCurrentUser() != null) {
            event.forwardTo(DashboardView.class);
        }
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
        private final boolean valid;
        private final boolean minLength;
        private final boolean hasUppercase;
        private final boolean hasLowercase;
        private final boolean hasDigit;
        private final boolean hasSpecialChar;

        public PasswordValidationResult(boolean valid, boolean minLength, boolean hasUppercase, 
                                       boolean hasLowercase, boolean hasDigit, boolean hasSpecialChar) {
            this.valid = valid;
            this.minLength = minLength;
            this.hasUppercase = hasUppercase;
            this.hasLowercase = hasLowercase;
            this.hasDigit = hasDigit;
            this.hasSpecialChar = hasSpecialChar;
        }

        public boolean isValid() { return valid; }
        public boolean isMinLength() { return minLength; }
        public boolean isHasUppercase() { return hasUppercase; }
        public boolean isHasLowercase() { return hasLowercase; }
        public boolean isHasDigit() { return hasDigit; }
        public boolean isHasSpecialChar() { return hasSpecialChar; }
    }
}
