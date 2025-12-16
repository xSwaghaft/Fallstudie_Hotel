package com.hotel.booking.view.components;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.List;

/**
 * RoomCategoryImageSelector Component - Reusable form for assigning images to room categories.
 * Displays image preview, allows category selection, and sets display properties.
 */
public class RoomCategoryImageSelector extends FormLayout {

    private final Binder<RoomImage> binder = new Binder<>(RoomImage.class);
    private RoomImage assignedImage;

    private final List<RoomCategory> availableCategories;
    
    private Image imagePreview;
    private final TextField altTextField = new TextField("Alt Text");
    private final TextField titleField = new TextField("Title");
    private final Checkbox primaryField = new Checkbox("Primary");
    private final Select<RoomCategory> categorySelect = new Select<>();

    public RoomCategoryImageSelector(RoomImage roomImage, List<RoomCategory> categories) {
        this.availableCategories = categories != null ? categories : List.of();
        this.assignedImage = roomImage;
        initializeFields(this.availableCategories);
        setupBinder();
        layoutForm();
        setImage(roomImage);
    }

    private void initializeFields(List<RoomCategory> categories) {
        // Image preview
        imagePreview = new Image(assignedImage.getImagePath(), "Image preview");
        imagePreview.setWidth("200px");
        imagePreview.setHeight("150px");
        imagePreview.addClassName("image-selector-preview");

        // Category select
        categorySelect.setLabel("Room Category");
        categorySelect.setItems(categories);
        categorySelect.setItemLabelGenerator(RoomCategory::getName);
        categorySelect.setWidthFull();
        categorySelect.setRequiredIndicatorVisible(false);

        // Alt text
        altTextField.setLabel("Alt Text (for accessibility)");
        altTextField.setPlaceholder("Describe the image...");
        altTextField.setWidthFull();

        // Title
        titleField.setLabel("Title");
        titleField.setPlaceholder("Image title...");
        titleField.setWidthFull();

        // Primary
        primaryField.setWidthFull();
    }

    private void setupBinder() {
        binder.forField(categorySelect)
            .bind(RoomImage::getCategory, RoomImage::setCategory);

        binder.forField(altTextField)
            .bind(RoomImage::getAltText, RoomImage::setAltText);

        binder.forField(titleField)
            .bind(RoomImage::getTitle, RoomImage::setTitle);

        binder.forField(primaryField)
                .bind(RoomImage::getIsPrimary, RoomImage::setIsPrimary);
    }

    private void layoutForm() {
        setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("500px", 2)
        );
        setColspan(altTextField, 2);
        setColspan(titleField, 2);

        // Add preview section first
        VerticalLayout previewSection = new VerticalLayout();
        previewSection.setSpacing(false);
        previewSection.setPadding(false);
        
        Paragraph previewLabel = new Paragraph("Image Preview");
        previewLabel.addClassName("image-selector-label");
        
        previewSection.add(previewLabel, imagePreview);
        add(previewSection);
        setColspan(previewSection, 2);

        // Add form fields
        add(categorySelect, primaryField, altTextField, titleField);
    }

    private void setImage(RoomImage roomImage) {
        if (roomImage == null) {
            assignedImage = new RoomImage(null);
        } else {
            assignedImage = roomImage;
            imagePreview.setSrc(roomImage.getImagePath());
        }

        // Vaadin Select compares items via equals(); if RoomCategory doesn't implement equals/hashCode,
        // we must normalize the selected value to the same instance that exists in the items list.
        if (assignedImage.getCategory() != null && assignedImage.getCategory().getCategory_id() != null) {
            Long selectedId = assignedImage.getCategory().getCategory_id();
            for (RoomCategory c : availableCategories) {
                if (c != null && c.getCategory_id() != null && c.getCategory_id().equals(selectedId)) {
                    assignedImage.setCategory(c);
                    break;
                }
            }
        }
        
        binder.readBean(assignedImage);
    }

    public RoomImage getAssignedImage() {
        return assignedImage;
    }

    public void writeBean() throws ValidationException {
        binder.writeBean(assignedImage);
    }
}
