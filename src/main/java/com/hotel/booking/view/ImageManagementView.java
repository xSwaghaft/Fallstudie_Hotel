package com.hotel.booking.view;

import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomImageService;
import com.hotel.booking.view.components.CardFactory;
import com.hotel.booking.view.components.RoomImageDialogue;
import com.hotel.booking.view.components.RoomImageGrid;
import com.hotel.booking.view.components.RoomImageUploadSection;
import com.hotel.booking.entity.RoomCategory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;

/**
 * Image Management View for managing room images and their category assignments.
 * 
 * This Vaadin view provides functionality to:
 * <ul>
 *     <li>Upload images for hotel rooms</li>
 *     <li>Assign images to room categories</li>
 *     <li>Edit image metadata</li>
 *     <li>Delete images from the system</li>
 * </ul>
 * 
 * The view is accessible only to users with RECEPTIONIST or MANAGER roles.
 * It displays a grid of all uploaded images with options to manage their assignments
 * and metadata. New images can be uploaded through the integrated upload section.
 * 
 * @author Artur Derr
 */
@Route(value = "image-management", layout = MainLayout.class)
@PageTitle("Image Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/image-management.css")
public class ImageManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final RoomCategoryService roomCategoryService;
    private final RoomImageService roomImageService;

    private final RoomImageGrid roomImageGrid = new RoomImageGrid();
    private RoomImageDialogue roomImageDialogue;

    private RoomCategory assignToCategory;

    /**
     * Constructs an ImageManagementView with required service dependencies.
     * 
     * Initializes the view by wiring components, configuring the layout,
     * and loading image data from the database.
     * 
     * @param sessionService the service for managing user session information
     * @param roomCategoryService the service for managing room categories
     * @param roomImageService the service for managing room images
     */
    public ImageManagementView(SessionService sessionService,
                               RoomCategoryService roomCategoryService,
                               RoomImageService roomImageService) {
        this.sessionService = sessionService;
        this.roomCategoryService = roomCategoryService;
        this.roomImageService = roomImageService;

        wireComponents();

        configureLayout();
        initializeComponents();
    }

    /**
     * Wires all UI components and sets up event listeners.
     * 
     * Creates the image dialog and connects it with the image grid.
     * Establishes callbacks for edit, delete, and assign operations.
     */
    private void wireComponents() {
        roomImageDialogue = new RoomImageDialogue(
                roomCategoryService,
                roomImageService,
                this::refreshImageData,
                this::showSuccessNotification,
                this::showErrorNotification
        );

        roomImageGrid.setOnEdit(roomImageDialogue::openEditImageDialog);
        roomImageGrid.setOnDelete(roomImageDialogue::openDeleteImageDialog);
        roomImageGrid.setOnAssign(this::assignUnassignedImage);
    }

    /**
     * Configures the main layout properties.
     * 
     * Sets spacing, padding, and alignment to create a responsive layout
     * that spans the full width of the container.
     */
    private void configureLayout() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
    }

    /**
     * Initializes all UI components and adds them to the view.
     * 
     * Adds the header, upload section, and images grid to the layout.
     * Loads initial image data from the database.
     * 
     * @throws Exception if an error occurs during initialization
     */
    private void initializeComponents() {
        try {
            add(
                createHeader(),
                createUploadCard(),
                createImagesCard()
            );

            refreshImageData();
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    /**
     * Handles initialization errors by displaying an error notification.
     * 
     * Shows a user-friendly error message in a notification popup
     * using Vaadin 24.9.5 notification component.
     * 
     * @param e the exception that occurred during initialization
     */
    private void handleInitializationError(Exception e) {
        Notification notification = Notification.show(
            "Error loading the view: " + e.getMessage()
        );
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        notification.setDuration(5000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // ==================== HEADER ====================
    
    /**
     * Creates the header component with title and description.
     * 
     * Builds a header section containing the view title and a descriptive subtitle.
     * 
     * @return a Component containing the header with title and subtitle
     */
    private Component createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.setPadding(false);
        header.addClassName("view-header");

        H1 title = new H1("Image Management");
        Paragraph subtitle = new Paragraph("Upload, organize and assign images to room categories");

        header.add(title, subtitle);
        return header;
    }

    // ==================== UPLOAD CARD ====================
    
    /**
     * Creates the image upload section component.
     * 
     * Builds the upload interface using modern Vaadin 24.9.5 API.
     * Automatically opens the edit dialog after successful upload and handles errors.
     * 
     * @return a Component containing the upload section for new room images
     */
    private Component createUploadCard() {
        return new RoomImageUploadSection(
                roomImageService,
                saved -> {
                    refreshImageData();
                    // Open edit dialog so the user can assign category immediately
                    roomImageDialogue.openEditImageDialog(saved);
                },
                this::showErrorNotification
        );
    }

    // ==================== IMAGES CARD ====================
    
    /**
     * Creates a card containing the grid of all images.
     * 
     * Wraps the image grid in a card component with title and description.
     * 
     * @return a Component containing the images card with grid
     */
    private Component createImagesCard() {
        return CardFactory.createContentCard(
            "All Images",
            "All uploaded images (assigned and unassigned to categories)",
            roomImageGrid
        );
    }

    /**
     * Assigns an unassigned image to the currently selected category.
     * 
     * If a target category is set, this method assigns the given image to it
     * and displays a success notification. Shows an error notification if assignment fails.
     * 
     * @param roomImage the image to be assigned to a category
     */
    private void assignUnassignedImage(RoomImage roomImage) {
        try {
            if (assignToCategory == null) {
                return;
            }
            roomImageService.assignImageToCategory(roomImage, assignToCategory);
            refreshImageData();
            showSuccessNotification("Image assigned to: " + assignToCategory.getName());
        } catch (Exception ex) {
            showErrorNotification("Error assigning image: " + ex.getMessage());
        }
    }

    // ==================== DATA MANAGEMENT ====================
    
    /**
     * Refreshes the image data in the grid.
     * 
     * Fetches all images from the database and updates the grid.
     * If a target category is set, filters to show only unassigned images.
     * Updates the grid's category context for proper assignment handling.
     */
    private void refreshImageData() {
        List<RoomImage> images = roomImageService.findAllWithCategory();

        if (assignToCategory != null) {
            images = images.stream()
                    .filter(img -> img.getCategory() == null)
                    .toList();
        }
        roomImageGrid.setAssignToCategory(assignToCategory);
        roomImageGrid.setItems(images);
    }

    /**
     * Displays a success notification to the user.
     * 
     * Shows a green success notification at the bottom center of the screen
     * with a 3-second duration using Vaadin 24.9.5 notification component.
     * 
     * @param message the success message to display
     */
    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message);
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        notification.setDuration(3000);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Displays an error notification to the user.
     * 
     * Shows a red error notification at the bottom center of the screen
     * with a 3-second duration using Vaadin 24.9.5 notification component.
     * 
     * @param message the error message to display
     */
    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message);
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        notification.setDuration(3000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Checks authorization before entering the view.
     * 
     * Verifies that the user is logged in and has the required role (RECEPTIONIST or MANAGER).
     * Processes optional categoryId query parameter to set the target category for image assignment.
     * Redirects to login view if authorization check fails.
     * 
     * @param event the before-enter event containing navigation context
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || 
            !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
            return;
        }

        String categoryIdParam = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("categoryId", List.of())
                .stream()
                .findFirst()
                .orElse(null);

        if (categoryIdParam != null && !categoryIdParam.isBlank()) {
            try {
                Long categoryId = Long.parseLong(categoryIdParam);
                assignToCategory = roomCategoryService.getRoomCategoryById(categoryId).orElse(null);
            } catch (NumberFormatException ignored) {
                assignToCategory = null;
            }
        } else {
            assignToCategory = null;
        }

        refreshImageData();
    }
}
