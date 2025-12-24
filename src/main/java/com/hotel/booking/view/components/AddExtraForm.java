package com.hotel.booking.view.components;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.service.BookingExtraService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

/**
 * Simple form component for creating or editing {@link BookingExtra} entities.
 * <p>
 * Uses Vaadin {@link Binder} for data binding and validation and delegates
 * persistence to the {@link BookingExtraService}.
 * </p>
 *
 * @author Matthias Lohr
 */
public class AddExtraForm extends FormLayout {

    private BookingExtra bookingExtra;

    private final Binder<BookingExtra> binder = new Binder<>(BookingExtra.class);
    private final BookingExtraService extraService;

    private final TextField nameField = new TextField("Name");
    private final TextArea descriptionField = new TextArea("Description");
    private final NumberField priceField = new NumberField("Price (€)");
    private final Checkbox perPersonCheckbox = new Checkbox("Per Person");

    public AddExtraForm(BookingExtraService extraService, BookingExtra bookingExtra) {
        this.extraService = extraService;
        this.bookingExtra = bookingExtra;

        priceField.setMin(0.0);
        priceField.setStep(0.01);
        priceField.setRequiredIndicatorVisible(true);
        nameField.setRequired(true);

        perPersonCheckbox.setValue(true); // default value

        add(nameField, descriptionField, priceField, perPersonCheckbox);

        binder.forField(nameField)
            .asRequired("Name is required")
            .bind(BookingExtra::getName, BookingExtra::setName);

        binder.forField(descriptionField)
            .bind(BookingExtra::getDescription, BookingExtra::setDescription);

        binder.forField(priceField)
            .asRequired("Price is required")
            .bind(BookingExtra::getPrice, BookingExtra::setPrice);

        binder.forField(perPersonCheckbox)
            .bind(BookingExtra::isPerPerson, BookingExtra::setPerPerson);

        setExtra(bookingExtra);
    }

    /**
     * Sets the {@link BookingExtra} instance to be edited by the form.
     * <p>
     * If the provided instance is {@code null}, a new {@link BookingExtra}
     * is created and bound to the form.
     * </p>
     */
    public void setExtra(BookingExtra extra) {
        if (bookingExtra == null) {
            bookingExtra = new BookingExtra();
        } else {
            bookingExtra = extra;
        }
        binder.readBean(bookingExtra);
    }

    public BookingExtra getExtra() {
        return bookingExtra;
    }

    /**
     * Validates the form input, writes the values into the bound entity
     * and persists it using the service layer.
     *
     * @return {@code true} if validation and persistence were successful,
     *         {@code false} otherwise
     */
    public boolean writeBeanIfValid() {
        if (binder.validate().isOk()) {
            try {
                binder.writeBean(bookingExtra);
                extraService.saveBookingExtra(getExtra());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
