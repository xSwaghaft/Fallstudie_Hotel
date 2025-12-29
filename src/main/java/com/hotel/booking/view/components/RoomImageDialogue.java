package com.hotel.booking.view.components;

import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomImageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Dialog helper for editing and deleting RoomImage entities.
 *
 * This class provides dialog windows for users to edit room image assignments
 * and delete images from the system. It manages the user interactions and
 * delegates business logic to the appropriate services.
 *
 * Extracted from ImageManagementView.
 *
 * @author Artur Derr
 */
public class RoomImageDialogue {

    private final RoomCategoryService roomCategoryService;
    private final RoomImageService roomImageService;
    private final Runnable refresh;
    private final Consumer<String> success;
    private final Consumer<String> error;

    /**
     * Constructs a RoomImageDialogue with the required dependencies.
     *
     * @param roomCategoryService service for accessing room category data
     * @param roomImageService service for managing room image operations
     * @param refresh callback to refresh the UI after modifications
     * @param success callback to handle success messages
     * @param error callback to handle error messages
     * @throws NullPointerException if any parameter is null
     */
    public RoomImageDialogue(
            RoomCategoryService roomCategoryService,
            RoomImageService roomImageService,
            Runnable refresh,
            Consumer<String> success,
            Consumer<String> error
    ) {
        this.roomCategoryService = Objects.requireNonNull(roomCategoryService);
        this.roomImageService = Objects.requireNonNull(roomImageService);
        this.refresh = Objects.requireNonNull(refresh);
        this.success = Objects.requireNonNull(success);
        this.error = Objects.requireNonNull(error);
    }

    /**
     * Opens a dialog to edit an existing room image.
     *
     * The dialog allows the user to select or change the room category
     * assignment for the given image. Changes are persisted to the database
     * when the user confirms the update.
     *
     * @param roomImage the room image to be edited
     */
    public void openEditImageDialog(RoomImage roomImage) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Image");
        dialog.setWidth("600px");
        dialog.setMaxWidth("90vw");
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);

        RoomCategoryImageSelector selector = new RoomCategoryImageSelector(
                roomImage,
                roomCategoryService.getAllRoomCategories()
        );

        Button saveBtn = new Button("Update Image");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> handleImageUpdate(selector, dialog));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelBtn, saveBtn);
        buttonLayout.setSpacing(true);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        dialog.add(selector, buttonLayout);
        dialog.open();
    }

    /**
     * Opens a confirmation dialog to delete a room image.
     *
     * Displays a confirmation prompt to the user before permanently removing
     * the image from the system. The deletion is only executed if the user
     * confirms the action.
     *
     * @param roomImage the room image to be deleted
     */
    public void openDeleteImageDialog(RoomImage roomImage) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Delete Image?");
        confirmDialog.setWidth("400px");
        confirmDialog.setMaxWidth("90vw");
        confirmDialog.setModal(true);
        confirmDialog.setCloseOnOutsideClick(false);

        Paragraph message = new Paragraph("Are you sure you want to delete this image?");
        message.addClassName("dialog-message");

        Button confirmBtn = new Button("Delete", VaadinIcon.TRASH.create());
        confirmBtn.addClassName("confirm-delete-btn");
        confirmBtn.addClickListener(e -> handleImageDeletion(roomImage, confirmDialog));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelBtn, confirmBtn);
        buttonLayout.setSpacing(true);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        confirmDialog.add(message, buttonLayout);
        confirmDialog.open();
    }

    /**
     * Handles the update operation for a room image.
     *
     * Processes the image update by retrieving the selected category assignment,
     * writing the bean data, and persisting the changes to the database. On success,
     * triggers the refresh callback and closes the dialog. Errors are caught and
     * reported through the error callback.
     *
     * @param selector the RoomCategoryImageSelector containing the updated image data
     * @param dialog the dialog to close after successful update
     */
    private void handleImageUpdate(RoomCategoryImageSelector selector, Dialog dialog) {
        try {
            RoomImage updatedImage = selector.getAssignedImage();
            selector.writeBean();

            roomImageService.updateImage(updatedImage);
            refresh.run();
            dialog.close();

            String message = updatedImage.getCategory() != null
                    ? "Image updated successfully!"
                    : "Image updated (no category assigned).";
            success.accept(message);
        } catch (Exception ex) {
            error.accept("Error: " + ex.getMessage());
        }
    }

    /**
     * Handles the deletion of a room image.
     *
     * Deletes the specified room image from the database and updates the UI
     * by invoking the refresh callback. The confirmation dialog is closed after
     * successful deletion, and a success message is displayed. If an error occurs
     * during deletion, an error message is shown through the error callback.
     *
     * @param roomImage the room image to be deleted
     * @param confirmDialog the confirmation dialog to close after successful deletion
     */
    private void handleImageDeletion(RoomImage roomImage, Dialog confirmDialog) {
        try {
            roomImageService.deleteImage(roomImage);
            refresh.run();
            confirmDialog.close();
            success.accept("Image deleted successfully!");
        } catch (Exception ex) {
            error.accept("Error deleting image: " + ex.getMessage());
        }
    }
}
