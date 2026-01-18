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
 * Reusable form component for assigning images to room categories.
 * <p>
 * This component provides:
 * </p>
 * <ul>
 *   <li>Image preview display</li>
 *   <li>Room category selection</li>
 *   <li>Alt text and title configuration</li>
 *   <li>Primary image designation</li>
 * </ul>
 *
 * @author Artur Derr
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

    /**
     * Constructs a RoomCategoryImageSelector for managing room category images.
     *
     * @param roomImage the RoomImage entity to edit, or null for a new image
     * @param categories the list of available room categories
     */
    public RoomCategoryImageSelector(RoomImage roomImage, List<RoomCategory> categories) {
        this.availableCategories = categories != null ? categories : List.of();
        this.assignedImage = roomImage != null ? roomImage : new RoomImage(null);
        initializeFields(this.availableCategories);
        setupBinder();
        layoutForm();
        setImage(roomImage);
    }

    /**
     * Initializes and configures all form input fields and image preview.
     *
     * @param categories the list of available room categories for selection
     */
    private void initializeFields(List<RoomCategory> categories) {
        // Image preview
        imagePreview = new Image(safeImageSrc(assignedImage), "Image preview");
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

    /**
     * Configures the Vaadin Binder for form validation and data binding.
     * <p>
     * Sets up binding for category, alt text, title, and primary image fields.
     * </p>
     */
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

    /**
     * Arranges the form components in a responsive layout.
     * <p>
     * Displays the image preview at the top, followed by form fields below.
     * Uses responsive steps to adapt to different screen sizes.
     * </p>
     */
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

    /**
     * Sets the image to be edited in the form.
     * <p>
     * If the image is null, a new RoomImage object is created.
     * Normalizes the selected category to ensure proper binding with the Select component.
     * </p>
     *
     * @param roomImage the RoomImage entity to edit, or null for a new image
     */
    private void setImage(RoomImage roomImage) {
        if (roomImage == null) {
            assignedImage = new RoomImage(null);
            imagePreview.setSrc("");
        } else {
            assignedImage = roomImage;
            imagePreview.setSrc(roomImage.getImagePath());
        }

        // Normalizes the selected value to the same instance that exists in the items list
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

    private static String safeImageSrc(RoomImage image) {
        if (image == null) {
            return "";
        }
        String src = image.getImagePath();
        return src != null ? src : "";
    }

    /**
     * Retrieves the room image being edited in the form.
     *
     * @return the RoomImage entity
     */
    public RoomImage getAssignedImage() {
        return assignedImage;
    }

    /**
     * Writes the form data to the room image bean after validation.
     *
     * @throws ValidationException if validation of form data fails
     */
    public void writeBean() throws ValidationException {
        binder.writeBean(assignedImage);
    }
}
