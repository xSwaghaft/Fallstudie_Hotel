package com.hotel.booking.view.components;

import com.hotel.booking.entity.AdressEmbeddable;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.hotel.booking.service.UserService;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.StringLengthValidator;

//Matthias Lohr
//Formular zum Anlegen und Bearbeiten von Usern mit binding an User Entity (FDO)
//Anordnung und Breite der Felder kann noch optimiert werden
public class AddUserForm extends FormLayout {

	private final Binder<User> binder = new Binder<>(User.class);
	private User formUser;
	private final UserService userService;
	private final boolean registrationMode;

    // fields
    private final TextField usernameField = new TextField("Username");
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");

    private final TextField streetField = new TextField("Street");
    private final TextField houseNumberField = new TextField("House Number");
    private final TextField cityField = new TextField("City");
    private final TextField postalCodeField = new TextField("Postal Code");
    private final TextField countryField = new TextField("Country");

	private final PasswordField passwordField = new PasswordField("Password");
	private final PasswordField passwordRepeatField = new PasswordField("Confirm Password");
	private final EmailField emailField = new EmailField("Email");
	private final Select<UserRole> roleSelect = new Select<>();
	private final Checkbox activeCheckbox = new Checkbox("Active");

	// Registration-only UI
	private Div passwordCriteriaContainer;
	private Div minLengthCriteria;
	private Div uppercaseCriteria;
	private Div lowercaseCriteria;
	private Div digitCriteria;
	private Div specialCharCriteria;
	private final Button registerBtn = new Button("Register");
	private final Button cancelBtn = new Button("Back to Login");
	private Runnable onRegisterClick;
	private Runnable onCancelClick;

	public static AddUserForm forRegistration(UserService userService) {
		return new AddUserForm(null, userService, true);
	}

	public AddUserForm(User existingUser, UserService userService) {
		this(existingUser, userService, false);
	}

	private AddUserForm(User existingUser, UserService userService, boolean registrationMode) {

		this.userService = userService;
		this.registrationMode = registrationMode;
		if (registrationMode) {
			addClassName("registration-form");
		}

		// Setup role select
		roleSelect.setLabel("Role");
		roleSelect.setItems(UserRole.values());
		roleSelect.setWidthFull();

		if (registrationMode) {
			initializeRegistrationStylingAndBehavior();
			createPasswordCriteria();

			// Name row (First + Last)
			HorizontalLayout nameRow = new HorizontalLayout();
			nameRow.setWidthFull();
			nameRow.setSpacing(false);
			nameRow.addClassName("registration-form-row");
			nameRow.add(firstNameField, lastNameField);

			// Address row 1 (Street + House No.)
			HorizontalLayout addressRow1 = new HorizontalLayout();
			addressRow1.setWidthFull();
			addressRow1.setSpacing(false);
			addressRow1.addClassName("registration-form-row");
			addressRow1.add(streetField, houseNumberField);

			// Address row 2 (Postal + City + Country)
			HorizontalLayout addressRow2 = new HorizontalLayout();
			addressRow2.setWidthFull();
			addressRow2.setSpacing(false);
			addressRow2.addClassName("registration-form-row");
			addressRow2.add(postalCodeField, cityField, countryField);

			HorizontalLayout buttonLayout = createRegistrationButtons();
			this.add(
				usernameField,
				emailField,
				nameRow,
				addressRow1,
				addressRow2,
				passwordField,
				passwordCriteriaContainer,
				passwordRepeatField,
				buttonLayout
			);
			setColspan(nameRow, 2);
			setColspan(addressRow1, 2);
			setColspan(addressRow2, 2);
			setColspan(buttonLayout, 2);
		} else {
			this.add(usernameField, firstNameField, lastNameField,
				streetField, houseNumberField, cityField, postalCodeField, countryField,
				passwordField, emailField, roleSelect, activeCheckbox);
		}

		//FormLayout spalten auf basis der Bildschirmbreite hinzufügen
		this.setResponsiveSteps(
		    new ResponsiveStep("0", 1), // Single column layout for narrow screens (handy)
		    new ResponsiveStep("500px", 2) // Two columns layout for wider screens (PC, Tablet)
		);

		// Set column span for fields
		setColspan(streetField, 1); // Street field spans one column
		setColspan(houseNumberField, 1); // House number field spans one column
		setColspan(cityField, 1);
		setColspan(postalCodeField,1);
		setColspan(usernameField, 2);
		setColspan(emailField, 2);
		setColspan(passwordField, 2);
		if (registrationMode) {
			setColspan(passwordRepeatField, 2);
			setColspan(passwordCriteriaContainer, 2);
		}

		// Binder wird konfiguriert
		binder.forField(firstNameField)
			.asRequired("First name is required")
			.bind(User::getFirstName, User::setFirstName);

		binder.forField(lastNameField)
			.asRequired("Last name is required")
			.bind(User::getLastName, User::setLastName);

		binder.forField(emailField)
			.asRequired("Email is required")
			.withValidator(this::isValidEmailFormat, "Please enter a valid email address")
			.withValidator(
				email -> isEmailAvailable(email, formUser),
				"Email already in use")
			.bind(User::getEmail, User::setEmail);

		if (!registrationMode) {
			binder.forField(roleSelect)
				.asRequired("Role is required")
				.bind(User::getRole, User::setRole);

			binder.forField(activeCheckbox)
				.bind(User::isActive, (user, active) -> user.setActive(active));
		}

		// Username uniqueness validator (uses userService). On edit allow same username.
		binder.forField(usernameField)
			.asRequired("Username is required")
			.withValidator(new StringLengthValidator("Username must be at least 3 characters", 3, null))
			.withValidator(
				username -> isUsernameAvailable(username, formUser),
			"Username already exists")
			.bind(User::getUsername, User::setUsername);
		// Address bindings via lambdas
		binder.forField(streetField)
			.asRequired("Street is required")
			.bind(u -> u.getAddress().getStreet(),
					(u, v) -> u.getAddress().setStreet(v));

		binder.forField(houseNumberField)
			.asRequired("House Number is required")
			.bind(u -> u.getAddress().getHouseNumber(),
				  (u, v) -> u.getAddress().setHouseNumber(v));

		binder.forField(postalCodeField)
			.asRequired("Postal Code is required")
			.bind(u -> u.getAddress().getPostalCode(),
				  (u, v) -> u.getAddress().setPostalCode(v));
				  
		binder.forField(cityField)
			.asRequired("City is required")
			.bind(u -> u.getAddress().getCity(),
				  (u, v) -> u.getAddress().setCity(v));

		binder.forField(countryField)
			.asRequired("Country is required")
			.bind(u -> u.getAddress().getCountry(),
				  (u, v) -> u.getAddress().setCountry(v));

		// Initialize form user and bind values
		setUser(existingUser);
	}

	private void initializeRegistrationStylingAndBehavior() {
		usernameField.setPlaceholder("Choose a unique username");
		usernameField.setWidthFull();
		emailField.setPlaceholder("your@email.com");
		emailField.setWidthFull();
		firstNameField.setPlaceholder("Max");
		firstNameField.setWidth("50%");
		lastNameField.setPlaceholder("Smith");
		lastNameField.setWidth("50%");
		streetField.setPlaceholder("Main Street");
		streetField.setWidth("75%");
		houseNumberField.setPlaceholder("42");
		houseNumberField.setWidth("25%");
		postalCodeField.setPlaceholder("12345");
		postalCodeField.setWidth("24%");
		cityField.setPlaceholder("Berlin");
		cityField.setWidth("34%");
		countryField.setPlaceholder("Germany");
		countryField.setWidth("38%");
		passwordField.setPlaceholder("Secure password");
		passwordField.setWidthFull();
		passwordRepeatField.setPlaceholder("Confirm password");
		passwordRepeatField.setWidthFull();

		passwordField.addValueChangeListener(e -> updatePasswordCriteria(e.getValue()));
	}

	private HorizontalLayout createRegistrationButtons() {
		registerBtn.addClassName("gold-button");
		registerBtn.setWidth("50%");
		registerBtn.addClickListener(e -> handleRegistration());
		registerBtn.addClickShortcut(Key.ENTER);

		cancelBtn.addClassName("back-login-button");
		cancelBtn.setWidth("50%");
		cancelBtn.addClickListener(e -> {
			if (onCancelClick != null) onCancelClick.run();
		});

		HorizontalLayout buttonLayout = new HorizontalLayout(registerBtn, cancelBtn);
		buttonLayout.setWidthFull();
		buttonLayout.setSpacing(false);
		buttonLayout.addClassName("registration-form-row");
		buttonLayout.addClassName("registration-form-buttons");
		return buttonLayout;
	}

	private void createPasswordCriteria() {
		passwordCriteriaContainer = new Div();
		passwordCriteriaContainer.addClassName("password-criteria");

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
		criteriaElement.addClassName("password-criteria-item");
		criteriaElement.add(new Span(text));
		return criteriaElement;
	}

	private void updatePasswordCriteria(String password) {
		if (!registrationMode) {
			return;
		}
		PasswordValidationResult result = validatePasswordStrengthLogic(password);
		updateCriteriaElement(minLengthCriteria, result.isMinLength());
		updateCriteriaElement(uppercaseCriteria, result.isHasUppercase());
		updateCriteriaElement(lowercaseCriteria, result.isHasLowercase());
		updateCriteriaElement(digitCriteria, result.isHasDigit());
		updateCriteriaElement(specialCharCriteria, result.isHasSpecialChar());
	}

	private void updateCriteriaElement(Div criteriaElement, boolean fulfilled) {
		if (criteriaElement == null) {
			return;
		}
		if (fulfilled) {
			criteriaElement.addClassName("is-fulfilled");
		} else {
			criteriaElement.removeClassName("is-fulfilled");
		}
	}

	public void setUser(User existingUser) {
		boolean isNew = existingUser == null;
		if (isNew) {
			formUser = new User("", "", "", new AdressEmbeddable(), "", "", UserRole.GUEST, true);
		} else {
			formUser = existingUser;
			if (formUser.getAddress() == null) {
				formUser.setAddress(new AdressEmbeddable());
			}
		}

		// Configure password field visibility based on edit mode
		if (existingUser != null) {
			passwordField.setVisible(false); // Hide password field when editing
		} else {
			passwordField.setVisible(true); // Show password field when creating a new user
		}

		// Configure password validation depending on new/edit
		binder.removeBinding(passwordField);
		if (isNew) {
			if (registrationMode) {
				binder.forField(passwordField)
					.asRequired("Password is required")
					.withValidator(this::validatePasswordStrength)
					.bind(User::getPassword, User::setPassword);
			} else {
				binder.forField(passwordField)
					.asRequired("Password is required")
					.withValidator(pw -> pw != null && pw.length() >= 6, "Password must be at least 6 characters")
					.bind(User::getPassword, User::setPassword);
			}
		} else {
			binder.forField(passwordField)
				.withValidator(pw -> pw == null || pw.isEmpty() || pw.length() >= 6, "Password must be at least 6 characters")
				.bind(u -> "", (u, v) -> { if (v != null && !v.isEmpty()) u.setPassword(v); });
		}

		if (registrationMode) {
			formUser.setRole(UserRole.GUEST);
			formUser.setActive(true);

			// Confirm password binding (registration only)
			binder.removeBinding(passwordRepeatField);
			binder.forField(passwordRepeatField)
				.asRequired("Password confirmation is required")
				.withValidator(this::validatePasswordMatch)
				.bind(u -> "", (u, v) -> {});
			updatePasswordCriteria(passwordField.getValue());
		}

		// Make required indicators visible for some fields
		if (!registrationMode) {
			usernameField.setRequiredIndicatorVisible(true);
			firstNameField.setRequiredIndicatorVisible(true);
			lastNameField.setRequiredIndicatorVisible(true);
			roleSelect.setRequiredIndicatorVisible(true);
		} else {
			// Hide the required indicator dots in the registration UI
			usernameField.setRequiredIndicatorVisible(false);
			emailField.setRequiredIndicatorVisible(false);
			firstNameField.setRequiredIndicatorVisible(false);
			lastNameField.setRequiredIndicatorVisible(false);
			streetField.setRequiredIndicatorVisible(false);
			houseNumberField.setRequiredIndicatorVisible(false);
			postalCodeField.setRequiredIndicatorVisible(false);
			cityField.setRequiredIndicatorVisible(false);
			countryField.setRequiredIndicatorVisible(false);
			passwordField.setRequiredIndicatorVisible(false);
			passwordRepeatField.setRequiredIndicatorVisible(false);
		}

		binder.readBean(formUser);
	}

	private boolean isValidEmailFormat(String email) {
		if (email == null || email.isBlank()) {
			return true;
		}
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		return email.matches(emailRegex);
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
		if (password == null || !password.equals(value)) {
			return ValidationResult.error("Passwords do not match");
		}
		return ValidationResult.ok();
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

	private void handleRegistration() {
		if (!registrationMode) {
			return;
		}
		if (binder.writeBeanIfValid(formUser)) {
			if (onRegisterClick != null) {
				onRegisterClick.run();
			}
		}
	}

	public void setOnRegisterClick(Runnable callback) {
		this.onRegisterClick = callback;
	}

	public void setOnCancelClick(Runnable callback) {
		this.onCancelClick = callback;
	}

	private boolean isEmailAvailable(String email, User formUser) {
		if (email == null || email.isEmpty()) {
			return true; //Wenn es leer ist soll asRequired die Fehlermeldung werfen
		}
		// Wenn Bearbeitung: gleiche E-Mail ist erlaubt
		if (formUser != null && email.equals(formUser.getEmail())) {
			return true;
		}
		// Sonst prüfen, ob die E-Mail frei ista
		return !userService.existsByEmail(email);
	}

	private Boolean isUsernameAvailable(String username, User formUser) {
		if (username == null || username.isEmpty()) {
			return true; //Wenn es leer ist soll asRequired die Fehlermeldung werfen
		}
		// Wenn Bearbeitung: gleicher Username ist erlaubt
		if (formUser != null && username.equals(formUser.getUsername())) {
			return true;
		}
		// Sonst prüfen, ob der Username frei ist
		return !userService.existsByUsername(username);
	}

	public User getUser() {
		return formUser;
	}

	public void writeBean() throws ValidationException {
		binder.writeBean(formUser);
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
