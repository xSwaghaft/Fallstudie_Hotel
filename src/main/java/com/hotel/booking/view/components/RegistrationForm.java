package com.hotel.booking.view.components;

import com.hotel.booking.entity.User;
import com.hotel.booking.service.UserService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import java.util.Arrays;

/**
 * Registration form for new users.
 * <p>
 * Extends {@link AddUserForm} and adds registration-specific validation and UI elements,
 * including password confirmation and live password strength criteria feedback.
 * </p>
 *
 * @author Artur Derr
 */
public class RegistrationForm extends AddUserForm {

	// Constants
	private static final String REGISTRATION_FORM_CLASS = "registration-form";
	private static final String GOLD_BUTTON_CLASS = "gold-button";
	private static final String BACK_LOGIN_BUTTON_CLASS = "back-login-button";
	private static final String REGISTRATION_ROW_CLASS = "registration-form-row";
	private static final String PASSWORD_CRITERIA_CLASS = "password-criteria";
	private static final String PASSWORD_CRITERIA_ITEM_CLASS = "password-criteria-item";
	private static final String IS_FULFILLED_CLASS = "is-fulfilled";
	private static final String DATE_PICKER_ALIGNMENT_CLASS = "date-picker-alignment";
	private static final int PASSWORD_MIN_LENGTH = 8;

	// Registration-specific fields
	private final PasswordField passwordRepeatField = new PasswordField("Confirm Password");
	private final Button registerBtn = new Button("Register");
	private final Button cancelBtn = new Button("Back to Login");

	// Password Criteria UI
	private Div passwordCriteriaContainer;
	private Div minLengthCriteria;
	private Div uppercaseCriteria;
	private Div lowercaseCriteria;
	private Div digitCriteria;
	private Div specialCharCriteria;

	private Runnable onRegisterClick;
	private Runnable onCancelClick;

	/**
	 * Creates a registration form instance.
	 *
	 * @param userService service used by the underlying form to persist and validate users
	 */
	public RegistrationForm(UserService userService) {
		super(null, userService);
		addClassName(REGISTRATION_FORM_CLASS);
		
		// Additional configuration for registration
		roleSelect.setVisible(false);
		activeCheckbox.setVisible(false);

		createPasswordCriteria();
		initializeRegistrationStylingAndBehavior();

		// Remove the layout from the parent form and rebuild it
		removeAll();

		HorizontalLayout nameRow = createHorizontalRow(firstNameField, lastNameField, birthdateField);
		HorizontalLayout addressRow1 = createHorizontalRow(streetField, houseNumberField, postalCodeField);
		HorizontalLayout addressRow2 = createHorizontalRow(cityField, countryField);
		HorizontalLayout buttonLayout = createRegistrationButtons();

		this.add(usernameField, emailField, nameRow, addressRow1, addressRow2,
			passwordField, passwordCriteriaContainer, passwordRepeatField, buttonLayout);

		// Override password validation
		getBinder().removeBinding(passwordField);
		getBinder().forField(passwordField).asRequired("Password is required")
			.withValidator(this::validatePasswordStrength).bind(User::getPassword, User::setPassword);

		// Password confirmation
		getBinder().forField(passwordRepeatField).asRequired("Password confirmation is required")
			.withValidator(this::validatePasswordMatch).bind(u -> "", (u, v) -> {});

		// Adjust required indicator visibility
		configureRequiredIndicators();
	}

	/**
	 * Applies registration-specific placeholders, sizing, and listeners.
	 */
	private void initializeRegistrationStylingAndBehavior() {
		usernameField.setPlaceholder("Choose a unique username");
		emailField.setPlaceholder("your@email.com");

		firstNameField.setPlaceholder("Max");
		lastNameField.setPlaceholder("Smith");
		Arrays.asList(firstNameField, lastNameField).forEach(f -> f.setWidth("33.33%"));
		birthdateField.setWidth("33.33%");
		birthdateField.addClassName(DATE_PICKER_ALIGNMENT_CLASS);

		streetField.setWidth("50%");
		houseNumberField.setPlaceholder("42");
		houseNumberField.setWidth("25%");
		postalCodeField.setPlaceholder("12345");
		postalCodeField.setWidth("25%");
		cityField.setPlaceholder("Berlin");
		cityField.setWidth("50%");
		countryField.setPlaceholder("Germany");
		countryField.setWidth("50%");

		passwordRepeatField.setPlaceholder("Confirm password");
		passwordField.addValueChangeListener(e -> updatePasswordCriteria(e.getValue()));
	}

	/**
	 * Creates and configures the action buttons for registration.
	 *
	 * @return layout containing the Register and Back to Login buttons
	 */
	private HorizontalLayout createRegistrationButtons() {
		registerBtn.addClassName(GOLD_BUTTON_CLASS);
		registerBtn.setWidth("50%");
		registerBtn.addClickListener(e -> handleRegistration());
		registerBtn.addClickShortcut(Key.ENTER);

		cancelBtn.addClassName(BACK_LOGIN_BUTTON_CLASS);
		cancelBtn.setWidth("50%");
		cancelBtn.addClickListener(e -> { if (onCancelClick != null) onCancelClick.run(); });

		HorizontalLayout buttonLayout = new HorizontalLayout(registerBtn, cancelBtn);
		buttonLayout.setWidthFull();
		buttonLayout.setSpacing(false);
		buttonLayout.addClassName(REGISTRATION_ROW_CLASS);
		return buttonLayout;
	}

	/**
	 * Initializes the password criteria UI container and its criteria items.
	 */
	private void createPasswordCriteria() {
		passwordCriteriaContainer = new Div();
		passwordCriteriaContainer.addClassName(PASSWORD_CRITERIA_CLASS);

		minLengthCriteria = createCriteriaElement("at least 8 characters");
		uppercaseCriteria = createCriteriaElement("at least 1 uppercase letter");
		lowercaseCriteria = createCriteriaElement("at least 1 lowercase letter");
		digitCriteria = createCriteriaElement("at least 1 number");
		specialCharCriteria = createCriteriaElement("at least 1 special character");

		passwordCriteriaContainer.add(minLengthCriteria, uppercaseCriteria, lowercaseCriteria,
			digitCriteria, specialCharCriteria);
	}

	/**
	 * Creates a single criteria row element.
	 *
	 * @param text label text for the criteria
	 * @return a criteria element containing the provided text
	 */
	private Div createCriteriaElement(String text) {
		Div criteriaElement = new Div();
		criteriaElement.addClassName(PASSWORD_CRITERIA_ITEM_CLASS);
		criteriaElement.add(new Span(text));
		return criteriaElement;
	}

	/**
	 * Updates the criteria UI based on the current password value.
	 *
	 * @param password the current password input
	 */
	private void updatePasswordCriteria(String password) {
		PasswordValidationResult result = validatePasswordStrengthLogic(password);
		updateCriteriaElement(minLengthCriteria, result.isMinLength());
		updateCriteriaElement(uppercaseCriteria, result.isHasUppercase());
		updateCriteriaElement(lowercaseCriteria, result.isHasLowercase());
		updateCriteriaElement(digitCriteria, result.isHasDigit());
		updateCriteriaElement(specialCharCriteria, result.isHasSpecialChar());
	}

	/**
	 * Marks/unmarks a criteria element as fulfilled.
	 *
	 * @param criteriaElement the criteria element to update
	 * @param fulfilled whether the criterion is fulfilled
	 */
	private void updateCriteriaElement(Div criteriaElement, boolean fulfilled) {
		if (criteriaElement == null) return;
		if (fulfilled) criteriaElement.addClassName(IS_FULFILLED_CLASS);
		else criteriaElement.removeClassName(IS_FULFILLED_CLASS);
	}

	/**
	 * Creates a horizontal row for the given components.
	 *
	 * @param components components to place in the row
	 * @return a horizontal layout configured for the registration form
	 */
	private HorizontalLayout createHorizontalRow(com.vaadin.flow.component.Component... components) {
		HorizontalLayout row = new HorizontalLayout(components);
		row.setWidthFull();
		row.setSpacing(false);
		row.addClassName(REGISTRATION_ROW_CLASS);
		return row;
	}

	/**
	 * Hides the required indicator markers for all fields in this form.
	 */
	private void configureRequiredIndicators() {
		Arrays.asList(usernameField, firstNameField, lastNameField,
			streetField, houseNumberField, postalCodeField, cityField, countryField)
			.forEach(field -> field.setRequiredIndicatorVisible(false));
		emailField.setRequiredIndicatorVisible(false);
		birthdateField.setRequiredIndicatorVisible(false);
		passwordField.setRequiredIndicatorVisible(false);
		passwordRepeatField.setRequiredIndicatorVisible(false);
	}

	/**
	 * Validates that the password meets all strength criteria.
	 *
	 * @param value password value
	 * @param context binder context
	 * @return validation result
	 */
	private ValidationResult validatePasswordStrength(String value, ValueContext context) {
		if (value == null || value.isBlank()) return ValidationResult.error("Password is required");
		return validatePasswordStrengthLogic(value).isValid() ? ValidationResult.ok() : 
			ValidationResult.error("Password does not meet all criteria");
	}

	/**
	 * Validates that the password confirmation matches the original password.
	 *
	 * @param value confirmation value
	 * @param context binder context
	 * @return validation result
	 */
	private ValidationResult validatePasswordMatch(String value, ValueContext context) {
		String password = passwordField.getValue();
		if (value == null || value.isBlank()) return ValidationResult.error("Password confirmation is required");
		return (password != null && password.equals(value)) ? ValidationResult.ok() :
			ValidationResult.error("Passwords do not match");
	}

	/**
	 * Performs the password strength checks and returns a structured result.
	 *
	 * @param password password value (may be {@code null})
	 * @return password validation result with individual criteria flags
	 */
	private PasswordValidationResult validatePasswordStrengthLogic(String password) {
		if (password == null) {
			return new PasswordValidationResult(false, false, false, false, false, false);
		}

		boolean hasMinLength = password.length() >= PASSWORD_MIN_LENGTH;
		boolean hasUppercase = password.matches(".*[A-Z].*");
		boolean hasLowercase = password.matches(".*[a-z].*");
		boolean hasDigit = password.matches(".*\\d.*");
		boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

		boolean isValid = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
		return new PasswordValidationResult(isValid, hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecialChar);
	}

	/**
	 * Runs binder validation and triggers the registration callback on success.
	 */
	private void handleRegistration() {
		if (binder.writeBeanIfValid(formUser) && onRegisterClick != null) {
			onRegisterClick.run();
		}
	}

	/**
	 * Sets the callback invoked after a successful registration.
	 *
	 * @param callback callback to run on successful registration
	 */
	public void setOnRegisterClick(Runnable callback) {
		this.onRegisterClick = callback;
	}

	/**
	 * Sets the callback invoked when the user navigates back to the login view.
	 *
	 * @param callback callback to run when cancelling registration
	 */
	public void setOnCancelClick(Runnable callback) {
		this.onCancelClick = callback;
	}

	private static class PasswordValidationResult {
		private final boolean isValid, minLength, hasUppercase, hasLowercase, hasDigit, hasSpecialChar;

		/**
		 * Creates a password validation result.
		 *
		 * @param isValid overall validity (all criteria met)
		 * @param minLength whether the minimum length criterion is met
		 * @param hasUppercase whether an uppercase letter is present
		 * @param hasLowercase whether a lowercase letter is present
		 * @param hasDigit whether a digit is present
		 * @param hasSpecialChar whether a special character is present
		 */
		public PasswordValidationResult(boolean isValid, boolean minLength, boolean hasUppercase,
									   boolean hasLowercase, boolean hasDigit, boolean hasSpecialChar) {
			this.isValid = isValid;
			this.minLength = minLength;
			this.hasUppercase = hasUppercase;
			this.hasLowercase = hasLowercase;
			this.hasDigit = hasDigit;
			this.hasSpecialChar = hasSpecialChar;
		}

		/**
		 * @return {@code true} if all criteria are met
		 */
		public boolean isValid() { return isValid; }

		/**
		 * @return {@code true} if the minimum length criterion is met
		 */
		public boolean isMinLength() { return minLength; }

		/**
		 * @return {@code true} if an uppercase letter is present
		 */
		public boolean isHasUppercase() { return hasUppercase; }

		/**
		 * @return {@code true} if a lowercase letter is present
		 */
		public boolean isHasLowercase() { return hasLowercase; }

		/**
		 * @return {@code true} if a digit is present
		 */
		public boolean isHasDigit() { return hasDigit; }

		/**
		 * @return {@code true} if a special character is present
		 */
		public boolean isHasSpecialChar() { return hasSpecialChar; }
	}
}
