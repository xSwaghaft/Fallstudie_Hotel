package com.hotel.booking.view;

import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.repository.RoomImageRepository;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.view.components.CardFactory;
import com.hotel.booking.view.components.RoomCategoryImageSelector;
import com.hotel.booking.entity.RoomCategory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * ImageManagementView - Verwaltung von Bildern für Zimmerkategorien
 * Ermöglicht Upload, Kategorien-Zuweisung und Löschen von Bildern
 * 
 * Vaadin 24.9.5 Standards:
 * - Moderne Component-APIs
 * - ButtonVariant für Styling
 * - FlexComponent für Layout-Alignment
 * - Verbesserte Error Handling
 */
@Route(value = "image-management", layout = MainLayout.class)
@PageTitle("Image Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/image-management.css")
public class ImageManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final RoomCategoryService roomCategoryService;
    private final RoomImageRepository roomImageRepository;

    private final Grid<RoomImage> imageGrid = new Grid<>(RoomImage.class, false);

    private RoomCategory assignToCategory;

    private static final String IMAGE_DIRECTORY = "src/main/resources/static/images/rooms";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    public ImageManagementView(SessionService sessionService,
                               RoomCategoryService roomCategoryService,
                               RoomImageRepository roomImageRepository) {
        this.sessionService = sessionService;
        this.roomCategoryService = roomCategoryService;
        this.roomImageRepository = roomImageRepository;

        configureLayout();
        initializeComponents();
    }

    /**
     * Konfiguriert das Hauptlayout
     */
    private void configureLayout() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
    }

    /**
     * Initialisiert die UI-Komponenten
     */
    private void initializeComponents() {
        try {
            configureImageGrid();
            
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
     * Behandelt Fehler bei der Initialisierung - Vaadin 24.9.5
     */
    private void handleInitializationError(Exception e) {
        Notification notification = Notification.show(
            "Fehler beim Laden der View: " + e.getMessage()
        );
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        notification.setDuration(5000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // ==================== HEADER ====================
    
    /**
     * Erstellt den Header mit Titel und Untertitel
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
     * Erstellt die Upload-Komponente mit modernem Vaadin 24.9.5 API
     */
    private Component createUploadCard() {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("image-upload-card");
        card.setPadding(true);
        card.setSpacing(true);
        card.setWidth("100%");

        H3 title = new H3("Upload Images");
        title.getStyle().set("margin", "0 0 var(--lumo-space-m) 0");

        createImageDirectoryIfNeeded();

        // Vaadin 24.8+: Receiver API is deprecated; use UploadHandler instead
        Upload upload = new Upload(UploadHandler.toFile(
            (metadata, file) -> {
                String webPath = "/images/rooms/" + file.getName();
                String originalFileName = metadata.fileName();

                RoomImage roomImage = new RoomImage(null);
                roomImage.setImagePath(webPath);
                roomImage.setTitle(originalFileName);

                RoomImage saved = roomImageRepository.save(roomImage);
                refreshImageData();

                // Open edit dialog so the user can assign category immediately
                openEditImageDialog(saved);
            },
            metadata -> {
                String originalFileName = metadata.fileName();
                String cleanFileName = originalFileName.replaceAll("[\\\\/:*?\"<>|]", "_");
                String uniqueFileName = UUID.randomUUID() + "_" + cleanFileName;
                return new File(IMAGE_DIRECTORY, uniqueFileName);
            }
        ));
        configureUploadComponent(upload);
        
        Paragraph info = new Paragraph("Accepted formats: JPEG, PNG, GIF, WebP. Max file size: 10 MB");
        info.getStyle().set("font-size", "var(--lumo-font-size-s)");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");

        card.add(title, upload, info);
        return card;
    }

    /**
     * Erstellt das Bildverzeichnis falls nicht vorhanden
     */
    private void createImageDirectoryIfNeeded() {
        Path dirPath = Paths.get(IMAGE_DIRECTORY);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            Notification notification = Notification.show(
                "Error creating image directory: " + e.getMessage()
            );
            notification.setPosition(Notification.Position.BOTTOM_CENTER);
            notification.setDuration(5000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Konfiguriert die Upload-Komponente mit modernem Vaadin 24.9.5 API
     */
    private void configureUploadComponent(Upload upload) {
        upload.setAcceptedFileTypes("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");
        upload.setMaxFiles(10);
        upload.setDropAllowed(true);
        upload.setMaxFileSize(MAX_FILE_SIZE);
        upload.addClassName("image-upload-component");

        // Note: "all finished" does not necessarily mean all succeeded (errors/aborts may occur)
        upload.addAllFinishedListener(event -> 
            showSuccessNotification("Upload finished")
        );
        
        // Error Handler
        upload.addFileRejectedListener(rejectedEvent -> 
            showErrorNotification("File rejected: " + rejectedEvent.getErrorMessage())
        );
    }

    // ==================== IMAGES CARD ====================
    
    /**
     * Erstellt die Card mit allen Bildern
     */
    private Component createImagesCard() {
        return CardFactory.createContentCard(
            "All Images",
            "All uploaded images (assigned and unassigned to categories)",
            null,
            null,
            null,
            imageGrid
        );
    }

    // ==================== GRID CONFIGURATION ====================
    
    /**
     * Konfiguriert das Grid mit modernem Vaadin 24.9.5 API
     */
    private void configureImageGrid() {
        imageGrid.removeAllColumns();
        imageGrid.setSelectionMode(Grid.SelectionMode.NONE); // Vaadin 24.9.5: Explizit setzen

        // Image Preview Column
        imageGrid.addComponentColumn(this::createImagePreview)
            .setHeader("Preview")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Filename Column
        imageGrid.addColumn(RoomImage::getTitle)
            .setHeader("Filename")
            .setAutoWidth(true)
            .setSortable(true)
            .setFlexGrow(1);

        // Alt Text Column
        imageGrid.addColumn(RoomImage::getAltText)
            .setHeader("Alt Text")
            .setAutoWidth(true)
            .setSortable(true)
            .setFlexGrow(1);

        // Category Column
        imageGrid.addComponentColumn(this::createCategoryBadge)
            .setHeader("Category / Status")
            .setAutoWidth(true)
            .setSortable(true)
            .setFlexGrow(1);

        // Primary Column
        imageGrid.addColumn(roomImage -> Boolean.TRUE.equals(roomImage.getIsPrimary()) ? "Yes" : "No")
                .setHeader("Primary")
                .setAutoWidth(true)
                .setSortable(true);

        // Actions Column
        imageGrid.addComponentColumn(this::createImageActions)
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        // Let the page scroll (Grid grows with its content)
        imageGrid.setAllRowsVisible(true);
        imageGrid.setWidth("100%");
    }

    /**
     * Erstellt die Bild-Vorschau
     */
    private Component createImagePreview(RoomImage roomImage) {
        Image preview = new Image(roomImage.getImagePath(), "preview");
        preview.setWidth("80px");
        preview.setHeight("80px");
        preview.addClassName("image-grid-thumbnail");
        return preview;
    }

    /**
     * Erstellt das Kategorie-Badge
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

    // ==================== ACTION COMPONENTS ====================
    
    /**
     * Erstellt die Action-Buttons mit modernem ButtonVariant API - Vaadin 24.9.5
     */
    private Component createImageActions(RoomImage roomImage) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setMargin(false);

        if (assignToCategory != null && roomImage.getCategory() == null) {
            Button assignBtn = new Button("Assign", VaadinIcon.CHECK.create());
            assignBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            assignBtn.addClickListener(e -> assignUnassignedImage(roomImage));
            actions.add(assignBtn);
        }

        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.addClickListener(e -> openEditImageDialog(roomImage));

        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> deleteImage(roomImage));

        actions.add(editBtn, deleteBtn);
        return actions;
    }

    private void assignUnassignedImage(RoomImage roomImage) {
        try {
            if (assignToCategory == null) {
                return;
            }
            roomImage.setCategory(assignToCategory);
            roomImageRepository.save(roomImage);
            refreshImageData();
            showSuccessNotification("Image assigned to: " + assignToCategory.getName());
        } catch (Exception ex) {
            showErrorNotification("Error assigning image: " + ex.getMessage());
        }
    }

    // ==================== DIALOGS ====================

    /**
     * Öffnet Dialog zum Bearbeiten eines vorhandenen Bildes - Vaadin 24.9.5
     */
    private void openEditImageDialog(RoomImage roomImage) {
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
     * Behandelt das Update eines Bildes
     */
    private void handleImageUpdate(RoomCategoryImageSelector selector, Dialog dialog) {
        try {
            RoomImage updatedImage = selector.getAssignedImage();
            selector.writeBean();

            enforceSinglePrimaryPerCategory(updatedImage);
            roomImageRepository.save(updatedImage);

            refreshImageData();
            dialog.close();
            
            String message = updatedImage.getCategory() != null 
                ? "Image updated successfully!" 
                : "Image updated (no category assigned).";
            showSuccessNotification(message);
        } catch (Exception ex) {
            showErrorNotification("Error: " + ex.getMessage());
        }
    }

    private void enforceSinglePrimaryPerCategory(RoomImage updatedImage) {
        if (!Boolean.TRUE.equals(updatedImage.getIsPrimary())) {
            return;
        }

        RoomCategory category = updatedImage.getCategory();
        if (category == null || category.getCategory_id() == null) {
            updatedImage.setIsPrimary(false);
            return;
        }

        Long categoryId = category.getCategory_id();
        List<RoomImage> primaries = roomImageRepository.findPrimaryByCategoryId(categoryId);
        for (RoomImage other : primaries) {
            if (updatedImage.getId() != null && updatedImage.getId().equals(other.getId())) {
                continue;
            }
            other.setIsPrimary(false);
        }
        roomImageRepository.saveAll(primaries);
    }

    // ==================== DATA MANAGEMENT ====================
    
    /**
     * Aktualisiert die Bilddaten im Grid
     */
    private void refreshImageData() {
        List<RoomImage> images = roomImageRepository.findAllWithCategory();

        if (assignToCategory != null) {
            images = images.stream()
                    .filter(img -> img.getCategory() == null)
                    .toList();
        }
        imageGrid.setItems(images);
    }

    /**
     * Löscht ein Bild mit Bestätigungsdialog - Vaadin 24.9.5
     */
    private void deleteImage(RoomImage roomImage) {
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
     * Behandelt das Löschen eines Bildes
     */
    private void handleImageDeletion(RoomImage roomImage, Dialog confirmDialog) {
        try {
            File fileToDelete = new File(IMAGE_DIRECTORY, extractFileName(roomImage.getImagePath()));
            if (fileToDelete.exists()) {
                if (!fileToDelete.delete()) {
                    showErrorNotification("Could not delete file from disk");
                    return;
                }
            }

            roomImageRepository.delete(roomImage);
            refreshImageData();
            confirmDialog.close();
            showSuccessNotification("Image deleted successfully!");
        } catch (Exception ex) {
            showErrorNotification("Error deleting image: " + ex.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Extrahiert den Dateinamen aus einem Pfad
     */
    private String extractFileName(String imagePath) {
        return imagePath.substring(imagePath.lastIndexOf("/") + 1);
    }

    /**
     * Zeigt eine Success-Notification - Vaadin 24.9.5
     */
    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message);
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        notification.setDuration(3000);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Zeigt eine Error-Notification - Vaadin 24.9.5
     */
    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message);
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        notification.setDuration(3000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Autorisierungsprüfung vor dem Seiteneintritt
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
