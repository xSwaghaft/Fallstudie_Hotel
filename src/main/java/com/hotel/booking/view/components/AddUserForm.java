package com.hotel.booking.view.components;

import com.hotel.booking.entity.AdressEmbeddable;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.hotel.booking.service.UserService;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.validator.StringLengthValidator;

/**
 * Form for creating and editing users with data binding to the User entity.
 * <p>
 * This form provides a comprehensive interface for user management, allowing the creation
 * of new user accounts and editing of existing ones. It includes fields for personal information,
 * address details, login credentials, and role assignment.
 * </p>
 * <p>
 * The form uses responsive layout that adapts to different screen sizes:
 * <ul>
 *   <li>Single column on narrow screens (mobile devices)</li>
 *   <li>Two columns on wider screens (tablets and desktops)</li>
 * </ul>
 * </p>
 * <p>
 * Note: The field layout and width can be further optimized as needed.
 * </p>
 *
 * @author Matthias Lohr
 */
public class AddUserForm extends FormLayout {

	protected final Binder<User> binder = new Binder<>(User.class);
	protected User formUser;
	protected final UserService userService;

	// Input fields
	protected final TextField usernameField = new TextField("Username");
	protected final TextField firstNameField = new TextField("First Name");
	protected final TextField lastNameField = new TextField("Last Name");

	// Address fields
	protected final TextField streetField = new TextField("Street");
	protected final TextField houseNumberField = new TextField("House Number");
	protected final TextField cityField = new TextField("City");
	protected final TextField postalCodeField = new TextField("Postal Code");
	protected final TextField countryField = new TextField("Country");

	// Authentication and profile fields
	protected final PasswordField passwordField = new PasswordField("Password");
	protected final TextField emailField = new TextField("Email");
	protected final DatePicker birthdateField = new DatePicker("Date of Birth");
	protected final Select<UserRole> roleSelect = new Select<>();
	protected final Checkbox activeCheckbox = new Checkbox("Active");


	/**
	 * Constructs an AddUserForm with the given user and user service.
	 *
	 * @param existingUser the user to edit, or null to create a new user
	 * @param userService the service for user-related operations
	 */
	public AddUserForm(User existingUser, UserService userService) {

		this.userService = userService;

		// Configure role select component
		roleSelect.setLabel("Role");
		roleSelect.setItems(UserRole.values());
		roleSelect.setWidthFull();

		// Add all form fields to layout
		this.add(usernameField, firstNameField, lastNameField, birthdateField,
			streetField, houseNumberField, cityField, postalCodeField, countryField,
			passwordField, emailField, roleSelect, activeCheckbox);

		// Add responsive layout based on screen width
		this.setResponsiveSteps(
		    new ResponsiveStep("0", 1), // Single column layout for narrow screens (mobile)
		    new ResponsiveStep("500px", 2) // Two column layout for wider screens (tablet, desktop)
		);

		// Configure column span for form fields
		setColspan(streetField, 1);
		setColspan(houseNumberField, 1);
		setColspan(cityField, 1);
		setColspan(postalCodeField,1);
		setColspan(usernameField, 2);
		setColspan(emailField, 2);
		setColspan(passwordField, 2);

		// Configure data binding with validation rules
		binder.forField(firstNameField)
			.asRequired("First name is required")
			.bind(User::getFirstName, User::setFirstName);

		binder.forField(lastNameField)
			.asRequired("Last name is required")
			.bind(User::getLastName, User::setLastName);

		binder.forField(emailField)
			.asRequired("Email is required")
			.withValidator((email, context) -> {
				if (email == null || email.isBlank()) {
					return ValidationResult.ok();
				}
				if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
					return ValidationResult.ok();
				}
				return ValidationResult.error("Email must be in format: username@domain.com");
			})
			.withValidator(email -> isEmailAvailable(email, formUser), "Email already in use")
			.bind(User::getEmail, User::setEmail);

		binder.forField(roleSelect)
			.asRequired("Role is required")
			.bind(User::getRole, User::setRole);

		// Username uniqueness validator (uses userService). On edit allow same username.
		binder.forField(usernameField)
			.asRequired("Username is required")
			.withValidator(new StringLengthValidator("Username must be at least 3 characters", 3, null))
			.withValidator(
				username -> isUsernameAvailable(username, formUser),
			"Username already exists")
			.bind(User::getUsername, User::setUsername);

        binder.forField(activeCheckbox)
			.bind(User::isActive, (user, active) -> user.setActive(active));
		
		// Address field bindings using lambdas
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
		binder.forField(birthdateField)
			.bind(User::getBirthdate, User::setBirthdate);
		
		// Initialize form with user data
		setUser(existingUser);
	}

	/**
	 * Sets the user to be edited or creates a new user form for creation.
	 * <p>
	 * When editing an existing user, the password field is hidden. When creating a new user,
	 * the password field is shown and required. The form is populated with the user's current data.
	 * </p>
	 *
	 * @param existingUser the user to edit, or null to create a new user
	 */
	public void setUser(User existingUser) {
		// Configure password validation depending on new/edit mode
		binder.removeBinding(passwordField);
		boolean isNew = existingUser == null;
		if (isNew) {
			formUser = new User("", "", "", new AdressEmbeddable(), "", "", UserRole.GUEST, true);
			passwordField.setVisible(true); // Show password field when creating a new user
			binder.forField(passwordField)
				.asRequired("Password is required")
				.withValidator(pw -> pw != null && pw.length() >= 6, "Password must be at least 6 characters")
				.bind(User::getPassword, User::setPassword);
		} else {
			formUser = existingUser;
			passwordField.setVisible(false); // Hide password field when editing
			binder.forField(passwordField)
				.withValidator(pw -> pw == null || pw.isEmpty() || pw.length() >= 6, "Password must be at least 6 characters")
				.bind(u -> "", (u, v) -> { if (v != null && !v.isEmpty()) u.setPassword(v); }); // Only set password if not empty - should not be empty
		}

		// Make required indicators visible for required fields
		usernameField.setRequiredIndicatorVisible(true);
		firstNameField.setRequiredIndicatorVisible(true);
		lastNameField.setRequiredIndicatorVisible(true);
		emailField.setRequiredIndicatorVisible(true);

		roleSelect.setRequiredIndicatorVisible(true);

		binder.readBean(formUser);
	}

	/**
	 * Checks if the given email is available for use.
	 * <p>
	 * Returns true if the email is empty (validation is handled by asRequired),
	 * if it belongs to the user being edited, or if it is not already in use by another user.
	 * </p>
	 *
	 * @param email the email to check
	 * @param formUser the user being edited, or null if creating a new user
	 * @return true if the email is available, false otherwise
	 */
	private boolean isEmailAvailable(String email, User formUser) {
		if (email == null || email.isEmpty()) {
			return true; // If empty, asRequired will throw the error message
		}
		// If editing: same email is allowed
		if (formUser != null && email.equals(formUser.getEmail())) {
			return true;
		}
		// Otherwise check if the email is available
		return !userService.existsByEmail(email);
	}

	/**
	 * Checks if the given username is available for use.
	 * <p>
	 * Returns true if the username is empty (validation is handled by asRequired),
	 * if it belongs to the user being edited, or if it is not already in use by another user.
	 * </p>
	 *
	 * @param username the username to check
	 * @param formUser the user being edited, or null if creating a new user
	 * @return true if the username is available, false otherwise
	 */
	private Boolean isUsernameAvailable(String username, User formUser) {
		if (username == null || username.isEmpty()) {
			return true; // If empty, asRequired will throw the error message
		}
		// If editing: same username is allowed
		if (formUser != null && username.equals(formUser.getUsername())) {
			return true;
		}
		// Otherwise check if the username is available
		return !userService.existsByUsername(username);
	}

	/**
	 * Returns the user being edited or created.
	 *
	 * @return the form user
	 */
	public User getUser() {
		return formUser;
	}

	/**
	 * Validates and writes the form data to the user object.
	 *
	 * @throws ValidationException if validation fails
	 */
	public void writeBean() throws ValidationException {
		binder.writeBean(formUser);
	}

	/**
	 * Returns the data binder for this form.
	 *
	 * @return the binder
	 */
	protected Binder<User> getBinder() {
		return binder;
	}

	/**
	 * Returns the user service.
	 *
	 * @return the user service
	 */
	protected UserService getUserService() {
		return userService;
	}
}