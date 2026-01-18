package com.hotel.booking.view.components;

import java.math.BigDecimal;

import com.hotel.booking.entity.Amenities;
import com.hotel.booking.entity.RoomCategory;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * Reusable form component for creating and editing room categories.
 * <p>
 * This form provides fields for entering room category information including name,
 * description, pricing, maximum occupancy, and amenities. Follows a similar pattern
 * to AddUserForm for dialog integration.
 * </p>
 *
 * @author Artur Derr
 */
public class RoomCategoryForm extends FormLayout {

    private final Binder<RoomCategory> binder = new Binder<>(RoomCategory.class);
    private RoomCategory formCategory;

    private final TextField nameField = new TextField("Category Name *");
    private final TextArea descArea = new TextArea("Description *");
    private final NumberField priceField = new NumberField("Price per Night (€) *");
    private final NumberField maxOccupancyField = new NumberField("Max Occupancy *");
    private final MultiSelectComboBox<Amenities> amenitiesSelect = new MultiSelectComboBox<>();
    private final Checkbox activeCheck = new Checkbox("Active");

    /**
     * Constructs a RoomCategoryForm for creating or editing a room category.
     *
     * @param existingCategory the RoomCategory entity to edit, or null for creating a new category
     */
    public RoomCategoryForm(RoomCategory existingCategory) {
        initializeFields();
        setupBinder();
        layoutForm();
        setCategory(existingCategory);
    }

    /**
     * Initializes and configures all form input fields.
     * <p>
     * Sets up placeholders, validators, and constraints for each field.
     * </p>
     */
    private void initializeFields() {
        nameField.setPlaceholder("e.g., Standard Room, Deluxe Suite");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);

        descArea.setPlaceholder("Describe this room category...");
        descArea.setWidthFull();
        descArea.setHeight("100px");
        descArea.setRequiredIndicatorVisible(true);

        priceField.setMin(0);
        priceField.setStep(0.01);
        priceField.setWidthFull();
        priceField.setRequiredIndicatorVisible(true);

        maxOccupancyField.setMin(1);
        maxOccupancyField.setStep(1);
        maxOccupancyField.setWidthFull();
        maxOccupancyField.setRequiredIndicatorVisible(true);

        amenitiesSelect.setLabel("Amenities");
        amenitiesSelect.setItems(Amenities.values());
        amenitiesSelect.setWidthFull();
        amenitiesSelect.setPlaceholder("Select amenities...");

        activeCheck.setValue(true);
    }

    /**
     * Configures the Vaadin Binder for form validation and data binding.
     * <p>
     * Sets up validation rules and type conversion for all form fields including
     * price and max occupancy numeric conversions.
     * </p>
     */
    private void setupBinder() {
        binder.forField(nameField)
            .asRequired("Category name is required")
            .bind(RoomCategory::getName, RoomCategory::setName);

        binder.forField(descArea)
            .asRequired("Description is required")
            .bind(RoomCategory::getDescription, RoomCategory::setDescription);

        binder.forField(priceField)
            .asRequired("Price is required")
            .bind(category -> category.getPricePerNight() != null ? category.getPricePerNight().doubleValue() : null,
                  (category, value) -> category.setPricePerNight(value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO));

        binder.forField(maxOccupancyField)
            .asRequired("Max occupancy is required")
            .bind(category -> category.getMaxOccupancy() != null ? category.getMaxOccupancy().doubleValue() : null,
                  (category, value) -> category.setMaxOccupancy(value != null ? value.intValue() : 1));

        binder.forField(activeCheck)
            .bind(RoomCategory::getActive, RoomCategory::setActive);
        
        binder.forField(amenitiesSelect)
            .bind(RoomCategory::getAmenities, RoomCategory::setAmenities);
    }

    /**
     * Arranges the form components in a responsive layout.
     * <p>
     * Uses responsive steps to adapt to different screen sizes.
     * </p>
     */
    private void layoutForm() {
        setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("600px", 2)
        );
        setColspan(descArea, 2);
        setColspan(amenitiesSelect, 2);

        add(nameField, priceField, descArea, maxOccupancyField, amenitiesSelect, activeCheck);
    }

    /**
     * Sets the category to be edited in the form.
     * <p>
     * If the category is null, a new RoomCategory object is created for form entry.
     * </p>
     *
     * @param existingCategory the RoomCategory entity to edit, or null for creating a new category
     */
    private void setCategory(RoomCategory existingCategory) {
        if (existingCategory == null) {
            formCategory = new RoomCategory();
            activeCheck.setValue(true);
        } else {
            formCategory = existingCategory;
        }
        
        binder.readBean(formCategory);
    }

    /**
     * Retrieves the category object being edited in the form.
     *
     * @return the RoomCategory entity
     */
    public RoomCategory getCategory() {
        return formCategory;
    }

    /**
     * Writes the form data to the category bean after validation.
     *
     * @throws ValidationException if validation of form data fails
     */
    public void writeBean() throws ValidationException {
        binder.writeBean(formCategory);
    }
}
