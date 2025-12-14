package com.hotel.booking.view.components;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.service.BookingExtraService;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

//Simples Formular zum Anlegen von Extras
//Matthias Lohr
public class AddExtraForm extends FormLayout {

    private BookingExtra bookingExtra;

    private final Binder<BookingExtra> binder = new Binder<>(BookingExtra.class);
    private final BookingExtraService extraService;

    private final TextField nameField = new TextField("Name");
    private final TextArea descriptionField = new TextArea("Description");
    private final NumberField priceField = new NumberField("Price (€)");

    public AddExtraForm(BookingExtraService extraService, BookingExtra bookingExtra) {
        this.extraService = extraService;
        this.bookingExtra = bookingExtra;


        priceField.setMin(0.0);
        priceField.setStep(0.01);
        priceField.setRequiredIndicatorVisible(true);
        nameField.setRequired(true);

        add(nameField, descriptionField, priceField);

        binder.forField(nameField)
            .asRequired("Name is required")
            .bind(BookingExtra::getName, BookingExtra::setName);

        binder.forField(descriptionField)
            .bind(BookingExtra::getDescription, BookingExtra::setDescription);

        binder.forField(priceField)
            .asRequired("Price is required")
            .bind(BookingExtra::getPrice, BookingExtra::setPrice);
        
        setExtra(bookingExtra);
    }

    public void setExtra(BookingExtra extra) {
        if(bookingExtra == null) {
            bookingExtra = new BookingExtra();
        } else {
            bookingExtra = extra;
        }
        binder.readBean(bookingExtra);
    }

    public BookingExtra getExtra() {
        return bookingExtra;
    }

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
