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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;

//Matthias Lohr
//Formular zum Anlegen und Bearbeiten von Usern mit binding an User Entity (FDO)
//Anordnung und Breite der Felder kann noch optimiert werden
public class AddUserForm extends FormLayout {

	private final Binder<User> binder = new Binder<>(User.class);
	private User formUser;

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


	public AddUserForm(User existingUser) {

		// Setup role select
		roleSelect.setLabel("Role");
		roleSelect.setItems(UserRole.values());
		roleSelect.setWidthFull();

		// Layout: you can tune where fields appear
		this.add(usernameField, firstNameField, lastNameField,
			streetField, houseNumberField, cityField, postalCodeField, countryField,
			passwordField, emailField, roleSelect, activeCheckbox);
        
        this.setColspan(houseNumberField, 2);
        this.setColspan(streetField, 1);
        this.setColspan(cityField, 1);
        this.setColspan(postalCodeField, 1);

		// Binder setup
		binder.forField(usernameField)
			.asRequired("Username is required")
			.withValidator(new StringLengthValidator("Username must be at least 3 characters", 3, null))
			.bind(User::getUsername, User::setUsername);

		binder.forField(firstNameField)
			.asRequired("First name is required")
			.bind(User::getFirstName, User::setFirstName);

		binder.forField(lastNameField)
			.asRequired("Last name is required")
			.bind(User::getLastName, User::setLastName);

		binder.forField(emailField)
			.bind(User::getEmail, User::setEmail);

		binder.forField(roleSelect)
			.asRequired("Role is required")
			.bind(User::getRole, User::setRole);

		binder.forField(activeCheckbox)
			.bind(User::isActive, User::setActive);

		// Address bindings via lambdas to handle null embeddable
		binder.forField(streetField)
			.bind(u -> u.getAddress() != null ? u.getAddress().getStreet() : "",
				  (u, v) -> { if (u.getAddress() == null) u.setAddress(new AdressEmbeddable()); u.getAddress().setStreet(v); });

		binder.forField(houseNumberField)
			.bind(u -> u.getAddress() != null ? u.getAddress().getHouseNumber() : "",
				  (u, v) -> { if (u.getAddress() == null) u.setAddress(new AdressEmbeddable()); u.getAddress().setHouseNumber(v); });

		binder.forField(postalCodeField)
			.bind(u -> u.getAddress() != null ? u.getAddress().getPostalCode() : "",
				  (u, v) -> { if (u.getAddress() == null) u.setAddress(new AdressEmbeddable()); u.getAddress().setPostalCode(v); });

		binder.forField(cityField)
			.bind(u -> u.getAddress() != null ? u.getAddress().getCity() : "",
				  (u, v) -> { if (u.getAddress() == null) u.setAddress(new AdressEmbeddable()); u.getAddress().setCity(v); });

		binder.forField(countryField)
			.bind(u -> u.getAddress() != null ? u.getAddress().getCountry() : "",
				  (u, v) -> { if (u.getAddress() == null) u.setAddress(new AdressEmbeddable()); u.getAddress().setCountry(v); });

		// Initialize form user and bind values
		setUser(existingUser);
	}

	public void setUser(User existingUser) {
		boolean isNew = existingUser == null;
		if (isNew) {
			formUser = new User("", "", "", null, "", "", UserRole.GUEST, true);
		} else {
			formUser = existingUser;
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

	public User getUser() {
		return formUser;
	}

	public void writeBean() throws ValidationException {
		binder.writeBean(formUser);
	}
}
