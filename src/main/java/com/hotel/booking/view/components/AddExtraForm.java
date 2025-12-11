package com.hotel.booking.view.components;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.service.BookingExtraService;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

public class AddExtraForm extends FormLayout {

    private final Binder<BookingExtra> binder = new Binder<>(BookingExtra.class);
    private final BookingExtraService extraService;

    private final TextField nameField = new TextField("Name");
    private final com.vaadin.flow.component.textfield.TextArea descriptionField = new com.vaadin.flow.component.textfield.TextArea("Description");
    private final com.vaadin.flow.component.textfield.NumberField priceField = new com.vaadin.flow.component.textfield.NumberField("Price (€)");
    private final com.vaadin.flow.component.combobox.ComboBox<com.hotel.booking.entity.ExtraTypeEnum> typeField = new com.vaadin.flow.component.combobox.ComboBox<>("Type");

    public AddExtraForm(BookingExtraService extraService) {
        this.extraService = extraService;

        typeField.setItems(com.hotel.booking.entity.ExtraTypeEnum.values());
        typeField.setRequired(true);
        priceField.setMin(0.0);
        priceField.setStep(0.01);
        priceField.setRequiredIndicatorVisible(true);
        nameField.setRequired(true);

        add(nameField, descriptionField, priceField, typeField);

        binder.forField(nameField)
            .asRequired("Name is required")
            .bind(BookingExtra::getName, BookingExtra::setName);

        binder.forField(descriptionField)
            .bind(BookingExtra::getDescription, BookingExtra::setDescription);

        binder.forField(priceField)
            .asRequired("Price is required")
            .bind(BookingExtra::getPrice, BookingExtra::setPrice);

        binder.forField(typeField)
            .asRequired("Type is required")
            .bind(BookingExtra::getExtraType, BookingExtra::setExtraType);
    }

    public void setExtra(BookingExtra extra) {
        binder.setBean(extra);
    }

    public BookingExtra getExtra() {
        return binder.getBean();
    }

    public boolean writeBeanIfValid() {
        if (binder.validate().isOk()) {
            try {
                extraService.saveBookingExtra(getExtra());
                return true;
            } catch (Exception e) {
                // Fehlerbehandlung ggf. hier
                return false;
            }
        }
        return false;
    }

}
