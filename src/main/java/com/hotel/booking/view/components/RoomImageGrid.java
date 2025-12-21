package com.hotel.booking.view.components;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Grid component for displaying and managing RoomImage listings.
 *
 * <p>This component provides a grid-based UI for displaying room images with preview thumbnails,
 * metadata, and action buttons for editing, deleting, and assigning images to room categories.
 * The grid supports filtering, sorting, and provides callbacks for various user interactions.</p>
 *
 * <p>Extracted from ImageManagementView to provide reusable image management functionality.</p>
 *
 * @author Artur Derr
 */
public class RoomImageGrid extends Grid<RoomImage> {

    private RoomCategory assignToCategory;

    private Consumer<RoomImage> onEdit = img -> {};
    private Consumer<RoomImage> onDelete = img -> {};
    private Consumer<RoomImage> onAssign = img -> {};

    /**
     * Constructs a new RoomImageGrid component.
     *
     * <p>Initializes the grid without automatically adding columns and configures
     * the grid layout, columns, and styling.</p>
     */
    public RoomImageGrid() {
        super(RoomImage.class, false);
        configureGrid();
    }

    /**
     * Sets the room category to which images can be assigned.
     *
     * <p>When a room category is set, the grid will display an "Assign" button
     * for images that are not yet assigned to any category. Additionally,
     * the data provider is refreshed to update the UI.</p>
     *
     * @param assignToCategory the room category to assign images to, or {@code null}
     *                         to disable the assignment functionality
     */
    public void setAssignToCategory(RoomCategory assignToCategory) {
        this.assignToCategory = assignToCategory;
        getDataProvider().refreshAll();
    }

    /**
     * Sets the callback function to be invoked when the edit action is triggered.
     *
     * <p>The callback is called with the RoomImage that the user wants to edit.</p>
     *
     * @param onEdit a {@code Consumer} that accepts a RoomImage to be edited;
     *               must not be {@code null}
     * @throws NullPointerException if {@code onEdit} is {@code null}
     */
    public void setOnEdit(Consumer<RoomImage> onEdit) {
        this.onEdit = Objects.requireNonNull(onEdit);
    }

    /**
     * Sets the callback function to be invoked when the delete action is triggered.
     *
     * <p>The callback is called with the RoomImage that the user wants to delete.</p>
     *
     * @param onDelete a {@code Consumer} that accepts a RoomImage to be deleted;
     *                 must not be {@code null}
     * @throws NullPointerException if {@code onDelete} is {@code null}
     */
    public void setOnDelete(Consumer<RoomImage> onDelete) {
        this.onDelete = Objects.requireNonNull(onDelete);
    }

    /**
     * Sets the callback function to be invoked when the assign action is triggered.
     *
     * <p>The callback is called with the RoomImage that the user wants to assign
     * to the currently selected room category.</p>
     *
     * @param onAssign a {@code Consumer} that accepts a RoomImage to be assigned;
     *                 must not be {@code null}
     * @throws NullPointerException if {@code onAssign} is {@code null}
     */
    public void setOnAssign(Consumer<RoomImage> onAssign) {
        this.onAssign = Objects.requireNonNull(onAssign);
    }

    /**
     * Configures the grid layout, columns, and styling.
     *
     * <p>This method sets up the grid with the following columns:
     * <ul>
     *   <li>Preview - Image thumbnail</li>
     *   <li>Filename - Image title</li>
     *   <li>Alt Text - Alternative text for accessibility</li>
     *   <li>Category / Status - Assigned category or unassigned status</li>
     *   <li>Primary - Indicates if the image is the primary image</li>
     *   <li>Actions - Edit, delete, and optionally assign buttons</li>
     * </ul>
     * All columns are configured with appropriate widths, sorting capabilities, and styling.</p>
     */
    private void configureGrid() {
        removeAllColumns();
        setSelectionMode(Grid.SelectionMode.NONE);

        addComponentColumn(this::createImagePreview)
                .setHeader("Preview")
                .setAutoWidth(true)
                .setFlexGrow(0);

        addColumn(RoomImage::getTitle)
                .setHeader("Filename")
                .setAutoWidth(true)
                .setSortable(true)
                .setFlexGrow(1);

        addColumn(RoomImage::getAltText)
                .setHeader("Alt Text")
                .setAutoWidth(true)
                .setSortable(true)
                .setFlexGrow(1);

        addComponentColumn(this::createCategoryBadge)
                .setHeader("Category / Status")
                .setAutoWidth(true)
                .setSortable(true)
                .setFlexGrow(1);

        addColumn(roomImage -> Boolean.TRUE.equals(roomImage.getIsPrimary()) ? "Yes" : "No")
                .setHeader("Primary")
                .setAutoWidth(true)
                .setSortable(true);

        addComponentColumn(this::createImageActions)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        setAllRowsVisible(true);
        setWidth("100%");
    }

    /**
     * Creates an image preview component for the grid.
     *
     * <p>Renders a square image preview (80x80 pixels) for the given room image.
     * The preview image is retrieved from the room image's image path.</p>
     *
     * @param roomImage the room image for which to create a preview
     * @return an Image component displaying the preview thumbnail
     */
    private Component createImagePreview(RoomImage roomImage) {
        Image preview = new Image(roomImage.getImagePath(), "preview");
        preview.setWidth("80px");
        preview.setHeight("80px");
        preview.addClassName("image-grid-thumbnail");
        return preview;
    }

    /**
     * Creates a category badge component for the grid.
     *
     * <p>Displays the name of the category to which the image is assigned, or
     * "Not Assigned" if the image has no category. Different CSS classes are applied
     * based on the assignment status to provide visual distinction.</p>
     *
     * @param roomImage the room image for which to create the category badge
     * @return a Span component displaying the category name or unassigned status
     */
    private Component createCategoryBadge(RoomImage roomImage) {
        String categoryName = roomImage.getCategory() != null
                ? roomImage.getCategory().getName()
                : "Not Assigned";

        Span categorySpan = new Span(categoryName);
        if (roomImage.getCategory() != null) {
            categorySpan.addClassName("image-category-badge");
        } else {
            categorySpan.addClassName("unassigned-badge");
        }
        return categorySpan;
    }

    /**
     * Creates the action buttons component for the grid row.
     *
     * <p>Generates a horizontal layout containing action buttons for the given room image.
     * An "Assign" button is displayed if a target category is set and the image is
     * not yet assigned to any category. Additionally, "Edit" and "Delete" buttons
     * are always provided. Each button triggers its corresponding callback when clicked.</p>
     *
     * @param roomImage the room image for which to create action buttons
     * @return a Component containing the action buttons
     */
    private Component createImageActions(RoomImage roomImage) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setMargin(false);

        if (assignToCategory != null && roomImage.getCategory() == null) {
            Button assignBtn = new Button("Assign", VaadinIcon.CHECK.create());
            assignBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            assignBtn.addClickListener(e -> onAssign.accept(roomImage));
            actions.add(assignBtn);
        }

        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.addClickListener(e -> onEdit.accept(roomImage));

        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> onDelete.accept(roomImage));

        actions.add(editBtn, deleteBtn);
        return actions;
    }
}
