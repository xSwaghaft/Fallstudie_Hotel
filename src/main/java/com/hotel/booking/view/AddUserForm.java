package com.hotel.booking.view;

import com.hotel.booking.entity.AdressEmbeddable;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.hotel.booking.service.UserService;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;

//Matthias Lohr
//Formular zum Anlegen und Bearbeiten von Usern mit binding an User Entity (FDO)
//Anordnung und Breite der Felder kann noch optimiert werden
public class AddUserForm extends FormLayout {

	private final Binder<User> binder = new Binder<>(User.class);
	private User formUser;
	private final UserService userService;

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
	private final EmailField emailField = new EmailField("Email");
	private final Select<UserRole> roleSelect = new Select<>();
	private final Checkbox activeCheckbox = new Checkbox("Active");


	public AddUserForm(User existingUser, UserService userService) {

		this.userService = userService;

		// Setup role select
		roleSelect.setLabel("Role");
		roleSelect.setItems(UserRole.values());
		roleSelect.setWidthFull();

		// Layout: you can tune where fields appear
		this.add(usernameField, firstNameField, lastNameField,
			streetField, houseNumberField, cityField, postalCodeField, countryField,
			passwordField, emailField, roleSelect, activeCheckbox);

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

		// Binder wird konfiguriert
		binder.forField(firstNameField)
			.asRequired("First name is required")
			.bind(User::getFirstName, User::setFirstName);

		binder.forField(lastNameField)
			.asRequired("Last name is required")
			.bind(User::getLastName, User::setLastName);

		binder.forField(emailField)
			.asRequired("Email is required")
			.withValidator(
				email -> isEmailAvailable(email, formUser),
				"Email already in use")
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
			.bind(User::isActive, User::setActive);

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

	public void setUser(User existingUser) {
		boolean isNew = existingUser == null;
		if (isNew) {
			formUser = new User("", "", "", new AdressEmbeddable(), "", "", UserRole.GUEST, true);
		} else {
			formUser = existingUser;
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
			binder.forField(passwordField)
				.asRequired("Password is required")
				.withValidator(pw -> pw != null && pw.length() >= 6, "Password must be at least 6 characters")
				.bind(User::getPassword, User::setPassword);
		} else {
			binder.forField(passwordField)
				.withValidator(pw -> pw == null || pw.isEmpty() || pw.length() >= 6, "Password must be at least 6 characters")
				.bind(u -> "", (u, v) -> { if (v != null && !v.isEmpty()) u.setPassword(v); });
		}

		// Make required indicators visible for some fields
		usernameField.setRequiredIndicatorVisible(true);
		firstNameField.setRequiredIndicatorVisible(true);
		lastNameField.setRequiredIndicatorVisible(true);

		roleSelect.setRequiredIndicatorVisible(true);

		binder.readBean(formUser);
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
}
