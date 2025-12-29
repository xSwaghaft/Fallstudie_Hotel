package com.hotel.booking.view.components;

import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.service.RoomImageService;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Upload section for room images.
 *
 * This component provides a user interface for uploading room images. It uses CardFactory
 * internally to render the card UI and handles file uploads through a configurable upload
 * handler. Supports multiple file formats (JPEG, PNG, GIF, WebP) with a maximum file size
 * of 10 MB.
 *
 * @author Artur Derr
 */
public class RoomImageUploadSection extends VerticalLayout {

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final RoomImageService roomImageService;
    private final Consumer<RoomImage> onUploaded;
    private final Consumer<String> onError;

    /**
     * Constructs a RoomImageUploadSection with the specified dependencies.
     *
     * @param roomImageService the service used to handle room image operations; must not be null
     * @param onUploaded a callback function invoked when an image is successfully uploaded;
     *                   receives the uploaded RoomImage object; must not be null
     * @param onError a callback function invoked when an error occurs during upload;
     *                receives the error message as a string; must not be null
     * @throws NullPointerException if any of the parameters are null
     */
    public RoomImageUploadSection(
            RoomImageService roomImageService,
            Consumer<RoomImage> onUploaded,
            Consumer<String> onError
    ) {
        this.roomImageService = Objects.requireNonNull(roomImageService);
        this.onUploaded = Objects.requireNonNull(onUploaded);
        this.onError = Objects.requireNonNull(onError);

        setPadding(false);
        setSpacing(false);
        setWidthFull();

        add(buildCard());
    }

    /**
     * Builds and returns the card component containing the upload interface.
     *
     * Constructs the upload handler with file handling logic, creates the Upload component,
     * and organizes them within a card layout. The card includes an info paragraph describing
     * accepted file formats and maximum file size.
     *
     * @return a VerticalLayout containing the configured card with upload controls
     */
    private VerticalLayout buildCard() {
        UploadHandler fileUploadHandler = UploadHandler.toFile(
                (metadata, file) -> {
                    try {
                        RoomImage saved = roomImageService.createAndSaveUploadedImage(metadata.fileName(), file.getName());
                        getUI().ifPresent(ui -> ui.access(() -> onUploaded.accept(saved)));
                    } catch (Exception ex) {
                        getUI().ifPresent(ui -> ui.access(() -> onError.accept("Upload failed: " + ex.getMessage())));
                    }
                },
                metadata -> roomImageService.createTargetFile(metadata.fileName())
        ).whenComplete(success -> {
            if (!success) {
                getUI().ifPresent(ui -> ui.access(() -> onError.accept("Upload failed")));
            }
        });

        Upload upload = new Upload(fileUploadHandler);
        configureUploadComponent(upload);

        Paragraph info = new Paragraph("Accepted formats: JPEG, PNG, GIF, WebP. Max file size: 10 MB");
        info.getStyle().set("font-size", "var(--lumo-font-size-s)");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout card = new VerticalLayout();
        card.addClassName("content-card");
        card.addClassName("image-upload-card");
        card.setPadding(true);
        card.setSpacing(true);
        card.setWidthFull();

        H3 title = new H3("Upload Images");
        title.addClassName("content-card-title");

        Paragraph subtitle = new Paragraph("Upload images to manage and assign them to room categories");
        subtitle.addClassName("content-card-subtitle");

        card.add(title, subtitle, upload, info);
        return card;
    }

    /**
     * Configures the upload component with file type restrictions and validation settings.
     *
     * Sets accepted file types (JPEG, PNG, GIF, WebP), maximum number of files,
     * drop zone support, and maximum file size. Also attaches a listener to handle
     * rejected files and notify the error callback.
     *
     * @param upload the Upload component to configure; must not be null
     */
    private void configureUploadComponent(Upload upload) {
        upload.setAcceptedFileTypes("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");
        upload.setMaxFiles(10);
        upload.setDropAllowed(true);
        upload.setMaxFileSize(MAX_FILE_SIZE);
        upload.addClassName("image-upload-component");

        upload.addFileRejectedListener(rejectedEvent ->
                onError.accept("File rejected: " + rejectedEvent.getErrorMessage())
        );
    }
}
