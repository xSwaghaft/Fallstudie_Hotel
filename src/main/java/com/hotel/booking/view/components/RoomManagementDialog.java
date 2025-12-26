package com.hotel.booking.view.components;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.service.BookingExtraService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.UI;

/**
 * Manages all dialog interactions for the Room Management View.
 * Handles dialogs for:
 * - Room operations (add, edit, delete)
 * - Room Category operations (add, edit, delete)
 * - Extras operations (add)
 *
 * Centralizes dialog creation and management logic to reduce code duplication.
 *
 * @author Artur Derr
 */
public class RoomManagementDialog {

    private final RoomService roomService;
    private final RoomCategoryService roomCategoryService;
    private final BookingExtraService extraService;
    private final Runnable onDataChanged;

    /**
     * Constructs a new RoomManagementDialog with required dependencies.
     *
     * @param roomService the service for managing room operations
     * @param roomCategoryService the service for managing room categories
     * @param extraService the service for managing booking extras
     * @param onDataChanged callback to execute when data is modified
     */
    public RoomManagementDialog(RoomService roomService,
                                 RoomCategoryService roomCategoryService,
                                 BookingExtraService extraService,
                                 Runnable onDataChanged) {
        this.roomService = roomService;
        this.roomCategoryService = roomCategoryService;
        this.extraService = extraService;
        this.onDataChanged = onDataChanged;
    }

    // ==================== ROOM DIALOGS ====================

    /**
     * Opens a dialog for adding a new room.
     * The dialog contains a room form and save/cancel buttons.
     * On successful save, triggers the onDataChanged callback to refresh the view.
     */
    public void openAddRoomDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Room");
        dialog.setWidth("600px");

        RoomForm form = new RoomForm(null, roomCategoryService);

        Button saveBtn = new Button("Add Room");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            try {
                form.writeBean();
                roomService.saveRoom(form.getRoom());
                onDataChanged.run();
                dialog.close();
                Notification.show("Room added successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelBtn, saveBtn);
        buttonLayout.setSpacing(true);
        dialog.add(form, buttonLayout);
        dialog.open();
    }

    /**
     * Opens a dialog for editing an existing room.
     * Pre-loads the room's category from the database to ensure consistency.
     * On successful save, triggers the onDataChanged callback to refresh the view.
     *
     * @param room the room entity to edit
     */
    public void openEditRoomDialog(Room room) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Room " + room.getRoomNumber());
        dialog.setWidth("600px");

        // Ensure the category is loaded from the database
        if (room.getCategory() != null && room.getCategory().getCategory_id() != null) {
            roomCategoryService.getRoomCategoryById(room.getCategory().getCategory_id())
                .ifPresent(room::setCategory);
        }

        RoomForm form = new RoomForm(room, roomCategoryService);

        Button saveBtn = new Button("Update Room");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            try {
                form.writeBean();
                roomService.saveRoom(form.getRoom());
                onDataChanged.run();
                dialog.close();
                Notification.show("Room updated successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelBtn, saveBtn);
        buttonLayout.setSpacing(true);
        dialog.add(form, buttonLayout);
        dialog.open();
    }

    // ==================== CATEGORY DIALOGS ====================

    /**
     * Opens a dialog for adding or editing a room category.
     * Includes a category form and an images section for managing category images.
     * On successful save, triggers the onDataChanged callback to refresh the view.
     *
     * @param existing the existing category to edit, or null to create a new one
     */
    public void openCategoryDialog(RoomCategory existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Add New Room Category" : "Edit Room Category");
        dialog.setWidth("700px");

        RoomCategoryForm form = new RoomCategoryForm(existing);

        // Images section
        VerticalLayout imagesSection = createCategoryImagesSection(existing, dialog);

        // Main content layout
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSpacing(true);
        contentLayout.setPadding(false);
        contentLayout.add(form, imagesSection);
        contentLayout.setFlexGrow(1, form);

        Button saveBtn = new Button(existing == null ? "Add Category" : "Update Category");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            try {
                form.writeBean();
                roomCategoryService.saveRoomCategory(form.getCategory());
                onDataChanged.run();
                dialog.close();
                Notification.show("Category saved successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelBtn, saveBtn);
        buttonLayout.setSpacing(true);
        dialog.add(contentLayout, buttonLayout);
        dialog.open();
    }

    /**
     * Creates the images section for a room category dialog.
     * Displays existing images and provides a button to add new pictures.
     * For new categories, shows a message to save first before adding images.
     *
     * @param category the room category entity, or null if creating a new category
     * @param parentDialog the parent dialog that contains this section
     * @return a VerticalLayout containing the images section
     */
    private VerticalLayout createCategoryImagesSection(RoomCategory category, Dialog parentDialog) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.addClassName("category-images-section");

        com.vaadin.flow.component.html.H3 title = new com.vaadin.flow.component.html.H3("Images");
        title.getStyle().set("margin", "0 0 var(--lumo-space-m) 0");

        HorizontalLayout imagesPreview = new HorizontalLayout();
        imagesPreview.setSpacing(true);
        imagesPreview.addClassName("category-images-preview");

        if (category != null && category.getCategory_id() != null) {
            java.util.List<com.hotel.booking.entity.RoomImage> images = roomCategoryService.getCategoryImages(category.getCategory_id());
            
            if (!images.isEmpty()) {
                for (com.hotel.booking.entity.RoomImage image : images) {
                    com.vaadin.flow.component.html.Div imageCard = createImagePreviewCard(image);
                    imagesPreview.add(imageCard);
                }
            } else {
                com.vaadin.flow.component.html.Paragraph emptyMsg = new com.vaadin.flow.component.html.Paragraph("No images assigned yet");
                emptyMsg.getStyle().set("color", "var(--lumo-secondary-text-color)");
                section.add(title, emptyMsg);
                
                Button addPicturesBtn = new Button("Add Pictures", VaadinIcon.PLUS.create());
                addPicturesBtn.addClassName("primary-button");
                addPicturesBtn.addClickListener(e -> {
                    parentDialog.close();
                    // Navigate to image management page with category ID to filter unassigned images
                    UI.getCurrent().navigate("image-management?categoryId=" + category.getCategory_id());
                });
                section.add(addPicturesBtn);
                return section;
            }
        } else {
            com.vaadin.flow.component.html.Paragraph emptyMsg = new com.vaadin.flow.component.html.Paragraph("Save the category first, then add images");
            emptyMsg.getStyle().set("color", "var(--lumo-secondary-text-color)");
            section.add(title, emptyMsg);
            return section;
        }

        Button addPicturesBtn = new Button("Add Pictures", VaadinIcon.PLUS.create());
        addPicturesBtn.addClassName("primary-button");
        addPicturesBtn.addClickListener(e -> {
            parentDialog.close();
            // Navigate to image management page with category ID to filter unassigned images
            UI.getCurrent().navigate("image-management?categoryId=" + category.getCategory_id());
        });

        section.add(title, imagesPreview, addPicturesBtn);
        return section;
    }

    /**
     * Creates a preview card component for a room image.
     * Displays the image as a 80x80 pixel thumbnail in a card layout.
     *
     * @param image the room image entity to display
     * @return a Div component containing the image preview
     */
    private com.vaadin.flow.component.html.Div createImagePreviewCard(com.hotel.booking.entity.RoomImage image) {
        com.vaadin.flow.component.html.Div card = new com.vaadin.flow.component.html.Div();
        card.addClassName("image-preview-card");

        com.vaadin.flow.component.html.Image preview = new com.vaadin.flow.component.html.Image(image.getImagePath(), "image");
        preview.setWidth("80px");
        preview.setHeight("80px");
        preview.addClassName("image-preview-thumbnail");

        card.add(preview);
        return card;
    }

    // ==================== EXTRA DIALOG ====================

    /**
     * Opens a dialog for adding a new extra/additional service.
     * The dialog contains a form for entering extra details and save/cancel buttons.
     * On successful save, triggers the onDataChanged callback to refresh the view.
     */
    public void openExtraDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Extra");
        dialog.setWidth("500px");

        AddExtraForm form = new AddExtraForm(extraService, null);
        form.setExtra(new BookingExtra());

        Button saveBtn = new Button("Add Extra");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            if (form.writeBeanIfValid()) {
                onDataChanged.run();
                dialog.close();
                Notification.show("Extra added successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Please fill all required fields.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveBtn, cancelBtn);
        buttonLayout.setSpacing(true);
        dialog.add(form, buttonLayout);
        dialog.open();
    }

    /**
     * Opens a dialog for editing an existing extra/additional service.
     * The dialog contains a form for updating extra details and save/cancel buttons.
     * On successful save, triggers the onDataChanged callback to refresh the view.
     *
     * @param extra the existing extra to edit
     */
    public void openEditExtraDialog(BookingExtra extra) {
        if (extra == null) {
            showErrorDialog("Cannot Edit Extra", "Extra is null");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Extra");
        dialog.setWidth("500px");

        AddExtraForm form = new AddExtraForm(extraService, extra);

        Button saveBtn = new Button("Save Changes");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            if (form.writeBeanIfValid()) {
                onDataChanged.run();
                dialog.close();
                Notification.show("Extra updated successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Please fill all required fields.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveBtn, cancelBtn);
        buttonLayout.setSpacing(true);
        dialog.add(form, buttonLayout);
        dialog.open();
    }

    // ==================== DIALOG HELPER METHODS ====================

    /**
     * Shows an error dialog with a message.
     * Displays a modal dialog with the given title and message, with an OK button to close.
     *
     * @param title the dialog title
     * @param message the error message to display
     */
    public void showErrorDialog(String title, String message) {
        Dialog errorDialog = new Dialog();
        errorDialog.setHeaderTitle(title);
        Paragraph messageElement = new Paragraph(message);
        messageElement.addClassName("dialog-message");
        Button okBtn = new Button("OK", e -> errorDialog.close());
        errorDialog.add(messageElement);
        errorDialog.getFooter().add(okBtn);
        errorDialog.open();
    }

    /**
     * Shows a confirmation dialog with customizable buttons.
     * Displays a modal dialog with the given title and message, with confirm and cancel buttons.
     * Message text can contain double line breaks to separate paragraphs.
     *
     * @param title the dialog title
     * @param message the dialog message (may contain newlines for formatting)
     * @param confirmLabel the label for the confirmation button
     * @param onConfirm callback to execute when user confirms the action
     */
    public void showConfirmDialog(String title, String message, String confirmLabel, Runnable onConfirm) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);
        
        // Split text into paragraphs (separated by double line breaks)
        String[] paragraphs = message.split("\n\n");
        for (String para : paragraphs) {
            if (!para.trim().isEmpty()) {
                Paragraph p = new Paragraph(para.trim());
                p.addClassName("dialog-message");
                content.add(p);
            }
        }
        
        Button confirmBtn = new Button(confirmLabel);
        confirmBtn.addClassName("confirm-delete-btn");
        confirmBtn.addClickListener(e -> {
            dialog.close();
            onConfirm.run();
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());
        
        dialog.add(content);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, confirmBtn));
        dialog.open();
    }
}
